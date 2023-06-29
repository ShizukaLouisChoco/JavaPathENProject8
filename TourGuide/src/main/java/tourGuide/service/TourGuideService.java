package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourGuide.dto.AttractionsDto;
import tourGuide.exception.UserInfoException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class TourGuideService {
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	public final ExecutorService executorService = Executors.newFixedThreadPool(100);

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

		if(testMode) {
			log.info("TestMode enabled");
			log.debug("Initializing users");
			initializeInternalUsers();
			log.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		rewardsService.calculateRewards(user);
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		return (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
	}

	public User getUser(String userName) throws UserInfoException {
		User user = internalUserMap.get(userName);
		if (user != null) {
			return user;}
		else{
			throw new UserInfoException("User not found with userName : "+userName);
		}
	}

	public List<User> getAllUsers() {
		return new ArrayList<>(internalUserMap.values());
	}

	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}else{
			throw new UserInfoException(user.getUserName()+" is already used");
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}


	public VisitedLocation trackUserLocation(User user) {
		//creation visitedLocation from last or actual userLocation
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		executorService.submit(() -> {
			user.addToVisitedLocations(visitedLocation);
			rewardsService.calculateRewards(user);
		});
		return visitedLocation;
	}

	public List<AttractionsDto> getNearByAttractions(String userName) {
		VisitedLocation visitedLocation = getUserLocation(getUser(userName));
		List<Attraction> nearbyAttractions = gpsUtil.getAttractions();
		List<Attraction> fiveAttractions = nearbyAttractions.stream()
				.sorted(Comparator.comparingDouble(a -> rewardsService.getDistance(a, visitedLocation.location)))
				.limit(5)
				.collect(Collectors.toList());

		List<AttractionsDto> attractionsDtoList = new ArrayList<>();
		for(Attraction attraction : fiveAttractions){
			attractionsDtoList.add(new AttractionsDto(
					// Name of Tourist attraction,
					attraction.attractionName,
					// Tourist attractions lat/long,
					attraction,
					// The user's location lat/long,
					getUserLocation(getUser(userName)).location,
					// The distance in miles between the user's location and each of the attractions.
					rewardsService.getDistance(attraction,getUserLocation(getUser(userName)).location),
					// The reward points for visiting each Attraction.
					rewardsService.getRewardPoints(attraction,getUser(userName))));
		}

		return attractionsDtoList;
	}

	public Map<String, Location> getAllUserLocations() {
		List<User> users = getAllUsers();
		Map<String, Location> allUserLocations = new HashMap<>();
		for(User user : users){
			Location location = getUserLocation(user).location;
			allUserLocations.put(user.getUserId().toString(),location);
		}

		return allUserLocations;
	}

	public void stopTracking() throws Exception {
		tracker.stopTracking();
		log.debug("Tracker stopped. Completing tasks");
		executorService.shutdown();
		while(!executorService.awaitTermination(1, TimeUnit.MINUTES)){
		}
		log.debug("Tasks Completed");
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> tracker.stopTracking()));
	}

	/**********************************************************************************
	 *
	 * Methods Below: For Internal Testing
	 *
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		log.debug("Creating " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
