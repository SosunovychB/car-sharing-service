package project.carsharingservice.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateRoleRequestDto {
    @NotNull
    @Positive
    private Long roleId;
}
