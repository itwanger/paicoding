package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.user.service.UserService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Objects;

/** Full-body eligibility for AI context. Preview readers may discuss their quote, not request hidden text. */
@Service
public class ArticleReadingAccess {
    @Resource private ColumnService columns;
    @Resource private UserService users;
    @Resource private ArticlePayService payments;

    public boolean canReadFull(ArticleDO article, Long userId) {
        if (!Objects.equals(article.getStatus(), 1) || !Objects.equals(article.getDeleted(), 0)) return false;
        ColumnArticleDO relation = columns.getColumnArticleRelation(article.getId());
        int type = article.getReadType() == null ? 0 : article.getReadType();
        if (relation != null) {
            ColumnDTO column = columns.queryBasicColumnInfo(relation.getColumnId());
            if (column == null || (Objects.equals(column.getState(), 0) && !Objects.equals(userId, column.getAuthor()))) return false;
            if (Objects.equals(userId, article.getUserId())) return true;
            type = relation.getReadType() == null ? 0 : relation.getReadType();
            if (type == 0) {
                type = column.getType();
                if (type == 2) {
                    long now = System.currentTimeMillis();
                    type = now < column.getFreeStartTime() || now > column.getFreeEndTime() ? 1 : 0;
                }
            }
        }
        if (userId != null && Objects.equals(userId, article.getUserId())) return true;
        if (type == 1) return userId != null;
        if (type == 4) return userId != null && payments.hasPayed(article.getId(), userId);
        if (type == 3) {
            BaseUserInfoDTO user = userId == null ? null : users.queryBasicUserInfo(userId);
            return user != null && user.getStarStatus() == UserAIStatEnum.FORMAL;
        }
        return true;
    }
}
