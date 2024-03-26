package project.carsharingservice.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
