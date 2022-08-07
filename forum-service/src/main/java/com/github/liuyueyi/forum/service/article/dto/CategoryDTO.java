package com.github.liuyueyi.forum.service.article.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2022/7/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO implements Serializable {
    public static final String DEFAULT_TOTAL_CATEGORY = "全部";
    public static final CategoryDTO DEFAULT_CATEGORY = new CategoryDTO(0L, "全部");

    private static final long serialVersionUID = 8272116638231812207L;
    public static CategoryDTO EMPTY = new CategoryDTO(-1L, "illegal");

    private Long categoryId;

    private String category;

    private Boolean selected;

    public CategoryDTO(Long categoryId, String category) {
        this.categoryId = categoryId;
        this.category = category;
        this.selected = false;
    }
}
