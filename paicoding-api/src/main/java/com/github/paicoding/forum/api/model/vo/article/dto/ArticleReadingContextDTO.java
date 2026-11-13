package com.github.paicoding.forum.api.model.vo.article.dto;
import lombok.Data;
@Data
public class ArticleReadingContextDTO {
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long articleId;
    private boolean versioned;
    private String discussionScope = "current";
    private Integer currentCommentCount;
    private Integer historyCommentCount;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long currentEditionId;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long selectedEditionId;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long selectedDetailId;
    private boolean viewingHistoryEdition;
    private String selectedEditionName;
    private String selectedEditionCode;
    private java.util.List<ArticleEditionSummaryDTO> editions;
}
