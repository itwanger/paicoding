package com.github.paicoding.forum.service.user.cahce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import org.springframework.stereotype.Component;

/**
 * @program: pai_coding
 * @description: 用户信息缓存
 * @author: XuYifei
 * @create: 2024-10-24
 */

@Component
public class UserInfoCacheManager {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static final String USER_INFO_PREFIX = "user_info:";

    /**
     * 获取用户信息缓存
     * @param userId
     * @return
     */
    public UserStatisticInfoDTO getUserInfo(long userId) {
        String userInfo = RedisClient.getStr(USER_INFO_PREFIX + userId);
        if (userInfo == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(userInfo, UserStatisticInfoDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置用户信息缓存
     * @param userId
     * @param userStatisticInfoDTO
     */
    public void setUserInfo(long userId, UserStatisticInfoDTO userStatisticInfoDTO) {
        try {
            RedisClient.setStr(USER_INFO_PREFIX + userId, OBJECT_MAPPER.writeValueAsString(userStatisticInfoDTO));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
