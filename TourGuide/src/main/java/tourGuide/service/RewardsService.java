package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import tourGuide.user.User;

public interface RewardsService {

    void setProximityBuffer(int proximityBuffer);

    void calculateRewards(User user);


    int getRewardPoints(Attraction attraction, User user);

    double getDistance(Location loc1, Location loc2);
}
