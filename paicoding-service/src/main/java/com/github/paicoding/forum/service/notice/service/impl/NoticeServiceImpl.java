package com.github.paicoding.forum.service.notice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
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

    @Override
    public Integer getTotal() {

        Long userId = this.getUserId();
        String key = totalPre + userId;

        int num = Integer.parseInt(redisUtil.get(key).toString());

        return num;
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
