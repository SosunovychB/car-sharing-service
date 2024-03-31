package project.carsharingservice.dto.rental;

import java.time.LocalDate;
import lombok.Data;
import project.carsharingservice.dto.car.RentedCarDto;

@Data
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private RentedCarDto rentedCarDto;
    private long userId;
}
