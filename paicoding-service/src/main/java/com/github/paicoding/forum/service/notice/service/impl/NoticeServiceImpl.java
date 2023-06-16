package com.github.paicoding.forum.service.notice.service.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.article.RemoveNotice;
import com.github.paicoding.forum.api.model.vo.article.dto.NoticeDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.constant.RedisConstant;
import com.github.paicoding.forum.service.notice.service.NoticeService;
import com.github.paicoding.forum.service.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * NoticeServiceImpl
 *
 * @ClassName: NoticeServiceImpl
 * @Author: ygl
 * @Date: 2023/6/16 07:04
 * @Version: 1.0
 */
@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private RedisUtil redisUtil;

    String totalPre = RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE
            + RedisConstant.TOTAL;

    String commentPre = RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE
            + RedisConstant.COMMENT;

    String recoverPre = RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE
            + RedisConstant.RECOVER;

    String praisePre = RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE
            + RedisConstant.PRAISE;

    String collectionPre = RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE
            + RedisConstant.COLLECTION;

    @Override
    public NoticeDTO getTotal() {

        Long userId = this.getUserId();

        int totalNum = this.getNum(totalPre, userId);
        int commentNum = this.getNum(commentPre, userId);
        int recoverNum = this.getNum(recoverPre, userId);
        int praiseNum = this.getNum(praisePre, userId);
        int collectionNum = this.getNum(collectionPre, userId);

        NoticeDTO noticeDTO = new NoticeDTO();
        noticeDTO.setTotalNum(totalNum);
        noticeDTO.setCommentNum(commentNum);
        noticeDTO.setRecoverNum(recoverNum);
        noticeDTO.setPraiseNum(praiseNum);
        noticeDTO.setCollectionNum(collectionNum);

        return noticeDTO;
    }

    @Override
    public void removeNum(RemoveNotice param) {

        Long userId = this.getUserId();
        this.deleteRedisKey(param.getType());

    }

    /**
     * -1-总
     * 2-点赞
     * 3-收藏
     * 6-评论
     * 8-回复
     */
    private void deleteRedisKey(Integer type) {

        Long userId = this.getUserId();

        if (type == -1) {
            redisUtil.del(totalPre + userId);
        } else if (type == 2) {
            redisUtil.del(praisePre + userId);
        } else if (type == 3) {
            redisUtil.del(collectionPre + userId);
        } else if (type == 6) {
            redisUtil.del(commentPre + userId);
        } else if (type == 8) {
            redisUtil.del(recoverPre + userId);
        }

    }


    private int getNum(String pre, Long userId) {

        String key = pre + userId;
        Object o = redisUtil.get(key);
        if (ObjectUtils.isEmpty(o)) {
            return 0;
        } else {
            return Integer.parseInt(o.toString());
        }

    }

    private Long getUserId() {

        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        try {
            return user.getUserId();
        } catch (Exception e) {
            log.error("Error getting user");

        }
        return 1L;

    }


}
