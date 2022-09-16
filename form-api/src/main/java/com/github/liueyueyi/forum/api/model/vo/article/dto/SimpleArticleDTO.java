package com.github.liueyueyi.forum.api.model.vo.article.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 文章推荐
 *
 * @author YiHui
 * @date 2022/9/6
 */
@Data
public class SimpleArticleDTO implements Serializable {
    private static final long serialVersionUID = 3646376715620165839L;

    private Long id;

    private String title;

    private Timestamp createTime;
}
