package com.github.paicoding.forum.service.chatai.service.impl.xunfei;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 主体来自讯飞官方java sdk
 *
 * <a href="https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E"/>
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Setter
@Component
public class XunFeiIntegration {

    @Autowired
    private XunFeiConfig xunFeiConfig;

    @Getter
    private OkHttpClient okHttpClient;

    @PostConstruct
    public void init() {
        okHttpClient = new OkHttpClient.Builder().build();
    }

    public String buildXunFeiUrl() {
        try {
            String authUrl = getAuthorizationUrl(xunFeiConfig.hostUrl, xunFeiConfig.apiKey, xunFeiConfig.apiSecret);
            String url = authUrl.replace("https://", "wss://").replace("http://", "ws://");
            return url;
        } catch (Exception e) {
            log.warn("讯飞url创建失败", e);
            return null;
        }
    }

    /**
     * 构建授权url
     *
     * @param hostUrl
     * @param apikey
     * @param apisecret
     * @return
     * @throws Exception
     */
    public String getAuthorizationUrl(String hostUrl, String apikey, String apisecret) throws Exception {
        //获取host
        URL url = new URL(hostUrl);
        //获取鉴权时间 date
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        //获取signature_origin字段
        String builder = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        //获得signatue
        Charset charset = StandardCharsets.UTF_8;
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec sp = new SecretKeySpec(apisecret.getBytes(charset), "hmacsha256");
        mac.init(sp);
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(builder.getBytes(charset)));
        //获得 authorization_origin
        String authorizationOrigin = String.format("api_key=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"", apikey, "hmac-sha256", "host date request-line", signature);
        //获得authorization
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(charset));
        //获取httpurl
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().
                addQueryParameter("authorization", authorization).
                addQueryParameter("date", date).
                addQueryParameter("host", url.getHost()).
                build();
        return httpUrl.toString();
    }

    public String buildSendMsg(String uid, String question) {
        JsonObject frame = new JsonObject();
        JsonObject header = new JsonObject();
        JsonObject chat = new JsonObject();
        JsonObject parameter = new JsonObject();
        JsonObject payload = new JsonObject();
        JsonObject message = new JsonObject();
        JsonObject text = new JsonObject();
        JsonArray ja = new JsonArray();

        //填充header
        header.addProperty("app_id", xunFeiConfig.appId);
        header.addProperty("uid", uid);
        //填充parameter
        chat.addProperty("domain", xunFeiConfig.domain);
        chat.addProperty("random_threshold", 0);
        chat.addProperty("max_tokens", 1024);
        chat.addProperty("auditing", "default");
        parameter.add("chat", chat);
        //填充payload
        text.addProperty("role", "user");
        text.addProperty("content", question);
        ja.add(text);
        message.add("text", ja);
        payload.add("message", message);
        frame.add("header", header);
        frame.add("parameter", parameter);
        frame.add("payload", payload);
        return frame.toString();
    }

    public ResponseData parse2response(String text) {
        return JsonUtil.toObj(text, ResponseData.class);
    }


    @Component
    @ConfigurationProperties(prefix = "xunfei")
    @Data
    public static class XunFeiConfig {
        public String hostUrl = "http://spark-api.xf-yun.com/v1.1/chat";
        public String appId = "";
        public String apiKey = "";
        public String apiSecret = "";
        // 指定访问的领域,general指向V1.5版本 generalv2指向V2版本。注意：不同的取值对应的url也不一样！
        public String domain = "general";
    }

    @Data
    public static class ResponseData {
        private Header header;
        private Payload payload;

        public boolean successReturn() {
            return header != null && header.code == 0;
        }

        /**
         * 首次返回结果
         *
         * @return
         */
        public boolean firstResonse() {
            return header != null && "0".equalsIgnoreCase(header.status);
        }

        /**
         * 判断是否是最后一次返回的结果
         *
         * @return
         */
        public boolean endResponse() {
            return header != null && "2".equalsIgnoreCase(header.status);
        }
    }

    @Data
    public static class Header {
        /**
         * 错误码，0表示正常，非0表示出错；详细释义可在接口说明文档最后的错误码说明了解
         */
        private int code;
        /**
         * 会话是否成功的描述信息
         */
        private String message;
        /**
         * 会话的唯一id，用于讯飞技术人员查询服务端会话日志使用,出现调用错误时建议留存该字段
         */
        private String sid;
        /**
         * 会话状态，取值为[0,1,2]；0代表首次结果；1代表中间结果；2代表最后一个结果
         */
        private String status;
    }

    @Data
    public static class Payload {
        private Choices choices;
        private Usage usage;
    }

    @Data
    public static class Choices {
        /**
         * 文本响应状态，取值为[0,1,2]; 0代表首个文本结果；1代表中间文本结果；2代表最后一个文本结果
         */
        private int status;
        /**
         * 返回的数据序号，取值为[0,9999999]
         */
        private int seq;

        private List<ChoicesText> text;
    }

    @Data
    public static class ChoicesText {
        /**
         * 结果序号，取值为[0,10]; 当前为保留字段，开发者可忽略
         */
        private int index;
        /**
         * 角色标识，固定为assistant，标识角色为AI
         */
        private String role;
        /**
         * AI的回答内容
         */
        private String content;
    }

    @Data
    public static class Usage {
        private UsageText text;
    }

    @Data
    public static class UsageText {
        /**
         * 保留字段，可忽略
         */
        @JsonAlias("question_tokens")
        private int questionTokens;
        /**
         * 包含历史问题的总tokens大小
         */
        @JsonAlias("prompt_tokens")
        private int promptTokens;
        /**
         * 回答的tokens大小
         */
        @JsonAlias("completion_tokens")
        private int completionTokens;

        /**
         * TODO: 官网api改了，多了这么个字段，还没看是什么含义
         */
        @JsonAlias("search_prompt_tokens")
        private int searchPromptTokens;

        /**
         * prompt_tokens和completion_tokens的和，也是本次交互计费的tokens大小
         */
        @JsonAlias("total_tokens")
        private int totalTokens;
    }
}
