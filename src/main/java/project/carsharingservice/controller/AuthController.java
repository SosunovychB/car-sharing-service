package project.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.carsharingservice.dto.auth.login.UserLoginRequestDto;
import project.carsharingservice.dto.auth.login.UserLoginResponseDto;
import project.carsharingservice.dto.auth.registration.UserRegistrationRequestDto;
import project.carsharingservice.dto.auth.registration.UserRegistrationResponseDto;
import project.carsharingservice.exception.RegistrationException;
import project.carsharingservice.security.AuthenticationService;
import project.carsharingservice.service.UserService;

@Tag(name = "Auth management", description = "Endpoints for registration and login")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Endpoint for login",
            description = "Endpoint for login")
    public UserLoginResponseDto login(
            @RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }

    @PostMapping("/registration")
    @Operation(summary = "Endpoint for registration",
            description = "Endpoint for registration")
    public UserRegistrationResponseDto registration(
            @RequestBody @Valid UserRegistrationRequestDto requestDto
    ) throws RegistrationException {
        return userService.register(requestDto);
    }
}
