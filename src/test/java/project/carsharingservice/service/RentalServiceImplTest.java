package project.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.carsharingservice.dto.car.RentedCarDto;
import project.carsharingservice.dto.rental.CreateRentalRequestDto;
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
import project.carsharingservice.service.impl.RentalServiceImpl;
import project.carsharingservice.service.notification.bot.NotificationService;

@ExtendWith(MockitoExtension.class)
public class RentalServiceImplTest {
    @Mock
    private NotificationService notificationService;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @InjectMocks
    private RentalServiceImpl rentalServiceImpl;

    @Test
    @DisplayName("Verify getRentalsByUserId() method works when "
            + "userId is null, isRentalActive is true and managerUser")
    public void getRentalsByUserId_UserIdIsNullAndUserManager_ReturnsFilteredRentals() {
        // given
        Long userId = null;
        Boolean isRentalActive = true;
        User managerUser = createManagerUser();

        List<Rental> rentals = List.of(
                createNewRental(managerUser, true),
                createNewRental(managerUser, false)
        );

        List<RentalDtoWithoutCarInfo> expectedRentals = List.of(
                createDtoWithoutCarInfo(rentals.get(0)));

        Mockito.when(rentalRepository.findAll())
                .thenReturn(rentals);
        Mockito.when(rentalMapper.entityRentalDtoWithoutCarInfo(rentals.get(0)))
                .thenReturn(expectedRentals.get(0));

        // when
        List<RentalDtoWithoutCarInfo> actualRentals = rentalServiceImpl
                .getRentalsByUserId(userId, isRentalActive, managerUser);

        // then
        Assertions.assertEquals(expectedRentals, actualRentals);

        Mockito.verify(rentalRepository, times(1))
                .findAll();
        Mockito.verify(rentalMapper, times(1))
                .entityRentalDtoWithoutCarInfo(rentals.get(0));
        Mockito.verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalsByUserId() method works when "
            + "userId is not null, isRentalActive is true and managerUser")
    public void getRentalsByUserId_UserIdIsNotNull_ReturnsFilteredRentals() {
        // given
        Long userId = 2L;
        User user1 = createUser();
        user1.setId(1L);
        User user2 = createUser();
        user2.setId(userId);

        List<Rental> rentals = Arrays.asList(
                createNewRental(user1, true),
                createNewRental(user2, true)
        );

        List<RentalDtoWithoutCarInfo> expectedRentals = List.of(
                createDtoWithoutCarInfo(rentals.get(1)));

        Mockito.when(rentalRepository.findAllByUserId(userId))
                .thenReturn(List.of(rentals.get(1)));
        Mockito.when(rentalMapper.entityRentalDtoWithoutCarInfo(rentals.get(1)))
                .thenReturn(expectedRentals.get(0));

        // when
        List<RentalDtoWithoutCarInfo> actualRentals = rentalServiceImpl
                .getRentalsByUserId(userId, true, user2);

        // then
        Assertions.assertEquals(expectedRentals, actualRentals);

        Mockito.verify(rentalRepository, times(1))
                .findAllByUserId(userId);
        Mockito.verify(rentalMapper, times(1))
                .entityRentalDtoWithoutCarInfo(rentals.get(1));
        Mockito.verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalById() method works for valid rental id and user customer")
    public void getRentalById_ValidRentalIdAndUserCustomer_ReturnsRentalList() {
        //given
        long rentalId = 1L;

        User customerUser = createCustomerUser();
        Rental rental = createNewRental(customerUser, true);
        RentalDto expectedRentalDto = new RentalDto()
                .setId(rental.getId())
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setActualReturnDate(rental.getActualReturnDate())
                .setUserId(rental.getUser().getId());

        Mockito.when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        Mockito.when(rentalMapper.entityToRentalDto(rental)).thenReturn(expectedRentalDto);

        //when
        RentalDto actualRentalDto = rentalServiceImpl.getRentalById(rentalId, customerUser);

        //then
        Assertions.assertEquals(expectedRentalDto, actualRentalDto);

        Mockito.verify(rentalRepository, times(1)).findById(rentalId);
        Mockito.verify(rentalMapper, times(1)).entityToRentalDto(rental);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalById() method works for valid rental id and user manager")
    public void getRentalById_ValidRentalIdAndUserManager_ReturnRental() {
        //given
        long rentalId = 1L;

        User managerUser = createManagerUser();
        Rental rental = createNewRental(new User().setId(100L), true);
        RentalDto expectedRentalDto = new RentalDto()
                .setId(rental.getId())
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setActualReturnDate(rental.getActualReturnDate())
                .setUserId(rental.getUser().getId());

        Mockito.when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        Mockito.when(rentalMapper.entityToRentalDto(rental)).thenReturn(expectedRentalDto);

        //when
        RentalDto actualRentalDto = rentalServiceImpl.getRentalById(rentalId, managerUser);

        //then
        Assertions.assertEquals(expectedRentalDto, actualRentalDto);

        Mockito.verify(rentalRepository, times(1)).findById(rentalId);
        Mockito.verify(rentalMapper, times(1)).entityToRentalDto(rental);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalById() method throws exception for valid rental id "
            + "and invalid user customer")
    public void getRentalById_ValidRentalIdAndInvalidUserCustomer_ReturnRental() {
        //given
        long rentalId = 1L;

        User customerUser = createCustomerUser();
        Rental rental = createNewRental(new User().setId(100L), true);

        Mockito.when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        //when
        Exception exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> rentalServiceImpl.getRentalById(rentalId, customerUser));

        //then
        String expectedMessage = "You do not have access to specified rental(s)";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1)).findById(rentalId);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify getRentalById() method throws exception for invalid rental id "
            + "and valid user manager")
    public void getRentalById_InvalidRentalIdAndValidUserManager_ReturnRental() {
        //given
        long rentalId = -1L;

        User managerUser = createManagerUser();

        Mockito.when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> rentalServiceImpl.getRentalById(rentalId, managerUser));

        //then
        String expectedMessage = "Rental with id " + rentalId + " was not found";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1)).findById(rentalId);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify createRental() method works")
    public void createRental_ValidCarIdAndInventory_ReturnsRentalDto() {
        //given
        CreateRentalRequestDto createRentalRequestDto = new CreateRentalRequestDto()
                .setCarId(1L)
                .setNumberOfDays(7L);
        User customerUser = createCustomerUser();
        Car car = new Car()
                .setId(createRentalRequestDto.getCarId())
                .setInventory(1);
        Rental newRental = createNewRental(customerUser, true);
        RentalDto expectedRentalDto = createRentalDto(newRental);

        Mockito.when(carRepository.findById(car.getId()))
                .thenReturn(Optional.of(car));
        Mockito.when(carRepository.save(car))
                .thenReturn(car);
        Mockito.when(rentalRepository.save(any(Rental.class)))
                .thenReturn(newRental);
        Mockito.when(rentalMapper.entityToRentalDto(newRental))
                .thenReturn(expectedRentalDto);

        //when
        RentalDto actualRentalDto = rentalServiceImpl
                .createRental(createRentalRequestDto, customerUser);

        //then
        Assertions.assertEquals(expectedRentalDto, actualRentalDto);

        Mockito.verify(carRepository, times(1))
                .findById(car.getId());
        Mockito.verify(carRepository, times(1))
                .save(car);
        Mockito.verify(rentalRepository, times(1))
                .save(any(Rental.class));
        Mockito.verify(rentalMapper, times(1))
                .entityToRentalDto(newRental);
        Mockito.verifyNoMoreInteractions(carRepository);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify createRental() throws exception for invalid car inventory")
    public void createRental_InvalidCarInventory_ReturnsRentalDto() {
        //given
        CreateRentalRequestDto createRentalRequestDto = new CreateRentalRequestDto()
                .setCarId(1L)
                .setNumberOfDays(7L);
        User customerUser = createCustomerUser();
        Car car = new Car()
                .setId(createRentalRequestDto.getCarId())
                .setInventory(0);

        Mockito.when(carRepository.findById(createRentalRequestDto.getCarId()))
                .thenReturn(Optional.of(car));

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> rentalServiceImpl.createRental(createRentalRequestDto, customerUser)
        );

        //then
        String expectedMessage = "Sorry, this car with id "
                + createRentalRequestDto.getCarId() + " is not available now";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(carRepository, times(1))
                .findById(createRentalRequestDto.getCarId());
        Mockito.verifyNoInteractions(rentalRepository);
        Mockito.verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify createRental() throws exception for invalid car id")
    public void createRental_InvalidCarId_ReturnsRentalDto() {
        //given
        CreateRentalRequestDto createRentalRequestDto = new CreateRentalRequestDto()
                .setCarId(-1L)
                .setNumberOfDays(7L);
        User customerUser = createCustomerUser();

        Mockito.when(carRepository.findById(createRentalRequestDto.getCarId()))
                .thenReturn(Optional.empty());

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> rentalServiceImpl.createRental(createRentalRequestDto, customerUser)
        );

        //then
        String expectedMessage = "Car with id "
                + createRentalRequestDto.getCarId() + " was not found";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(carRepository, times(1))
                .findById(createRentalRequestDto.getCarId());
        Mockito.verifyNoInteractions(rentalRepository);
        Mockito.verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify setActualReturnDate() method works")
    public void setActualReturnDate_ValidRentalIdAndUserId_ReturnsRentalDto() {
        // given
        Long rentalId = 1L;
        Long userId = 1L;

        Car car = new Car().setId(1L).setInventory(0);

        Rental activeRental = new Rental()
                .setId(rentalId)
                .setCar(car)
                .setUser(new User().setId(userId))
                .setActualReturnDate(null);
        Rental closedRental = new Rental()
                .setId(rentalId)
                .setCar(car)
                .setUser(new User().setId(userId))
                .setActualReturnDate(LocalDate.now());
        RentalDto rentalDto = createRentalDto(closedRental);

        Mockito.when(rentalRepository.findRentalByIdAndUserId(rentalId, userId))
                .thenReturn(Optional.of(activeRental));
        Mockito.when(carRepository.findById(car.getId()))
                .thenReturn(Optional.of(car));
        Mockito.when(rentalRepository.save(any(Rental.class)))
                .thenReturn(closedRental);
        Mockito.when(rentalMapper.entityToRentalDto(any(Rental.class)))
                .thenReturn(rentalDto);

        // when
        RentalDto actualRentalDto = rentalServiceImpl.setActualReturnDate(rentalId, userId);

        // then
        Assertions.assertNotNull(actualRentalDto.getActualReturnDate());

        Mockito.verify(rentalRepository, times(1))
                .findRentalByIdAndUserId(rentalId, userId);
        Mockito.verify(carRepository, times(1))
                .findById(car.getId());
        Mockito.verify(rentalRepository, times(1))
                .save(any(Rental.class));
        Mockito.verify(rentalMapper, times(1))
                .entityToRentalDto(any(Rental.class));
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify setActualReturnDate() method throws exception")
    public void setActualReturnDate_InvalidRentalIdAndUserId_ThrowsException() {
        // given
        Long rentalId = -1L;
        Long userId = -1L;

        Mockito.when(rentalRepository.findRentalByIdAndUserId(rentalId, userId))
                .thenReturn(Optional.empty());

        // when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> rentalServiceImpl.setActualReturnDate(rentalId, userId)
        );

        // then
        String expectedMessage = "User with id " + userId
                + " does not have rental with id " + rentalId;
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1))
                .findRentalByIdAndUserId(rentalId, userId);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoInteractions(carRepository);
        Mockito.verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Verify setActualReturnDate() method throws exception")
    public void setActualReturnDate_ClosedRental_ThrowsException() {
        // given
        Long rentalId = 1L;
        Long userId = 1L;

        Car car = new Car().setId(1L).setInventory(0);

        Rental closedRental = new Rental()
                .setId(rentalId)
                .setCar(car)
                .setUser(new User().setId(userId))
                .setActualReturnDate(LocalDate.now());

        Mockito.when(rentalRepository.findRentalByIdAndUserId(rentalId, userId))
                .thenReturn(Optional.of(closedRental));

        // when
        Exception exception = assertThrows(
                ClosedRentalException.class,
                () -> rentalServiceImpl.setActualReturnDate(rentalId, userId)
        );

        // then
        String expectedMessage = "Rental with id "
                + closedRental.getId() + " was already closed";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verify(rentalRepository, times(1))
                .findRentalByIdAndUserId(rentalId, userId);
        Mockito.verifyNoMoreInteractions(rentalRepository);
        Mockito.verifyNoInteractions(carRepository);
        Mockito.verifyNoInteractions(rentalMapper);
    }

    private Rental createNewRental(User user, Boolean isActive) {
        return new Rental()
                .setRentalDate(LocalDate.now())
                .setReturnDate(LocalDate.now().plusDays(7))
                .setActualReturnDate(isActive ? null : LocalDate.now())
                .setUser(user);
    }

    private User createCustomerUser() {
        User user = createUser();
        user.getRoles().add(new Role().setRoleName(Role.RoleName.ROLE_CUSTOMER));
        return user;
    }

    private User createManagerUser() {
        User user = createUser();
        user.getRoles().add(new Role().setRoleName(Role.RoleName.ROLE_MANAGER));
        return user;
    }

    private User createUser() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = "Password1234$";

        User user = new User();
        user.setId(2L)
                .setEmail("email@example.com")
                .setPassword(passwordEncoder.encode(password))
                .setFirstName("FirstName")
                .setLastName("LastName")
                .setDeleted(false);
        return user;
    }

    private RentalDtoWithoutCarInfo createDtoWithoutCarInfo(Rental rental) {
        return new RentalDtoWithoutCarInfo()
                .setId(rental.getId())
                .setUserId(rental.getUser().getId())
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setActualReturnDate(rental.getActualReturnDate());
    }

    private RentalDto createRentalDto(Rental rental) {
        return new RentalDto()
                .setId(1L)
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setActualReturnDate(rental.getActualReturnDate())
                .setRentedCarDto(new RentedCarDto().setId(1L))
                .setUserId(rental.getUser().getId());
    }
}
