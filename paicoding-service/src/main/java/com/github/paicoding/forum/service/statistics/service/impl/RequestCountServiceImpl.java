package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.service.statistics.repository.dao.RequestCountDao;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.service.RequestCountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/24/23
 */
@Slf4j
@Service
public class RequestCountServiceImpl implements RequestCountService {
    @Autowired
    private RequestCountDao requestCountDao;

    @Override
    public RequestCountDO getRequestCount(String host) {
        return requestCountDao.getRequestCount(host, Date.valueOf(LocalDate.now()));
    }

    @Override
    public void insert(String host) {
        try {
            // 使用 INSERT ON DUPLICATE KEY UPDATE 避免并发插入导致的重复键异常
            requestCountDao.insertOrUpdate(host, Date.valueOf(LocalDate.now()));
        } catch (Exception e) {
            // 记录异常但不影响主流程
            log.error("save requestCount error for host: {}, date: {}", host, LocalDate.now(), e);
        }
    }

    @Override
    public void incrementCount(Long id) {
        requestCountDao.incrementCount(id);
    }

    @Override
    public Long getPvTotalCount() {
        return requestCountDao.getPvTotalCount();
    }

    @Override
    public List<StatisticsDayDTO> getPvUvDayList(Integer day) {
        return requestCountDao.getPvUvDayList(day);
    }

    @Override
    public long count() {
        return requestCountDao.count();
    }

    @Override
    public List<RequestCountDO> listRequestCount(PageParam pageParam) {
        return requestCountDao.listRequestCount(pageParam);
    }

}
