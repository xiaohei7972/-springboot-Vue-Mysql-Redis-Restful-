package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String action;
    private String method;
    private String path;
    private String ip;
    private LocalDateTime createdAt;
}
