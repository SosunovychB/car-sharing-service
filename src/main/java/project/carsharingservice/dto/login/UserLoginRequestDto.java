package project.carsharingservice.dto.login;

import jakarta.validation.constraints.*;
import lombok.*;
import project.carsharingservice.validation.email.Email;
import project.carsharingservice.validation.password.Password;

@Data
public class UserLoginRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Password
    private String password;
}
