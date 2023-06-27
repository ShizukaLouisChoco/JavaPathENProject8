package tourGuide.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final TourGuideService tourGuideService;


    public UserServiceImpl(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
    }

    @Override
    public UserPreferencesDto updateUserPreferences(UserPreferencesDto userPreferenceDto)throws NumberFormatException{
        User user = tourGuideService.getUser(userPreferenceDto.getUserName());
        UserPreferences userPreferences = user.getUserPreferences();

            userPreferences.setTripDuration(userPreferenceDto.getTripDuration());
            userPreferences.setTicketQuantity(userPreferenceDto.getTicketQuantity());
            userPreferences.setNumberOfAdults(userPreferenceDto.getNumberOfAdults());
            userPreferences.setNumberOfChildren(userPreferenceDto.getNumberOfChildren());


        user.setUserPreferences(userPreferences);
        return userPreferenceDto;
    }


}
