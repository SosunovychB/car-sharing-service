package project.carsharingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.carsharingservice.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(Role.RoleName roleName);
}
