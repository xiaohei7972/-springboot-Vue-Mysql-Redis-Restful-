package com.student.auth;

import com.student.common.BusinessException;
import com.student.security.JwtService;
import com.student.system.entity.UserEntity;
import com.student.system.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userMapper, passwordEncoder, jwtService, redis);
    }

    @Test
    void loginStoresRedisSessionAndReturnsUser() {
        when(redis.opsForValue()).thenReturn(valueOperations);
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "admin");
        user.put("realName", "系统管理员");
        user.put("password", "encoded");
        user.put("role", "ADMIN");
        user.put("status", "1");
        when(userMapper.findLoginUser("admin")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(jwtService.createToken(1L, "admin", "ADMIN")).thenReturn("jwt-token");
        when(jwtService.expirationMs()).thenReturn(7_200_000L);

        Map<String, Object> result = authService.login(
                new AuthService.LoginRequest("  admin ", "123456"));

        assertEquals("jwt-token", result.get("token"));
        assertEquals("ADMIN", ((Map<?, ?>) result.get("user")).get("role"));
        verify(valueOperations).set(
                eq("session:1"), eq("jwt-token"), eq(7_200_000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void disabledAccountCannotLogin() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("password", "encoded");
        user.put("status", "0");
        user.put("role", "ADMIN");
        when(userMapper.findLoginUser("admin")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new AuthService.LoginRequest("admin", "123456")));

        assertEquals(403, exception.getCode());
    }

    @Test
    void profileUpdateRejectsBlankName() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.updateProfile(
                        Map.of("realName", "  "),
                        new org.springframework.security.authentication.TestingAuthenticationToken(
                                "1", null, "ROLE_ADMIN")));

        assertEquals(400, exception.getCode());
    }

    @Test
    void profileUpdateOnlyChangesRealName() {
        UserEntity existing = new UserEntity();
        existing.setId(1L);
        existing.setUsername("admin");
        existing.setPassword("encoded");
        existing.setRole("ADMIN");
        existing.setStatus("1");
        when(userMapper.selectById(1L)).thenReturn(existing);
        var authentication = new org.springframework.security.authentication.TestingAuthenticationToken(
                "1", null, "ROLE_ADMIN");

        authService.updateProfile(Map.of("realName", "新名称"), authentication);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("新名称", captor.getValue().getRealName());
    }
}
