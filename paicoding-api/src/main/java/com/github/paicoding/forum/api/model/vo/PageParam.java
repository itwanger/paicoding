package com.github.paicoding.forum.api.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据库分页参数
 *
 * @author louzai
 * @date 2022-07-120
 */
@Data
public class PageParam {

    public static final Long DEFAULT_PAGE_NUM = 1L;
    public static final Long DEFAULT_PAGE_SIZE = 10L;

    public static final Long TOP_PAGE_SIZE = 4L;


    @ApiModelProperty("请求页数，从1开始计数")
    private long pageNum;

    @ApiModelProperty("请求页大小，默认为 10")
    private long pageSize;

    public static PageParam newPageInstance() {
        return newPageInstance(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
    }

    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }

    public long getLimit() {
        return pageSize;
    }

    public static PageParam newPageInstance(Integer pageNum, Integer pageSize) {
        return newPageInstance(pageNum.longValue(), pageSize.longValue());
    }

    public static PageParam newPageInstance(Long pageNum, Long pageSize) {
        if (pageNum == null || pageSize == null) {
            return null;
        }

        final PageParam pageParam = new PageParam();
        pageParam.pageNum = pageNum;
        pageParam.pageSize = pageSize;
        return pageParam;
    }

    public static String getLimitSql(PageParam pageParam) {
        return String.format("limit %s,%s", pageParam.getOffset(), pageParam.getLimit());
    }

    /**
     * 自动初始化
     */
    public void autoInit() {
        if (this.pageSize == 0) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        }
        if (this.pageNum == 0) {
            this.pageNum = DEFAULT_PAGE_NUM;
        }
    }
}
