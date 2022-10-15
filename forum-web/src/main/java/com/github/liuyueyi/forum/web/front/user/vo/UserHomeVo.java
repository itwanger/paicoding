package com.github.liuyueyi.forum.web.front.user.vo;

import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagSelectDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/2
 */
@Data
public class UserHomeVo {
    private String homeSelectType;
    private List<TagSelectDTO> homeSelectTags;
    private UserFollowListDTO fansList;
    private UserFollowListDTO followList;
    private String followSelectType;
    private List<TagSelectDTO> followSelectTags;
    private UserStatisticInfoDTO userHome;

    private PageListVo<ArticleDTO> homeSelectList;
}
