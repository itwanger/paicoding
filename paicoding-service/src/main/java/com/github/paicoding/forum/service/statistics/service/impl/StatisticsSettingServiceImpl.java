package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsCountDTO;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.service.article.service.ArticleSettingService;
import com.github.paicoding.forum.service.statistics.repository.dao.RequestCountDao;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
import com.github.paicoding.forum.service.user.service.UserSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
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
    private RequestCountDao requestCountDao;

    @Autowired
    private UserSettingService userSettingService;

    @Autowired
    private ArticleSettingService articleSettingService;

    @Override
    public void saveRequestCount(String host) {
        RequestCountDO requestCountDO = requestCountDao.getRequestCount(host, Date.valueOf(LocalDate.now()));
        if (requestCountDO == null) {
            try {
                requestCountDO = new RequestCountDO();
                requestCountDO.setHost(host);
                requestCountDO.setCnt(1);
                requestCountDO.setDate(Date.valueOf(LocalDate.now()));
                requestCountDao.save(requestCountDO);
            } catch (Exception e) {
                // fixme 非数据库原因得异常，则大概率是0点的并发访问，导致同一天写入多条数据的问题； 可以考虑使用分布式锁来避免
                // todo 后续考虑使用redis自增来实现pv计数统计
                log.error("save requestCount error: {}", requestCountDO, e);
            }
        } else {
            // 计数无需精确值，不用考虑并发情况
            requestCountDO.setCnt(requestCountDO.getCnt() + 1);
            requestCountDao.updateById(requestCountDO);
        }
    }

    @Override
    public StatisticsCountDTO getStatisticsCount() {
        Integer userCount = userSettingService.getUserCount();
        Integer articleCount = articleSettingService.getArticleCount();
        Integer pvCount = requestCountDao.getPvTotalCount();

        StatisticsCountDTO totalCount = new StatisticsCountDTO();
        totalCount.setUserCount(userCount);
        totalCount.setArticleCount(articleCount);
        totalCount.setPvCount(pvCount);
        return totalCount;
    }

    @Override
    public List<StatisticsDayDTO> getPvDayList(Integer day) {
        return requestCountDao.getPvDayList(day);
    }

    @Override
    public List<StatisticsDayDTO> getUvDayList(Integer day) {
        return requestCountDao.getUvDayList(day);
    }
}
