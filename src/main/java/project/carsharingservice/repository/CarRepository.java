package project.carsharingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.carsharingservice.model.Car;

public interface CarRepository extends JpaRepository<Car, Long> {
}
