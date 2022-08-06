package com.github.liuyueyi.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleTagDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签映mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {

    /**
     * 批量保存
     *
     * @param entityList
     * @return
     */
    @Insert("<script>" +
            "insert into article_tag(`article_id`, `tag_id`, `deleted`) values " +
            "<foreach collection='list' item='item' separator=','>" +
            "   (#{item.articleId}, #{item.tagId}, 0)" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<ArticleTagDO> entityList);
}
