package com.github.paicoding.forum.service.chatai.service.impl.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatai.service.RecordsNumberService;
import com.github.paicoding.forum.service.user.repository.entity.UserAiDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserAiMapper;
import com.github.paicoding.forum.service.user.repository.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ai记录次数
 *
 * @ClassName: RecordsNumberServiceImpl
 * @Author: ygl
 * @Date: 2023/6/24 11:09
 * @Version: 1.0
 */
@Service
@Slf4j
public class RecordsNumberServiceImpl implements RecordsNumberService {

    @Resource
    UserMapper userMapper;
    @Resource
    private UserAiMapper userAiMapper;

    @Value("${chatgpt.number.normal}")
    private Integer normalNumber;

    @Value("${chatgpt.number.vip}")
    private Integer vipNumber;


    @Override
    public Integer getRecordsNumber() {

        String key = this.setRecordsNumberKey();
        Integer value = RedisClient.getStr(key) == null ? null : Integer.parseInt(RedisClient.getStr(key));
        if (ObjectUtils.isEmpty(value)) {
            // 根据userId去数据库中查询是否是星球用户，星球用户设置多少次，其他默认50次每天
            value = this.getRecordsNumberByUserId();
            Long expireTime = 24 * 60 * 60L;
            // 设置一天过期时间
            RedisClient.setStrWithExpire(key, String.valueOf(value), expireTime);
        }

        return value;
    }

    @Override
    public Integer decrRecordsNumber() {

        String key = this.setRecordsNumberKey();
        Integer value = RedisClient.getStr(key) == null ? null : Integer.parseInt(RedisClient.getStr(key));
        value -= 1;
        Long expireTime = 24 * 60 * 60L;
        // 设置一天过期时间
        RedisClient.setStrWithExpire(key, String.valueOf(value), expireTime);
        return value;
    }

    private Integer getRecordsNumberByUserId() {

        // 默认50
        Integer recordsNumber = normalNumber;

        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        if (ObjectUtils.isEmpty(user)) {
            return recordsNumber;
        }
        Long userId = user.getUserId();

        LambdaQueryWrapper<UserDO> query = Wrappers.lambdaQuery();

        // 支持userName or starNumber查询
        query.and(wrapper -> wrapper.eq(UserDO::getId, userId))
                .eq(UserDO::getDeleted, YesOrNoEnum.NO.getCode());

        UserDO userDO = userMapper.selectOne(query);
        LambdaQueryWrapper<UserAiDO> queryAi = Wrappers.lambdaQuery();
        queryAi.eq(UserAiDO::getUserId, userDO.getId())
                .eq(UserAiDO::getState, 2);
        UserAiDO userAiDO = userAiMapper.selectOne(queryAi);

        if (ObjectUtils.isEmpty(userAiDO)) {
            return recordsNumber;
        }
        // 星球用户100
        return vipNumber;

    }

    private String setRecordsNumberKey() {

        String key = "chat:records:number:";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date currentDate = new Date();
        String currentDateStr = sdf.format(currentDate);
        key = key + currentDateStr + ":";
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        Long userId = -1L;
        if (!ObjectUtils.isEmpty(user)) {
            userId = user.getUserId();
        }
        key = key + userId;

        return key;

    }


}
