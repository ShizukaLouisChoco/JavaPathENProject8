package tourGuide;

import com.jsoniter.output.JsonStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.exception.UserInfoException;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class TourGuideController {

	private final TourGuideService tourGuideService;


    public TourGuideController(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
		return JsonStream.serialize(tourGuideService.getUserLocation(getUser(userName)).location);
    }

    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
    	return JsonStream.serialize(tourGuideService.getNearByAttractions(userName));
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
    	//- Note: does not use gpsUtil to query for their current location,
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }
    	return JsonStream.serialize(tourGuideService.getAllUserLocations());
    }

    @PutMapping(value = "/userPreferences")
    public UserPreferencesDto updateUserPreferences(@RequestBody UserPreferencesDto userPreferenceDto) throws UserInfoException {
        log.debug("Request details: PUTMapping, body person : {}", userPreferenceDto);
        return tourGuideService.updateUserPreferences(userPreferenceDto);
    }


    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	return JsonStream.serialize(providers);
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}