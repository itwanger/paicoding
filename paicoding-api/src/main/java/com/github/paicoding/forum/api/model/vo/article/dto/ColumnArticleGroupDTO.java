package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/14
 */
@Data
public class ColumnArticleGroupDTO {

    /**
     * 专栏id
     */
    private Long columnId;

    /**
     * 当前分组id
     */
    private Long groupId;

    /**
     * 父分组
     */
    private Long parentGroupId;

    /**
     * 文案说明
     */
    private String title;

    /**
     * 顺序
     */
    private Long section;

    /**
     * 子分组
     */
    private List<ColumnArticleGroupDTO> children;

    /**
     * 分组下的文章列表
     */
    private List<ColumnArticleDTO> articles;

    public static ColumnArticleGroupDTO defaultGroup = new ColumnArticleGroupDTO();

    public static ColumnArticleGroupDTO newDefaultGroup(Long columnId) {
        ColumnArticleGroupDTO dto = new ColumnArticleGroupDTO();
        dto.setColumnId(columnId);
        dto.setSection(0L);
        dto.setTitle("未分组");
        return dto;
    }
}
