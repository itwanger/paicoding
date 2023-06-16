package com.github.paicoding.forum.api.model.vo.article.dto;

import lombok.Data;

/**
 * NoticeDTO
 *
 * @ClassName: NoticeDTO
 * @Author: ygl
 * @Date: 2023/6/16 10:11
 * @Version: 1.0
 */
@Data
public class NoticeDTO {

    /**
     * 总提醒数
     */
    private Integer totalNum;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 回复数
     */
    private Integer recoverNum;

    /**
     * 点赞数
     */
    private Integer praiseNum;

    /**
     * 收藏数
     */
    private Integer collectionNum;


}
