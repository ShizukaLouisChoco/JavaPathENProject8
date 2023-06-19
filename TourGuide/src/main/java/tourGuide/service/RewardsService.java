package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private final int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private final int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	private final ExecutorService executorService = Executors.newFixedThreadPool(100);

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		log.info("setting proximityBuffer in RewardService");
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		log.info("setting default proximityBuffer in RewardService");
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		log.info("calculating rewards in RewardService");
		//create copy of user's visitedLocation list
		List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>( user.getVisitedLocations());
		//create attraction list from gpsUtil
		List<Attraction> attractions = gpsUtil.getAttractions();

		/*//create task list
		List<Callable<UserReward>> tasks  = new ArrayList<>();
		//verify attraction of user's visitedLocation list

		userLocations.forEach(visitedLocation -> attractions
				.stream()
				//filter attractions which is not in user's userRewards list
				.filter(attraction -> user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName)))
				//filter attractions which is near from visited location
				.filter(attraction -> nearAttraction(visitedLocation, attraction))
				//create rewards to each attraction by visitedLocation, attraction and rewardPoints
				.forEach(attraction -> tasks.add( ()-> user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)))))
		);*/

		userLocations.forEach(visitedLocation -> attractions
				.stream()
				//filter attractions which is not in user's userRewards list
				.filter(attraction -> user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName)))
				//filter attractions which is near from visited location
				.filter(attraction -> nearAttraction(visitedLocation, attraction))
				//create rewards to each attraction by visitedLocation, attraction and rewardPoints
				.forEach(attraction -> user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user))))

		);
		log.info("rewards calculated in RewardService");


	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		log.info("verifying if attraction and location is proximity or not in RewardService");
		//verify if the distance of attraction and the location is not bigger than 200
		return !(getDistance(attraction, location) > attractionProximityRange);
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		log.info("verifying if attraction and visited location is proximity or not in RewardService");
		//verify if the distance of attraction and the location is not bigger than 10
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	private int getRewardPoints(Attraction attraction, User user) {
		log.info("getting reward points in RewardService");
		//get random attraction reward points
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
		log.info("getting distance of location1 and location2 in RewardService");
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
