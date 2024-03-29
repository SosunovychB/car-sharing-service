package project.carsharingservice.dto.car;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import project.carsharingservice.model.*;

import java.math.*;

@Data
public class UpdateCarInfoRequestDto {
    private String brand;
    private String model;
    @Pattern(regexp = "sedan|suv|hatchback|universal", message = "Invalid car type. "
            + "There are be only sedan, suv, hatchback, or universal.")
    private String type;
    @Positive
    private Integer inventory;
    @Positive
    private BigDecimal dailyFee;
}
