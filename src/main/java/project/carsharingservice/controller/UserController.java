package project.carsharingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
    public GetUserInfoResponseDto updateUserInfo(
            Authentication authentication,
            @RequestBody UpdateUserInfoRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateUserInfo(user.getEmail(), requestDto);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public void updateRole(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateRoleRequestDto requestDto) {
        userService.updateRole(userId, requestDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
