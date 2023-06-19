package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
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
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		log.info("getting user rewards list in TourGuideService");
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		log.info("getting user's (last) visited location or tracking user location in TourGuideService");
		return (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
	}

	public User getUser(String userName) {
		log.info("getting user from internalUserMap in TourGuideService");
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		log.info("getting all user list from internalUserMap in TourGuideService");
		return new ArrayList<>(internalUserMap.values());
	}

	public void addUser(User user) {
		log.info("adding user if user is not already in internalUserMap in TourGuideService");
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		log.info("getting tripDeals list(Provider) in TourGuideService");
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		log.info("got total user reward points accumulated in TourGuideService");
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		log.info("got provider list in TourGuideService");
		user.setTripDeals(providers);
		log.info("set tripdeals of user in TourGuideService");
		return providers;
	}


	public VisitedLocation trackUserLocation(User user) {
		log.info("tracking user location(visitedLocation) in TourGuideService");
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		log.info("got localization info of user in TourGuideService");
		executorService.submit(() -> {
			user.addToVisitedLocations(visitedLocation);
			rewardsService.calculateRewards(user);
		});
		log.info("add localization info in user's visited location list and calculate user's rewards in TourGuideService");
		return visitedLocation;
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		log.info("getting 5 nearest attractions from visited location in TourGuideService");
		List<Attraction> nearbyAttractions = gpsUtil.getAttractions();
		log.info("got all attractions of gpsUtil in TourGuideService");
		List<Attraction> sortedAttractions = nearbyAttractions.stream()
				.sorted(Comparator.comparingDouble(a -> rewardsService.getDistance(a, visitedLocation.location)))
				.limit(5)
				.collect(Collectors.toList());
		log.info("sorting all attractions of gpsUtil and have only 5 nearest in TourGuideService");
		return sortedAttractions;
	}


	public void stopTracking() throws Exception {
		log.info("stopping tracking in TourGuideService");
		tracker.stopTracking();
		logger.debug("Tracker stopped. Completing tasks");
		executorService.shutdown();
		while(!executorService.awaitTermination(1, TimeUnit.MINUTES)){
		}
		logger.debug("Tasks Completed");
	}

	private void addShutDownHook() {
		log.info("adding shut down hook in TourGuideService");
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
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
		logger.debug("Creating " + i + " internal test user.");

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		log.info("generating 3 user location history in TourGuideService");
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		log.info("generating random longitude in TourGuideService");
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		log.info("generating random latitude in TourGuideService");
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		log.info("getting random time in TourGuideService");
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}
