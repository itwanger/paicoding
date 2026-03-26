package com.github.paicoding.forum.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.article.repository.entity.ReadCountDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 标签mapper接口
 *
 * @author louzai
 * @date 2022-07-18
 */
public interface ReadCountMapper extends BaseMapper<ReadCountDO> {

    /**
     * 插入或更新阅读总数
     * 使用 INSERT ... ON DUPLICATE KEY UPDATE 保持与 Redis 中的总数一致
     *
     * @param documentId   文档ID
     * @param documentType 文档类型
     * @param cnt          当前总计数
     */
    @Insert("INSERT INTO read_count (document_id, document_type, cnt, create_time, update_time) " +
            "VALUES (#{documentId}, #{documentType}, #{cnt}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE cnt = #{cnt}, update_time = NOW()")
    void insertOrUpdate(@Param("documentId") Long documentId,
                        @Param("documentType") Integer documentType,
                        @Param("cnt") Integer cnt);
}
