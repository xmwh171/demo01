package com.example.demofirst.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demofirst.entity.OperateLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperateLogMapper extends BaseMapper<OperateLog> {
}
