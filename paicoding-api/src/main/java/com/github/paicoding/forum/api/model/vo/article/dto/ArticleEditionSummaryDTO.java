package com.github.paicoding.forum.api.model.vo.article.dto;
import lombok.Data;
@Data
public class ArticleEditionSummaryDTO {
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;
    private Integer editionNo;
    private String editionCode;
    private String editionName;
    private String audienceTag;
    private String guideTip;
    private String changeNote;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long publishedDetailId;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long draftDetailId;
    private Integer draftStatus;
    private Integer state;
    private java.util.Date lastPublishedAt;
}
