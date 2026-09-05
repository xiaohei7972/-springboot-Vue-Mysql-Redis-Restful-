package com.student.auth;

import com.student.common.ApiResponse;
import com.student.common.BusinessException;
import com.student.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder; private final JwtService jwt; private final StringRedisTemplate redis;
    public AuthController(JdbcTemplate jdbc, PasswordEncoder encoder, JwtService jwt, StringRedisTemplate redis) {
        this.jdbc = jdbc; this.encoder = encoder; this.jwt = jwt; this.redis = redis;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        var users = jdbc.queryForList("SELECT id, username, password, real_name, role, status FROM sys_user WHERE username = ?", request.username());
        if (users.isEmpty() || !encoder.matches(request.password(), String.valueOf(users.get(0).get("password"))))
            throw new BusinessException(401, "用户名或密码错误");
        var user = users.get(0);
        if (!"1".equals(String.valueOf(user.get("status")))) throw new BusinessException(403, "账号已被禁用");
        String role = String.valueOf(user.get("role"));
        String token = jwt.createToken(((Number) user.get("id")).longValue(), request.username(), role);
        redis.opsForValue().set("session:" + user.get("id"), token, jwt.expirationMs(), TimeUnit.MILLISECONDS);
        return ApiResponse.ok(Map.of("token", token, "user", Map.of("id", user.get("id"), "username", user.get("username"),
                "realName", user.get("real_name"), "role", role)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try { long ttl = Math.max(1, jwt.parse(token).getExpiration().getTime() - System.currentTimeMillis()); redis.opsForValue().set("jwt:blacklist:" + token, "1", ttl, TimeUnit.MILLISECONDS); } catch (Exception ignored) { }
        }
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<?> me(Authentication authentication) {
        return ApiResponse.ok(jdbc.queryForMap("SELECT id, username, real_name AS realName, role FROM sys_user WHERE id = ?", Long.valueOf(authentication.getName())));
    }
}
