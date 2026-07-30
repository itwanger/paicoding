package com.github.paicoding.forum.service.article.service;

import com.github.paicoding.forum.api.model.vo.article.AiIllustrationGenerateReq;
import com.github.paicoding.forum.api.model.vo.article.AiIllustrationGenerateRes;

public interface AiIllustrationService {

    AiIllustrationGenerateRes generateIllustrations(AiIllustrationGenerateReq req);
}