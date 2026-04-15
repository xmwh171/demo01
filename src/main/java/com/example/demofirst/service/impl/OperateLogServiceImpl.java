package com.example.demofirst.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demofirst.entity.OperateLog;
import com.example.demofirst.mapper.OperateLogMapper;
import com.example.demofirst.service.OperateLogService;
import org.springframework.stereotype.Service;

@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {
}
