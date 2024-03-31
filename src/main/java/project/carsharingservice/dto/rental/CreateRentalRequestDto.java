package project.carsharingservice.dto.rental;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateRentalRequestDto {
    @Positive
    private long numberOfDays;
    @Positive
    private long carId;
}
