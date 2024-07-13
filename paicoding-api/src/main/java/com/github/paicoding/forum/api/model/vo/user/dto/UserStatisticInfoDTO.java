package com.github.paicoding.forum.api.model.vo.user.dto;

import com.github.paicoding.forum.api.model.vo.article.dto.YearArticleDTO;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 用户主页信息
 *
 * @author XuYifei
 * @since 2024-07-12
 */
@Data
@ToString(callSuper = true)
public class UserStatisticInfoDTO extends BaseUserInfoDTO {

    /**
     * 关注数
     */
    private Integer followCount;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 加入天数
     */
    private Integer joinDayCount;

    /**
     * 已发布文章数
     */
    private Integer articleCount;

    /**
     * 文章点赞数
     */
    private Integer praiseCount;

    /**
     * 文章被阅读数
     */
    private Integer readCount;

    /**
     * 文章被收藏数
     */
    private Integer collectionCount;

    /**
     * 是否关注当前用户
     */
    private Boolean followed;

    /**
     * 身份信息完整度百分比
     */
    private Integer infoPercent;

    /**
     * 创造历程
     */
    private List<YearArticleDTO> yearArticleList;
}
