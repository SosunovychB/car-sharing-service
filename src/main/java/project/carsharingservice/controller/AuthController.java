package project.carsharingservice.controller;

import jakarta.validation.*;
import lombok.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;
import project.carsharingservice.dto.login.*;
import project.carsharingservice.dto.registration.*;
import project.carsharingservice.security.*;
import project.carsharingservice.service.*;
import project.carsharingservice.validation.password.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public UserLoginResponseDto login(
            @RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }

    @PostMapping("/registration")
    public UserRegistrationResponseDto registration(
            @RequestBody @Valid UserRegistrationRequestDto requestDto) {
        return userService.register(requestDto);
    }
}
