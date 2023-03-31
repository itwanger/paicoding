package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceConfigurationError;

/**
 * FixEndPoint加载器，这里不适用jdk自带的加载器的原因在于我们获取
 * Created by @author yihui in 19:45 18/12/30.
 */
@Slf4j
public class EndPointLoader {
    private static final String PREFIX = "META-INF/services/";
    private static final Class<FixEndPoint> SERVICE = FixEndPoint.class;

    public static void autoLoadEndPoint() {
        String fullName = PREFIX + SERVICE.getName();
        Enumeration<URL> configs = null;
        try {
            configs = ClassLoader.getSystemResources(fullName);
        } catch (IOException x) {
            fail(SERVICE, "Error locating configuration files", x);
        }

        List<String> pending = new ArrayList<>();
        while (configs.hasMoreElements()) {
            pending.addAll(parse(configs.nextElement()));
        }

        loadFixEndPoint(pending);
    }

    private static List<String> parse(URL url) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader serviceReader = null;
        ArrayList<String> names = new ArrayList<>();
        try {
            in = url.openStream();
            serviceReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            names.add(serviceReader.readLine().trim());
        } catch (IOException x) {
            fail(SERVICE, "Error reading configuration file", x);
        } finally {
            try {
                if (serviceReader != null) {
                    serviceReader.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException y) {
                fail(SERVICE, "Error closing configuration file", y);
            }
        }
        return names;
    }


    private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

    private static void loadFixEndPoint(List<String> nameList) {
        if (nameList.isEmpty()) {
            fail(SERVICE, "no fixEndPoint selected!", null);
        }

        int lastOrder = 0;
        EndPoint endPoint, selectEndPoint = null;
        Class tmpClz = null, selectClz = null;
        for (String name : nameList) {
            try {
                tmpClz = ClassLoader.getSystemClassLoader().loadClass(name);
                if (!SERVICE.isAssignableFrom(tmpClz)) {
                    fail(tmpClz, " illegal FixEndPoint Class!", null);
                    continue;
                }
            } catch (Exception e) {
                fail(SERVICE, name + " is not FixEndPoin Instance", e);
            }

            endPoint = (EndPoint) tmpClz.getAnnotation(EndPoint.class);
            if (selectClz == null || endPoint.order() < lastOrder) {
                selectClz = tmpClz;
                selectEndPoint = endPoint;
                lastOrder = endPoint.order();
            }
        }

        if (selectEndPoint.instance()) {
            try {
                selectClz.newInstance();
            } catch (Exception e) {
                fail(selectClz, " can's instance!", e);
            }
        }

        log.info("current FixEndPoint is: {}", selectClz.getName());
    }

}
