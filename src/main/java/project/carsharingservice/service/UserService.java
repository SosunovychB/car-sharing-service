package project.carsharingservice.service;

import project.carsharingservice.dto.auth.registration.UserRegistrationRequestDto;
import project.carsharingservice.dto.auth.registration.UserRegistrationResponseDto;
import project.carsharingservice.dto.user.GetUserInfoResponseDto;
import project.carsharingservice.dto.user.UpdateRoleRequestDto;
import project.carsharingservice.dto.user.UpdateUserInfoRequestDto;
import project.carsharingservice.exception.RegistrationException;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    GetUserInfoResponseDto getUserInfo(String email);

    GetUserInfoResponseDto updateUserInfo(String email, UpdateUserInfoRequestDto requestDto);

    void updateRole(Long userId, UpdateRoleRequestDto requestDto);
}
