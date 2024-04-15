package project.carsharingservice.dto.auth.registration;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserRegistrationResponseDto {
    private Long id;
    private String email;
}
