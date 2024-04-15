package project.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static project.carsharingservice.model.Car.Type.SEDAN;
import static project.carsharingservice.model.Payment.PaymentStatus.PAID;
import static project.carsharingservice.model.Payment.PaymentStatus.PENDING;
import static project.carsharingservice.model.Payment.PaymentType.PAYMENT;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.carsharingservice.dto.payment.MakePaymentRequestDto;
import project.carsharingservice.dto.payment.PaymentDto;
import project.carsharingservice.exception.EntityNotFoundException;
import project.carsharingservice.exception.PaidPaymentException;
import project.carsharingservice.exception.RentalReturnException;
import project.carsharingservice.exception.UnauthorizedAccessException;
import project.carsharingservice.mapper.PaymentMapper;
import project.carsharingservice.model.Car;
import project.carsharingservice.model.Payment;
import project.carsharingservice.model.Rental;
import project.carsharingservice.model.Role;
import project.carsharingservice.model.User;
import project.carsharingservice.repository.PaymentRepository;
import project.carsharingservice.repository.RentalRepository;
import project.carsharingservice.service.impl.StripePaymentService;
import project.carsharingservice.service.notification.bot.NotificationService;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {
    @Mock
    private NotificationService notificationService;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private StripePaymentService stripePaymentService;

    @Test
    @DisplayName("Verify createPaymentSession() method works")
    public void testCreatePaymentSession_WithValidRequestDto_ReturnsExpectedPaymentDto()
            throws MalformedURLException {
        //given
        User user = createCustomerUser();
        Rental rental = createRental(user, false);
        rental.setCar(createDefaultCar());
        MakePaymentRequestDto requestDto = new MakePaymentRequestDto().setRentalId(rental.getId());
        Payment modelPayment = createModelPayment();
        Payment expectedPayment = createPayment(1L, rental, createDefaultSession());
        PaymentDto expectedPaymentDto = createPaymentDto(expectedPayment);

        Mockito.when(rentalRepository.findById(requestDto.getRentalId()))
                .thenReturn(Optional.of(rental));
        Mockito.when(paymentRepository.findSuccessfulPaymentByRentalId(rental.getId()))
                .thenReturn(Optional.empty());
        Mockito.when(paymentRepository.save(any()))
                .thenReturn(modelPayment);
        Mockito.when(paymentRepository.save(any()))
                .thenReturn(expectedPayment);
        Mockito.when(paymentMapper.entityToPaymentDto(expectedPayment))
                .thenReturn(expectedPaymentDto);

        //when
        PaymentDto actualPaymentDto = stripePaymentService
                .createPaymentSession(requestDto, user);

        //then
        Assertions.assertEquals(expectedPaymentDto, actualPaymentDto);

        Mockito.verify(rentalRepository, times(1))
                .findById(requestDto.getRentalId());
        Mockito.verify(paymentRepository, times(1))
                .findSuccessfulPaymentByRentalId(rental.getId());
        Mockito.verify(paymentRepository, times(2))
                .save(any());
        Mockito.verify(paymentMapper, times(1))
                .entityToPaymentDto(expectedPayment);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoMoreInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify createPaymentSession() throws exception when invalid rentalId")
    public void createPaymentSession_InvalidRentalId_ThrowsException() {
        //given
        MakePaymentRequestDto requestDto = new MakePaymentRequestDto().setRentalId(100L);
        User user = createCustomerUser();

        Mockito.when(rentalRepository.findById(requestDto.getRentalId()))
                .thenReturn(Optional.empty());

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> stripePaymentService.createPaymentSession(requestDto, user)
        );

        //then
        String expectedMessage = "Rental with id " + requestDto.getRentalId() + " was not found";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1))
                .findById(requestDto.getRentalId());
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoInteractions(paymentRepository);
        Mockito.verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify createPaymentSession() throws exception when rental is paid")
    public void createPaymentSession_PaidRental_ThrowsException()
            throws MalformedURLException {
        //given
        User user = createCustomerUser();
        Rental rental = createRental(user, false);
        MakePaymentRequestDto requestDto = new MakePaymentRequestDto().setRentalId(rental.getId());
        Payment paidPayment = createPayment(1L, rental, createDefaultSession());
        paidPayment.setPaymentStatus(PAID);

        Mockito.when(rentalRepository.findById(requestDto.getRentalId()))
                .thenReturn(Optional.of(rental));
        Mockito.when(paymentRepository.findSuccessfulPaymentByRentalId(rental.getId()))
                .thenReturn(Optional.of(paidPayment));

        //when
        Exception exception = assertThrows(
                PaidPaymentException.class,
                () -> stripePaymentService.createPaymentSession(requestDto, user)
        );

        //then
        String expectedMessage = "Payment for rental with id " + rental.getId()
                + " was already done";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1))
                .findById(requestDto.getRentalId());
        Mockito.verify(paymentRepository, times(1))
                .findSuccessfulPaymentByRentalId(rental.getId());
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify createPaymentSession() throws exception when user is not rental owner")
    public void createPaymentSession_UserIsNotRentalOwner_ThrowsException()
            throws MalformedURLException {
        //given
        User user = createCustomerUser();
        Rental rental = createRental(new User().setId(user.getId() + 10), false);
        MakePaymentRequestDto requestDto = new MakePaymentRequestDto().setRentalId(rental.getId());

        Mockito.when(rentalRepository.findById(requestDto.getRentalId()))
                .thenReturn(Optional.of(rental));
        Mockito.when(paymentRepository.findSuccessfulPaymentByRentalId(rental.getId()))
                .thenReturn(Optional.empty());

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> stripePaymentService.createPaymentSession(requestDto, user)
        );

        //then
        String expectedMessage = "User with id " + user.getId()
                + " does not have rental with id " + rental.getId();
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1))
                .findById(requestDto.getRentalId());
        Mockito.verify(paymentRepository, times(1))
                .findSuccessfulPaymentByRentalId(rental.getId());
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify createPaymentSession() throws exception when "
            + "rental is not closed")
    public void testCreatePaymentSession_RentalIsNotClosed_ThrowsException()
            throws MalformedURLException {
        //given
        User user = createCustomerUser();
        Rental rental = createRental(user, true);
        rental.setCar(createDefaultCar());
        MakePaymentRequestDto requestDto = new MakePaymentRequestDto().setRentalId(rental.getId());
        Payment modelPayment = createModelPayment();

        Mockito.when(rentalRepository.findById(requestDto.getRentalId()))
                .thenReturn(Optional.of(rental));
        Mockito.when(paymentRepository.findSuccessfulPaymentByRentalId(rental.getId()))
                .thenReturn(Optional.empty());
        Mockito.when(paymentRepository.save(any()))
                .thenReturn(modelPayment);

        //when
        Exception exception = assertThrows(
                RentalReturnException.class,
                () -> stripePaymentService.createPaymentSession(requestDto, user)
        );

        //then
        String expectedMessage = "Before payment for rental with id "
                + rental.getId() + " you must return the car";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1))
                .findById(requestDto.getRentalId());
        Mockito.verify(paymentRepository, times(1))
                .findSuccessfulPaymentByRentalId(rental.getId());
        Mockito.verify(paymentRepository, times(1))
                .save(any());
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify getAllPaymentsByUserId() method works when "
            + "valid usedId for customer user ")
    public void getAllPaymentsByUserId_ValidUserIdForCustomerUser_ReturnsPaymentDtoList()
            throws MalformedURLException {
        //given
        User customerUser = createCustomerUser();
        long userId = customerUser.getId();

        Payment payment = createPayment(1L,
                createRental(customerUser, false),
                createDefaultSession());
        List<Payment> paymentList = List.of(payment);

        PaymentDto paymentDto = createPaymentDto(paymentList.get(0));
        List<PaymentDto> expectedPaymentDtoList = List.of(paymentDto);

        Mockito.when(paymentRepository.findAllByUserId(userId))
                .thenReturn(paymentList);
        Mockito.when(paymentMapper.entityToPaymentDto(paymentList.get(0)))
                .thenReturn(paymentDto);

        //when
        List<PaymentDto> actualPaymentDtoList = stripePaymentService
                .getAllPaymentsByUserId(userId, customerUser);

        //then
        Assertions.assertEquals(expectedPaymentDtoList, actualPaymentDtoList);

        Mockito.verify(paymentRepository, times(1))
                .findAllByUserId(userId);
        Mockito.verify(paymentMapper, times(1))
                .entityToPaymentDto(paymentList.get(0));
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoMoreInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify getAllPaymentsByUserId() method works when "
            + "any usedId for manager user ")
    public void getAllPaymentsByUserId_AnyUserIdAndManagerUser_ReturnsPaymentDtoList()
            throws MalformedURLException {
        //given
        User managerUser = createManagerUser();
        long userId = managerUser.getId() + 10;

        Payment payment = createPayment(1L,
                createRental(managerUser, false),
                createDefaultSession());
        List<Payment> paymentList = List.of(payment);

        PaymentDto paymentDto = createPaymentDto(paymentList.get(0));
        List<PaymentDto> expectedPaymentDtoList = List.of(paymentDto);

        Mockito.when(paymentRepository.findAllByUserId(userId))
                .thenReturn(paymentList);
        Mockito.when(paymentMapper.entityToPaymentDto(paymentList.get(0)))
                .thenReturn(paymentDto);

        //when
        List<PaymentDto> actualPaymentDtoList = stripePaymentService
                .getAllPaymentsByUserId(userId, managerUser);

        //then
        Assertions.assertEquals(expectedPaymentDtoList, actualPaymentDtoList);

        Mockito.verify(paymentRepository, times(1))
                .findAllByUserId(userId);
        Mockito.verify(paymentMapper, times(1))
                .entityToPaymentDto(paymentList.get(0));
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoMoreInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify getAllPaymentsByUserId() throws exception when "
            + "invalid usedId for customer user")
    public void getAllPaymentsByUserId_InvalidUserIdForCustomerUser_ThrowsException()
            throws MalformedURLException {
        //given
        User customerUser = createCustomerUser();
        long userId = customerUser.getId() + 1;

        //when
        Exception exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> stripePaymentService.getAllPaymentsByUserId(userId, customerUser)
        );

        //then
        String expectedMessage = "You do not have access to specified payment(s)";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verifyNoInteractions(paymentRepository);
        Mockito.verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Verify verifySuccessfulPayment() method works")
    public void verifySuccessfulPayment_ValidPaymentId_Works()
            throws MalformedURLException {
        //given
        long paymentId = 1L;
        Rental rental = createRental(new User().setId(1L), false);
        Session session = createDefaultSession();
        Payment payment = createPayment(paymentId, rental, session);

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        //when
        stripePaymentService.verifySuccessfulPayment(paymentId);

        //then
        Mockito.verify(paymentRepository, times(1))
                .findById(paymentId);
        Mockito.verify(paymentRepository, times(1))
                .save(any());
        Mockito.verify(notificationService, times(1))
                .sendSuccessfulPaymentNotification(rental.getId());
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Verify verifySuccessfulPayment() throws exception for invalid paymentId")
    public void verifySuccessfulPayment_InvalidPaymentId_ThrowsException()
            throws MalformedURLException {
        //given
        long paymentId = -1L;

        Mockito.when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> stripePaymentService.verifySuccessfulPayment(paymentId)
        );

        //then
        String expectedMessage = "Ooops... Something went wrong... "
                + "Payment with id " + paymentId + " was not found";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(paymentRepository, times(1)).findById(paymentId);
        Mockito.verifyNoMoreInteractions(paymentRepository);
        Mockito.verifyNoInteractions(notificationService);
    }

    private User createCustomerUser() {
        User user = createUser();
        user.getRoles().add(new Role().setRoleName(Role.RoleName.ROLE_CUSTOMER));
        return user;
    }

    private User createManagerUser() {
        User user = createUser();
        user.getRoles().add(new Role().setRoleName(Role.RoleName.ROLE_MANAGER));
        return user;
    }

    private User createUser() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = "Password1234$";

        User user = new User();
        user.setId(2L)
                .setEmail("email@example.com")
                .setPassword(passwordEncoder.encode(password))
                .setFirstName("FirstName")
                .setLastName("LastName")
                .setDeleted(false);
        return user;
    }

    private Session createDefaultSession() throws MalformedURLException {
        Session session = new Session();
        session.setUrl("http://default.url");
        session.setId("1234a");
        return session;
    }

    private Rental createRental(User user, Boolean isActive) {
        return new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.now())
                .setReturnDate(LocalDate.now().plusDays(7))
                .setActualReturnDate(isActive ? null : LocalDate.now())
                .setUser(user);
    }

    private Payment createPayment(long paymentId, Rental rental, Session session)
            throws MalformedURLException {
        return new Payment()
                .setId(paymentId)
                .setRental(rental)
                .setPaymentStatus(PENDING)
                .setPaymentType(PAYMENT)
                .setSessionUrl(new URL(session.getUrl()))
                .setSessionId(session.getId())
                .setTotalPrice(new BigDecimal("99.99"));
    }

    private PaymentDto createPaymentDto(Payment payment) {
        return new PaymentDto().setId(payment.getId())
                .setRentalId(payment.getRental().getId())
                .setPaymentStatus(payment.getPaymentStatus())
                .setPaymentType(payment.getPaymentType())
                .setSessionUrl(payment.getSessionUrl())
                .setSessionId(payment.getSessionId())
                .setTotalPrice(payment.getTotalPrice());
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

    private Car createDefaultCar() {
        return new Car()
                .setId(1L)
                .setBrand("Audi")
                .setModel("A4")
                .setType(SEDAN)
                .setInventory(10)
                .setDailyFee(new BigDecimal("199.99"))
                .setDeleted(false);
    }
}
