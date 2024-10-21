package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.vo.PageParam;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 全局属性配置
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class BaseViewController {
    @Autowired
    protected GlobalInitService globalInitService;

    public PageParam buildPageParam(Long page, Long size) {
        if (page <= 0) {
            page = PageParam.DEFAULT_PAGE_NUM;
        }
        if (size == null || size > PageParam.DEFAULT_PAGE_SIZE) {
            size = PageParam.DEFAULT_PAGE_SIZE;
        }
        return PageParam.newPageInstance(page, size);
    }

}
