package tourGuide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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




}
