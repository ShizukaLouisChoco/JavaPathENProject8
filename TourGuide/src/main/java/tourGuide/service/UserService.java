package tourGuide.service;

import tourGuide.dto.UserPreferencesDto;

public interface UserService {

    UserPreferencesDto updateUserPreferences(UserPreferencesDto userPreferenceDto);

    }
