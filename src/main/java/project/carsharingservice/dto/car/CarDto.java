package project.carsharingservice.dto.car;

import jakarta.persistence.*;
import lombok.*;
import project.carsharingservice.model.*;

import java.math.*;

@Data
public class CarDto {
    private Long id;
    private String brand;
    private String model;
    private Car.Type type;
    private int inventory;
    private BigDecimal dailyFee;
}
