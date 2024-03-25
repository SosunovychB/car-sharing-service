package project.carsharingservice.repository;

import org.springframework.data.jpa.repository.*;
import project.carsharingservice.model.*;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmail(String email);
}
