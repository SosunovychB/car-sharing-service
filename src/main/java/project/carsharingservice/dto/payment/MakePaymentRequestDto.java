package project.carsharingservice.dto.payment;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MakePaymentRequestDto {
    @Positive
    private long rentalId;
}
