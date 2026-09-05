package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("teacher")
public class TeacherEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String teacherNo;
    private String name;
    private String title;
    private Long departmentId;
    private String phone;
}
