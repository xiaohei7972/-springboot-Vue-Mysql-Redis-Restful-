package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("grade")
public class GradeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enrollmentId;
    private BigDecimal usualScore;
    private BigDecimal midtermScore;
    private BigDecimal finalScore;
    private BigDecimal totalScore;
    private String gradeStatus;
    private LocalDateTime updatedAt;
}
