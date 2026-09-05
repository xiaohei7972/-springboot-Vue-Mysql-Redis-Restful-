package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class UserEntity {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String status;
}
