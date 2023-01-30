package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.vo.PageParam;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 全局属性配置
 *
 * @author YiHui
 * @date 2022/9/3
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

//
//  推荐使用它替代 GlobalViewInterceptor 中的全局属性设置
//    /**
//     * 全局属性配置
//     *
//     * @param model
//     */
//    @ModelAttribute
//    public void globalAttr(Model model) {
//        model.addAttribute("global", globalInitService.globalAttr());
//    }
}
