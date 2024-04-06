package project.carsharingservice.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.carsharingservice.dto.payment.MakePaymentRequestDto;
import project.carsharingservice.dto.payment.PaymentDto;
import project.carsharingservice.exception.CreateSessionException;
import project.carsharingservice.exception.EntityNotFoundException;
import project.carsharingservice.exception.RentalReturnException;
import project.carsharingservice.exception.UnauthorizedAccessException;
import project.carsharingservice.mapper.PaymentMapper;
import project.carsharingservice.model.Payment;
import project.carsharingservice.model.Rental;
import project.carsharingservice.model.Role;
import project.carsharingservice.model.User;
import project.carsharingservice.repository.PaymentRepository;
import project.carsharingservice.repository.RentalRepository;
import project.carsharingservice.service.PaymentService;
import project.carsharingservice.service.notification.bot.NotificationService;

@Service
@RequiredArgsConstructor
public class StripePaymentService implements PaymentService {
    private static final int FINE_MULTIPLIER = 2;
    private static final String LOCAL_DOMAIN = "http://localhost:8080";
    private static final String SUCCESSFUL_PAYMENT_PATH = "/payments/success/";
    private static final String CANCELED_PAYMENT_PATH = "/payments/cancel/";
    private static final String CURRENCY = "usd";
    private final NotificationService notificationService;
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentDto createPaymentSession(MakePaymentRequestDto requestDto) {
        Rental rental = getRentalById(requestDto.getRentalId());
        BigDecimal totalPrice = calculateTotalPrice(rental);
        Session session = createSession(totalPrice, rental.getId());

        Payment payment = createPayment(rental, totalPrice, session);
        paymentRepository.save(payment);
        return paymentMapper.entityToPaymentDto(payment);
    }

    @Override
    public List<PaymentDto> getAllPaymentsByUserId(long userId, User user) {
        checkAccessToPayments(userId, user);

        List<Payment> payments = paymentRepository.findAllByUserId(userId);
        return payments.stream()
                .map(paymentMapper::entityToPaymentDto)
                .toList();
    }

    @Override
    @Transactional
    public void verifySuccessfulPayment(long rentalId) {
        Payment payment = paymentRepository.findByRentalId(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Ooops... Something went wrong... "
                        + "Payment for rental with id " + rentalId + " was not found")
        );
        payment.setPaymentStatus(Payment.PaymentStatus.PAID);
        paymentRepository.save(payment);

        notificationService.sendSuccessfulPaymentNotification(rentalId);
    }

    private Rental getRentalById(long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id " + rentalId + " was not found")
        );
    }

    private BigDecimal calculateTotalPrice(Rental rental) {
        checkIfRentalClosed(rental);

        long numberOfRentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(), rental.getActualReturnDate());
        BigDecimal dailyFee = rental.getCar().getDailyFee();

        return isRentalReturnedLate(rental)
                ? dailyFee.multiply(new BigDecimal(numberOfRentalDays))
                .multiply(new BigDecimal(FINE_MULTIPLIER))
                : dailyFee.multiply(new BigDecimal(numberOfRentalDays));
    }

    private boolean isRentalReturnedLate(Rental rental) {
        return rental.getActualReturnDate().isAfter(rental.getReturnDate());
    }

    private void checkIfRentalClosed(Rental rental) {
        if (rental.getActualReturnDate() == null) {
            throw new RentalReturnException("Before payment for rental with id "
                    + rental.getId() + " you must return car");
        }
    }

    private Session createSession(BigDecimal totalPrice, long rentalId) {
        Stripe.apiKey = System.getenv().get("STRIPE_API_KEY");
        final long expirationTime =
                Instant.now().plusSeconds(24 * 60 * 60).getEpochSecond();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(String.valueOf(
                                URI.create(LOCAL_DOMAIN + SUCCESSFUL_PAYMENT_PATH + rentalId)))
                        .setCancelUrl(String.valueOf(
                                URI.create(LOCAL_DOMAIN + CANCELED_PAYMENT_PATH + rentalId)))
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPrice(String.valueOf(totalPrice))
                                        .setCurrency(CURRENCY)
                                        .build())
                        .setExpiresAt(expirationTime)
                        .build();

        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            throw new CreateSessionException("Can not create session", e);
        }
        return session;
    }

    private Payment createPayment(Rental rental, BigDecimal totalPrice, Session session) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentType(isRentalReturnedLate(rental)
                ? Payment.PaymentType.FINE : Payment.PaymentType.PAYMENT);
        payment.setSessionId(session.getId());
        payment.setTotalPrice(totalPrice);
        try {
            payment.setSessionUrl(new URL(session.getUrl()));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Your string " + session.getUrl()
                    + " can not be parsed to URL", e);
        }
        return payment;
    }

    private void checkAccessToPayments(long userId, User user) {
        if (user == null
                || user.getRoles().stream().noneMatch(
                        role -> Role.RoleName.ROLE_MANAGER.equals(role.getRoleName()))
                && userId != user.getId()
        ) {
            throw new UnauthorizedAccessException("You do not have access to specified payment(s)");
        }
    }
}
