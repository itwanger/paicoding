package com.github.paicoding.forum.api.model.vo.article.dto;

import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "文章分类DTO", description = "文章分类DTO")
public class CategoryDTO implements Serializable {
    public static final String DEFAULT_TOTAL_CATEGORY = "全部";
    public static final CategoryDTO DEFAULT_CATEGORY = new CategoryDTO(0L, "全部");

    private static final long serialVersionUID = 8272116638231812207L;
    public static CategoryDTO EMPTY = new CategoryDTO(-1L, "illegal");

    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称")
    private String category;

    @ApiModelProperty(value = "分类排序")
    private Integer rank;

    @ApiModelProperty(value = "分类状态")
    private Integer status;


    public CategoryDTO(Long categoryId, String category) {
        this(categoryId, category, 0);
    }

    public CategoryDTO(Long categoryId, String category, Integer rank) {
        this.categoryId = categoryId;
        this.category = category;
        this.status = PushStatusEnum.ONLINE.getCode();
        this.rank = rank;
    }
}
