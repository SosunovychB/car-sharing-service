package project.carsharingservice.dto.car;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateCarInfoRequestDto {
    private String brand;
    private String model;
    @Pattern(regexp = "(?i)sedan|suv|hatchback|universal", message = "Invalid car type. "
            + "There are be only sedan, suv, hatchback, or universal.")
    private String type;
    @Positive
    private Integer inventory;
    @Positive
    private BigDecimal dailyFee;
}
