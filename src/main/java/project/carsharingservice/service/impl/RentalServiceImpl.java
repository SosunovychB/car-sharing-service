package project.carsharingservice.service.impl;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.carsharingservice.dto.rental.CreateRentalRequestDto;
import project.carsharingservice.dto.rental.GetAllRentalsRequestDto;
import project.carsharingservice.dto.rental.RentalDto;
import project.carsharingservice.dto.rental.RentalDtoWithoutCarInfo;
import project.carsharingservice.exception.ClosedRentalException;
import project.carsharingservice.exception.EntityNotFoundException;
import project.carsharingservice.exception.UnauthorizedAccessException;
import project.carsharingservice.mapper.RentalMapper;
import project.carsharingservice.model.Car;
import project.carsharingservice.model.Rental;
import project.carsharingservice.model.Role;
import project.carsharingservice.model.User;
import project.carsharingservice.repository.CarRepository;
import project.carsharingservice.repository.RentalRepository;
import project.carsharingservice.service.RentalService;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;

    @Override
    public List<RentalDtoWithoutCarInfo> getRentalsByUserId(GetAllRentalsRequestDto requestDto,
                                                            User user) {
        List<Rental> rentalDtoList = rentalRepository.findAllByUserId(requestDto.getUserId());
        checkAccessToRentals(user, rentalDtoList);

        if (requestDto.getIsActive() == null) {
            return rentalDtoList.stream()
                    .map(rentalMapper::entityRentalDtoWithoutCarInfo)
                    .toList();
        }
        return requestDto.getIsActive()
                ? rentalDtoList.stream()
                .filter(rental -> rental.getActualReturnDate() == null)
                .map(rentalMapper::entityRentalDtoWithoutCarInfo)
                .toList()
                : rentalDtoList.stream()
                .filter(rental -> rental.getActualReturnDate() != null)
                .map(rentalMapper::entityRentalDtoWithoutCarInfo)
                .toList();
    }

    @Override
    public RentalDto getRentalById(Long rentalId,
                                   User user) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Rental with id " + rentalId + " was not found.")
        );
        checkAccessToRentals(user, List.of(rental));

        return rentalMapper.entityToRentalDto(rental);
    }

    @Override
    @Transactional
    public RentalDto createRental(CreateRentalRequestDto requestDto, User user) {
        Rental newRental = new Rental();
        newRental.setRentalDate(LocalDate.now());
        newRental.setReturnDate(LocalDate.now().plusDays(requestDto.getNumberOfDays()));
        newRental.setActualReturnDate(null);
        newRental.setUser(user);
        setUpCarForNewRental(newRental, requestDto.getCarId());

        Rental savedNewRental = rentalRepository.save(newRental);
        return rentalMapper.entityToRentalDto(savedNewRental);
    }

    @Override
    @Transactional
    public RentalDto setActualReturnDate(Long rentalId, Long userId) {
        Rental rental = findRentalByIdAndUserId(rentalId, userId);
        checkIfRentalNotClosed(rental);

        rental.setReturnDate(LocalDate.now());
        increaseCarQuantity(rental);

        Rental updatedRental = rentalRepository.save(rental);
        return rentalMapper.entityToRentalDto(updatedRental);
    }

    private void checkAccessToRentals(User user, List<Rental> rentals) {
        if (user.getRoles().contains(Role.RoleName.ROLE_MANAGER)) {
            return;
        }

        List<Long> userRentalIds = rentals.stream()
                .map(Rental::getId)
                .toList();

        if (!userRentalIds.contains(user.getId())) {
            throw new UnauthorizedAccessException("You do not have access to specified rentals.");
        }
    }

    private Rental findRentalByIdAndUserId(Long rentalId, Long userId) {
        return rentalRepository.findRentalByIdAndUserId(rentalId, userId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + userId
                        + " does not have rental with id " + rentalId)
        );
    }

    private void checkIfRentalNotClosed(Rental rental) {
        if (rental.getActualReturnDate() != null) {
            throw new ClosedRentalException("Rental with id "
                    + rental.getId() + " was already closed.");
        }
    }

    private void setUpCarForNewRental(Rental newRental, Long carId) {
        Car car = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Car with id " + carId + " was not found.")
        );

        if (car.getInventory() <= 0) {
            throw new EntityNotFoundException("Sorry, this car is not available now.");
        } else {
            car.setInventory(car.getInventory() - 1);
        }

        newRental.setCar(car);
        carRepository.save(car);
    }

    private void increaseCarQuantity(Rental rental) {
        Car car = carRepository.findById(rental.getCar().getId()).orElseThrow(
                () -> new EntityNotFoundException("Something went wrong! "
                        + "Car with id " + rental.getCar().getId() + " was not found!")
        );
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
    }
}
