package project.carsharingservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import project.carsharingservice.dto.car.RentedCarDto;
import project.carsharingservice.dto.rental.CreateRentalRequestDto;
import project.carsharingservice.dto.rental.RentalDto;
import project.carsharingservice.model.Car;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:database/rentals/delete-rentals-from-the-rentals-table.sql",
        "classpath:database/cars/delete-cars-from-the-cars-table.sql",
        "classpath:database/users/delete-users-from-the-users-table.sql",
        "classpath:database/users/add-users-to-the-users-table.sql",
        "classpath:database/cars/add-cars-to-the-cars-table.sql",
        "classpath:database/rentals/add-rentals-to-the-rentals-table.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:database/rentals/delete-rentals-from-the-rentals-table.sql",
        "classpath:database/cars/delete-cars-from-the-cars-table.sql",
        "classpath:database/users/delete-users-from-the-users-table.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class RentalControllerTests {
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

    @WithMockUser(username = "admin@example.com", roles = {"CUSTOMER", "MANAGER"})
    @Test
    @DisplayName("Get rentals by user id")
    public void getRentalsByUserId_ValidRequest_Success() throws Exception {
        //given
        int userId = 1;

        RentalDto rentalDto2 = new RentalDto()
                .setId(1L)
                .setRentalDate(LocalDate.of(2023, 1, 1))
                .setReturnDate(LocalDate.of(2024, 1, 1))
                .setActualReturnDate(LocalDate.of(2024, 1, 1))
                .setUserId(userId);
        RentalDto rentalDto1 = new RentalDto()
                .setId(3L)
                .setRentalDate(LocalDate.of(2023, 3, 3))
                .setReturnDate(LocalDate.of(2024, 3, 3))
                .setActualReturnDate(null)
                .setUserId(userId);

        List<RentalDto> expectedCarDtoList1 = List.of(rentalDto1);
        List<RentalDto> expectedCarDtoList2 = List.of(rentalDto2);

        //when
        MvcResult mvcResult1 = mockMvc.perform(get("/rentals")
                        .param("userId", "1")
                        .param("isActive", "false")
                )
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcResult2 = mockMvc.perform(get("/rentals")
                        .param("userId", "1")
                        .param("isActive", "true")
                )
                .andExpect(status().isOk())
                .andReturn();

        //then
        List<RentalDto> actualCarDtoList1 = objectMapper.readValue(
                mvcResult1.getResponse().getContentAsString(),
                new TypeReference<List<RentalDto>>(){});

        List<RentalDto> actualCarDtoList2 = objectMapper.readValue(
                mvcResult2.getResponse().getContentAsString(),
                new TypeReference<List<RentalDto>>(){});

        Assertions.assertEquals(expectedCarDtoList1.size(),
                actualCarDtoList1.size());
        EqualsBuilder.reflectionEquals(expectedCarDtoList1.get(0), actualCarDtoList1.get(0),
                "rentedCarDto");
        Assertions.assertEquals(expectedCarDtoList2.size(),
                actualCarDtoList2.size());
        EqualsBuilder.reflectionEquals(expectedCarDtoList2.get(0), actualCarDtoList2.get(0),
                "rentedCarDto");
    }

    @WithMockUser(username = "admin@example.com", roles = {"CUSTOMER", "MANAGER"})
    @Test
    @DisplayName("Get a rental by id")
    public void getRentalById_ValidRequest_Success() throws Exception {
        //given
        int rentalId = 2;

        RentalDto expectedRentalDto = new RentalDto()
                .setId(2L)
                .setRentalDate(LocalDate.of(2023, 2, 2))
                .setReturnDate(LocalDate.of(2024, 2, 2))
                .setActualReturnDate(null)
                .setRentedCarDto(new RentedCarDto().setId(1L))
                .setUserId(1L);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/rentals/{rentalId}", rentalId))
                .andExpect(status().isOk())
                .andReturn();

        //then
        RentalDto actualRentalDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                RentalDto.class);

        Assertions.assertEquals(expectedRentalDto.getRentedCarDto().getId(),
                actualRentalDto.getRentedCarDto().getId());
        EqualsBuilder.reflectionEquals(expectedRentalDto, actualRentalDto,
                "rentedCarDto");
    }

    @WithMockUser(username = "user@example.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Create a new rental")
    public void createRental_ValidRequest_Success() throws Exception {
        //given
        CreateRentalRequestDto createRentalRequestDto = new CreateRentalRequestDto()
                .setCarId(1L)
                .setNumberOfDays(7L);

        RentedCarDto rentedCarDto = new RentedCarDto()
                .setId(createRentalRequestDto.getCarId())
                .setBrand("Toyota")
                .setModel("Camry")
                .setType(Car.Type.valueOf("SEDAN"))
                .setDailyFee(new BigDecimal("50.00"));

        RentalDto expectedRentalDto = new RentalDto()
                .setRentalDate(LocalDate.now())
                .setReturnDate(LocalDate.now().plusDays(createRentalRequestDto.getNumberOfDays()))
                .setActualReturnDate(null)
                .setRentedCarDto(rentedCarDto);

        String jsonRequest = objectMapper.writeValueAsString(createRentalRequestDto);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //then
        RentalDto actualRentalDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                RentalDto.class);

        EqualsBuilder.reflectionEquals(expectedRentalDto, actualRentalDto,
                "id", "userId");
    }

    @WithMockUser(username = "user@example.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Set an actual return date for a rental")
    public void setActualReturnDate_ValidRequest_Success() throws Exception {
        //given
        int rentalId = 2;

        //when
        MvcResult mvcResult = mockMvc.perform(post("/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isOk())
                .andReturn();

        //then
        RentalDto rentalDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                RentalDto.class);

        Assertions.assertNotNull(rentalDto.getActualReturnDate());
    }
}
