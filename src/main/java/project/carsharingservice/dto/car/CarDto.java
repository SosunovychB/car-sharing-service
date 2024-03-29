package project.carsharingservice.dto.car;

import java.math.BigDecimal;
import lombok.Data;
import project.carsharingservice.model.Car;

@Data
public class CarDto {
    private Long id;
    private String brand;
    private String model;
    private Car.Type type;
    private int inventory;
    private BigDecimal dailyFee;
}
