package project.carsharingservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.carsharingservice.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("FROM Payment p LEFT JOIN FETCH p.rental r LEFT JOIN FETCH r.user u "
            + "WHERE u.id = :userId")
    List<Payment> findAllByUserId(long userId);

    @Query("FROM Payment p LEFT JOIN FETCH p.rental r "
            + "WHERE p.paymentStatus = 'PAID' AND r.id = :rentalId")
    Optional<Payment> findSuccessfulPaymentByRentalId(long rentalId);
}
