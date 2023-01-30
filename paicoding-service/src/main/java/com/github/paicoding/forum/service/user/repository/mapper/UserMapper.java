package com.github.paicoding.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户登录mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface UserMapper extends BaseMapper<UserDO> {
    /**
     * 根据三方唯一id进行查询
     *
     * @param accountId
     * @return
     */
    @Select("select * from user where third_account_id = #{account_id} limit 1")
    UserDO getByThirdAccountId(@Param("account_id") String accountId);
}
