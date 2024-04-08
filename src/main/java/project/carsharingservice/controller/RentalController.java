package project.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.carsharingservice.dto.rental.CreateRentalRequestDto;
import project.carsharingservice.dto.rental.RentalDto;
import project.carsharingservice.dto.rental.RentalDtoWithoutCarInfo;
import project.carsharingservice.model.User;
import project.carsharingservice.service.RentalService;

@Tag(name = "Rental management", description = "Endpoints for managing rentals")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @GetMapping
    @Operation(summary = "Get rentals by user id",
            description = "Get rentals by user id")
    public List<RentalDtoWithoutCarInfo> getRentalsByUserId(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isActive,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getRentalsByUserId(userId, isActive, user);
    }

    @GetMapping("/{rentalId}")
    @Operation(summary = "Get a rental by id",
            description = "Get a rental by id")
    public RentalDto getRentalById(@PathVariable Long rentalId,
                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getRentalById(rentalId, user);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new rental",
            description = "Create a new rental")
    public RentalDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.createRental(requestDto, user);
    }

    @PostMapping("/{rentalId}/return")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Set an actual return date for a rental",
            description = "Set an actual return date for a rental")
    public RentalDto setActualReturnDate(@PathVariable Long rentalId,
                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.setActualReturnDate(rentalId, user.getId());
    }
}
