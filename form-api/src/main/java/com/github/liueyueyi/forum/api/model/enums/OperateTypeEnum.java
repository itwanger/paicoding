package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * 操作类型
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum OperateTypeEnum {

    EMPTY(0, "") {
        @Override
        public int getDbStatCode() {
            return 0;
        }
    },
    READ(1, "阅读") {
        @Override
        public int getDbStatCode() {
            return ReadStatEnum.READ.getCode();
        }
    },
    PRAISE(2, "点赞") {
        @Override
        public int getDbStatCode() {
            return PraiseStatEnum.PRAISE.getCode();
        }
    },
    COLLECTION(3, "收藏") {
        @Override
        public int getDbStatCode() {
            return CollectionStatEnum.COLLECTION.getCode();
        }
    },
    CANCEL_PRAISE(4, "取消点赞") {
        @Override
        public int getDbStatCode() {
            return PraiseStatEnum.CANCEL_PRAISE.getCode();
        }
    },
    CANCEL_COLLECTION(5, "取消收藏") {
        @Override
        public int getDbStatCode() {
            return CollectionStatEnum.CANCEL_COLLECTION.getCode();
        }
    },
    COMMENT(6, "评论") {
        @Override
        public int getDbStatCode() {
            return CommentStatEnum.COMMENT.getCode();
        }
    },
    DELETE_COMMENT(7, "删除评论") {
        @Override
        public int getDbStatCode() {
            return CommentStatEnum.DELETE_COMMENT.getCode();
        }
    },
    ;

    OperateTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static OperateTypeEnum fromCode(Integer code) {
        for (OperateTypeEnum value : OperateTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return OperateTypeEnum.EMPTY;
    }

    public abstract int getDbStatCode();
}
