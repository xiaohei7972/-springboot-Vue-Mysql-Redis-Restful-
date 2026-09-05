package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("course")
public class CourseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseNo;
    private String name;
    private BigDecimal credit;
    private Integer hours;
    private String semester;
    private Long teacherId;
    private String description;
}
