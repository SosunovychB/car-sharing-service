package project.carsharingservice.dto.rental;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateRentalRequestDto {
    @Positive
    private long numberOfDays;
    @Positive
    private long carId;
}
