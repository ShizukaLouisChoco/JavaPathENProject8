package tourGuide.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import gpsUtil.location.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"attractionName", "attractionLocation", "userLocation", "distance", "rewardPoints"})
public class AttractionsDto implements Serializable {

    private String attractionName;

    private Location attractionLocation;

    private Location userLocation;

    private double distance;

    private int rewardPoints;

}
