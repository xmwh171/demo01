package com.example.demofirst.exception;


import com.example.demofirst.constant.ErrorCodeEnum;
import lombok.Getter;

/**
 * 优化版业务异常：关联错误码枚举
 */
@Getter // 提供getter方法
public class BusinessException extends RuntimeException {
    // 错误码枚举（替代Integer）
    private final ErrorCodeEnum errorCode;
    // 详细错误信息（可选）
    private final String detailMsg;

    // 构造1：只传枚举
    public BusinessException(ErrorCodeEnum errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
        this.detailMsg = errorCode.getMsg();
    }

    // 构造2：枚举 + 自定义提示
    public BusinessException(ErrorCodeEnum errorCode, String detailMsg) {
        super(detailMsg);
        this.errorCode = errorCode;
        this.detailMsg = detailMsg;
    }
}
