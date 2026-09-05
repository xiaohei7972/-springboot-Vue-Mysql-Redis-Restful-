package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("attendance")
public class AttendanceEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDate attendanceDate;
    private String status;
    private String remark;
}
