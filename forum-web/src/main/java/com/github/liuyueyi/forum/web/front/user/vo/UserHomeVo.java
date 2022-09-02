package com.github.liuyueyi.forum.web.front.user.vo;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
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
    String homeSelectType;
    List<TagSelectDTO> homeSelectTags;
    UserFollowListDTO fansList;
    UserFollowListDTO followList;
    String followSelectType;
    List<TagSelectDTO> followSelectTags;
    UserStatisticInfoDTO userHome;

    ArticleListDTO homeSelectList;
}
