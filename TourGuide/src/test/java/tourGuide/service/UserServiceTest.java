package tourGuide.service;

import gpsUtil.GpsUtil;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.impl.UserServiceImpl;
import tourGuide.user.User;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

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
        assertEquals(1, user.getUserPreferences().getTripDuration());
        assertEquals(1, user.getUserPreferences().getTicketQuantity());
        assertEquals(1, user.getUserPreferences().getNumberOfAdults());
        assertEquals(0, user.getUserPreferences().getNumberOfChildren());

        //WHEN
        userService.updateUserPreferences(new UserPreferencesDto("jon",2,2,2,2));
        tourGuideService.tracker.stopTracking();
        //THEN
        assertEquals(2, user.getUserPreferences().getTripDuration());
        assertEquals(2, user.getUserPreferences().getTicketQuantity());
        assertEquals(2, user.getUserPreferences().getNumberOfAdults());
        assertEquals(2, user.getUserPreferences().getNumberOfChildren());

    }


}
