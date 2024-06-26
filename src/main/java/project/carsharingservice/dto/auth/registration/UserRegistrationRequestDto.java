package project.carsharingservice.dto.auth.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import project.carsharingservice.validation.email.Email;
import project.carsharingservice.validation.password.Password;
import project.carsharingservice.validation.password.matcher.PasswordMatcher;

@Data
@PasswordMatcher
@Accessors(chain = true)
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Password
    private String password;
    @NotBlank
    private String repeatPassword;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
