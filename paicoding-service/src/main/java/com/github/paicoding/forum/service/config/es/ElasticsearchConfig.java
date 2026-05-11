package com.github.paicoding.forum.service.config.es;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

/**
 * es配置类
 *
 * @author ygl
 * @since 2023-05-25
 **/
@Slf4j
@Data
@Configuration
// 下面这个表示只有 elasticsearch.open = true 时，采进行es的配置初始化；当不使用es时，则不会实例 RestHighLevelClient
@ConditionalOnProperty(prefix = "elasticsearch", name = "open")
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchConfig {

    // 是否开启ES
    private Boolean open;

    // es host ip 地址（集群）
    private String hosts = "127.0.0.1:9200";

    // es用户名
    private String userName;

    // es密码
    private String password;

    // es 请求方式
    private String scheme = "http";

    // es集群名称
    private String clusterName;

    // es 连接超时时间
    private int connectTimeOut = 1000;

    // es socket 连接超时时间
    private int socketTimeOut = 30000;

    // es 请求超时时间
    private int connectionRequestTimeOut = 500;

    // es 最大连接数
    private int maxConnectNum = 100;

    // es 每个路由的最大连接数
    private int maxConnectNumPerRoute = 100;

    // 连接 Elasticsearch 8.x 时，7.16+ HLRC 需要开启兼容模式
    private Boolean apiCompatibilityMode = false;

    // 本地自签名 https 证书调试开关，生产环境不要打开
    private Boolean insecureTrustAllCertificates = false;


    /**
     * 如果@Bean没有指定bean的名称，那么这个bean的名称就是方法名
     */
    @Bean(name = "restHighLevelClient")
    public RestHighLevelClient restHighLevelClient() {

        // 此处为单节点es
        String host = hosts.split(":")[0];
        String port = hosts.split(":")[1];
        HttpHost httpHost = new HttpHost(host, Integer.parseInt(port), scheme);

        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);

        // 设置用户名、密码
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (StringUtils.isNotBlank(userName)) {
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        }

        // 连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeOut);
            requestConfigBuilder.setSocketTimeout(socketTimeOut);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
            return requestConfigBuilder;
        });
        // 连接数配置
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnectNum);
            httpClientBuilder.setMaxConnPerRoute(maxConnectNumPerRoute);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            if ("https".equalsIgnoreCase(scheme) && Boolean.TRUE.equals(insecureTrustAllCertificates)) {
                try {
                    SSLContext sslContext = SSLContexts.custom()
                            .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                            .build();
                    httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                } catch (Exception e) {
                    log.warn("init elasticsearch ssl trust-all config failed", e);
                }
            }
            return httpClientBuilder;
        });

        RestHighLevelClientBuilder highLevelClientBuilder = new RestHighLevelClientBuilder(builder.build());
        if (Boolean.TRUE.equals(apiCompatibilityMode)) {
            highLevelClientBuilder.setApiCompatibilityMode(true);
        }
        return highLevelClientBuilder.build();
    }
}
