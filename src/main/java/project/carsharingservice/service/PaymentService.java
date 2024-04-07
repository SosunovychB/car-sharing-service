package project.carsharingservice.service;

import java.util.List;
import project.carsharingservice.dto.payment.MakePaymentRequestDto;
import project.carsharingservice.dto.payment.PaymentDto;
import project.carsharingservice.model.User;

public interface PaymentService {
    PaymentDto createPaymentSession(MakePaymentRequestDto requestDto, User user);

    List<PaymentDto> getAllPaymentsByUserId(long userId, User user);

    void verifySuccessfulPayment(long paymentId);
}
