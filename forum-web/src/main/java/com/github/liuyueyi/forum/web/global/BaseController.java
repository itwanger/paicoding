package com.github.liuyueyi.forum.web.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全局属性配置
 *
 * @author YiHui
 * @date 2022/9/3
 */
public class BaseController {
    @Autowired
    protected GlobalInitService globalInitService;

    /**
     * 全局属性配置
     *
     * @param model
     */
    @ModelAttribute
    public void globalAttr(Model model) {
        model.addAttribute("global", globalInitService.globalAttr());
    }
}
