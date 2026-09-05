package com.student.system.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.BusinessException;
import com.student.common.PageResult;
import com.student.system.entity.UserEntity;
import com.student.system.mapper.UserMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private static final List<Map<String, String>> ROLES = List.of(
            Map.of("code", "ADMIN", "name", "管理员", "description", "维护系统用户和全部教学数据"),
            Map.of("code", "TEACHER", "name", "教师", "description", "维护本人课程的教学数据"),
            Map.of("code", "STUDENT", "name", "学生", "description", "查看个人教学数据"));

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public record UserRequest(
            @NotBlank String username,
            @NotBlank String realName,
            @NotBlank String role,
            String password,
            String status) {
    }

    public List<Map<String, String>> roles(Authentication authentication) {
        requireAdmin(authentication);
        return ROLES;
    }

    public PageResult<Map<String, Object>> users(int page, int size, String keyword,
                                                  Authentication authentication) {
        requireAdmin(authentication);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        String key = keyword == null ? "" : keyword.trim();

        Page<UserEntity> resultPage = new Page<>(safePage, safeSize);
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        query.select(UserEntity::getId, UserEntity::getUsername, UserEntity::getRealName,
                        UserEntity::getRole, UserEntity::getStatus, UserEntity::getCreatedAt)
                .and(!key.isBlank(), wrapper -> wrapper.like(UserEntity::getUsername, key)
                        .or().like(UserEntity::getRealName, key))
                .orderByDesc(UserEntity::getId);
        userMapper.selectPage(resultPage, query);

        List<Map<String, Object>> records = resultPage.getRecords().stream()
                .map(this::toView)
                .toList();
        return new PageResult<>(records, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Transactional
    public Map<String, Object> createUser(UserRequest request, Authentication authentication) {
        requireAdmin(authentication);
        ensureValidRole(request.role());
        ensureUsernameAvailable(request.username(), null);

        UserEntity user = new UserEntity();
        user.setUsername(request.username().trim());
        user.setRealName(request.realName().trim());
        user.setRole(request.role());
        user.setStatus(normalizeStatus(request.status()));
        user.setPassword(passwordEncoder.encode(
                request.password() == null || request.password().isBlank() ? "123456" : request.password()));
        userMapper.insert(user);
        return toView(user);
    }

    @Transactional
    public void updateUser(long id, UserRequest request, Authentication authentication) {
        requireAdmin(authentication);
        UserEntity existing = requireUser(id);
        ensureValidRole(request.role());
        ensureUsernameAvailable(request.username(), id);
        if (!existing.getRole().equals(request.role())
                && (userMapper.countTeacherReference(id) > 0 || userMapper.countStudentReference(id) > 0)) {
            throw new BusinessException(400, "已关联教师或学生档案的账号不能直接变更角色");
        }
        if (id == userId(authentication)
                && (!existing.getRole().equals(request.role())
                || !existing.getStatus().equals(normalizeStatus(request.status())))) {
            throw new BusinessException(400, "不能修改当前登录账号的角色或状态");
        }

        existing.setUsername(request.username().trim());
        existing.setRealName(request.realName().trim());
        existing.setRole(request.role());
        existing.setStatus(normalizeStatus(request.status()));
        if (request.password() != null && !request.password().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.password()));
        }
        userMapper.updateById(existing);
    }

    @Transactional
    public void deleteUser(long id, Authentication authentication) {
        requireAdmin(authentication);
        if (id == userId(authentication)) {
            throw new BusinessException(400, "不能删除当前登录账号");
        }
        requireUser(id);
        if (userMapper.countTeacherReference(id) > 0 || userMapper.countStudentReference(id) > 0) {
            throw new BusinessException(409, "账号仍关联教师或学生档案，不能直接删除");
        }
        userMapper.deleteById(id);
    }

    private Map<String, Object> toView(UserEntity user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName(),
                "role", user.getRole(),
                "status", user.getStatus(),
                "createdAt", user.getCreatedAt() == null ? "" : user.getCreatedAt());
    }

    private UserEntity requireUser(long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username, Long excludedId) {
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username.trim());
        if (excludedId != null) {
            query.ne(UserEntity::getId, excludedId);
        }
        if (userMapper.selectCount(query) > 0) {
            throw new BusinessException(409, "用户名已存在");
        }
    }

    private void ensureValidRole(String role) {
        if (ROLES.stream().noneMatch(item -> item.get("code").equals(role))) {
            throw new BusinessException(400, "角色必须是 ADMIN、TEACHER 或 STUDENT");
        }
    }

    private String normalizeStatus(String status) {
        return "0".equals(status) ? "0" : "1";
    }

    private long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(item -> "ROLE_ADMIN".equals(item.getAuthority()))) {
            throw new BusinessException(403, "没有权限执行此操作");
        }
    }
}
