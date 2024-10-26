package com.github.paicoding.forum.service.statistics.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.repository.mapper.RequestCountMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

/**
 * 请求计数
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Repository
public class RequestCountDao extends ServiceImpl<RequestCountMapper, RequestCountDO> {

    public Long getPvTotalCount() {
        return baseMapper.getPvTotalCount();
    }

    /**
     * 获取请求数据
     *
     * @param host
     * @param date
     * @return
     */
    public RequestCountDO getRequestCount(String host, Date date) {
        return lambdaQuery()
                .eq(RequestCountDO::getHost, host)
                .eq(RequestCountDO::getDate, date)
                .one();
    }

    /**
     * 获得某一天的所有请求数据
     * @param date
     * @return
     */
    public List<RequestCountDO> getOneDayAllRequestCounts(Date date){
        return lambdaQuery()
                .eq(RequestCountDO::getDate, date)
                .list();
    }

    /**
     * 获取 PV UV 数据列表
     * @param day
     * @return
     */
    public List<StatisticsDayDTO> getPvUvDayList(Integer day) {
        return baseMapper.getPvUvDayList(day);
    }


    public void incrementCount(Long id) {
        baseMapper.incrementCount(id);
    }
}
