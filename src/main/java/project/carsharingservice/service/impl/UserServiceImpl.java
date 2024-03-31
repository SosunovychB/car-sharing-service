package project.carsharingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.carsharingservice.dto.auth.registration.UserRegistrationRequestDto;
import project.carsharingservice.dto.auth.registration.UserRegistrationResponseDto;
import project.carsharingservice.dto.user.GetUserInfoResponseDto;
import project.carsharingservice.dto.user.UpdateRoleRequestDto;
import project.carsharingservice.dto.user.UpdateUserInfoRequestDto;
import project.carsharingservice.exception.EntityNotFoundException;
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
        return userMapper.entityToLoginResponseDto(savedUser);
    }

    @Override
    public GetUserInfoResponseDto getUserInfo(String email) {
        User user = findUserByEmail(email);
        return userMapper.entityToUserInfoResponseDto(user);
    }

    @Override
    @Transactional
    public GetUserInfoResponseDto updateUserInfo(String email,
                                                 UpdateUserInfoRequestDto requestDto) {
        User user = findUserByEmail(email);
        User updatedUser = userMapper.updateUserInfo(user, requestDto);
        User savedUpdatedUser = userRepository.save(updatedUser);
        return userMapper.entityToUserInfoResponseDto(savedUpdatedUser);
    }

    @Override
    @Transactional
    public void updateRole(Long userId,
                           UpdateRoleRequestDto requestDto) {
        checkManagerId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id "
                        + userId + " was not found"));
        Role role = roleRepository.findById(requestDto.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role with id "
                        + requestDto.getRoleId() + " was not found"));

        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        checkManagerId(userId);
        userRepository.deleteById(userId);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email "
                        + email + " was not found"));
    }

    private void checkManagerId(Long userId) {
        if (userId == 1) {
            throw new RuntimeException("Manager with id 1 can not do any updates with yourself");
        }
    }
}
