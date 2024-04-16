package project.carsharingservice.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
import project.carsharingservice.dto.user.GetUserInfoResponseDto;
import project.carsharingservice.dto.user.UpdateRoleRequestDto;
import project.carsharingservice.dto.user.UpdateUserInfoRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:database/users/delete-users-from-the-users-table.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class UserControllerTest {
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

    @WithMockUser(username = "user@example.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Get user info")
    @Sql(scripts = "classpath:database/users/add-users-to-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-users-from-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getUserInfo_ValidRequest_Success() throws Exception {
        //given
        GetUserInfoResponseDto expectedResponseDto = new GetUserInfoResponseDto()
                .setId(1L)
                .setEmail("user@example.com")
                .setFirstName("John")
                .setLastName("Doe");

        //when
        MvcResult mvcResult = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andReturn();

        //then
        GetUserInfoResponseDto actualResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                GetUserInfoResponseDto.class);

        EqualsBuilder.reflectionEquals(expectedResponseDto, actualResponseDto,
                "id", "roles");
    }

    @WithMockUser(username = "user@example.com", roles = "CUSTOMER")
    @Test
    @DisplayName("Update user info")
    @Sql(scripts = "classpath:database/users/add-users-to-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-users-from-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateUserInfo_ValidRequest_Success() throws Exception {
        //given
        UpdateUserInfoRequestDto updateUserInfoRequestDto = new UpdateUserInfoRequestDto()
                .setFirstName("NewFirstName")
                .setLastName("NewLastName");

        GetUserInfoResponseDto expectedResponseDto = new GetUserInfoResponseDto()
                .setId(1L)
                .setEmail("user@example.com")
                .setFirstName(updateUserInfoRequestDto.getFirstName())
                .setLastName(updateUserInfoRequestDto.getLastName());

        String jsonRequest = objectMapper.writeValueAsString(updateUserInfoRequestDto);

        //when
        MvcResult mvcResult = mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //then
        GetUserInfoResponseDto actualResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                GetUserInfoResponseDto.class);

        EqualsBuilder.reflectionEquals(expectedResponseDto, actualResponseDto,
                "id", "roles");
    }

    @WithMockUser(username = "admin@example.com", roles = "MANAGER")
    @Test
    @DisplayName("Update user role")
    @Sql(scripts = "classpath:database/users/add-users-to-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-users-from-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateRole_ValidRequest_Success() throws Exception {
        //given
        long userId = 2L;
        UpdateRoleRequestDto updateRoleRequestDto = new UpdateRoleRequestDto()
                .setRoleId(1L);

        String jsonRequest = objectMapper.writeValueAsString(updateRoleRequestDto);

        //when
        MvcResult mvcResult = mockMvc.perform(patch("/users/{userId}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "admin@example.com", roles = "MANAGER")
    @Test
    @DisplayName("Delete user")
    @Sql(scripts = "classpath:database/users/add-users-to-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-users-from-the-users-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteUser_ValidRequest_Success() throws Exception {
        //given
        long userId = 2L;

        //when
        MvcResult mvcResult = mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent())
                .andReturn();
    }
}
