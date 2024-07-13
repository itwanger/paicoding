package com.github.paicoding.forum.core.dal;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.baomidou.mybatisplus.core.MybatisParameterHandler;
import com.github.paicoding.forum.core.util.DateUtil;
import com.mysql.cj.MysqlConnection;
import com.zaxxer.hikari.pool.HikariProxyConnection;
import com.zaxxer.hikari.pool.HikariProxyPreparedStatement;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * mybatis拦截器。输出sql执行情况
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}), @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
public class SqlStateInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long time = System.currentTimeMillis();
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        String sql = buildSql(statementHandler);
        Object[] args = invocation.getArgs();
        String uname = "";
        if (args[0] instanceof HikariProxyPreparedStatement) {
            HikariProxyConnection connection = (HikariProxyConnection) ((HikariProxyPreparedStatement) invocation.getArgs()[0]).getConnection();
            uname = connection.getMetaData().getUserName();
        } else if (DruidCheckUtil.hasDuridPkg()) {
            if (args[0] instanceof DruidPooledPreparedStatement) {
                Connection connection = ((DruidPooledPreparedStatement) args[0]).getStatement().getConnection();
                if (connection instanceof MysqlConnection) {
                    Properties properties = ((MysqlConnection) connection).getProperties();
                    uname = properties.getProperty("user");
                }
            }
        }

        Object rs;
        try {
            rs = invocation.proceed();
        } catch (Throwable e) {
            log.error("error sql: " + sql, e);
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - time;
            sql = this.replaceContinueSpace(sql);
            // 这个方法的总耗时
            log.info("\n\n ============= \nsql ----> {}\nuser ----> {}\ncost ----> {}\n ============= \n", sql, uname, cost);
        }

        return rs;
    }

    /**
     * 拼接sql
     *
     * @param statementHandler
     * @return
     */
    //  TODO: 修改了原本的代码，使用了代替的方案
//    private String buildSql(StatementHandler statementHandler) {
//        BoundSql boundSql = statementHandler.getBoundSql();
//        Configuration configuration = null;
//        if (statementHandler.getParameterHandler() instanceof DefaultParameterHandler) {
//            DefaultParameterHandler handler = (DefaultParameterHandler) statementHandler.getParameterHandler();
//            configuration = (Configuration) ReflectionUtils.getFieldVal(handler, "configuration", false);
//        } else if (statementHandler.getParameterHandler() instanceof MybatisParameterHandler) {
//            MybatisParameterHandler paramHandler = (MybatisParameterHandler) statementHandler.getParameterHandler();
//            configuration = ((MappedStatement) ReflectionUtils.getFieldVal(paramHandler, "mappedStatement", false)).getConfiguration();
//        }
//
//        if (configuration == null) {
//            return boundSql.getSql();
//        }
//
//        return getSql(boundSql, configuration);
//    }

    private String buildSql(StatementHandler statementHandler) {
        BoundSql boundSql = statementHandler.getBoundSql();
        Configuration configuration = null;

        if (statementHandler.getParameterHandler() instanceof DefaultParameterHandler) {
            DefaultParameterHandler handler = (DefaultParameterHandler) statementHandler.getParameterHandler();
            configuration = (Configuration) getFieldVal(handler, "configuration");
        } else if (statementHandler.getParameterHandler() instanceof MybatisParameterHandler) {
            MybatisParameterHandler paramHandler = (MybatisParameterHandler) statementHandler.getParameterHandler();
            configuration = ((MappedStatement) getFieldVal(paramHandler, "mappedStatement")).getConfiguration();
        }

        if (configuration == null) {
            return boundSql.getSql();
        }

        return getSql(boundSql, configuration);
    }

    private Object getFieldVal(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value", e);
        }
    }


    /**
     * 生成要执行的SQL命令
     *
     * @param boundSql
     * @param configuration
     * @return
     */
    private String getSql(BoundSql boundSql, Configuration configuration) {
        String sql = boundSql.getSql();
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (CollectionUtils.isEmpty(parameterMappings) || parameterObject == null) {
            return sql;
        }

        MetaObject mo = configuration.newMetaObject(boundSql.getParameterObject());
        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() == ParameterMode.OUT) {
                continue;
            }

            //参数值
            Object value;
            //获取参数名称
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) {
                //获取参数值
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass())) {
                //如果是单个值则直接赋值
                value = parameterObject;
            } else {
                value = mo.getValue(propertyName);
            }
            String param = Matcher.quoteReplacement(getParameter(value));
            sql = sql.replaceFirst("\\?", param);
        }
        sql += ";";
        return sql;
    }

    public String getParameter(Object parameter) {
        if (parameter instanceof String) {
            return "'" + parameter + "'";
        } else if (parameter instanceof Date) {
            // 日期格式化
            return "'" + DateUtil.format(DateUtil.DB_FORMAT, ((Date) parameter).getTime()) + "'";
        } else if (parameter instanceof java.util.Date) {
            // 日期格式化
            return "'" + DateUtil.format(DateUtil.DB_FORMAT, ((java.util.Date) parameter).getTime()) + "'";
        }
        return parameter.toString();
    }

    /**
     * 替换连续的空白
     *
     * @param str
     * @return
     */
    private String replaceContinueSpace(String str) {
        StringBuilder builder = new StringBuilder(str.length());
        boolean preSpace = false;
        for (int i = 0, len = str.length(); i < len; i++) {
            char ch = str.charAt(i);
            boolean isSpace = Character.isWhitespace(ch);
            if (preSpace && isSpace) {
                continue;
            }

            if (preSpace) {
                // 前面的是空白字符，当前的不是空白字符
                preSpace = false;
                builder.append(ch);
            } else if (isSpace) {
                // 当前字符为空白字符，前面的那个不是的
                preSpace = true;
                builder.append(" ");
            } else {
                // 前一个和当前字符都非空白字符
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}