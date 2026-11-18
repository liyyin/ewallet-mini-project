package com.ewallet.user_service.controller;
import com.ewallet.user_service.dto.ApiResponse;
import com.ewallet.user_service.dto.CreateUserRequest;
import com.ewallet.user_service.dto.UserResponse;
import com.ewallet.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUser(id));
    }
}