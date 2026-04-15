package com.example.demofirst.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operate_log")
public class OperateLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;        // 操作人ID（从Token中获取）
    private String url;         // 访问的接口地址
    private String method;      // 请求方式（GET/POST）
    private String params;      // 请求参数（JSON格式）
    private String result;      // 返回结果（JSON格式）
    private Long costTime;      // 接口执行耗时（毫秒）
    private LocalDateTime createTime; // 操作时间
}
