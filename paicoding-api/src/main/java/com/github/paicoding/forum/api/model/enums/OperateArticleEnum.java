package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 操作文章
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Getter
public enum OperateArticleEnum {

    EMPTY(0, "") {
        @Override
        public int getDbStatCode() {
            return 0;
        }
    },
    OFFICAL(1, "官方") {
        @Override
        public int getDbStatCode() {
            return OfficalStatEnum.OFFICAL.getCode();
        }
    },
    CANCEL_OFFICAL(2, "非官方"){
        @Override
        public int getDbStatCode() {
            return OfficalStatEnum.NOT_OFFICAL.getCode();
        }
    },
    TOPPING(3, "置顶"){
        @Override
        public int getDbStatCode() {
            return ToppingStatEnum.TOPPING.getCode();
        }
    },
    CANCEL_TOPPING(4, "不置顶"){
        @Override
        public int getDbStatCode() {
            return ToppingStatEnum.NOT_TOPPING.getCode();
        }
    },
    CREAM(5, "加精"){
        @Override
        public int getDbStatCode() {
            return CreamStatEnum.CREAM.getCode();
        }
    },
    CANCEL_CREAM(6, "不加精"){
        @Override
        public int getDbStatCode() {
            return CreamStatEnum.NOT_CREAM.getCode();
        }
    };

    OperateArticleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static OperateArticleEnum fromCode(Integer code) {
        for (OperateArticleEnum value : OperateArticleEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return OperateArticleEnum.OFFICAL;
    }

    public abstract int getDbStatCode();
}
