package project.carsharingservice.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@NotNull
@Accessors(chain = true)
public class AddNewCarRequestDto {
    @NotBlank
    private String brand;
    @NotBlank
    private String model;
    @NotBlank
    @Pattern(regexp = "(?i)sedan|suv|hatchback|universal", message = "Invalid car type. "
            + "It can be only sedan, suv, hatchback, or universal.")
    private String type;
    @NotNull
    @Positive
    private Integer inventory;
    @NotNull
    @Positive
    private BigDecimal dailyFee;
}
