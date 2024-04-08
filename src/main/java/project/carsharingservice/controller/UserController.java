package project.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@Tag(name = "User management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get user info about yourself",
            description = "Get user info about yourself")
    public GetUserInfoResponseDto getUserInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getUserInfo(user.getEmail());
    }

    @PutMapping("/me")
    @Operation(summary = "Update user info about yourself",
            description = "Update user info about yourself")
    public GetUserInfoResponseDto updateUserInfo(
            Authentication authentication,
            @RequestBody UpdateUserInfoRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return userService.updateUserInfo(user.getEmail(), requestDto);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update an user role",
            description = "Update an user role")
    @ResponseStatus(HttpStatus.OK)
    public void updateRole(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateRoleRequestDto requestDto) {
        userService.updateRole(userId, requestDto);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete an user by id",
            description = "Delete an user by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
