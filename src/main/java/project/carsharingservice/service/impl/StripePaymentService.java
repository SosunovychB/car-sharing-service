package project.carsharingservice.service.impl;

import static project.carsharingservice.model.Payment.PaymentStatus.PAID;
import static project.carsharingservice.model.Payment.PaymentStatus.PENDING;
import static project.carsharingservice.model.Payment.PaymentType.FINE;
import static project.carsharingservice.model.Payment.PaymentType.PAYMENT;

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
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.carsharingservice.dto.payment.MakePaymentRequestDto;
import project.carsharingservice.dto.payment.PaymentDto;
import project.carsharingservice.exception.CreateSessionException;
import project.carsharingservice.exception.EntityNotFoundException;
import project.carsharingservice.exception.PaidPaymentException;
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
    public PaymentDto createPaymentSession(MakePaymentRequestDto requestDto, User user) {
        Rental rental = getRentalById(requestDto.getRentalId());
        checkIfRentalIsPaid(rental);
        checkIfUserIsRentalOwner(rental, user);

        Payment newPayment = paymentRepository.save(createModelPayment());

        BigDecimal totalPrice = calculateTotalPrice(rental);
        Session session = createSession(totalPrice, newPayment, rental);

        updateActualInfoForPayment(newPayment, rental, totalPrice, session);
        paymentRepository.save(newPayment);
        return paymentMapper.entityToPaymentDto(newPayment);
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
    public void verifySuccessfulPayment(long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Ooops... Something went wrong... "
                        + "Payment with id " + paymentId + " was not found")
        );
        payment.setPaymentStatus(PAID);
        paymentRepository.save(payment);

        notificationService.sendSuccessfulPaymentNotification(payment.getRental().getId());
    }

    private Rental getRentalById(long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id " + rentalId + " was not found")
        );
    }

    private void checkIfRentalIsPaid(Rental rental) {
        Optional<Payment> paymentOptional = paymentRepository
                .findSuccessfulPaymentByRentalId(rental.getId());
        if (paymentOptional.isPresent()) {
            throw new PaidPaymentException("Payment for rental with id " + rental.getId()
                    + " was already done");
        }
    }

    private void checkIfUserIsRentalOwner(Rental rental, User user) {
        if (!Objects.equals(rental.getUser().getId(), user.getId())) {
            throw new EntityNotFoundException("User with id " + user.getId()
                    + " does not have rental with id " + rental.getId());
        }
    }

    private BigDecimal calculateTotalPrice(Rental rental) {
        checkIfRentalClosed(rental);

        long numberOfRentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(), rental.getActualReturnDate());
        if (numberOfRentalDays == 0) {
            numberOfRentalDays = 1;
        }
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
                    + rental.getId() + " you must return the car");
        }
    }

    private Payment createModelPayment() {
        Rental rental = new Rental();
        rental.setId(0L);

        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setPaymentStatus(PENDING);
        payment.setPaymentType(PAYMENT);
        payment.setSessionId("id");
        payment.setTotalPrice(new BigDecimal(0));
        try {
            payment.setSessionUrl(new URL("http://default.url"));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can not create default url", e);
        }
        return payment;
    }

    private Session createSession(BigDecimal totalPrice,
                                  Payment payment,
                                  Rental rental) {
        Stripe.apiKey = "sk_test_51P1aBM04jfql3zF3R1rHSqGC4p0U5Lny8EKbEiKu8CNUb"
                    + "5i0ZAVRcC0LbhOG9jI8GhvAWHhdWl9BHS1RKDS2kM5W00pltajtgF";
        final long expirationTime =
                Instant.now().plusSeconds(24 * 60 * 60).getEpochSecond();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(String.valueOf(
                                URI.create(LOCAL_DOMAIN + SUCCESSFUL_PAYMENT_PATH
                                        + payment.getId())))
                        .setCancelUrl(String.valueOf(
                                URI.create(LOCAL_DOMAIN + CANCELED_PAYMENT_PATH
                                        + payment.getId())))
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setProductData(
                                                                SessionCreateParams.LineItem
                                                                        .PriceData
                                                                        .ProductData.builder()
                                                                        .setName("Payment for "
                                                                                + "rental "
                                                                                + rental)
                                                                        .build()
                                                        )
                                                        .setUnitAmount(
                                                                totalPrice.longValue() * 100L)
                                                        .setCurrency(CURRENCY)
                                                        .build())
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

    private Payment updateActualInfoForPayment(Payment payment,
                                               Rental rental,
                                               BigDecimal totalPrice,
                                               Session session) {
        payment.setRental(rental);
        payment.setPaymentType(isRentalReturnedLate(rental) ? FINE : PAYMENT);
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
