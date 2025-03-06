package com.github.paicoding.forum.service.shortlink.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.shortlink.repository.entity.ShortLinkDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {
    @Select("SELECT * FROM short_link WHERE short_code = #{shortCode} LIMIT 1")
    ShortLinkDO getByShortCode(@Param("shortCode") String shortCode);

    @Insert("INSERT INTO short_link (original_url, short_code, deleted, create_time, update_time) VALUES (#{originalUrl}, #{shortCode}, #{deleted}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int getIdAfterInsert(ShortLinkDO shortLinkDO);
}