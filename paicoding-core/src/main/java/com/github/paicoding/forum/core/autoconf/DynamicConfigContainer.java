package com.github.paicoding.forum.core.autoconf;

import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * 自定义的配置工厂类，专门用于 ConfDot 属性配置文件的配置加载，支持从自定义的配置源获取
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Component
public class DynamicConfigContainer implements EnvironmentAware, ApplicationContextAware, CommandLineRunner {
    private ConfigurableEnvironment environment;
    private ApplicationContext applicationContext;
    /**
     * 存储db中的全局配置，优先级最高
     */
    @Getter
    public Map<String, Object> cache;

    private DynamicConfigBinder binder;

    /**
     * 配置变更的回调任务
     */
    @Getter
    private Map<Class, Runnable> refreshCallback = Maps.newHashMap();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        cache = Maps.newHashMap();
        bindBeansFromLocalCache("dbConfig", cache);
    }

    /**
     * 从db中获取全量的配置信息
     *
     * @return true 表示有信息变更; false 表示无信息变更
     */
    private boolean loadAllConfigFromDb() {
        List<Map<String, Object>> list = SpringUtil.getBean(JdbcTemplate.class).queryForList("select `key`, `value` from global_conf where deleted = 0");
        Map<String, Object> val = Maps.newHashMapWithExpectedSize(list.size());
        for (Map<String, Object> conf : list) {
            val.put(conf.get("key").toString(), conf.get("value").toString());
        }
        if (val.equals(cache)) {
            return false;
        }
        cache.clear();
        cache.putAll(val);
        return true;
    }

    private void bindBeansFromLocalCache(String namespace, Map<String, Object> cache) {
        // 将内存的配置信息设置为最高优先级
        MapPropertySource propertySource = new MapPropertySource(namespace, cache);
        environment.getPropertySources().addFirst(propertySource);
        this.binder = new DynamicConfigBinder(this.applicationContext, environment.getPropertySources());
    }

    /**
     * 配置绑定
     *
     * @param bindable
     */
    public void bind(Bindable bindable) {
        binder.bind(bindable);
    }


    /**
     * 监听配置的变更
     */
    public void reloadConfig() {
        String before = JsonUtil.toStr(cache);
        boolean toRefresh = loadAllConfigFromDb();
        if (toRefresh) {
            refreshConfig();
            log.info("配置刷新! 旧:{}, 新:{}", before, JsonUtil.toStr(cache));
        }
    }

    /**
     * 强制刷新缓存配置
     */
    public void forceRefresh() {
        loadAllConfigFromDb();
        refreshConfig();
        log.info("db配置强制刷新! {}", JsonUtil.toStr(cache));
    }

    /**
     * 支持配置的动态刷新
     */
    private void refreshConfig() {
        applicationContext.getBeansWithAnnotation(ConfigurationProperties.class).values().forEach(bean -> {
            Bindable<?> target = Bindable.ofInstance(bean).withAnnotations(AnnotationUtils.findAnnotation(bean.getClass(), ConfigurationProperties.class));
            bind(target);
            if (refreshCallback.containsKey(bean.getClass())) {
                refreshCallback.get(bean.getClass()).run();
            }
        });
    }

    /**
     * 注册db的动态配置变更
     */
    @Scheduled(fixedRate = 300000) // 5 minutes expressed in milliseconds
    private void registerConfRefreshTask() {
        try {
            log.debug("5分钟，自动更新db配置信息!");
            reloadConfig();
        } catch (Exception e) {
            log.warn("自动更新db配置信息异常!", e);
        }
    }

    /**
     * 注册配置变更的回调任务
     *
     * @param bean
     * @param run
     */
    public void registerRefreshCallback(Object bean, Runnable run) {
        refreshCallback.put(bean.getClass(), run);
    }


    /**
     * 应用启动之后，执行的动态配置初始化
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        reloadConfig();
        registerConfRefreshTask();
    }
}
