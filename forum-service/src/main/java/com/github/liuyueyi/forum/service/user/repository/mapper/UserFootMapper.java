package com.github.liuyueyi.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liuyueyi.forum.service.user.dto.ArticleFootCountDTO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户足迹mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface UserFootMapper extends BaseMapper<UserFootDO> {

    /**
     * 查询用户文章计数
     * @param userId
     * @return
     */
    ArticleFootCountDTO queryArticleFootCount(@Param("userId") Long userId);
}
