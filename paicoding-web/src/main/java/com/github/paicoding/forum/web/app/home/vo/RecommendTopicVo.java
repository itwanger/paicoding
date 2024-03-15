package com.github.paicoding.forum.web.app.home.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 推荐的专栏
 *
 * @author YiHui
 * @date 2024/3/14
 */
@Data
public class RecommendTopicVo implements Serializable {
    private static final long serialVersionUID = 3251443415827716364L;
    /**
     * 主键id
     */
    private Long id;

    /**
     * 主题
     */
    private String topic;

    /**
     * 描述信息
     */
    private String desc;

    /**
     * 图片
     */
    private String cover;

    /**
     * 推荐的类型: category/tag/column
     */
    private String topicType;
}
