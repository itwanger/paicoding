package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.article.AiSeoGenerateReq;
import com.github.paicoding.forum.api.model.vo.article.AiSeoGenerateRes;

/**
 * AI SEO生成服务
 *
 * @author 沉默王二
 * @date 2026/1/28
 */
public interface AiSeoService {
    
    /**
     * 根据短标题和正文内容生成SEO优化的长标题和描述
     *
     * @param req 包含短标题和正文内容的请求
     * @return AI生成的SEO标题和描述
     */
    AiSeoGenerateRes generateSeoTitleAndDescription(AiSeoGenerateReq req);
}
