package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlOps;

import java.util.List;
import java.util.Map;

@Slf4j
public class OgnlFacade {
    private static final String BEAN_TYPE_PREFIX = "bean@";
    private static final int BEAN_TYPE_PREFIX_INDEX = 5;
    private OgnlContextExtend ognlContextExtend;

    private static class InnerClz {
        private static final OgnlFacade instance = new OgnlFacade();
    }

    private OgnlFacade() {
    }

    public void init(List<ServerLoader> serverLoaders) {
        this.ognlContextExtend = new OgnlContextExtend(new DefaultClassResolver(), new DefaultTypeConverter() {
            private Gson gson = new Gson();

            @Override
            public Object convertValue(Map context, Object value, Class toType) {
                if (value instanceof String && ((String) value).toLowerCase().startsWith(BEAN_TYPE_PREFIX)) {
                    try {
                        String sub = ((String) value).substring(BEAN_TYPE_PREFIX_INDEX, ((String) value).length());
                        return gson.fromJson(sub, toType);
                    } catch (Exception e) {
                        throw new IllegalInvokeArgumentException(value + " 非法的json串", e);
                    }
                }

                return OgnlOps.convertValue(value, toType);
            }
        }, new DefaultMemberAccess(true));
        this.ognlContextExtend.setRoot(this);
        this.ognlContextExtend.setServerLoaders(serverLoaders);
    }

    public static OgnlFacade instance() {
        return InnerClz.instance;
    }

    public Object execute(OgnlReqDTO req) {
        Object expression;
        try {
            expression = Ognl.parseExpression(req.getExpression());
        } catch (Exception e) {
            throw new IllegalInvokeArgumentException("非法的OGNL表达式: " + req.getExpression(), e);
        }

        try {
            return Ognl.getValue(expression, ognlContextExtend, ognlContextExtend.getRoot());
        } catch (Exception e) {
            log.error("执行失败! req: {}, e: {}", req, e);
            throw new ServerInvokedException("执行失败: " + req, e);
        }
    }
}