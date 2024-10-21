package com.github.paicoding.forum.web.controller.user.vo;

import com.github.paicoding.forum.api.model.enums.FollowSelectEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagSelectDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
public class UserHomeVo {
    private String homeSelectType;
    private List<TagSelectDTO> homeSelectTags;
    /**
     * 关注列表/粉丝列表
     */
    private PageListVo<FollowUserInfoDTO> followList;
    /**
     * @see FollowSelectEnum#getCode()
     */
    private String followSelectType;
    private List<TagSelectDTO> followSelectTags;
    private UserStatisticInfoDTO userHome;

    /**
     * 文章列表
     */
    private PageListVo<ArticleDTO> homeSelectList;
}
