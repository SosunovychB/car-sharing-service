package project.carsharingservice.dto.rental;

import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;
import project.carsharingservice.dto.car.RentedCarDto;

@Data
@Accessors(chain = true)
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private RentedCarDto rentedCarDto;
    private long userId;
}
