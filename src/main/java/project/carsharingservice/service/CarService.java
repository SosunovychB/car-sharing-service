package project.carsharingservice.service;

import org.springframework.data.domain.*;
import project.carsharingservice.dto.car.*;

import java.util.*;

public interface CarService {
    List<CarDto> getAllCars(Pageable pageable);

    CarDto getCarById(Long carId);

    CarDto addNewCar(AddNewCarRequestDto requestDto);

    CarDto updateCarInfoById(Long carId, UpdateCarInfoRequestDto requestDto);

    void deleteCarById(Long carId);
}
