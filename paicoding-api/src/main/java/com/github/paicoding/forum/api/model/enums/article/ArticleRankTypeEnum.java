package com.github.paicoding.forum.api.model.enums.article;

import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * 文章排行类型
 *
 * @author YiHui
 * @date 2024/3/14
 */
public enum ArticleRankTypeEnum {
    /**
     * 阅读数
     */
    READ_COUNT("read"),
    /**
     * 评论数
     */
    COMMENT_COUNT("comment"),
    /**
     * 点赞数
     */
    PRAISE_COUNT("praise"),
    ;

    @Getter
    private String type;

    ArticleRankTypeEnum(String type) {
        this.type = type;
    }


    /**
     * 根据类型匹配排序枚举
     *
     * @param type
     * @return
     */
    public static ArticleRankTypeEnum typeOf(String type) {
        for (ArticleRankTypeEnum typeEnum : values()) {
            if (StringUtils.endsWithIgnoreCase(typeEnum.getType(), type)) {
                return typeEnum;
            }
        }
        return null;
    }
}
