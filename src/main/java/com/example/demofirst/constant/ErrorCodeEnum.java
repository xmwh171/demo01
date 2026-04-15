package com.example.demofirst.constant;


import lombok.Getter;

/**
 * 全局错误码枚举（企业级标准）
 * 规则：
 * - 2xx：成功
 * - 4xx：客户端错误（参数、权限、业务）
 * - 5xx：服务端错误
 */
@Getter
public enum ErrorCodeEnum {

    // 通用成功
    SUCCESS(200, "操作成功"),
    // 客户端错误
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或Token失效"),
    FORBIDDEN(403, "无操作权限"),
    BUSINESS_ERROR(405, "业务逻辑异常"),
    // 服务端错误
    SYSTEM_ERROR(500, "服务器内部错误"),
    // 自定义扩展
    REPEAT_SUBMIT_ERROR(406, "请求重复提交"),
    RATE_LIMIT_ERROR(407, "接口请求过于频繁");

    // 错误码
    private final Integer code;
    // 默认提示信息
    private final String msg;

    ErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // 静态方法：根据码获取枚举
    public static ErrorCodeEnum getByCode(Integer code) {
        for (ErrorCodeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return SYSTEM_ERROR;
    }
}
