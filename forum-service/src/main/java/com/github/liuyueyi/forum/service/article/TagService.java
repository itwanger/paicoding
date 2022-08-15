package com.github.liuyueyi.forum.service.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liueyueyi.forum.api.model.enums.PushStatusEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;

import java.util.Collection;
import java.util.List;

public interface TagService {

    /**
     * 查询类目
     *
     * @param tagIds
     * @return
     */
    List<TagDTO> getTags(Collection<Long> tagIds);

    /**
     * 添加标签
     *
     * @param tagDTO
     * @return
     */
    Long addTag(TagDO tagDTO);

    /**
     * 更新标签
     *
     * @param tagId
     * @param tagName
     */
    void updateTag(Long tagId, String tagName);

    /**
     * 删除标签
     *
     * @param tagId
     */
    void deleteTag(Long tagId);

    /**
     * 上线/下线标签
     *
     * @param tagId
     */
    void operateTag(Long tagId, PushStatusEnum pushStatusEnum);

    /**
     * 标签分页查询
     *
     * @return
     */
    IPage<TagDO> getTagByPage(PageParam pageParam);

    /**
     * 根据类目ID查询标签列表
     *
     * @param categoryId
     * @return
     */
    List<TagDTO> getTagListByCategoryId(Long categoryId);
}
