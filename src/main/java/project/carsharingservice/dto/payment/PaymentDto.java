package project.carsharingservice.dto.payment;

import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;
import project.carsharingservice.model.Payment;

@Data
public class PaymentDto {
    private long id;
    private long rentalId;
    private Payment.PaymentStatus status;
    private Payment.PaymentType type;
    private URL sessionUrl;
    private String sessionId;
    private BigDecimal totalPrice;
}
