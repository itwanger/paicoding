package com.github.liuyueyi.forum.web.front.backstage.view;

import com.github.liuyueyi.forum.service.article.service.ColumnSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 专栏后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/column/")
public class ColumnSettingViewController {

    @Autowired
    private ColumnSettingService columnSettingService;
}
