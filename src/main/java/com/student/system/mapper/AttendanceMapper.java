package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.AttendanceEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendanceMapper extends BaseMapper<AttendanceEntity> {
}
