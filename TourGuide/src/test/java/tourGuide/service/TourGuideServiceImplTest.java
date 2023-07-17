package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.dto.AttractionsDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.exception.UserInfoException;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.impl.RewardsServiceImpl;
import tourGuide.service.impl.TourGuideServiceImpl;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TourGuideServiceImplTest {

	@Test
	public void getUserLocation() throws ExecutionException, InterruptedException {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//WHEN
		VisitedLocation visitedLocation = tourGuideServiceImpl.trackUserLocation(user);
		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		//WHEN
		tourGuideServiceImpl.addUser(user);
		tourGuideServiceImpl.addUser(user2);
		
		User retrievedUser = tourGuideServiceImpl.getUser(user.getUserName());
		User retrievedUser2 = tourGuideServiceImpl.getUser(user2.getUserName());

		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertEquals(user, retrievedUser);
		assertEquals(user2, retrievedUser2);
	}

	@Test(expected = UserInfoException.class)
	public void addUserWithException() {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon", "000", "jon2@tourGuide.com");
		tourGuideServiceImpl.addUser(user);
		//WHEN
		tourGuideServiceImpl.addUser(user2);

		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
	}

	@Test(expected = UserInfoException.class)
	public void getUserWithException() {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		//WHEN
		tourGuideServiceImpl.getUser("testUser");

		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
	}

	@Test
	public void getAllUsers() {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideServiceImpl.addUser(user);
		tourGuideServiceImpl.addUser(user2);
		//WHEN
		List<User> allUsers = tourGuideServiceImpl.getAllUsers();

		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void getAllUserLocations() {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideServiceImpl.addUser(user);
		tourGuideServiceImpl.addUser(user2);
		//WHEN
		Map<String, Location> allUserLocations = tourGuideServiceImpl.getAllUserLocations();

		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertTrue(allUserLocations.containsKey(user.getUserId().toString()));
		assertTrue(allUserLocations.containsKey(user2.getUserId().toString()));
		assertEquals(allUserLocations.size(),2);
	}
	
	@Test
	public void trackUser() throws ExecutionException, InterruptedException {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//WHEN
		VisitedLocation visitedLocation = tourGuideServiceImpl.trackUserLocation(user);
		
		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Test
	public void trackUserNoVisitedLocationHistory() throws ExecutionException, InterruptedException {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		user.clearVisitedLocations();
		//WHEN
		VisitedLocation visitedLocation = tourGuideServiceImpl.trackUserLocation(user);

		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Test
	public void getNearbyAttractions() throws ExecutionException, InterruptedException {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = tourGuideServiceImpl.getUser("internalUser0");
		VisitedLocation visitedLocation = tourGuideServiceImpl.trackUserLocation(user);
		//WHEN
		List<AttractionsDto> attractions = tourGuideServiceImpl.getNearByAttractions(user.getUserName());
		
		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertEquals(5, attractions.size());
	}

	@Test
	public void getTripDeals() {
		//GIVEN
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = tourGuideServiceImpl.getUser("internalUser0");
		//WHEN
		List<Provider> providers = tourGuideServiceImpl.getTripDeals(user);
		
		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertEquals(5, providers.size());
	}

	@Test
	public void updateUserPreferencesTest(){
		//GIVEN
		InternalTestHelper.setInternalUserNumber(1);
		GpsUtil gpsUtil = new GpsUtil();
		RewardsServiceImpl rewardsServiceImpl = new RewardsServiceImpl(gpsUtil, new RewardCentral());
		TourGuideServiceImpl tourGuideServiceImpl = new TourGuideServiceImpl(gpsUtil, rewardsServiceImpl);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideServiceImpl.addUser(user);
		assertEquals(1, user.getUserPreferences().getTripDuration());
		assertEquals(1, user.getUserPreferences().getTicketQuantity());
		assertEquals(1, user.getUserPreferences().getNumberOfAdults());
		assertEquals(0, user.getUserPreferences().getNumberOfChildren());

		//WHEN
		tourGuideServiceImpl.updateUserPreferences(new UserPreferencesDto("jon",2,2,2,2));
		tourGuideServiceImpl.tracker.stopTracking();
		//THEN
		assertEquals(2, user.getUserPreferences().getTripDuration());
		assertEquals(2, user.getUserPreferences().getTicketQuantity());
		assertEquals(2, user.getUserPreferences().getNumberOfAdults());
		assertEquals(2, user.getUserPreferences().getNumberOfChildren());

	}

}
