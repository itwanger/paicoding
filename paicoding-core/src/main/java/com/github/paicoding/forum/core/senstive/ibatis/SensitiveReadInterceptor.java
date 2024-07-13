package com.github.paicoding.forum.core.senstive.ibatis;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;


/**
 * 敏感词替换拦截器，这里主要是针对从db中读取的数据进行敏感词处理 （如果需要在写入db时，进行脱敏如加密，也可以使用类似的方式来实现）
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {java.sql.Statement.class})
})
@Component
@Slf4j
public class SensitiveReadInterceptor implements Interceptor {

    private static final String MAPPED_STATEMENT = "mappedStatement";

    @Autowired
    private SensitiveService sensitiveService;

    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final List<Object> results = (List<Object>) invocation.proceed();

        if (results.isEmpty()) {
            return results;
        }

        final ResultSetHandler statementHandler = realTarget(invocation.getTarget());
        final MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        final MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPED_STATEMENT);

        Optional firstOpt = results.stream().filter(Objects::nonNull).findFirst();
        if (!firstOpt.isPresent()) {
            return results;
        }
        Object firstObject = firstOpt.get();
        // 找到需要进行敏感词替换的数据库实体类的成员信息
        SensitiveObjectMeta sensitiveObjectMeta = findSensitiveObjectMeta(firstObject);

        // 执行替换的敏感词替换
        replaceSensitiveResults(results, mappedStatement, sensitiveObjectMeta);
        return results;
    }

    /**
     * 执行具体的敏感词替换
     *
     * @param results
     * @param mappedStatement
     * @param sensitiveObjectMeta
     */
    private void replaceSensitiveResults(Collection<Object> results, MappedStatement mappedStatement, SensitiveObjectMeta sensitiveObjectMeta) {
        for (Object obj : results) {
            if (sensitiveObjectMeta.getSensitiveFieldMetaList() == null) {
                continue;
            }

            final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(obj);
            sensitiveObjectMeta.getSensitiveFieldMetaList().forEach(i -> {
                Object value = objMetaObject.getValue(StringUtils.isBlank(i.getBindField()) ? i.getName() : i.getBindField());
                if (value == null) {
                    return;
                } else if (value instanceof String) {
                    String strValue = (String) value;
                    String processVal = sensitiveService.replace(strValue);
                    objMetaObject.setValue(i.getName(), processVal);
                } else if (value instanceof Collection) {
                    Collection listValue = (Collection) value;
                    if (CollectionUtils.isNotEmpty(listValue)) {
                        Optional firstValOpt = listValue.stream().filter(Objects::nonNull).findFirst();
                        if (firstValOpt.isPresent()) {
                            SensitiveObjectMeta valSensitiveObjectMeta = findSensitiveObjectMeta(firstValOpt.get());
                            if (Boolean.TRUE.equals(valSensitiveObjectMeta.getEnabledSensitiveReplace()) && CollectionUtils.isNotEmpty(valSensitiveObjectMeta.getSensitiveFieldMetaList())) {
                                replaceSensitiveResults(listValue, mappedStatement, valSensitiveObjectMeta);
                            }
                        }
                    }
                } else if (!ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
                    // 对于非基本类型的，需要对其内部进行敏感词替换
                    SensitiveObjectMeta valSensitiveObjectMeta = findSensitiveObjectMeta(value);
                    if (Boolean.TRUE.equals(valSensitiveObjectMeta.getEnabledSensitiveReplace()) && CollectionUtils.isNotEmpty(valSensitiveObjectMeta.getSensitiveFieldMetaList())) {
                        replaceSensitiveResults(newArrayList(value), mappedStatement, valSensitiveObjectMeta);
                    }
                }
            });
        }
    }

    /**
     * 查询对象中，携带有 @SensitiveField 的成员，进行敏感词替换
     *
     * @param firstObject 待查询的对象
     * @return 返回对象的敏感词元数据
     */
    private SensitiveObjectMeta findSensitiveObjectMeta(Object firstObject) {
        SensitiveMetaCache.computeIfAbsent(firstObject.getClass().getName(), s -> {
            Optional<SensitiveObjectMeta> sensitiveObjectMetaOpt = SensitiveObjectMeta.buildSensitiveObjectMeta(firstObject);
            return sensitiveObjectMetaOpt.orElse(null);
        });

        return SensitiveMetaCache.get(firstObject.getClass().getName());
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }
}
