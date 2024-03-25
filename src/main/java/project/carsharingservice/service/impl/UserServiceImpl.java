package project.carsharingservice.service.impl;

import lombok.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import project.carsharingservice.dto.registration.*;
import project.carsharingservice.mapper.*;
import project.carsharingservice.model.*;
import project.carsharingservice.repository.*;
import project.carsharingservice.service.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto) {
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.getRoles().add(roleRepository.findByRoleName(Role.RoleName.ROLE_CUSTOMER));

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }
}
