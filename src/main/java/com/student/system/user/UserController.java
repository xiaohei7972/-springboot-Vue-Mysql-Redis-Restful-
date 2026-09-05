package com.student.system.user;

import com.student.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/roles")
    public ApiResponse<?> roles(Authentication authentication) {
        return ApiResponse.ok(userService.roles(authentication));
    }

    @GetMapping("/users")
    public ApiResponse<?> users(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword,
                                Authentication authentication) {
        return ApiResponse.ok(userService.users(page, size, keyword, authentication));
    }

    @PostMapping("/users")
    public ApiResponse<?> createUser(@Valid @RequestBody UserService.UserRequest request,
                                     Authentication authentication) {
        return ApiResponse.ok(userService.createUser(request, authentication));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(@PathVariable long id,
                                        @Valid @RequestBody UserService.UserRequest request,
                                        Authentication authentication) {
        userService.updateUser(id, request, authentication);
        return ApiResponse.ok();
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable long id, Authentication authentication) {
        userService.deleteUser(id, authentication);
        return ApiResponse.ok();
    }
}
