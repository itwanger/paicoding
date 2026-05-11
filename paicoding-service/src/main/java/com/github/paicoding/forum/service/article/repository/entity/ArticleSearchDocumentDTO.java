package com.github.paicoding.forum.service.article.repository.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文章搜索索引文档
 */
@Data
public class ArticleSearchDocumentDTO {

    private Long articleId;

    private Long authorId;

    private String authorName;

    private String title;

    private String shortTitle;

    private String urlSlug;

    private String summary;

    private String content;

    private Integer status;

    private Integer officalStat;

    private Integer toppingStat;

    private Integer deleted;

    private String columnIds;

    private Date updateTime;

    public List<Long> parseColumnIds() {
        if (StringUtils.isBlank(columnIds)) {
            return Collections.emptyList();
        }
        return Stream.of(columnIds.split(","))
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
