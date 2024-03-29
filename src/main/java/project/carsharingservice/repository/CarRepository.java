package project.carsharingservice.repository;

import org.springframework.data.jpa.repository.*;
import project.carsharingservice.model.*;

public interface CarRepository extends JpaRepository<Car, Long> {
}
