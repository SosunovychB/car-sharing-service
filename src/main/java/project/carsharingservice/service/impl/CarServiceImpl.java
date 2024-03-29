package project.carsharingservice.service.impl;

import jakarta.transaction.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import project.carsharingservice.dto.car.*;
import project.carsharingservice.exception.*;
import project.carsharingservice.mapper.*;
import project.carsharingservice.model.*;
import project.carsharingservice.repository.*;
import project.carsharingservice.service.*;

import java.lang.reflect.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public List<CarDto> getAllCars(Pageable pageable) {
        Page<Car> carPage = carRepository.findAll(pageable);
        return carPage.stream()
                .map(carMapper::entityToCarDto)
                .toList();
    }

    @Override
    public CarDto getCarById(Long carId) {
        Car car = findCarById(carId);
        return carMapper.entityToCarDto(car);
    }

    @Override
    public CarDto addNewCar(AddNewCarRequestDto requestDto) {
        Car newCar = carMapper.addNewCarRequestDtoToEntity(requestDto);
        Car savedNewCar = carRepository.save(newCar);
        return carMapper.entityToCarDto(savedNewCar);
    }

    @Override
    @Transactional
    public CarDto updateCarInfoById(Long carId, UpdateCarInfoRequestDto requestDto) {
        Car car = findCarById(carId);
        Car updatedCat = carMapper.updateCarInfo(car, requestDto);
        Car savedCar = carRepository.save(updatedCat);
        return carMapper.entityToCarDto(savedCar);
    }

    @Override
    public void deleteCarById(Long carId) {
        carRepository.deleteById(carId);
    }

    private Car findCarById(Long carId) {
        return carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Car with id " + carId + " was not found"));
    }
}
