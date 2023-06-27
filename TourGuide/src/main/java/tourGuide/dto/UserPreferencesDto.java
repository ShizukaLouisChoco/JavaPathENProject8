package tourGuide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferencesDto implements Serializable {
    @NotNull
    private String userName;
    @NotNull
    private Integer tripDuration;
    @NotNull
    private String ticketQuantity;
    @NotNull
    private String numberOfAdults;
    @NotNull
    private String numberOfChildren;




}
