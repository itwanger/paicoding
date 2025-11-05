package com.github.paicoding.forum.api.model.vo.comment.dto;

import lombok.Data;

/**
 * 基于文章内容的划线评论
 * <p>
 * {"elementTag":"p","elementIndex":5,"startOffset":11,"endOffset":29,"selectedText":"层，强业务相关，其中每个划分出来的模"}
 *
 * @author YiHui
 * @date 2025/11/3
 */
@Data
public class HighlightDto {
    /**
     * 划线的文本内容
     */
    private String selectedText;

    private String elementTag;

    private Integer elementIndex;

    private Integer startOffset;

    private Integer endOffset;
}
