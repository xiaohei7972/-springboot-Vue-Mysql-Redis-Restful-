package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    @Select("""
            SELECT id, username, password, real_name AS realName, role, status
            FROM sys_user WHERE username = #{username}
            """)
    Map<String, Object> findLoginUser(String username);

    @Select("""
            SELECT id, username, real_name AS realName, role
            FROM sys_user WHERE id = #{id}
            """)
    Map<String, Object> findProfile(Long id);

    @Select("""
            SELECT id, role, status
            FROM sys_user WHERE id = #{id}
            """)
    Map<String, Object> findAuthenticationUser(Long id);

    @Select("SELECT COUNT(*) FROM teacher WHERE user_id = #{userId}")
    long countTeacherReference(Long userId);

    @Select("SELECT COUNT(*) FROM student WHERE user_id = #{userId}")
    long countStudentReference(Long userId);
}
