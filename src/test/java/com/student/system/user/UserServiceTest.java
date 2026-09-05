package com.student.system.user;

import com.student.common.BusinessException;
import com.student.system.entity.UserEntity;
import com.student.system.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private final TestingAuthenticationToken admin =
            new TestingAuthenticationToken("1", null, "ROLE_ADMIN");

    @Test
    void createUserUsesDefaultPasswordAndNormalizedStatus() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-default");
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, UserEntity.class).setId(99L);
            return 1;
        });

        UserService service = new UserService(userMapper, passwordEncoder);
        var result = service.createUser(
                new UserService.UserRequest("new-user", "新用户", "STUDENT", null, null),
                admin);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("new-user", captor.getValue().getUsername());
        assertEquals("encoded-default", captor.getValue().getPassword());
        assertEquals("STUDENT", captor.getValue().getRole());
        assertEquals("1", captor.getValue().getStatus());
        assertEquals("new-user", result.get("username"));
    }

    @Test
    void linkedAccountCannotChangeRole() {
        UserEntity existing = user(2L, "teacher01", "TEACHER", "1");
        when(userMapper.selectById(2L)).thenReturn(existing);
        when(userMapper.countTeacherReference(2L)).thenReturn(1L);

        UserService service = new UserService(userMapper, passwordEncoder);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateUser(
                        2L,
                        new UserService.UserRequest("teacher01", "教师", "STUDENT", null, "1"),
                        admin));

        assertEquals(400, exception.getCode());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void currentAccountCannotBeDeleted() {
        UserService service = new UserService(userMapper, passwordEncoder);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.deleteUser(1L, admin));

        assertEquals(400, exception.getCode());
        verify(userMapper, never()).deleteById(anyLong());
    }

    private UserEntity user(Long id, String username, String role, String status) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);
        entity.setRealName("测试用户");
        entity.setRole(role);
        entity.setStatus(status);
        entity.setPassword("encoded");
        return entity;
    }
}
