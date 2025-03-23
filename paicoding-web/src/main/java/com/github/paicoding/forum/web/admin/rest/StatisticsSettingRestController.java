package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsCountDTO;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 数据统计后台
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Api(value = "全栈统计分析控制器", tags = "统计分析")
@RequestMapping(path = {"api/admin/statistics/", "admin/statistics/"})
public class StatisticsSettingRestController {

    private static final Logger log = LoggerFactory.getLogger(StatisticsSettingRestController.class);
    @Autowired
    private StatisticsSettingService statisticsSettingService;

    static final Integer DEFAULT_DAY = 7;

    @GetMapping(path = "queryTotal")
    public ResVo<StatisticsCountDTO> queryTotal() {
        StatisticsCountDTO statisticsCountDTO = statisticsSettingService.getStatisticsCount();
        return ResVo.ok(statisticsCountDTO);
    }

    @ResponseBody
    @GetMapping(path = "pvUvDayList")
    public ResVo<List<StatisticsDayDTO>> pvUvDayList(@RequestParam(name = "day", required = false) Integer day) {
        day = (day == null || day == 0) ? DEFAULT_DAY : day;
        List<StatisticsDayDTO> pvDayList = statisticsSettingService.getPvUvDayList(day);
        return ResVo.ok(pvDayList);
    }

    @GetMapping("pvUvDayDownload2Excel")
    public void pvUvDayDownload2Excel(@RequestParam(name = "day", required = false) Integer day,
                                      HttpServletResponse response) throws IOException {
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("技术派", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 获取数据
        day = (day == null || day == 0) ? DEFAULT_DAY : day;
        statisticsSettingService.download2Excel(day, response);
    }

}
