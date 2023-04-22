package com.github.paicoding.forum.web.front.search.vo;

import com.github.paicoding.forum.api.model.vo.article.dto.SimpleColumnDTO;
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
@ApiModel(value="专栏信息")
public class SearchColumnVo implements Serializable {
    private static final long serialVersionUID = -2989169905031769195L;

    @ApiModelProperty("搜索的关键词")
    private String key;

    @ApiModelProperty("专栏列表")
    private List<SimpleColumnDTO> items;
}
