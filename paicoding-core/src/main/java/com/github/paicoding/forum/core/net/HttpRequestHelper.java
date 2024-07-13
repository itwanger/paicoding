package com.github.paicoding.forum.core.net;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 请求工具类
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
public class HttpRequestHelper {
    public static final String CHROME_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

    /**
     * rest template
     */
    private static LoadingCache<String, RestTemplate> restTemplateMap;

    static {
        restTemplateMap = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, RestTemplate>() {
                    @Override
                    public RestTemplate load(String key) throws Exception {
                        return buildRestTemplate();
                    }
                });
    }

    /**
     * build rest template
     *
     * @return
     */
    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    @Scheduled(cron = "0 0 0/1 * * ?")
    public static void refreshRestTemplate() {
        restTemplateMap.cleanUp();
    }


    /**
     * 文件上传
     *
     * @param url       上传url
     * @param paramName 参数名
     * @param fileName  上传的文件名
     * @param bytes     上传文件流
     * @return
     */
    public static String upload(String url, String paramName, String fileName, byte[] bytes) {
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //设置请求体，注意是LinkedMultiValueMap
        ByteArrayResource fileSystemResource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        // post的文件
        form.add(paramName, fileSystemResource);

        //用HttpEntity封装整个请求报文
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
        String threadName = Thread.currentThread().getName();
        RestTemplate restTemplate = restTemplateMap.getUnchecked(threadName);
        HttpEntity<String> res = restTemplate.postForEntity(url, files, String.class);
        return res.getBody();
    }

    /**
     * @param url
     * @param method
     * @param params
     * @param headers
     * @param responseClass
     * @param <R>
     * @return
     */
    public static <R> R fetchContentWithProxy(String url, HttpMethod method, Map<String, String> params,
                                              HttpHeaders headers, Class<R> responseClass) {
        R result = fetchContent(url, method, params, headers, responseClass, true);
        if (result == null) {
            return fetchContent(url, method, params, headers, responseClass, false);
        }

        return result;
    }

    /**
     * @param url
     * @param method
     * @param params
     * @param headers
     * @param responseClass
     * @param <R>
     * @return
     */
    public static <R> R fetchContentWithoutProxy(String url, HttpMethod method, Map<String, String> params,
                                                 HttpHeaders headers, Class<R> responseClass) {
        return fetchContent(url, method, params, headers, responseClass, false);
    }

    /**
     * fetch content
     *
     * @param url
     * @param method
     * @param params
     * @param headers
     * @param responseClass
     * @param useProxy
     * @param <R>
     * @return
     */
    private static <R> R fetchContent(String url, HttpMethod method,
                                      Map<String, String> params,
                                      HttpHeaders headers,
                                      Class<R> responseClass, boolean useProxy) {
        String threadName = Thread.currentThread().getName();
        RestTemplate restTemplate = restTemplateMap.getUnchecked(threadName);

        String host = "";
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            log.error("Failed to parse url:{}", url);
        }

        if (useProxy) {
            ensureProxy(restTemplate, host);
        } else {
            ensureProxy(restTemplate, "");
        }

        return fetchContentInternal(restTemplate, url, method, params, headers, responseClass);
    }

    /**
     * ensure proxy
     *
     * @param restTemplate
     * @param host
     */
    private static void ensureProxy(RestTemplate restTemplate, String host) {
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        if (StringUtils.isBlank(host)) {
            factory.setProxy(null);
            return;
        }

        Optional.ofNullable(ProxyCenter.loadProxy(host)).ifPresent(factory::setProxy);
    }

    /**
     * fetch content
     *
     * @param restTemplate
     * @param url
     * @param method
     * @param params
     * @param headers
     * @param responseClass
     * @param <R>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <R> R fetchContentInternal(RestTemplate restTemplate, String url, HttpMethod method,
                                              Map<String, String> params, HttpHeaders headers, Class<R> responseClass) {
        ResponseEntity<R> responseEntity;
        try {
            SslUtils.ignoreSSL();
            if (method.equals(HttpMethod.GET)) {
                HttpEntity<?> entity = new HttpEntity<>(headers);
                responseEntity = restTemplate.exchange(url, method, entity, responseClass, params);
            } else {
                MultiValueMap<String, String> args = new LinkedMultiValueMap<>();
                args.setAll(params);
                HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(args, headers);
                responseEntity = restTemplate.exchange(url, method, entity, responseClass);
            }
        } catch (RestClientResponseException e) {
            String res = e.getResponseBodyAsString();
            if (String.class.isAssignableFrom(responseClass)) {
                return (R) res;
            } else if (JSONObject.class.isAssignableFrom(responseClass)) {
                return (R) JSONObject.parseObject(res);
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch content, url:{}, params:{}, exception:{}", url, params, e.getMessage());
            return null;
        }

        return responseEntity.getBody();
    }

    public static <R> R fetchByRequestBody(String url, Map<String, Object> params, HttpHeaders headers,
                                           Class<R> responseClass) {
        ResponseEntity<R> responseEntity;
        try {
            String threadName = Thread.currentThread().getName();
            RestTemplate restTemplate = restTemplateMap.getUnchecked(threadName);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, responseClass);
        } catch (Exception e) {
            log.warn("Failed to fetch content, url:{}, params:{}, exception:{}", url, params, e.getMessage());
            return null;
        }

        if (responseEntity != null) {
            return responseEntity.getBody();
        }

        return null;
    }
}