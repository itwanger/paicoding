package com.github.paicoding.forum.web.controller.user.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagSelectDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @program: tech-pai
 * @description: 用户个人信息页面所需信息
 * @author: XuYifei
 * @create: 2024-07-06
 */

@Data
public class UserHomeInfoVo {
    /**
     * 关注列表
     */
    private IPage<FollowUserInfoDTO> followList;

    /**
     * 粉丝列表
     */
    private IPage<FollowUserInfoDTO> fansList;

    private List<TagSelectDTO> followSelectTags;
    private UserStatisticInfoDTO userHome;

    /**
     * 文章列表
     */
    private IPage<ArticleDTO> articlesList;

    /**
     * 浏览记录
     */
    private IPage<ArticleDTO> historyList;

    /**
     * 浏览记录
     */
    private IPage<ArticleDTO> starList;
}
