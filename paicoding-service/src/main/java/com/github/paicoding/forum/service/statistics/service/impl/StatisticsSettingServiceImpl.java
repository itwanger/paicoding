package com.github.paicoding.forum.service.statistics.service.impl;

import cn.idev.excel.FastExcel;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsCountDTO;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserFootStatisticDTO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.ColumnService;
import com.github.paicoding.forum.service.statistics.converter.StatisticsConverter;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.repository.entity.StatisticsDayExcelDO;
import com.github.paicoding.forum.service.statistics.service.RequestCountService;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 数据统计后台接口
 *
 * @author louzai
 * @date 2022-09-19
 */
@Slf4j
@Service
public class StatisticsSettingServiceImpl implements StatisticsSettingService {

    @Autowired
    private RequestCountService requestCountService;

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnService columnService;

    @Autowired
    private UserFootService userFootService;

    @Autowired
    private ArticleReadService articleReadService;

    @Resource
    private AiConfig aiConfig;

    @Override
    public void saveRequestCount(String host) {
        RequestCountDO requestCountDO = requestCountService.getRequestCount(host);
        if (requestCountDO == null) {
            requestCountService.insert(host);
        } else {
            // 改为数据库直接更新
            requestCountService.incrementCount(requestCountDO.getId());
        }
    }

    @Override
    public StatisticsCountDTO getStatisticsCount() {
        // 从 user_foot 表中查询点赞数、收藏数、留言数、阅读数
        UserFootStatisticDTO userFootStatisticDTO =  userFootService.getFootCount();
        if (userFootStatisticDTO == null) {
            userFootStatisticDTO = new UserFootStatisticDTO();
        }
        return StatisticsCountDTO.builder()
                .userCount(userService.getUserCount())
                .articleCount(articleReadService.getArticleCount())
                .pvCount(requestCountService.getPvTotalCount())
                .tutorialCount(columnService.getTutorialCount())
                .commentCount(userFootStatisticDTO.getCommentCount())
                .collectCount(userFootStatisticDTO.getCollectionCount())
                .likeCount(userFootStatisticDTO.getPraiseCount())
                .readCount(userFootStatisticDTO.getReadCount())
                .starPayCount(aiConfig.getMaxNum().getStarNumber())
                .build();
    }

    @Override
    public List<StatisticsDayDTO> getPvUvDayList(Integer day) {
        return requestCountService.getPvUvDayList(day);
    }

    @Override
    public void download2Excel(Integer day, HttpServletResponse response) {
        List<StatisticsDayDTO> pvDayList = requestCountService.getPvUvDayList(day);
        // StatisticsDayDTO 转 StatisticsDayExcelDTO
        List<StatisticsDayExcelDO> excelDTOList = StatisticsConverter.convertToExcelDOList(pvDayList);

        // 使用 FastExcel 导出 Excel
        // TODO 这里可以用一个大文件，比如说 10万条数据测试一下，看看 FastExcel 的性能
        try {
            FastExcel.write(response.getOutputStream(), StatisticsDayExcelDO.class)
                    .sheet(day + "天统计")
                    .doWrite(excelDTOList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
