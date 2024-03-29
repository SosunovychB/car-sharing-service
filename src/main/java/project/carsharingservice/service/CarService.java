package project.carsharingservice.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import project.carsharingservice.dto.car.AddNewCarRequestDto;
import project.carsharingservice.dto.car.CarDto;
import project.carsharingservice.dto.car.UpdateCarInfoRequestDto;

public interface CarService {
    List<CarDto> getAllCars(Pageable pageable);

    CarDto getCarById(Long carId);

    CarDto addNewCar(AddNewCarRequestDto requestDto);

    CarDto updateCarInfoById(Long carId, UpdateCarInfoRequestDto requestDto);

    void deleteCarById(Long carId);
}
