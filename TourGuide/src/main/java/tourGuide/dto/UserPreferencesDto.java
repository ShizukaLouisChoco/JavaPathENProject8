package tourGuide.dto;

import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@AllArgsConstructor
public class UserPreferencesDto implements Serializable {
    @NotNull
    private String userName;
    @NotNull
    private Integer tripDuration;
    @NotNull
    private Integer ticketQuantity;
    @NotNull
    private Integer numberOfAdults;
    @NotNull
    private Integer numberOfChildren;

    public String getUserName(){
        return userName;
    }

    public Integer getTripDuration(){
        return tripDuration;
    }

    public Integer getTicketQuantity(){
        return ticketQuantity;
    }

    public Integer getNumberOfAdults(){
        return numberOfAdults;
    }

    public Integer getNumberOfChildren(){
        return numberOfChildren;
    }


}
