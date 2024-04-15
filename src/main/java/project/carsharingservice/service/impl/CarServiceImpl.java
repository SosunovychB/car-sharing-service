package project.carsharingservice.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.carsharingservice.dto.car.AddNewCarRequestDto;
import project.carsharingservice.dto.car.CarDto;
import project.carsharingservice.dto.car.UpdateCarInfoRequestDto;
import project.carsharingservice.exception.EntityNotFoundException;
import project.carsharingservice.mapper.CarMapper;
import project.carsharingservice.model.Car;
import project.carsharingservice.repository.CarRepository;
import project.carsharingservice.service.CarService;

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
        Car updatedCar = carMapper.updateCarInfo(car, requestDto);
        Car savedCar = carRepository.save(updatedCar);
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
