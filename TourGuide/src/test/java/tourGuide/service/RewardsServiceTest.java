package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.impl.RewardsServiceImpl;
import tourGuide.service.impl.TourGuideServiceImpl;
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
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//pickup one attraction for this test
		Attraction attraction = gpsUtil.getAttractions().get(0);
		//add visitedLocation with attraction location
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		//verify user location and give userRewards
		tourGuideServiceImpl.trackUserLocation(user);
		//WHEN
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideServiceImpl.stopTracking();

		//THEN
		assertEquals(1, userRewards.size());
	}
	
	@Test
	public void getRewardPoints() throws Exception {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//pickup one attraction for this test
		Attraction attraction = gpsUtil.getAttractions().get(0);
		//add visitedLocation with attraction location
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		//verify user location and give userRewards
		tourGuideServiceImpl.trackUserLocation(user);
		//WHEN
		tourGuideServiceImpl.stopTracking();
		rewardsServiceImpl.calculateRewards(user);
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();

		//THEN
		assertTrue(cumulatativeRewardPoints > 0);
	}
	
	@Test
	public void nearAllAttractions() throws Exception {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		rewardsServiceImpl.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		rewardsServiceImpl.calculateRewards(tourGuideServiceImpl.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideServiceImpl.getUserRewards(tourGuideServiceImpl.getAllUsers().get(0));
		tourGuideServiceImpl.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
