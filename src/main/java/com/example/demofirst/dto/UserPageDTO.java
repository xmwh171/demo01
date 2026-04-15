package com.example.demofirst.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户分页查询参数
 */
@Data
public class UserPageDTO {
    // 当前页码（默认第1页）
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;
    // 每页条数（默认10条）
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;
    // 用户名模糊搜索关键词
    private String username;
    // 创建时间起始范围
    private LocalDateTime startTime;
    // 创建时间结束范围
    private LocalDateTime endTime;
}
