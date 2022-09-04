package com.github.liuyueyi.forum.web.global;

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
