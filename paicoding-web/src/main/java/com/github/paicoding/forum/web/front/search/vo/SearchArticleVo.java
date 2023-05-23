package com.github.paicoding.forum.web.front.search.vo;

import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YiHui
 * @date 2022/10/28
 */
@Data
@ApiModel(value="文章信息")
public class SearchArticleVo implements Serializable {
    private static final long serialVersionUID = -2989169905031769195L;

    @ApiModelProperty("搜索的关键词")
    private String key;

    @ApiModelProperty("文章列表")
    private List<SimpleArticleDTO> items;
}
