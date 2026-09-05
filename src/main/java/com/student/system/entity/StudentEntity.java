package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student")
public class StudentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String studentNo;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private Long departmentId;
    private Long classId;
    private Integer admissionYear;
    private String status;
}
