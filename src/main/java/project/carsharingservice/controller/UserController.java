package project.carsharingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import project.carsharingservice.dto.user.GetUserInfoResponseDto;
import project.carsharingservice.dto.user.UpdateRoleRequestDto;
import project.carsharingservice.dto.user.UpdateUserInfoRequestDto;
import project.carsharingservice.model.User;
import project.carsharingservice.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public GetUserInfoResponseDto getUserInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getUserInfo(user.getEmail());
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public GetUserInfoResponseDto updateUserInfo(
            Authentication authentication,
            @RequestBody @Valid UpdateUserInfoRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateUserInfo(user.getEmail(), requestDto);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public void updateRole(
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequestDto requestDto) {
        userService.updateRole(userId, requestDto);
    }
}
