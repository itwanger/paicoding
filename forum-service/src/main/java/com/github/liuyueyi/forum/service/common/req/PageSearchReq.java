package com.github.liuyueyi.forum.service.common.req;

import lombok.Data;

/**
 * 分页入参
 *
 * @author louzai
 * @date 2022-07-24
 */
@Data
public class PageSearchReq {

    /**
     * 页数
     */
    private Long pageSize;

    /**
     * 页码
     */
    private Long pageNum;
}
