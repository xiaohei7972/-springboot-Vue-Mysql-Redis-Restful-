package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_department")
public class DepartmentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdAt;
}
