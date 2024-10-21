package com.github.paicoding.forum.web.controller.search.vo;

import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Tag(name="用户信息")
public class SearchUserVo implements Serializable {
    private static final long serialVersionUID = -2989169905031769195L;

    @Schema(description = "搜索的关键词")
    private String key;

    @Schema(description = "用户列表")
    private List<SimpleUserInfoDTO> items;
}
