package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RewardsServiceTest {

	@Test
	public void userGetRewards() throws Exception{
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//pickup one attraction for this test
		Attraction attraction = gpsUtil.getAttractions().get(0);
		//add visitedLocation with attraction location
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		//verify user location and give userRewards
		tourGuideService.trackUserLocation(user);
		//WHEN
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.stopTracking();

		//THEN
		assertEquals(1, userRewards.size());
	}
	
	@Test
	public void getRewardPoints() throws Exception {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//pickup one attraction for this test
		Attraction attraction = gpsUtil.getAttractions().get(0);
		//add visitedLocation with attraction location
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		//verify user location and give userRewards
		tourGuideService.trackUserLocation(user);
		//WHEN
		tourGuideService.stopTracking();
		rewardsService.calculateRewards(user);
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();

		//THEN
		assertTrue(cumulatativeRewardPoints > 0);
	}
	
	@Test
	public void nearAllAttractions() throws Exception {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
