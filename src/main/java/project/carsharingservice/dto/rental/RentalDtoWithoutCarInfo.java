package project.carsharingservice.dto.rental;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RentalDtoWithoutCarInfo {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private long userId;
}
