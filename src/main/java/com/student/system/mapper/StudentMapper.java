package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.StudentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<StudentEntity> {
}
