package project.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Car management", description = "Endpoints for managing cars")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @GetMapping
    @Operation(summary = "Get all cars in pages",
            description = "Get all cars in pages")
    public List<CarDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @GetMapping("/{carId}")
    @Operation(summary = "Get a car by id",
            description = "Get a car by id")
    public CarDto getCarById(@PathVariable Long carId) {
        return carService.getCarById(carId);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create a new car",
            description = "Create a new car",
            security = @SecurityRequirement(name = "bearerAuth"))
    public CarDto addNewCar(@RequestBody @Valid AddNewCarRequestDto requestDto) {
        return carService.addNewCar(requestDto);
    }

    @PutMapping("/{carId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update car info by id",
            description = "Update car info by id",
            security = @SecurityRequirement(name = "bearerAuth"))
    public CarDto updateCarInfoById(@PathVariable Long carId,
                                    @RequestBody @Valid UpdateCarInfoRequestDto requestDto) {
        return carService.updateCarInfoById(carId, requestDto);
    }

    @DeleteMapping("/{carId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete a car by id",
            description = "Delete a car by id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable Long carId) {
        carService.deleteCarById(carId);
    }
}
