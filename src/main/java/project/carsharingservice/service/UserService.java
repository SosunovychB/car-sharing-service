package project.carsharingservice.service;

import project.carsharingservice.dto.registration.UserRegistrationRequestDto;
import project.carsharingservice.dto.registration.UserRegistrationResponseDto;
import project.carsharingservice.exception.RegistrationException;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException;
}
