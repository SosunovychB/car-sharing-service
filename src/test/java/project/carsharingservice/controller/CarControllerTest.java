package project.carsharingservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import project.carsharingservice.dto.car.AddNewCarRequestDto;
import project.carsharingservice.dto.car.CarDto;
import project.carsharingservice.dto.car.UpdateCarInfoRequestDto;
import project.carsharingservice.model.Car;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:database/cars/delete-cars-from-the-cars-table.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class CarControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext webApplicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Get all cars in pages")
    @Sql(scripts = "classpath:database/cars/add-cars-to-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-cars-from-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getAllCars_ValidRequest_Success() throws Exception {
        //given
        CarDto carDto1 = new CarDto()
                .setModel("Toyota")
                .setBrand("Camry")
                .setType(Car.Type.valueOf("SEDAN"))
                .setInventory(10)
                .setDailyFee(new BigDecimal("50.00"));
        CarDto carDto2 = new CarDto()
                .setModel("Audi")
                .setBrand("A6")
                .setType(Car.Type.valueOf("SEDAN"))
                .setInventory(3)
                .setDailyFee(new BigDecimal("150.00"));
        List<CarDto> expectedCarDtoList = List.of(carDto1, carDto2);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<CarDto> actualCarDtoList = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<CarDto>>() {});

        Assertions.assertEquals(expectedCarDtoList.size(), actualCarDtoList.size());
        EqualsBuilder.reflectionEquals(expectedCarDtoList.get(0), actualCarDtoList.get(0));
        EqualsBuilder.reflectionEquals(expectedCarDtoList.get(1), actualCarDtoList.get(1));
    }

    @Test
    @DisplayName("Get a car by id")
    @Sql(scripts = "classpath:database/cars/add-cars-to-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-cars-from-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getCarById_ValidRequest_Success() throws Exception {
        //given
        long carId = 2L;

        CarDto expectedCarDto = new CarDto()
                .setId(carId)
                .setModel("Audi")
                .setBrand("A6")
                .setType(Car.Type.valueOf("SEDAN"))
                .setInventory(3)
                .setDailyFee(new BigDecimal("150.00"));

        //when
        MvcResult mvcResult = mockMvc.perform(get("/cars/{carId}", carId))
                .andExpect(status().isOk())
                .andReturn();

        //then
        CarDto actualCarDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CarDto.class);

        Assertions.assertEquals(expectedCarDto.getId(), actualCarDto.getId());
        EqualsBuilder.reflectionEquals(expectedCarDto, actualCarDto);
    }

    @WithMockUser(username = "admin@example.com", roles = "MANAGER")
    @Test
    @DisplayName("Create a new car")
    @Sql(scripts = "classpath:database/cars/add-cars-to-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-cars-from-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addNewCar_ValidRequest_Success() throws Exception {
        //given
        AddNewCarRequestDto addNewCarRequestDto = new AddNewCarRequestDto()
                .setModel("NewModel")
                .setBrand("NewBrand")
                .setType("SEDAN")
                .setInventory(10)
                .setDailyFee(new BigDecimal("10.00"));

        CarDto expectedCarDto = new CarDto()
                .setModel(addNewCarRequestDto.getModel())
                .setBrand(addNewCarRequestDto.getBrand())
                .setType(Car.Type.valueOf(addNewCarRequestDto.getType()))
                .setInventory(addNewCarRequestDto.getInventory())
                .setDailyFee(addNewCarRequestDto.getDailyFee());

        String jsonContent = objectMapper.writeValueAsString(addNewCarRequestDto);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/cars")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //then
        CarDto actualCarDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CarDto.class);

        EqualsBuilder.reflectionEquals(expectedCarDto, actualCarDto,
                "id");
    }

    @WithMockUser(username = "admin@example.com", roles = "MANAGER")
    @Test
    @DisplayName("Update car info by id")
    @Sql(scripts = "classpath:database/cars/add-cars-to-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-cars-from-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateCarInfoById_ValidRequest_Success() throws Exception {
        //given
        long carId = 1L;
        UpdateCarInfoRequestDto updateCarInfoRequestDto = new UpdateCarInfoRequestDto()
                .setModel("NewModel")
                .setBrand("NewBrand")
                .setType("SEDAN")
                .setInventory(10)
                .setDailyFee(new BigDecimal("10.00"));

        CarDto expectedCarDto = new CarDto()
                .setModel(updateCarInfoRequestDto.getModel())
                .setBrand(updateCarInfoRequestDto.getBrand())
                .setType(Car.Type.valueOf(updateCarInfoRequestDto.getType()))
                .setInventory(updateCarInfoRequestDto.getInventory())
                .setDailyFee(updateCarInfoRequestDto.getDailyFee());

        String jsonContent = objectMapper.writeValueAsString(updateCarInfoRequestDto);

        //when
        MvcResult mvcResult = mockMvc.perform(put("/cars/{carId}", carId)
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //then
        CarDto actualCarDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CarDto.class);

        EqualsBuilder.reflectionEquals(expectedCarDto, actualCarDto,
                "id");
    }

    @WithMockUser(username = "admin@example.com", roles = "MANAGER")
    @Test
    @DisplayName("Delete a car by id")
    @Sql(scripts = "classpath:database/cars/add-cars-to-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-cars-from-the-cars-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteUser_ValidRequest_Success() throws Exception {
        //given
        long carId = 1L;

        //when
        MvcResult mvcResult = mockMvc.perform(delete("/cars/{carId}", carId))
                .andExpect(status().isNoContent())
                .andReturn();
    }
}
