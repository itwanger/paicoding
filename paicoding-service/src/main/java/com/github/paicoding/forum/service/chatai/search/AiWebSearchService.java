package com.github.paicoding.forum.service.chatai.search;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.web_search.WebSearchRequest;
import ai.z.openapi.service.web_search.WebSearchResp;
import ai.z.openapi.service.web_search.WebSearchResponse;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shared web-search backend for providers that do not have hosted search tools.
 */
@Slf4j
@Component
public class AiWebSearchService {
    private static final int MAX_RESULT_CHARS = 4000;

    @Autowired
    private ZhipuIntegration.ZhipuConfig zhipuConfig;

    @Value("${ai.web-search.search-engine:search_pro}")
    private String webSearchEngine;

    @Value("${ai.web-search.count:5}")
    private int webSearchCount;

    @Value("${ai.web-search.search-recency-filter:noLimit}")
    private String webSearchRecencyFilter;

    @Value("${ai.web-search.content-size:high}")
    private String webSearchContentSize;

    public boolean isAvailable() {
        return StringUtils.isNotBlank(zhipuConfig.getApiSecretKey());
    }

    public String search(String query) {
        if (StringUtils.isBlank(query)) {
            return "搜索 query 为空，未执行搜索。";
        }
        if (!isAvailable()) {
            return "未配置 zhipu.apiSecretKey，无法执行联网搜索。";
        }

        ZhipuAiClient client = buildClient();
        try {
            WebSearchRequest request = WebSearchRequest.builder()
                    .searchEngine(StringUtils.defaultIfBlank(webSearchEngine, "search_pro"))
                    .searchQuery(query)
                    .count(Math.min(50, Math.max(1, webSearchCount)))
                    .searchRecencyFilter(StringUtils.defaultIfBlank(webSearchRecencyFilter, "noLimit"))
                    .contentSize(StringUtils.defaultIfBlank(webSearchContentSize, "high"))
                    .includeImage(Boolean.FALSE)
                    .requestId("paicoding-search-" + System.currentTimeMillis())
                    .build();
            WebSearchResponse response = client.webSearch().createWebSearch(request);
            if (!response.isSuccess()) {
                String errorMsg = response.getError() == null ? null : response.getError().getMessage();
                return "联网搜索失败：" + StringUtils.defaultIfBlank(errorMsg, StringUtils.defaultIfBlank(response.getMsg(), "code=" + response.getCode()));
            }
            return formatResults(query, response);
        } catch (Exception e) {
            log.error("联网搜索失败, query={}", query, e);
            return "联网搜索失败：" + StringUtils.defaultIfBlank(e.getMessage(), "未知异常");
        } finally {
            client.close();
        }
    }

    private ZhipuAiClient buildClient() {
        return ZhipuAiClient.builder()
                .ofZHIPU()
                .apiKey(zhipuConfig.getApiSecretKey())
                .networkConfig(60, 30, 30, 30, TimeUnit.SECONDS)
                .connectionPool(4, 1, TimeUnit.SECONDS)
                .build();
    }

    private String formatResults(String query, WebSearchResponse response) {
        if (response.getData() == null || response.getData().getWebSearchResp() == null || response.getData().getWebSearchResp().isEmpty()) {
            return "没有搜索到与「" + query + "」相关的结果。";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("联网搜索 query：").append(query).append('\n');
        List<WebSearchResp> results = response.getData().getWebSearchResp();
        for (int i = 0; i < results.size(); i++) {
            WebSearchResp item = results.get(i);
            builder.append('\n').append(i + 1).append(". ");
            builder.append(StringUtils.defaultIfBlank(item.getTitle(), "无标题"));
            if (StringUtils.isNotBlank(item.getMedia())) {
                builder.append(" - ").append(item.getMedia());
            }
            if (StringUtils.isNotBlank(item.getPublishDate())) {
                builder.append(" (").append(item.getPublishDate()).append(")");
            }
            if (StringUtils.isNotBlank(item.getLink())) {
                builder.append('\n').append("URL: ").append(item.getLink());
            }
            if (StringUtils.isNotBlank(item.getContent())) {
                builder.append('\n').append("摘要: ").append(item.getContent());
            }
            builder.append('\n');
            if (builder.length() >= MAX_RESULT_CHARS) {
                builder.append("\n结果过长，已截断。");
                break;
            }
        }
        return StringUtils.left(builder.toString(), MAX_RESULT_CHARS);
    }
}
