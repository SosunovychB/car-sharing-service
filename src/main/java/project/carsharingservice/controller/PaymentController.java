package project.carsharingservice.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.carsharingservice.dto.payment.MakePaymentRequestDto;
import project.carsharingservice.dto.payment.PaymentDto;
import project.carsharingservice.model.User;
import project.carsharingservice.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentDto createPaymentSession(@RequestBody @Valid MakePaymentRequestDto requestDto) {
        return paymentService.createPaymentSession(requestDto);
    }

    @GetMapping("/")
    public List<PaymentDto> getPaymentsByUserId(@RequestParam long userId,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return paymentService.getAllPaymentsByUserId(userId, user);
    }

    @GetMapping("/success/{rentalId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String paymentSuccessRedirect(@PathVariable long rentalId) {
        paymentService.verifySuccessfulPayment(rentalId);
        return "some-success-page";
    }

    @GetMapping("/cancel/{rentalId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String paymentCancelRedirect(@PathVariable long rentalId) {
        return "Payment for rental with id " + rentalId
                + " was canceled, but you can finish this payment for 24 hours.";
    }
}
