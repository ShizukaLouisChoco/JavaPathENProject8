package tourGuide.service;

import gpsUtil.GpsUtil;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.impl.UserServiceImpl;
import tourGuide.user.User;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class UserServiceTest {

    @Test
    public void updateUserPreferencesTest(){
        //GIVEN
        InternalTestHelper.setInternalUserNumber(1);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        UserService userService = new UserServiceImpl(tourGuideService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        assertTrue(user.getUserPreferences().getTripDuration() == 1);
        assertTrue(user.getUserPreferences().getTicketQuantity() == 1);
        assertTrue(user.getUserPreferences().getNumberOfAdults() == 1);
        assertTrue(user.getUserPreferences().getNumberOfChildren() == 0);

        //WHEN
        userService.updateUserPreferences(new UserPreferencesDto("jon","2","2","2","2"));
        tourGuideService.tracker.stopTracking();
        //THEN
        assertTrue(user.getUserPreferences().getTripDuration() == 2);
        assertTrue(user.getUserPreferences().getTicketQuantity() == 2);
        assertTrue(user.getUserPreferences().getNumberOfAdults() == 2);
        assertTrue(user.getUserPreferences().getNumberOfChildren() == 2);

    }


}
