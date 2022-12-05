package com.github.liueyueyi.forum.api.model.vo.constants;

import lombok.Getter;

/**
 * 异常码规范：
 * xxx - xxx - xxx
 * 业务 - 状态 - code
 * <p>
 * 业务取值
 * - 100 全局
 * - 200 文章相关
 * - 300 评论相关
 * - 400 用户相关
 * <p>
 * 状态：基于http status的含义
 * - 4xx 调用方使用姿势问题
 * - 5xx 服务内部问题
 * <p>
 * code: 具体的业务code
 *
 * @author YiHui
 * @date 2022/7/27
 */
@Getter
public enum StatusEnum {
    SUCCESS(0, "OK"),

    // 全局传参异常
    ILLEGAL_ARGUMENTS(100_400_001, "参数异常"),
    ILLEGAL_ARGUMENTS_MIXED(100_400_002, "参数异常:%s"),

    // 全局权限相关
    FORBID_ERROR(100_403_001, "无权限"),

    FORBID_ERROR_MIXED(100_403_002, "无权限:%s"),

    // 全局，数据不存在
    RECORDS_NOT_EXISTS(100_500_001, "记录不存在:%s"),


    // 用户相关异常
    LOGIN_FAILED_MIXED(400_403_001, "登录失败:%s"),
    USER_NOT_EXISTS(400_500_001, "用户不存在:%s"),
    USER_PWD_ERROR(400_500_002, "用户名or密码错误"),


    ;

    private int code;

    private String msg;

    StatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
