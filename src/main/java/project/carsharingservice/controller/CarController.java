package project.carsharingservice.controller;

import jakarta.validation.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import project.carsharingservice.dto.car.*;
import project.carsharingservice.model.*;
import project.carsharingservice.service.*;

import java.util.*;

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
