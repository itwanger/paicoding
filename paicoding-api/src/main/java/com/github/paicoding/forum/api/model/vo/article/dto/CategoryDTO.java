package com.github.paicoding.forum.api.model.vo.article.dto;

import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
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

    private Integer rank;

    private Integer status;

    private Boolean selected;

    public CategoryDTO(Long categoryId, String category) {
        this(categoryId, category, 0);
    }

    public CategoryDTO(Long categoryId, String category, Integer rank) {
        this.categoryId = categoryId;
        this.category = category;
        this.status = PushStatusEnum.ONLINE.getCode();
        this.rank = rank;
        this.selected = false;
    }
}
