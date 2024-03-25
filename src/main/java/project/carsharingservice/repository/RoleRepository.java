package project.carsharingservice.repository;

import org.springframework.data.jpa.repository.*;
import project.carsharingservice.model.*;

import java.util.*;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(Role.RoleName roleName);
}
