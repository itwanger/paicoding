package com.github.paicoding.forum.service.article.repository.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.paicoding.forum.api.model.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_edition")
public class ArticleEditionDO extends BaseDO {
    private Long articleId;
    private Integer editionNo;
    private String editionCode;
    private String editionName;
    private String audienceTag;
    private String guideTip;
    private String changeNote;
    private Long publishedDetailId;
    private Long draftDetailId;
    private Integer draftStatus;
    private Integer state;
    private Integer deleted;
    private Date firstPublishedAt;
    private Date lastPublishedAt;
}
