package tourGuide.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.output.JsonStream;
import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TourGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;





    @Test
    public void indexTest() throws Exception {
        //GIVEN
        final String url = "/";

        //WHEN
        final ResultActions response = mockMvc.perform(get(url))
                .andDo(MockMvcResultHandlers.print());

        final MvcResult result = mockMvc.perform(get(url))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //THEN
        response.andExpect(status().isOk());
        assertEquals(null,"Greetings from TourGuide!", result.getResponse().getContentAsString());

    }


    @Test
    public void getLocationTest() throws Exception {
        // GIVEN 1 user
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        tourGuideService.isTestMode(true);
       // User user = createTestUser();
        final String url = "/getLocation";

        tourGuideService.isTestMode(false);
        // WHEN
        final ResultActions result = mockMvc.perform(get(url)
                .param("userName", "internalUser0"));

        User user = tourGuideService.getAllUsers().get(0);
        //tourGuideService.tracker.stopTracking();
        // THEN
        result.andExpect(status().isOk());

        assertEquals(null,JsonStream.serialize(user.getLastVisitedLocation().location), result.andReturn().getResponse().getContentAsString());
    }

    /*

    @Test
    public void getLocationTest() throws Exception {
        // GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        //test user has 3 visitedlocations with random date which is not in chronological order
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        /*User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(),new Location(100,50),new Date().from(localDateTime.toInstant(ZoneOffset.UTC)));
        user.addToVisitedLocations(visitedLocation);
        tourGuideService.addUser(user);

        final String url = "/getLocation";

        // WHEN
        final ResultActions result = mockMvc.perform(get(url)
                        .param("userName", user.getUserName()));

        tourGuideService.tracker.stopTracking();
        // THEN
        result.andExpect(status().isOk());

        assertEquals(null,JsonStream.serialize(user.getLastVisitedLocation().location), result.getResponse().getContentAsString());
    }
    */

    @Test
    public void getTripDealsTest() throws Exception{
        //GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        final String url = "/getTripDeals";

        //WHEN
        final ResultActions response = mockMvc.perform(get(url)
                .param("userName", user.getUserName()));

        //THEN
        response.andExpect(status().isOk());
    }

    @Test
    public void updateUserPreferencesTest() throws Exception{
        //GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        UserPreferences userPreferences = new UserPreferences(3,3,3,3);
        user.setUserPreferences(userPreferences);
        tourGuideService.addUser(user);
        final String url = "/userPreferences";

        //WHEN
        final ResultActions response = mockMvc.perform(post(url)
                .content(asJsonString(userPreferences))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));


        //THEN
        response.andExpect(status().isOk());
    }

    @Test
    public void getRewardsTest() throws Exception{
        //GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        final String url = "/getRewards";

        //WHEN
        final ResultActions response = mockMvc.perform(get(url)
                .param("userName", user.getUserName()));


        //THEN
        response.andExpect(status().isOk());

    }
    @Test
    public void getNearbyAttractionsTest() throws Exception{
//GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        final String url = "/getNearbyAttractions";

        //WHEN
        final ResultActions response = mockMvc.perform(post(url)
                .param("userName", user.getUserName()));


        //THEN
        response.andExpect(status().isOk());
    }

    @Test
    public void getAllCurrentLocationsTest() throws Exception{
//GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final String url = "/getAllCurrentLocations";

        //WHEN
        final ResultActions response = mockMvc.perform(get(url));


        //THEN
        response.andExpect(status().isOk());
    }
    @Test
    public void getUserTest() throws Exception{
    //GIVEN
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);
        final String url = "/userPreferences";

        //WHEN
        final ResultActions response = mockMvc.perform(get(url)
                .param("userName", user.getUserName()));


        //THEN
        response.andExpect(status().isOk());
    }

    @Autowired
    private ObjectMapper objectMapper;

    //for jsonString
    protected String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public User createTestUser ()throws IOException {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        return tourGuideService.getUser("internalUser0");
    }
}
