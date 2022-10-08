package com.github.liuyueyi.forum.service.statistics.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.liueyueyi.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.liuyueyi.forum.service.statistics.repository.entity.RequestCountDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 请求计数mapper接口
 *
 * @author louzai
 * @date 2022-10-1
 */
public interface RequestCountMapper extends BaseMapper<RequestCountDO> {

    /**
     * 获取 PV 总数
     *
     * @return
     */
    Long getPvTotalCount();

    /**
     * 获取 PV 数据列表
     * @param day
     * @return
     */
    List<StatisticsDayDTO> getPvDayList(@Param("day") Integer day);

    /**
     * 获取 UV 数据列表
     *
     * @param day
     * @return
     */
    List<StatisticsDayDTO> getUvDayList(@Param("day") Integer day);
}
