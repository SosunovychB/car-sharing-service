package project.carsharingservice.controller;

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
import org.springframework.web.bind.annotation.RestController;
import project.carsharingservice.dto.rental.CreateRentalRequestDto;
import project.carsharingservice.dto.rental.GetAllRentalsRequestDto;
import project.carsharingservice.dto.rental.RentalDto;
import project.carsharingservice.dto.rental.RentalDtoWithoutCarInfo;
import project.carsharingservice.model.User;
import project.carsharingservice.service.RentalService;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @GetMapping
    public List<RentalDtoWithoutCarInfo> getRentalsByUserId(GetAllRentalsRequestDto requestDto,
                                                            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getRentalsByUserId(requestDto, user);
    }

    @GetMapping("/{rentalId}")
    public RentalDto getRentalById(@PathVariable Long rentalId,
                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.getRentalById(rentalId, user);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public RentalDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.createRental(requestDto, user);
    }

    @PostMapping("/{rentalId}/return")
    @PreAuthorize("hasRole('CUSTOMER')")
    public RentalDto setActualReturnDate(@PathVariable Long rentalId,
                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return rentalService.setActualReturnDate(rentalId, user.getId());
    }
}
