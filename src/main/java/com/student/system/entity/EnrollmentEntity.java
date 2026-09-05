package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("enrollment")
public class EnrollmentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDateTime enrolledAt;
    private String status;
}
