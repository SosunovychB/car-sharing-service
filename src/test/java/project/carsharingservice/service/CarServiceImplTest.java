package project.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static project.carsharingservice.model.Car.Type.SEDAN;

import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import project.carsharingservice.dto.car.AddNewCarRequestDto;
import project.carsharingservice.dto.car.CarDto;
import project.carsharingservice.dto.car.UpdateCarInfoRequestDto;
import project.carsharingservice.exception.EntityNotFoundException;
import project.carsharingservice.mapper.CarMapper;
import project.carsharingservice.model.Car;
import project.carsharingservice.repository.CarRepository;
import project.carsharingservice.service.impl.CarServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @InjectMocks
    private CarServiceImpl carServiceImpl;

    @Test
    @DisplayName("Verify getAllCars() method works")
    public void getAllCars_ValidPageable_ReturnsAllBooks() {
        //given
        long carId = 1L;
        Car car = createCar(carId);
        List<Car> carList = List.of(car);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> expectedCarsPage = new PageImpl<>(carList, pageable, carList.size());

        CarDto carDto = createCarDto(car);

        Mockito.when(carRepository.findAll(pageable)).thenReturn(expectedCarsPage);
        Mockito.when(carMapper.entityToCarDto(car)).thenReturn(carDto);

        //when
        List<CarDto> actualCarDtosList = carServiceImpl.getAllCars(pageable);

        //then
        Assertions.assertEquals(1, actualCarDtosList.size());
        Assertions.assertEquals(carDto, actualCarDtosList.get(0));

        Mockito.verify(carRepository, times(1)).findAll(pageable);
        Mockito.verify(carMapper, times(carList.size())).entityToCarDto(car);
        Mockito.verifyNoMoreInteractions(carRepository);
        Mockito.verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Verify getCarById() method works for valid car id")
    public void getCarById_ValidCarById_ReturnsAllBooks() {
        //given
        long carId = 1L;
        Car car = createCar(carId);
        Optional<Car> carOptional = Optional.of(car);
        CarDto expectedCarDto = createCarDto(car);

        Mockito.when(carRepository.findById(carId)).thenReturn(carOptional);
        Mockito.when(carMapper.entityToCarDto(car)).thenReturn(expectedCarDto);

        //when
        CarDto actualCarDto = carServiceImpl.getCarById(carId);

        //then
        Assertions.assertEquals(expectedCarDto, actualCarDto);

        Mockito.verify(carRepository, times(1)).findById(carId);
        Mockito.verify(carMapper, times(1)).entityToCarDto(car);
        Mockito.verifyNoMoreInteractions(carRepository);
        Mockito.verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("Verify getCarById() method throws exception for invalid car id")
    public void getCarById_InvalidCarById_ThrowsException() {
        //given
        long carId1 = 100L;
        long carId2 = 0L;
        long carId3 = -100L;

        Mockito.when(carRepository.findById(carId1)).thenReturn(Optional.empty());
        Mockito.when(carRepository.findById(carId2)).thenReturn(Optional.empty());
        Mockito.when(carRepository.findById(carId3)).thenReturn(Optional.empty());

        //when
        Exception exception1 = assertThrows(
                EntityNotFoundException.class,
                () -> carServiceImpl.getCarById(carId1));
        Exception exception2 = assertThrows(
                EntityNotFoundException.class,
                () -> carServiceImpl.getCarById(carId2));
        Exception exception3 = assertThrows(
                EntityNotFoundException.class,
                () -> carServiceImpl.getCarById(carId3));

        //then
        String expectedMessage1 = "Car with id " + carId1 + " was not found";
        String expectedMessage2 = "Car with id " + carId2 + " was not found";
        String expectedMessage3 = "Car with id " + carId3 + " was not found";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();
        String actualMessage3 = exception3.getMessage();

        Assertions.assertEquals(expectedMessage1, actualMessage1);
        Assertions.assertEquals(expectedMessage2, actualMessage2);
        Assertions.assertEquals(expectedMessage3, actualMessage3);

        Mockito.verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("Verify addNewCar() method works for valid dto")
    public void addNewCar_ValidAddNewCarRequestDto_ReturnNewCarDto() {
        //given
        long carId = 1L;
        Car car = createCar(carId);
        AddNewCarRequestDto addNewCarRequestDto = new AddNewCarRequestDto()
                .setBrand(car.getBrand())
                .setModel(car.getModel())
                .setType(String.valueOf(car.getType()))
                .setInventory(car.getInventory())
                .setDailyFee(car.getDailyFee());
        CarDto expectedCarDto = createCarDto(car);

        Mockito.when(carMapper.addNewCarRequestDtoToEntity(addNewCarRequestDto)).thenReturn(car);
        Mockito.when(carRepository.save(car)).thenReturn(car);
        Mockito.when(carMapper.entityToCarDto(car)).thenReturn(expectedCarDto);

        //when
        CarDto actualCarDto = carServiceImpl.addNewCar(addNewCarRequestDto);

        //then
        Assertions.assertEquals(expectedCarDto, actualCarDto);

        Mockito.verify(carMapper, times(1)).addNewCarRequestDtoToEntity(addNewCarRequestDto);
        Mockito.verify(carRepository, times(1)).save(car);
        Mockito.verify(carMapper, times(1)).entityToCarDto(car);
        Mockito.verifyNoMoreInteractions(carMapper);
        Mockito.verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify updateCarInfoById() method works for valid car id and dto")
    public void updateCarInfoById_ValidCarIdAndDto_ReturnsCarDto() {
        //given
        long carId = 1L;
        Car car = createCar(carId);
        UpdateCarInfoRequestDto updateCarInfoRequestDto = new UpdateCarInfoRequestDto()
                .setBrand("BMW")
                .setModel("3")
                .setInventory(1)
                .setDailyFee(new BigDecimal("100.00"));
        Car updatedCar = new Car()
                .setBrand(updateCarInfoRequestDto.getBrand())
                .setModel(updateCarInfoRequestDto.getModel())
                .setInventory(updateCarInfoRequestDto.getInventory())
                .setDailyFee(updateCarInfoRequestDto.getDailyFee());
        CarDto expectedCarDto = new CarDto()
                .setId(updatedCar.getId())
                .setBrand(updatedCar.getBrand())
                .setModel(updatedCar.getModel())
                .setType(updatedCar.getType())
                .setInventory(updatedCar.getInventory())
                .setDailyFee(updatedCar.getDailyFee());

        Mockito.when(carRepository.findById(carId))
                .thenReturn(Optional.of(car));
        Mockito.when(carMapper.updateCarInfo(car, updateCarInfoRequestDto))
                .thenReturn(updatedCar);
        Mockito.when(carRepository.save(updatedCar))
                .thenReturn(updatedCar);
        Mockito.when(carMapper.entityToCarDto(updatedCar))
                .thenReturn(expectedCarDto);

        //when
        CarDto actualCarDto = carServiceImpl.updateCarInfoById(carId, updateCarInfoRequestDto);

        //then
        Assertions.assertEquals(expectedCarDto, actualCarDto);

        Mockito.verify(carRepository, times(1))
                .findById(carId);
        Mockito.verify(carMapper, times(1))
                .updateCarInfo(car, updateCarInfoRequestDto);
        Mockito.verify(carRepository, times(1))
                .save(updatedCar);
        Mockito.verify(carMapper, times(1))
                .entityToCarDto(updatedCar);
        Mockito.verifyNoMoreInteractions(carMapper);
        Mockito.verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Verify deleteCarById() method works")
    public void deleteCarById_RandomCarId_WorksOnlyOnce() {
        //given
        long carId = 1L;

        //when
        carServiceImpl.deleteCarById(carId);

        //then
        Mockito.verify(carRepository, times(1)).deleteById(carId);
        Mockito.verifyNoMoreInteractions(carRepository);
        Mockito.verifyNoInteractions(carMapper);
    }

    private CarDto createCarDto(Car car) {
        return new CarDto()
                .setId(car.getId())
                .setBrand(car.getBrand())
                .setModel(car.getModel())
                .setType(car.getType())
                .setInventory(car.getInventory())
                .setDailyFee(car.getDailyFee());
    }

    private Car createCar(long carId) {
        return new Car()
                .setId(carId)
                .setBrand("Audi")
                .setModel("A4")
                .setType(SEDAN)
                .setInventory(10)
                .setDailyFee(new BigDecimal("199.99"))
                .setDeleted(false);
    }
}
