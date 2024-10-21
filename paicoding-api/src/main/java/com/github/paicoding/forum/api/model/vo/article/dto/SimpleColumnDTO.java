package com.github.paicoding.forum.api.model.vo.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SimpleColumnDTO implements Serializable {

    private static final long serialVersionUID = 3646376715620165839L;

    @Schema(description = "专栏id")
    private Long columnId;

    @Schema(description = "专栏名")
    private String column;

    // 封面
    @Schema(description = "封面")
    private String cover;
}
