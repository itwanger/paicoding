package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpMessageParser {
    /**
     * 根据标准的HTTP协议，解析请求行
     *
     * @param reader
     * @param request
     */
    private static void decodeRequestLine(BufferedReader reader, Request request) throws IOException {
        String[] strs = StringUtils.split(reader.readLine(), " ");
        assert strs.length == 3;
        request.setMethod(strs[0]);
        request.setUri(strs[1]);
        request.setVersion(strs[2]);
    }

    /**
     * 根据标准 HTTP 协议，解析请求头
     *
     * @param reader  读取请求头的 BufferedReader 对象
     * @param request 存储请求信息的 Request 对象
     * @throws IOException 当读取请求头信息时发生 I/O 异常时，将抛出该异常
     */
    private static void decodeRequestHeader(BufferedReader reader, Request request) throws IOException {
        // 创建一个 Map 对象，用于存储请求头信息
        Map<String, String> headers = new HashMap<>(16);
        // 读取请求头信息，每行都是一个键值对，以空行结束
        String line = reader.readLine();
        String[] kv;
        while (!"".equals(line)) {
            // 将每行请求头信息按冒号分隔，分别作为键和值存入 Map 中
            kv = StringUtils.split(line, ":");
            assert kv.length == 2;
            headers.put(kv[0].trim(), kv[1].trim());
            line = reader.readLine();
        }
        // 将解析出来的请求头信息存入 Request 对象中
        request.setHeaders(headers);
    }

    /**
     * 根据标注HTTP协议，解析正文
     *
     * @param reader    输入流读取器，用于读取请求中的数据
     * @param request   Request 对象，表示 HTTP 请求
     * @throws IOException 当发生 I/O 错误时抛出
     */
    private static void decodeRequestMessage(BufferedReader reader, Request request) throws IOException {
        // 从请求头中获取 Content-Length，如果没有，则默认为 0
        int contentLen = Integer.parseInt(request.getHeaders().getOrDefault("Content-Length", "0"));

        // 如果 Content-Length 为 0，表示没有请求正文，直接返回。
        // 例如 GET 和 OPTIONS 请求通常不包含请求正文
        if (contentLen == 0) {
            return;
        }

        // 根据 Content-Length 创建一个字符数组来存储请求正文
        char[] message = new char[contentLen];

        // 使用 BufferedReader 读取请求正文
        reader.read(message);

        // 将字符数组转换为字符串，并将其设置为 Request 对象的 message
        request.setMessage(new String(message));
    }

    /**
     * HTTP 请求可以分为三部分：
     * 1. 请求行：包括请求方法、URI 和 HTTP 协议版本
     * 2. 请求头：从第二行开始，直到一个空行为止
     * 3. 消息正文：紧跟在空行后的所有内容，长度由请求头中的 Content-Length 决定
     *
     * 本方法将 InputStream 中的 HTTP 请求数据解析为一个 Request 对象
     *
     * @param reqStream  包含 HTTP 请求数据的输入流
     * @return           一个表示 HTTP 请求的 Request 对象
     * @throws IOException 当发生 I/O 错误时抛出
     */
    public static Request parse2request(InputStream reqStream) throws IOException {
        // 使用 BufferedReader 和 InputStreamReader 读取输入流中的数据
        BufferedReader httpReader = new BufferedReader(new InputStreamReader(reqStream, "UTF-8"));

        // 创建一个新的 Request 对象
        Request httpRequest = new Request();

        // 解析请求行并设置到 Request 对象中
        decodeRequestLine(httpReader, httpRequest);

        // 解析请求头并设置到 Request 对象中
        decodeRequestHeader(httpReader, httpRequest);

        // 解析消息正文并设置到 Request 对象中
        decodeRequestMessage(httpReader, httpRequest);

        // 返回解析后的 Request 对象
        return httpRequest;
    }

    @Data
    public static class Request {
        /**
         * 请求方法 GET/POST/PUT/DELETE/OPTION...
         */
        private String method;
        /**
         * 请求的uri
         */
        private String uri;
        /**
         * HTTP版本
         */
        private String version;

        /**
         * 请求头
         */
        private Map<String, String> headers;

        /**
         * 请求参数相关
         */
        private String message;
    }

    /**
     * Response 类表示一个 HTTP 响应，包括版本、状态码、状态信息、响应头和响应正文。
     */
    @Data
    public static class Response {
        private String version;
        private int code;
        private String status;
        private Map<String, String> headers;
        private String message;
    }

    /**
     * 根据给定的 Request 对象和响应字符串构建一个 HTTP 响应。
     *
     * @param request   用于构建响应的 Request 对象
     * @param response  响应字符串
     * @return          一个表示 HTTP 响应的字符串
     */
    public static String buildResponse(Request request, String response) {
        // 创建一个新的 Response 对象，并设置版本、状态码和状态信息
        Response httpResponse = new Response();
        httpResponse.setCode(200);
        httpResponse.setStatus("ok");
        httpResponse.setVersion(request.getVersion());

        // 设置响应头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Content-Length", String.valueOf(response.getBytes().length));
        httpResponse.setHeaders(headers);

        // 设置响应正文
        httpResponse.setMessage(response);

        // 构建响应字符串
        StringBuilder builder = new StringBuilder();
        buildResponseLine(httpResponse, builder);
        buildResponseHeaders(httpResponse, builder);
        buildResponseMessage(httpResponse, builder);
        return builder.toString();
    }

    /**
     * 构建响应行，包括版本、状态码和状态信息。
     *
     * @param response      用于构建响应行的 Response 对象
     * @param stringBuilder 用于拼接响应字符串的 StringBuilder 对象
     */
    private static void buildResponseLine(Response response, StringBuilder stringBuilder) {
        stringBuilder.append(response.getVersion()).append(" ").append(response.getCode()).append(" ")
                .append(response.getStatus()).append("\n");
    }

    /**
     * 构建响应头。
     *
     * @param response      用于构建响应头的 Response 对象
     * @param stringBuilder 用于拼接响应字符串的 StringBuilder 对象
     */
    private static void buildResponseHeaders(Response response, StringBuilder stringBuilder) {
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        stringBuilder.append("\n");
    }

    /**
     * 构建响应正文。
     *
     * @param response      用于构建响应正文的 Response 对象
     * @param stringBuilder 用于拼接响应字符串的 StringBuilder 对象
     */
    private static void buildResponseMessage(Response response, StringBuilder stringBuilder) {
        stringBuilder.append(response.getMessage());
    }






}
