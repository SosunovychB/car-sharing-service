package project.carsharingservice.service;

import java.util.List;
import project.carsharingservice.dto.rental.CreateRentalRequestDto;
import project.carsharingservice.dto.rental.GetAllRentalsRequestDto;
import project.carsharingservice.dto.rental.RentalDto;
import project.carsharingservice.dto.rental.RentalDtoWithoutCarInfo;
import project.carsharingservice.model.User;

public interface RentalService {
    List<RentalDtoWithoutCarInfo> getRentalsByUserId(GetAllRentalsRequestDto requestDto, User user);

    RentalDto getRentalById(Long rentalId, User user);

    RentalDto createRental(CreateRentalRequestDto requestDto, User user);

    RentalDto setActualReturnDate(Long rentalId, Long userId);
}
