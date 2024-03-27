package project.carsharingservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserInfoRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
