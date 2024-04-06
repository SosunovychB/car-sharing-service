package project.carsharingservice.dto.payment;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MakePaymentRequestDto {
    @Positive
    private long rentalId;
}
