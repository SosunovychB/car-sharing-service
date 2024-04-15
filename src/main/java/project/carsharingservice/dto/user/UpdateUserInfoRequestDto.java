package project.carsharingservice.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateUserInfoRequestDto {
    private String firstName;
    private String lastName;
}
