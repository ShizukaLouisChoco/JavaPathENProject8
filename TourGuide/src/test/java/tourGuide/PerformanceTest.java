package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.impl.RewardsServiceImpl;
import tourGuide.service.impl.TourGuideServiceImpl;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class PerformanceTest {
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@Test
	public void highVolumeTrackLocation() throws Exception {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsServiceImpl(gpsUtil, new RewardCentral());// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100_000);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsService);
		List<User> allUsers = tourGuideServiceImpl.getAllUsers();
		
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		//WHEN
		List<Callable<VisitedLocation>> tasks = new ArrayList<>();
		allUsers.forEach(user -> tasks.add(() -> tourGuideServiceImpl.trackUserLocation(user)));
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		executorService.invokeAll(tasks);
		executorService.shutdown();

		stopWatch.stop();
		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() throws Exception {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100_000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		//WHEN
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = tourGuideServiceImpl.getAllUsers();
		List<Callable<Object>> tasks = new ArrayList<>();

		allUsers.forEach(user -> user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date())) );
		allUsers.forEach(user -> tasks.add(Executors.callable(()-> rewardsServiceImpl.calculateRewards(user))));

		ExecutorService executorService = Executors.newFixedThreadPool(100);

		executorService.invokeAll(tasks);
		executorService.shutdown();
		stopWatch.stop();
		tourGuideServiceImpl.stopTracking();
		//THEN
		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));


		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
	}
	
}
