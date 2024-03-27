package project.carsharingservice.dto.user;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import project.carsharingservice.model.Role;

@Data
public class GetUserInfoResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role> roles = new HashSet<>();
}
