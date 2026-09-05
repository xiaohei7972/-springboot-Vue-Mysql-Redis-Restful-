package com.student.auth;

import com.student.common.BusinessException;
import com.student.security.JwtService;
import com.student.system.entity.UserEntity;
import com.student.system.mapper.UserMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final StringRedisTemplate redis;

    public AuthService(UserMapper userMapper, PasswordEncoder encoder, JwtService jwt, StringRedisTemplate redis) {
        this.userMapper = userMapper;
        this.encoder = encoder;
        this.jwt = jwt;
        this.redis = redis;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> user = userMapper.findLoginUser(request.username());
        if (user == null || !encoder.matches(request.password(), String.valueOf(user.get("password")))) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"1".equals(String.valueOf(user.get("status")))) {
            throw new BusinessException(403, "账号已被禁用");
        }

        long userId = ((Number) user.get("id")).longValue();
        String role = String.valueOf(user.get("role"));
        String token = jwt.createToken(userId, request.username(), role);
        redis.opsForValue().set("session:" + userId, token, jwt.expirationMs(), TimeUnit.MILLISECONDS);
        return Map.of("token", token, "user", Map.of(
                "id", userId,
                "username", user.get("username"),
                "realName", user.get("realName"),
                "role", role));
    }

    public void logout(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        try {
            String token = header.substring(7);
            long ttl = Math.max(1, jwt.parse(token).getExpiration().getTime() - System.currentTimeMillis());
            redis.opsForValue().set("jwt:blacklist:" + token, "1", ttl, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            // Expired tokens are already invalid.
        }
    }

    public Map<String, Object> me(Authentication authentication) {
        return userMapper.findProfile(userId(authentication));
    }

    @Transactional
    public void updateProfile(Map<String, Object> body, Authentication authentication) {
        UserEntity user = new UserEntity();
        user.setId(userId(authentication));
        user.setRealName(String.valueOf(body.getOrDefault("realName", "")));
        userMapper.updateById(user);
    }

    private long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
