package project.carsharingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.carsharingservice.dto.registration.UserRegistrationRequestDto;
import project.carsharingservice.dto.registration.UserRegistrationResponseDto;
import project.carsharingservice.exception.RegistrationException;
import project.carsharingservice.mapper.UserMapper;
import project.carsharingservice.model.Role;
import project.carsharingservice.model.User;
import project.carsharingservice.repository.RoleRepository;
import project.carsharingservice.repository.UserRepository;
import project.carsharingservice.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException("User with email " + requestDto.getEmail()
                    + " already exists");
        }

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
