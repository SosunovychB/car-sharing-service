package project.carsharingservice.dto.user;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class UpdateRoleRequestDto {
    @NotNull
    @Positive
    private Long roleId;
}
