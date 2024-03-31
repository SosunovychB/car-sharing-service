package project.carsharingservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import project.carsharingservice.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findAllByUserId(Long userId);

    Optional<Rental> findRentalByIdAndUserId(Long rentalId, Long userId);
}
