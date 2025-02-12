package com.github.paicoding.forum.service.shortlink.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.api.model.vo.shortlink.ShortLinkDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接服务mapper接口
 * @author betasecond
 * @date 2022-07-18
 */
interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 新建短链接
     * @param shortLinkDO
     * @return
     */
    @Insert("insert into short_link(original_url, short_url, username, third_party_user_id, user_agent, login_method, deleted, create_time, update_time) values(#{original_url}, #{short_url}, #{username}, #{third_party_user_id}, #{user_agent}, #{login_method}, #{deleted}, #{create_time}, #{update_time})")
    int insert(ShortLinkDO shortLinkDO);

    /**
     * 根据短链接查询
     * @param shortUrl
     * @return
     */
    @Select("select * from short_link where short_url = #{short_url} limit 1")
    ShortLinkDO getByShortUrl(@Param("short_url") String shortUrl);

    /**
     * 根据原始链接查询
     * @param originalUrl
     * @return
     */
    @Select("select * from short_link where original_url = #{original_url} limit 1")
    ShortLinkDO getByOriginalUrl(@Param("original_url") String originalUrl);

    /**
     * 根据用户名查询
     * @param username
     * @return
     */
    @Select("select * from short_link where username = #{username}")
    List<ShortLinkDO> getByUsername(@Param("username") String username);


}
