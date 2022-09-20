package com.github.liuyueyi.forum.web.front.backstage.view;

import com.github.liuyueyi.forum.service.statistics.service.StatisticsSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "backstage/statistics/")
public class StatisticsSettingViewController {

    @Autowired
    private StatisticsSettingService statisticsSettingService;
}
