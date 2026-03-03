package com.github.paicoding.forum.service.knowledge.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.knowledge.repository.entity.KnowledgeDocDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDocDO> {

    @Select("""
            <script>
            SELECT *
            FROM knowledge_doc
            WHERE deleted = 0
              AND status = 1
              <if test='categoryId != null'> AND category_id = #{categoryId} </if>
              <if test='keyword != null and keyword != ""'>
                 AND MATCH(title, description, content_md) AGAINST (#{keyword} IN BOOLEAN MODE)
              </if>
            ORDER BY update_time DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<KnowledgeDocDO> queryPublishedDocs(@Param("categoryId") Long categoryId,
                                            @Param("keyword") String keyword,
                                            @Param("offset") long offset,
                                            @Param("pageSize") long pageSize);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM knowledge_doc
            WHERE deleted = 0
              AND status = 1
              <if test='categoryId != null'> AND category_id = #{categoryId} </if>
              <if test='keyword != null and keyword != ""'>
                 AND MATCH(title, description, content_md) AGAINST (#{keyword} IN BOOLEAN MODE)
              </if>
            </script>
            """)
    Long countPublishedDocs(@Param("categoryId") Long categoryId,
                            @Param("keyword") String keyword);
}
