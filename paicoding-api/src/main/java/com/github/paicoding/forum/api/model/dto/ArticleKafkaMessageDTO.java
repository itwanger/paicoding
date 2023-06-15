package com.github.paicoding.forum.api.model.dto;

import lombok.Data;

/**
 * 文章—kafkaMessage
 *
 * @ClassName: ArticleKafkaMessageDTO
 * @Author: ygl
 * @Date: 2023/6/14 16:48
 * @Version: 1.0
 */
@Data
public class ArticleKafkaMessageDTO {

    /**
     * 点赞者
     */
    private String sourceUserName;

    /**
     * 被点赞ID
     */
    private Long targetUserId;

    /**
     * 文章名称
     */
    private String articleTitle;

    /**
     * 操作类型
     */
    private int type;

    /**
     * 操作类型名字
     */
    private String typeName;

}
