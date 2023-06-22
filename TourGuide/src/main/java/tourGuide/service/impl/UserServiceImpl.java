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
        try{
            userPreferences.setTripDuration(Integer.parseInt(userPreferenceDto.getTripDuration()));
            userPreferences.setTicketQuantity(Integer.parseInt(userPreferenceDto.getTicketQuantity()));
            userPreferences.setNumberOfAdults(Integer.parseInt(userPreferenceDto.getNumberOfAdults()));
            userPreferences.setNumberOfChildren(Integer.parseInt(userPreferenceDto.getNumberOfChildren()));
        }catch(NumberFormatException e){
            log.debug("NumberFormatException : " + e);
        }

        user.setUserPreferences(userPreferences);
        return userPreferenceDto;
    }


}
