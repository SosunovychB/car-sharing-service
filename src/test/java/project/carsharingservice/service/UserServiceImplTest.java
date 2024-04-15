package project.carsharingservice.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import project.carsharingservice.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final String PASSWORD = "Password1234$";
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    @DisplayName("Verify register() method works")
    public void register_ValidUserRegistrationRequestDto_ReturnsUserRegistrationResponseDto()
            throws RegistrationException {
        //given
        String email = "email@example.com";
        User user = createUser(email);
        Role role = new Role().setRoleName(Role.RoleName.ROLE_CUSTOMER);
        user.getRoles().add(role);

        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto()
                .setEmail(user.getEmail())
                .setPassword(PASSWORD)
                .setRepeatPassword(PASSWORD)
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());
        UserRegistrationResponseDto expectedRegisterResponseDto = new UserRegistrationResponseDto()
                .setId(user.getId())
                .setEmail(user.getEmail());

        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        Mockito.when(roleRepository.findByRoleName(Role.RoleName.ROLE_CUSTOMER))
                .thenReturn(role);
        Mockito.when(passwordEncoder.encode(PASSWORD))
                .thenReturn(PASSWORD_ENCODER.encode(PASSWORD));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(user);
        Mockito.when(userMapper.entityToLoginResponseDto(user))
                .thenReturn(expectedRegisterResponseDto);

        //when
        UserRegistrationResponseDto actualRegisterResponseDto = userServiceImpl
                .register(requestDto);

        //then
        Assertions.assertEquals(expectedRegisterResponseDto, actualRegisterResponseDto);

        Mockito.verify(userRepository, times(1))
                .findByEmail(email);
        Mockito.verify(roleRepository, times(1))
                .findByRoleName(Role.RoleName.ROLE_CUSTOMER);
        Mockito.verify(passwordEncoder, times(1))
                .encode(PASSWORD);
        Mockito.verify(userRepository, times(1))
                .save(any(User.class));
        Mockito.verify(userMapper, times(1))
                .entityToLoginResponseDto(user);

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(passwordEncoder);
        Mockito.verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify getUserInfo() method works")
    public void getUserInfo_ValidEmail_ReturnsGetUserInfoResponseDto() {
        //given
        String email = "email@example.com";
        User user = createUser(email);
        GetUserInfoResponseDto expectedUserInfo = new GetUserInfoResponseDto()
                .setId(user.getId())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.entityToUserInfoResponseDto(user)).thenReturn(expectedUserInfo);

        //when
        GetUserInfoResponseDto actualUserInfo = userServiceImpl.getUserInfo(email);

        //then
        Assertions.assertEquals(expectedUserInfo, actualUserInfo);

        Mockito.verify(userRepository, times(1)).findByEmail(email);
        Mockito.verify(userMapper, times(1)).entityToUserInfoResponseDto(user);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify getUserInfo() method throws exception for invalid email")
    public void getUserInfo_InvalidEmail_ThrowsException() {
        //given
        String email = "notValidEmail@email.com";

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> userServiceImpl.getUserInfo(email));

        //then
        String expectedEmail = "User with email " + email + " was not found";
        String actualEmail = exception.getMessage();
        Assertions.assertEquals(expectedEmail, actualEmail);

        Mockito.verify(userRepository, times(1)).findByEmail(email);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify updateUserInfo() method works")
    public void updateUserInfo_ValidEmailAndRequest_ReturnsNewUserInfoDto() {
        //given
        String email = "email@example.com";
        User user = createUser(email);

        UpdateUserInfoRequestDto requestDto = new UpdateUserInfoRequestDto()
                .setFirstName("NewFirstName")
                .setLastName("NewLastName");

        User updatedUser = user
                .setFirstName(requestDto.getFirstName())
                .setLastName(requestDto.getLastName());

        GetUserInfoResponseDto expectedResponseDto = new GetUserInfoResponseDto()
                .setEmail(updatedUser.getEmail())
                .setId(updatedUser.getId())
                .setFirstName(updatedUser.getFirstName())
                .setLastName(updatedUser.getLastName())
                .setRoles(updatedUser.getRoles());

        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        Mockito.when(userMapper.updateUserInfo(user, requestDto))
                .thenReturn(updatedUser);
        Mockito.when(userRepository.save(updatedUser))
                .thenReturn(updatedUser);
        Mockito.when(userMapper.entityToUserInfoResponseDto(updatedUser))
                .thenReturn(expectedResponseDto);

        //when
        GetUserInfoResponseDto actualResponseDto = userServiceImpl
                .updateUserInfo(email, requestDto);

        //then
        Assertions.assertEquals(expectedResponseDto, actualResponseDto);

        Mockito.verify(userRepository, times(1))
                .findByEmail(email);
        Mockito.verify(userMapper, times(1))
                .updateUserInfo(user, requestDto);
        Mockito.verify(userRepository, times(1))
                .save(updatedUser);
        Mockito.verify(userMapper, times(1))
                .entityToUserInfoResponseDto(updatedUser);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify updateRole() method works")
    public void updateRole_ValidUserIdAndDto_Works() {
        //given
        String email = "email@example.com";
        User user = createUser(email);
        long userId = user.getId();

        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto()
                .setRoleId(2L);
        Role role = new Role().setRoleName(Role.RoleName.ROLE_CUSTOMER);

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findById(requestDto.getRoleId()))
                .thenReturn(Optional.ofNullable(role));

        user.getRoles().clear();
        user.getRoles().add(role);
        Mockito.when(userRepository.save(user))
                .thenReturn(user);

        //when
        userServiceImpl.updateRole(userId, requestDto);

        //then
        Mockito.verify(userRepository, times(1))
                .findById(userId);
        Mockito.verify(roleRepository, times(1))
                .findById(requestDto.getRoleId());
        Mockito.verify(userRepository, times(1))
                .save(any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify updateRole() method throws exception for id = 1")
    public void updateRole_InvalidUserId_ThrowsException() {
        //given
        long userId = 1L;
        UpdateRoleRequestDto requestDto = new UpdateRoleRequestDto()
                .setRoleId(1L);

        //when
        Exception exception = assertThrows(RuntimeException.class,
                () -> userServiceImpl.updateRole(userId, requestDto)
        );

        //then
        String expectedMessage = "Manager with id 1 can not do any updates with yourself";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);

        Mockito.verifyNoInteractions(userRepository);
        Mockito.verifyNoInteractions(roleRepository);
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify deleteUserById() method works")
    public void deleteUserById_RandomUserId_WorksOnlyOnce() {
        //given
        long userId = 2L;

        //when
        userServiceImpl.deleteUserById(userId);

        //then
        Mockito.verify(userRepository, times(1)).deleteById(userId);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoInteractions(userMapper);
    }

    private User createUser(String email) {
        return new User()
                .setId(2L)
                .setEmail(email)
                .setPassword(PASSWORD_ENCODER.encode(PASSWORD))
                .setFirstName("FirstName")
                .setLastName("LastName")
                .setDeleted(false);
    }
}
