package project.carsharingservice.dto.car;

import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;
import project.carsharingservice.model.Car;

@Data
@Accessors(chain = true)
public class RentedCarDto {
    private Long id;
    private String brand;
    private String model;
    private Car.Type type;
    private BigDecimal dailyFee;
}
