package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import com.google.gson.Gson;

/**
 * 默认的用于与外界打交道的EndPoint，基于Socket实现的一个简易http服务器
 * Created by @author yihui in 17:10 18/12/29.
 */
@EndPoint(order = Integer.MAX_VALUE)
public class ServletFixEndPoint implements FixEndPoint {
    private static volatile ServletFixEndPoint instance;

    public ServletFixEndPoint() {
        init(this);
    }

    public static ServletFixEndPoint getInstance() {
        return init(null);
    }

    private static ServletFixEndPoint init(ServletFixEndPoint servletFixEndPoint) {
        if (instance == null) {
            synchronized (ServletFixEndPoint.class) {
                if (instance == null) {
                    instance = servletFixEndPoint == null ? new ServletFixEndPoint() : servletFixEndPoint;
                    BasicHttpServer.startHttpServer();
                }
            }
        }

        return instance;
    }

    private Gson gson = new Gson();

    @Override
    public String call(ReflectReqDTO reqDTO) {
        //  fixme 这里改成gson进行序列化，使用fastjson序列化时，如果key为int，不会包含在双引号中
        return gson.toJson(FixEngine.instance().execute(reqDTO));
    }

    @Override
    public String ognl(OgnlReqDTO reqDTO) {
        return gson.toJson(FixEngine.instance().execute(reqDTO));
    }
}
