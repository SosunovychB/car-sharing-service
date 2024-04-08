package project.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Payment management", description = "Endpoints for managing payments")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a payment session",
            description = "Create a payment session")
    public PaymentDto createPaymentSession(@RequestBody @Valid MakePaymentRequestDto requestDto,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return paymentService.createPaymentSession(requestDto, user);
    }

    @GetMapping
    @Operation(summary = "Get a payment by user id",
            description = "Get a payment by user id")
    public List<PaymentDto> getPaymentsByUserId(@RequestParam long userId,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return paymentService.getAllPaymentsByUserId(userId, user);
    }

    @GetMapping("/success/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Successful page for redirection",
            description = "Successful page for redirection")
    public String paymentSuccessRedirect(@PathVariable long paymentId) {
        paymentService.verifySuccessfulPayment(paymentId);
        return "some-success-page";
    }

    @GetMapping("/cancel/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel page for redirection",
            description = "Cancel page for redirection")
    public String paymentCancelRedirect(@PathVariable long paymentId) {
        return "Payment with id " + paymentId
                + " was canceled, but you can finish it for 24 hours.";
    }
}
