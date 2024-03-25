package project.carsharingservice.service;

import project.carsharingservice.dto.registration.*;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);
}
