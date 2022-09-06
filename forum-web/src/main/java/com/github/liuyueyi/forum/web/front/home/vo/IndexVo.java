package com.github.liuyueyi.forum.web.front.home.vo;

import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/9/6
 */
@Data
public class IndexVo {
    List<CategoryDTO> categories;

    ArticleListDTO articles;

    UserStatisticInfoDTO user;
}
