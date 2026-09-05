package com.student.auth;

import com.student.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody AuthService.LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        authService.logout(header);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<?> me(Authentication authentication) {
        return ApiResponse.ok(authService.me(authentication));
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody Map<String, Object> body,
                                           Authentication authentication) {
        authService.updateProfile(body, authentication);
        return ApiResponse.ok();
    }
}
