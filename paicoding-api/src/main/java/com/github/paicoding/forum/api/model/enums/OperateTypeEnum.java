package com.github.paicoding.forum.api.model.enums;

import lombok.Getter;

/**
 * 操作类型
 *
 * @author XuYifei
 * @since 2024-07-12
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

    /**
     * 判断操作的是否是文章
     *
     * @param type
     * @return true 表示文章的相关操作 false 表示评论的相关文章
     */
    public static DocumentTypeEnum getOperateDocumentType(OperateTypeEnum type) {
        return (type == COMMENT || type == DELETE_COMMENT) ? DocumentTypeEnum.COMMENT : DocumentTypeEnum.ARTICLE;
    }

    public static NotifyTypeEnum getNotifyType(OperateTypeEnum type) {
        switch (type) {
            case PRAISE:
                return NotifyTypeEnum.PRAISE;
            case CANCEL_PRAISE:
                return NotifyTypeEnum.CANCEL_PRAISE;
            case COLLECTION:
                return NotifyTypeEnum.COLLECT;
            case CANCEL_COLLECTION:
                return NotifyTypeEnum.CANCEL_COLLECT;
            default:
                return null;
        }
    }
}
