package com.github.paicoding.forum.core.senstive.ibatis;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.senstive.ano.SensitiveField;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;


/**
 * 敏感词服务类
 *
 * @author YiHui
 * @date 2023/8/9
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

        SensitiveObjectMeta sensitiveObjectMeta = findSensitiveObjectMeta(firstObject);
        replaceSensitiveResults(results, mappedStatement, sensitiveObjectMeta);
        return results;
    }

    private void replaceSensitiveResults(List<Object> results, MappedStatement mappedStatement, SensitiveObjectMeta sensitiveObjectMeta) {
        for (Object obj : results) {
            if (sensitiveObjectMeta.getSensitiveFieldMetaList() == null) {
                continue;
            }

            final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(obj);
            sensitiveObjectMeta.getSensitiveFieldMetaList().forEach(i -> {
                Object value = objMetaObject.getValue(i.getName());

                if (nonNull(value)) {
                    if (value instanceof String) {
                        String strValue = (String) value;
                        String processVal = sensitiveService.replace(strValue);
                        objMetaObject.setValue(i.getName(), processVal);
                    } else if (value instanceof List) {
                        List listValue = (List) value;
                        if (CollectionUtils.isNotEmpty(listValue)) {
                            Optional firstValOpt = listValue.stream().filter(Objects::nonNull).findFirst();
                            if (firstValOpt.isPresent()) {
                                SensitiveObjectMeta valSensitiveObjectMeta = findSensitiveObjectMeta(firstValOpt.get());
                                if (Boolean.TRUE.equals(valSensitiveObjectMeta.getEnabledSensitiveReplace()) && CollectionUtils.isNotEmpty(valSensitiveObjectMeta.getSensitiveFieldMetaList())) {
                                    replaceSensitiveResults(listValue, mappedStatement, valSensitiveObjectMeta);
                                }
                            }
                        }
                    } else if (nonNull(value.getClass().getAnnotation(SensitiveField.class))) {
                        SensitiveObjectMeta valSensitiveObjectMeta = findSensitiveObjectMeta(value);
                        if (Boolean.TRUE.equals(valSensitiveObjectMeta.getEnabledSensitiveReplace()) && CollectionUtils.isNotEmpty(valSensitiveObjectMeta.getSensitiveFieldMetaList())) {
                            replaceSensitiveResults(newArrayList(value), mappedStatement, valSensitiveObjectMeta);
                        }
                    }
                }
            });

            sensitiveObjectMeta.getSensitiveBindFieldMetaList().forEach(i -> {
                String value = (String) objMetaObject.getValue(i.getBindField());
                if (nonNull(value)) {
                    String processVal = sensitiveService.replace(value);
                    objMetaObject.setValue(i.getName(), processVal);
                }
            });
        }
    }

    private SensitiveObjectMeta findSensitiveObjectMeta(Object firstObject) {
        SensitiveMetaCache.computeIfAbsent(firstObject.getClass().getName(), s -> {
            Optional<SensitiveObjectMeta> sensitiveObjectMetaOpt = SensitiveObjectMeta.generateSensitiveObjectMeta(firstObject);
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
