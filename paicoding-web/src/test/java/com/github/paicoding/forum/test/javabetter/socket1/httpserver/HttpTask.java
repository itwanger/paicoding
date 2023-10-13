package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * HttpTask 类实现了 Runnable 接口，用于处理一个 HTTP 请求。
 * 当在一个线程中执行时，该任务将处理一个 Socket 连接上的 HTTP 请求，
 * 并发送响应消息。
 */
@Slf4j
public class HttpTask implements Runnable {
    // 用于处理 HTTP 请求的 Socket
    private Socket socket;

    /**
     * 构造一个新的 HttpTask，用于处理指定的 Socket 连接。
     *
     * @param socket  用于处理 HTTP 请求的 Socket
     */
    public HttpTask(Socket socket) {
        this.socket = socket;
    }

    /**
     * 实现 Runnable 接口的 run 方法，用于处理 HTTP 请求并发送响应消息。
     */
    @Override
    public void run() {
        // 检查 socket 是否为 null，如果为 null 则抛出异常
        if (socket == null) {
            throw new IllegalArgumentException("socket can't be null.");
        }

        try {
            // 获取 Socket 的输出流，并创建一个 PrintWriter 对象
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outputStream);

            // 从 Socket 的输入流中解析 HTTP 请求
            HttpMessageParser.Request httpRequest = HttpMessageParser.parse2request(socket.getInputStream());
            Object req = null;
            try {
                // 根据请求结果进行响应，省略返回
                String result;
                if (httpRequest.getUri().endsWith("ognl")) {
                    OgnlReqDTO request = parseRequest(httpRequest, OgnlReqDTO.class);
                    req = request;
                    result = ServletFixEndPoint.getInstance().ognl(request);
                } else {
                    ReflectReqDTO request = parseRequest(httpRequest, ReflectReqDTO.class);
                    req = request;
                    result = ServletFixEndPoint.getInstance().call(request);
                }
                // 根据请求和结果构建 HTTP 响应
                String httpRes = HttpMessageParser.buildResponse(httpRequest, result);

                // 将 HTTP 响应发送到客户端
                out.print(httpRes);
            } catch (Exception e) {
                // 如果发生异常，构建一个包含异常信息的 HTTP 响应
                String httpRes = HttpMessageParser.buildResponse(httpRequest, e.toString());
                out.print(httpRes);
            }

            // 刷新输出流，确保响应消息被发送
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭 Socket 连接
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param httpRequest
     * @return
     */
    private <T> T parseRequest(HttpMessageParser.Request httpRequest, Class<T> clz) {
        T request = JSONUtil.toBean(httpRequest.getMessage(), clz);
        if (log.isDebugEnabled()) {
            log.debug("current request: {}", request);
        }
        return request;
    }
}



