package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.CollectionStatEnum;
import com.github.liueyueyi.forum.api.model.enums.CommentStatEnum;
import com.github.liueyueyi.forum.api.model.enums.PraiseStatEnum;
import com.github.liueyueyi.forum.api.model.enums.ReadStatEnum;
import com.github.liuyueyi.forum.service.user.UserFootService;
import com.github.liuyueyi.forum.service.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserFootMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户足迹Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class UserFootServiceImpl implements UserFootService {

    @Resource
    private UserFootMapper userFootMapper;

    /**
     * 获取文章计数
     * @param documentId
     * @return
     */
    @Override
    public ArticleFootCountDTO queryArticleCount(Long documentId) {
        ArticleFootCountDTO res = userFootMapper.queryCountByArticle(documentId);
        if (res == null) {
            res = new ArticleFootCountDTO();
        }
        return res;
    }
}
