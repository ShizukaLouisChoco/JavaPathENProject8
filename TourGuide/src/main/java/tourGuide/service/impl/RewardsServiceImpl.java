package tourGuide.service.impl;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class RewardsServiceImpl implements RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private final int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private final int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;


	public RewardsServiceImpl(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void calculateRewards(User user) {
		//create copy of user's visitedLocation list
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		//create attraction list from gpsUtil
		List<Attraction> attractions = gpsUtil.getAttractions();

		//create empty task list
		List<Callable<UserReward>> tasks  = new ArrayList<>();
		//verify attraction of user's visitedLocation list & creating task and add in task list
		userLocations.forEach(visitedLocation -> attractions
				.stream()
				//filter attractions which is not in user's userRewards list
				.filter(attraction -> user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName)))
				//filter attractions which is near from visited location
				.filter(attraction -> nearAttraction(visitedLocation, attraction))
				//create rewards to each attraction by visitedLocation, attraction and rewardPoints
				.forEach(attraction -> tasks.add( ()-> new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user))))
		);

		ExecutorService executorService = Executors.newFixedThreadPool(100);
		try {
			//wait completion of the tasks and get results of tasks and add user's user rewards list
			List<Future<UserReward>> rewardFutures = executorService.invokeAll(tasks);
			for (Future<UserReward> future : rewardFutures) {
				//add userReward list
				user.addUserReward(future.get()); // this setter filters out duplicated rewards
			}
		} catch (InterruptedException | ExecutionException e) {
			log.debug("runtimeException");
			throw new RuntimeException(e);
		} finally {
			executorService.shutdown();
		}

	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		//verify if the distance of attraction and the location is not bigger than 10
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	public int getRewardPoints(Attraction attraction, User user) {
		//get random attraction reward points
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
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
