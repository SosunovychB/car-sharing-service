package project.carsharingservice.dto.car;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import project.carsharingservice.model.*;

import java.math.*;

@Data
@NotNull
public class AddNewCarRequestDto {
    @NotBlank
    private String brand;
    @NotBlank
    private String model;
    @NotBlank
    @Pattern(regexp = "(?i)sedan|suv|hatchback|universal", message = "Invalid car type. "
            + "It can be only sedan, suv, hatchback, or universal.")
    private String type;
    @Positive
    private Integer inventory;
    @Positive
    private BigDecimal dailyFee;
}
