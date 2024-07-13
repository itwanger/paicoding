package com.github.paicoding.forum.core.util;

import com.github.hui.quick.plugin.base.file.FileWriteUtil;
import com.github.paicoding.forum.core.region.IpRegionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class IpUtil {
    private static final String UNKNOWN = "unKnown";

    public static final String DEFAULT_IP = "127.0.0.1";

    /**
     * 获取本机所有网卡信息   得到所有IP信息
     *
     * @return Inet4Address>
     */
    private static List<Inet4Address> getLocalIp4AddressFromNetworkInterface() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(1);

        // 所有网络接口信息
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (ObjectUtils.isEmpty(networkInterfaces)) {
            return addresses;
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            //滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
            if (!isValidInterface(networkInterface)) {
                continue;
            }

            // 所有网络接口的IP地址信息
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                // 判断是否是IPv4，并且内网地址并过滤回环地址.
                if (isValidAddress(inetAddress)) {
                    addresses.add((Inet4Address) inetAddress);
                }
            }
        }
        return addresses;
    }

    /**
     * 过滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
     *
     * @param ni 网卡
     * @return 如果满足要求则true，否则false
     */
    private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
    }

    /**
     * 判断是否是IPv4，并且内网地址并过滤回环地址.
     */
    private static boolean isValidAddress(InetAddress address) {
        return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
    }

    /**
     * 通过Socket 唯一确定一个IP
     * 当有多个网卡的时候，使用这种方式一般都可以得到想要的IP。甚至不要求外网地址8.8.8.8是可连通的
     *
     * @return Inet4Address>
     */
    private static Optional<Inet4Address> getIpBySocket() throws SocketException {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            if (socket.getLocalAddress() instanceof Inet4Address) {
                return Optional.of((Inet4Address) socket.getLocalAddress());
            }
        } catch (UnknownHostException networkInterfaces) {
            throw new RuntimeException(networkInterfaces);
        }
        return Optional.empty();
    }

    private static String LOCAL_IP = null;

    /**
     * 获取本地IPv4地址
     *
     * @return Inet4Address>
     */
    public static String getLocalIp4Address() throws SocketException {
        if (LOCAL_IP != null) {
            return LOCAL_IP;
        }

        final List<Inet4Address> inet4Addresses = getLocalIp4AddressFromNetworkInterface();
        if (inet4Addresses.size() != 1) {
            final Optional<Inet4Address> ipBySocketOpt = getIpBySocket();
            LOCAL_IP = ipBySocketOpt.map(Inet4Address::getHostAddress).orElseGet(() -> inet4Addresses.isEmpty() ? DEFAULT_IP : inet4Addresses.get(0).getHostAddress());
            return LOCAL_IP;
        }
        LOCAL_IP =  inet4Addresses.get(0).getHostAddress();
        return LOCAL_IP;
    }


    /**
     * 获取请求来源的ip地址
     *
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        try {
            String xIp = request.getHeader("X-Real-IP");
            String xFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.isNotEmpty(xFor) && !UNKNOWN.equalsIgnoreCase(xFor)) {
                //多次反向代理后会有多个ip值，第一个ip才是真实ip
                int index = xFor.indexOf(",");
                if (index != -1) {
                    return xFor.substring(0, index);
                } else {
                    return xFor;
                }
            }
            xFor = xIp;
            if (StringUtils.isNotEmpty(xFor) && !UNKNOWN.equalsIgnoreCase(xFor)) {
                return xFor;
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isBlank(xFor) || UNKNOWN.equalsIgnoreCase(xFor)) {
                xFor = request.getRemoteAddr();
            }

            if ("localhost".equalsIgnoreCase(xFor) || "127.0.0.1".equalsIgnoreCase(xFor) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(xFor)) {
                return getLocalIp4Address();
            }
            return xFor;
        } catch (Exception e) {
            log.error("get remote ip error!", e);
            return "x.0.0.1";
        }
    }

    /**
     * ip库路径
     * <a href="https://github.com/lionsoul2014/ip2region/tree/master/binding/java"/>
     */
    private static final String dbPath = "data/ip2region.xdb";
    private static String tmpPath = null;
    private static volatile byte[] vIndex = null;

    private static void initVIndex() {
        if (vIndex == null) {
            synchronized (IpUtil.class) {
                if (vIndex == null) {
                    try {
                        String file = IpUtil.class.getClassLoader().getResource(dbPath).getFile();
                        if (file.contains(".jar!")) {
                            // RandomAccessFile 无法加载jar包内的文件，因此我们将资源拷贝到临时目录下
                            FileWriteUtil.FileInfo tmpFile = new FileWriteUtil.FileInfo("/tmp/data", "ip2region", "xdb");
                            tmpPath = tmpFile.getAbsFile();
                            if (!new File(tmpPath).exists()) {
                                // fixme 如果已经存在，则无需继续拷贝，因此当ip库变更之后，需要手动去删除 临时目录下生成的文件，避免出现更新不生效；更好的方式则是比较两个文件的差异性；当不同时，也需要拷贝过去
                                FileWriteUtil.saveFileByStream(IpUtil.class.getClassLoader().getResourceAsStream(dbPath), tmpFile);
                            }
                        } else {
                            tmpPath = file;
                        }
                        vIndex = Searcher.loadVectorIndexFromFile(tmpPath);
                    } catch (Exception e) {
                        log.error("failed to load vector index from {}\n", dbPath, e);
                    }
                }
            }
        }
    }

    /**
     * 根据ip查询对应的地址: 国家|区域|省份|城市|ISP
     * 若对应的位置不存在值，则为0
     *
     * @param ip
     * @return
     */
    public static IpRegionInfo getLocationByIp(String ip) {
        // 2、使用全局的 vIndex 创建带 VectorIndex 缓存的查询对象。
        initVIndex();
        Searcher searcher = null;
        try {
            searcher = Searcher.newWithVectorIndex(tmpPath, vIndex);
            return new IpRegionInfo(searcher.search(ip));
        } catch (Exception e) {
            log.error("failed to create vectorIndex cached searcher with {}: {}\n", dbPath, e);
            return new IpRegionInfo("");
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (IOException e) {
                    log.error("failed to close file:{}\n", dbPath, e);
                }
            }
        }
    }
}
