package com.github.paicoding.forum.api.model.vo.article.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.enums.ArticleReadTypeEnum;
import com.github.paicoding.forum.api.model.enums.SourceTypeEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
import com.github.paicoding.forum.api.model.vo.user.dto.ArticleFootCountDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章信息
 * <p>
 * DTO 定义返回给web前端的实体类 (VO)
 *
 * @author YiHui
 * @date 2022/7/24
 */
@Data
public class ArticleDTO implements Serializable {
    private static final long serialVersionUID = -793906904770296838L;

    private Long articleId;

    /**
     * 文章类型：1-博文，2-问答
     */
    private Integer articleType;

    /**
     * 作者uid
     */
    private Long author;

    /**
     * 作者名
     */
    private String authorName;

    /**
     * 作者头像
     */
    @JsonSerialize(using = CdnImgSerializer.class)
    private String authorAvatar;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 短标题
     */
    private String shortTitle;

    /**
     * URL友好的文章标识,用于SEO优化
     */
    private String urlSlug;

    /**
     * 简介
     */
    private String summary;

    /**
     * 封面
     */
    private String cover;

    /**
     * 正文
     */
    private String content;

    /**
     * 文章来源
     *
     * @see SourceTypeEnum
     */
    private String sourceType;

    /**
     * 原文地址
     */
    private String sourceUrl;

    /**
     * 0 未发布 1 已发布
     */
    private Integer status;

    /**
     * 阅读类型
     *
     * @see ArticleReadTypeEnum#getType()
     */
    private Integer readType;

    /**
     * ture 表示可以阅读 false 表示无法阅读全文
     */
    private Boolean canRead;

    /**
     * 是否官方
     */
    private Integer officalStat;

    /**
     * 是否置顶
     */
    private Integer toppingStat;

    /**
     * 是否加精
     */
    private Integer creamStat;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;

    /**
     * 分类
     */
    private CategoryDTO category;

    /**
     * 标签
     */
    private List<TagDTO> tags;

    /**
     * 表示当前查看的用户是否已经点赞过
     */
    private Boolean praised;

    /**
     * 表示当用户是否评论过
     */
    private Boolean commented;

    /**
     * 表示当前用户是否收藏过
     */
    private Boolean collected;

    /**
     * 文章对应的统计计数
     */
    private ArticleFootCountDTO count;

    /**
     * 点赞用户信息
     */
    private List<SimpleUserInfoDTO> praisedUsers;

    /**
     * 支付金额，单位（元）, 为了防止精度问题，返回String格式
     */
    private String payAmount;

    /**
     * 付款方式
     *
     * @see ThirdPayWayEnum#wxPay()
     */
    private String payWay;

    public ArticleDTO setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = CdnUtil.autoTransCdn(authorAvatar);
        return this;
    }
}
