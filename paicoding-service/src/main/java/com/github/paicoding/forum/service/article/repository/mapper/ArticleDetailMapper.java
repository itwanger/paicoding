package com.github.paicoding.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDetailDO;
import org.apache.ibatis.annotations.Update;

/**
 * 文章详情mapper接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ArticleDetailMapper extends BaseMapper<ArticleDetailDO> {

    /**
     * 更新正文
     * fixme: 这里的版本迭代还没有管理起来；后续若存在审核中间态，则可以针对上线之后的文章，修改内容之后生成新的一条审核中的记录，版本 +1；而不是直接在原来的记录上进行版本+1
     *
     * @param articleId
     * @param content
     * @return
     */
    @Update("update article_detail set `content` = #{content}, `version` = `version` + 1 where article_id = #{articleId} and `deleted`=0 order by `version` desc limit 1")
    int updateContent(long articleId, String content);
}
