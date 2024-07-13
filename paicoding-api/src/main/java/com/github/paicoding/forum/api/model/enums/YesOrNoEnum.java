package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 状态的枚举
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum YesOrNoEnum {

    NO(0, "N","否", "no"),
    YES(1,"Y" ,"是", "yes");

    YesOrNoEnum(int code, String desc, String cnDesc, String enDesc) {
        this.code = code;
        this.cnDesc = cnDesc;
        this.enDesc = enDesc;
        this.desc = desc;
    }

    private final int code;
    private final String desc;
    private final String cnDesc;
    private final String enDesc;

    public static YesOrNoEnum formCode(int code) {
        for (YesOrNoEnum yesOrNoEnum : YesOrNoEnum.values()) {
            if (yesOrNoEnum.getCode() == code) {
                return yesOrNoEnum;
            }
        }
        return YesOrNoEnum.NO;
    }

    /**
     * 是否为是或否，主要用于某些场景字段未赋值的情况
     *
     * @return
     */
    public static boolean equalYN(Integer code) {
        if (code == null) {
            return false;
        }
        if (code != null && (code.equals(YES.code) || code.equals(NO.code))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是yes
     *
     * @param code
     * @return
     */
    public static boolean isYes(Integer code) {
        if (code == null) {
            return false;
        }
        return YesOrNoEnum.YES.getCode() == code;
    }

}
