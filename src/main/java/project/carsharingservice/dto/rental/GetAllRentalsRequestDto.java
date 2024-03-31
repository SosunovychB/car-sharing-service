package project.carsharingservice.dto.rental;

import lombok.Data;

@Data
public class GetAllRentalsRequestDto {
    private long userId;
    private Boolean isActive;
}
