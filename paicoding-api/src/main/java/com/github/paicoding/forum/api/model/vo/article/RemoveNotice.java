package com.github.paicoding.forum.api.model.vo.article;

import lombok.Data;

/**
 * 删除消息类型
 *
 * @ClassName: RemoveNotice
 * @Author: ygl
 * @Date: 2023/6/16 11:07
 * @Version: 1.0
 */
@Data
public class RemoveNotice {

    /**
     * -1-总
     * 2-点赞
     * 3-收藏
     * 6-评论
     * 8-回复
     */
    private Integer type;

}
