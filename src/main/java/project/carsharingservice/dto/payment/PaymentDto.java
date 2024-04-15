package project.carsharingservice.dto.payment;

import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;
import lombok.experimental.Accessors;
import project.carsharingservice.model.Payment;

@Data
@Accessors(chain = true)
public class PaymentDto {
    private long id;
    private long rentalId;
    private Payment.PaymentStatus paymentStatus;
    private Payment.PaymentType paymentType;
    private URL sessionUrl;
    private String sessionId;
    private BigDecimal totalPrice;
}
