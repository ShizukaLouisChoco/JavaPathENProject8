package tourGuide.service;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.dto.AttractionsDto;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.exception.UserInfoException;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;

public interface TourGuideService {

    List<UserReward> getUserRewards(User user);

    VisitedLocation getUserLocation(User user);

    User getUser(String userName) throws UserInfoException;

    List<User> getAllUsers();

    void addUser(User user);

    UserPreferencesDto updateUserPreferences(UserPreferencesDto userPreferenceDto);

    List<Provider> getTripDeals(User user);


    VisitedLocation trackUserLocation(User user);

    List<AttractionsDto> getNearByAttractions(String userName);

    Map<String, Location> getAllUserLocations();

    void stopTracking() throws Exception ;

}
