package com.github.liuyueyi.forum.web.admin.view;

import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.statistics.dto.StatisticsCountDTO;
import com.github.liueyueyi.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.liuyueyi.forum.service.statistics.service.StatisticsSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据统计后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@RequestMapping(path = "admin/statistics/")
public class StatisticsSettingViewController {

    @Autowired
    private StatisticsSettingService statisticsSettingService;

    static final Integer DEFAULT_DAY = 7;

    @ResponseBody
    @GetMapping(path = "queryTotal")
    public ResVo<StatisticsCountDTO> queryTotal() {
        StatisticsCountDTO statisticsCountDTO = statisticsSettingService.getStatisticsCount();
        return ResVo.ok(statisticsCountDTO);
    }

    @ResponseBody
    @GetMapping(path = "pvDayList")
    public ResVo<List<StatisticsDayDTO>> pvDayList(@RequestParam(name = "day", required = false) Integer day) {
        day = (day == null || day == 0) ? DEFAULT_DAY : day;
        List<StatisticsDayDTO> pvDayList = statisticsSettingService.getPvDayList(day);
        return ResVo.ok(pvDayList);
    }

    @ResponseBody
    @GetMapping(path = "uvDayList")
    public ResVo<List<StatisticsDayDTO>> uvDayList(@RequestParam(name = "day", required = false) Integer day) {
        day = (day == null || day == 0) ? DEFAULT_DAY : day;
        List<StatisticsDayDTO> pvDayList = statisticsSettingService.getUvDayList(day);
        return ResVo.ok(pvDayList);
    }
}
