package project.carsharingservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import project.carsharingservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
