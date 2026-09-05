package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
