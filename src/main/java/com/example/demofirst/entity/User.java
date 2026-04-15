package com.example.demofirst.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，和MySQL的user表映射
 */
@Data
@TableName("user")  // 指定映射的数据库表名
public class User {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)  // 标记主键，自增策略
    private Long id;

    /**
     * 用户名：非空 + 长度2-20
     */
    @NotBlank(message = "用户名不能为空")  // 非空校验
    @Size(min = 1, max = 20, message = "用户名长度必须在2-20之间")
    private String username;

    /**
     * 密码：非空 + 长度6-20
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 有效状态（0：已删除，1：未删除）
     */
    private Integer enable;

    /**
     * 创建时间（下划线转驼峰：create_time → createTime）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
