package project.carsharingservice.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import project.carsharingservice.dto.car.AddNewCarRequestDto;
import project.carsharingservice.dto.car.CarDto;
import project.carsharingservice.dto.car.UpdateCarInfoRequestDto;
import project.carsharingservice.service.CarService;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @GetMapping
    public List<CarDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @GetMapping("/{carId}")
    public CarDto getCarById(@PathVariable Long carId) {
        return carService.getCarById(carId);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public CarDto addNewCar(@RequestBody @Valid AddNewCarRequestDto requestDto) {
        return carService.addNewCar(requestDto);
    }

    @PutMapping("/{carId}")
    @PreAuthorize("hasRole('MANAGER')")
    public CarDto updateCarInfoById(@PathVariable Long carId,
                                    @RequestBody @Valid UpdateCarInfoRequestDto requestDto) {
        return carService.updateCarInfoById(carId, requestDto);
    }

    @DeleteMapping("/{carId}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable Long carId) {
        carService.deleteCarById(carId);
    }
}
