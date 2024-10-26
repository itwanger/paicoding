package com.github.paicoding.forum.web.config.init;

import com.github.paicoding.forum.core.cache.CacheSyncUtil;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.service.RequestCountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * @program: pai_coding
 * @description: 引用启动时将数据库中数据同步到redis缓存中
 * @author: XuYifei
 * @create: 2024-10-25
 */

@Slf4j
@Component
public class DB2CacheInitializer implements CommandLineRunner {


    @Autowired
    private RequestCountService requestCountService;

    @Override
    public void run(String... args) throws Exception {
        this.syncRequestCount();
    }


    private boolean syncRequestCount(){
        List<RequestCountDO> requestCountDOS = requestCountService.getTodayRequestCountList();
        try {
            CacheSyncUtil.syncFromDb2Cache(RequestCountService.REQUEST_COUNT_PREFIX + Date.valueOf(LocalDate.now()), RequestCountDO.class, requestCountDOS);
        } catch (IllegalAccessException e) {
            log.error(">>>>>> !! 同步request count数据到缓存失败", e);
            return false;
        }
        log.info(">>>>>> 同步request_count到缓存成功！");
        return true;
    }
}
