package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.statistics.repository.dao.RequestCountDao;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.service.RequestCountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author XuYifei
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
    public List<RequestCountDO> getTodayRequestCountList() {
        return requestCountDao.getOneDayAllRequestCounts(Date.valueOf(LocalDate.now()));
    }

    @Override
    public void insert(String host) {
        RequestCountDO requestCountDO = null;
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
    }


    @Override
    public boolean insertAndSetCount(String host, Integer count, Date date) {
        RequestCountDO requestCountDO = null;
        try {
            requestCountDO = new RequestCountDO();
            requestCountDO.setHost(host);
            requestCountDO.setCnt(count);
            requestCountDO.setDate(date);
            return requestCountDao.save(requestCountDO);
        } catch (Exception e) {
            // fixme 非数据库原因得异常，则大概率是0点的并发访问，导致同一天写入多条数据的问题； 可以考虑使用分布式锁来避免
            // todo 后续考虑使用redis自增来实现pv计数统计
            log.error("save requestCount error: {}", requestCountDO, e);
            return false;
        }
    }

    @Override
    public void insertOrUpdateBatch(List<RequestCountDO> requestCountDOList) {
        requestCountDao.saveOrUpdateBatch(requestCountDOList);
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

    /**
     * 每1小时执行一次定时任务，将redis中的数据同步到db
     * 固定30分钟是为了防止0点时Date的变化导致数据不准确
     */
    @Scheduled(cron = "0 30 */12 * * ?")
    public void persist2Db() {
        Date date = Date.valueOf(LocalDate.now());
        List<RequestCountDO> requestCountDOS = new ArrayList<>();
        // 将redis的数据同步到db
        Map<String, String> requestCountDOMap = RedisClient.hGetAll(RequestCountService.REQUEST_COUNT_PREFIX + Date.valueOf(LocalDate.now()), String.class);
        requestCountDOMap.forEach((host, cnt) -> {
            RequestCountDO requestCountDO = getRequestCount(host);
            if(requestCountDO == null){
                requestCountDO = new RequestCountDO();
                requestCountDO.setHost(host);
            }
            requestCountDO.setCnt(Integer.parseInt(cnt));
            requestCountDO.setDate(date);
            requestCountDOS.add(requestCountDO);
        });
        insertOrUpdateBatch(requestCountDOS);
        log.info(">>>>> request_cnt persist2Db success, date: {} <<<<<", date);
    }
}
