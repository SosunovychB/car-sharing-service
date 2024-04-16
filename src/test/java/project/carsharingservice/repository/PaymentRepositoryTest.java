package project.carsharingservice.repository;

import static project.carsharingservice.model.Payment.PaymentStatus.PAID;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import project.carsharingservice.model.Payment;
import project.carsharingservice.model.Rental;
import project.carsharingservice.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:database/payments/delete-payments-from-the-payments-table.sql",
        "classpath:database/rentals/delete-rentals-from-the-rentals-table.sql",
        "classpath:database/cars/delete-cars-from-the-cars-table.sql",
        "classpath:database/users/delete-users-from-the-users-table.sql",
        "classpath:database/users/add-users-to-the-users-table.sql",
        "classpath:database/cars/add-cars-to-the-cars-table.sql",
        "classpath:database/rentals/add-rentals-to-the-rentals-table.sql",
        "classpath:database/payments/add-payments-to-the-payments-table.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:database/payments/delete-payments-from-the-payments-table.sql",
        "classpath:database/rentals/delete-rentals-from-the-rentals-table.sql",
        "classpath:database/cars/delete-cars-from-the-cars-table.sql",
        "classpath:database/users/delete-users-from-the-users-table.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Verify findAllByUserId() method works")
    void findAllByUserId_UserWithId1_Returns2Payments() {
        //given
        User expectedUser = new User();
        expectedUser.setId(1L);

        Rental expectedRental = new Rental();
        expectedRental.setId(1L);
        expectedRental.setUser(expectedUser);

        Payment expectedPayment = new Payment();
        expectedPayment.setId(1L);
        expectedPayment.setRental(expectedRental);

        long userId = expectedPayment.getRental().getUser().getId();

        //when
        List<Payment> actualPaymentList = paymentRepository.findAllByUserId(userId);

        //then
        Assertions.assertEquals(2, actualPaymentList.size());
        Assertions.assertEquals(userId, actualPaymentList.get(0).getRental().getUser().getId());
        Assertions.assertEquals(userId, actualPaymentList.get(1).getRental().getUser().getId());
    }

    @Test
    @DisplayName("Verify findSuccessfulPaymentByRentalId() method works")
    void findSuccessfulPaymentByRentalId_RentalId3_ReturnsOnePayment() {
        //given
        Rental expectedRental = new Rental();
        expectedRental.setId(3L);

        Payment expectedPayment = new Payment();
        expectedPayment.setId(3L);
        expectedPayment.setRental(expectedRental);
        expectedPayment.setPaymentStatus(PAID);

        Optional<Payment> expectedPaymentOptional = Optional.of(expectedPayment);

        long rentalId = expectedPayment.getRental().getId();

        //when
        Optional<Payment> actualPaymentOptional = paymentRepository
                .findSuccessfulPaymentByRentalId(rentalId);

        //then
        Assertions.assertEquals(expectedPaymentOptional.orElseThrow().getId(),
                actualPaymentOptional.orElseThrow().getId());
        Assertions.assertEquals(PAID, actualPaymentOptional.orElseThrow().getPaymentStatus());
    }
}
