package com.github.paicoding.forum.service.article.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.recommend.SideBarDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.config.ArticleCacheProperties;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: pai_coding
 * @description: 管理文章相关信息的缓存
 * @author: XuYifei
 * @create: 2024-10-24
 */

@Component
@EnableConfigurationProperties(ArticleCacheProperties.class)
public class ArticleCacheManager {

    @Autowired
    private ArticleCacheProperties articleCacheProperties;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String ARTICLE_SCORE_PREFIX = "article_score:";

    public static final String ARTICLE_INFO_PREFIX = "article_info:";

    public static final String ARTICLE_CONTENT_PREFIX = "article_content:";

    public static final String ARTICLE_COLUMN_RELATION_PREFIX = "article_column_relation:";

    public static final String ARTICLE_COMMENTS_PREFIX = "article_comments:";

    public static final String ARTICLE_HOT_COMMENT_PREFIX = "article_hot_comment:";


    /**
     * 获取文章的评分
     * @param articleId
     * @return
     */
    public Double getArticleScore(long articleId) {
        Double score = RedisClient.zScore(ARTICLE_SCORE_PREFIX, String.valueOf(articleId));
        return score == null ? 0 : score;
    }

    /**
     * 设置文章的评分
     * @param articleId
     * @param score
     * @return 返回新的评分
     */
    public Double setArticleScore(long articleId, int score) {
        RedisClient.expire(ARTICLE_SCORE_PREFIX, articleCacheProperties.getExpireSeconds());
        return RedisClient.zIncrBy(ARTICLE_SCORE_PREFIX, String.valueOf(articleId), score);
    }

    public boolean isArticleColumnArticleExist(long articleId){
        return RedisClient.exists(ARTICLE_COLUMN_RELATION_PREFIX + articleId);
    }

    public void setColumnArticle(long articleId, ColumnArticleDO columnArticleDO){
        RedisClient.setObject(ARTICLE_COLUMN_RELATION_PREFIX + articleId, columnArticleDO);
    }

    public ColumnArticleDO getColumnArticle(long articleId) {
        Object value = RedisClient.getObject(ARTICLE_COLUMN_RELATION_PREFIX + articleId);
        if(value != null){
            return OBJECT_MAPPER.convertValue(value, ColumnArticleDO.class);
        }
        return null;
    }


    public ArticleDTO getArticleInfo(long articleId) {
//        String str = RedisClient.getStr(ARTICLE_INFO_PREFIX + articleId);
//        try {
//            return str == null ? null : OBJECT_MAPPER.readValue(str, ArticleDTO.class);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
        Object value = RedisClient.getObject(ARTICLE_INFO_PREFIX + articleId);
        if(value != null){
            return OBJECT_MAPPER.convertValue(value, ArticleDTO.class);
        }
        return null;
    }

    public void setArticleInfo(long articleId, ArticleDTO articleDTO){
        RedisClient.setObject(ARTICLE_INFO_PREFIX + articleId, articleDTO);
//        try {
//            RedisClient.setStr(ARTICLE_INFO_PREFIX + articleId, OBJECT_MAPPER.writeValueAsString(articleDTO));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * 获取文章的热评缓存
     * @param articleId
     * @return
     * @throws JsonProcessingException
     */
    public TopCommentDTO getHotComment(long articleId) throws JsonProcessingException {
        String hotCommentStr = RedisClient.getStr(ARTICLE_HOT_COMMENT_PREFIX + articleId);

        return OBJECT_MAPPER.readValue(hotCommentStr, TopCommentDTO.class);
    }

    /**
     * 设置文章的热评缓存
     * @param articleId
     * @param hotComment
     * @throws JsonProcessingException
     */
    public void setHotComment(long articleId, TopCommentDTO hotComment) throws JsonProcessingException {
        RedisClient.setStr(ARTICLE_HOT_COMMENT_PREFIX + articleId, OBJECT_MAPPER.writeValueAsString(hotComment));
    }

    /**
     * 获取文章评论的缓存
     * @param articleId
     * @return
     * @throws JsonProcessingException
     */
    public List<TopCommentDTO> getComments(long articleId) throws JsonProcessingException {
        String commentsStr = RedisClient.getStr(ARTICLE_COMMENTS_PREFIX + articleId);
        return OBJECT_MAPPER.readValue(commentsStr, List.class);
    }

    /**
     * 设置文章评论的缓存
     * @param articleId
     * @param comments
     * @throws JsonProcessingException
     */
    public void setComments(long articleId, List<TopCommentDTO> comments) throws JsonProcessingException {
        RedisClient.setStr(ARTICLE_COMMENTS_PREFIX + articleId, OBJECT_MAPPER.writeValueAsString(comments));
    }

    public List<SideBarDTO> getSideBarItems(long articleId, long userId) throws JsonProcessingException {
        String sideBarItemsStr = RedisClient.getStr(ARTICLE_COMMENTS_PREFIX + articleId + "_" + userId);
        return OBJECT_MAPPER.readValue(sideBarItemsStr, List.class);
    }

    public void setSideBarItems(long articleId, long userId, List<SideBarDTO> sideBarItems) throws JsonProcessingException {
        RedisClient.setStr(ARTICLE_COMMENTS_PREFIX + articleId + "_" + userId, OBJECT_MAPPER.writeValueAsString(sideBarItems));
    }

    /**
     * 删除文章信息缓存
     * @param articleId
     */
    public void delArticleInfo(long articleId) {
        RedisClient.delObject(ARTICLE_INFO_PREFIX + articleId);
    }

}
