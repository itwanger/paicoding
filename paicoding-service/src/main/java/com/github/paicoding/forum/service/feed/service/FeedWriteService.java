package com.github.paicoding.forum.service.feed.service;

import com.github.paicoding.forum.api.model.vo.feed.FeedSaveReq;

/**
 * @author YiHui
 * @date 2024/3/18
 */
public interface FeedWriteService {

    boolean save(FeedSaveReq saveReq);

    boolean updateCommentCnt(Long feedId, int cnt);

    boolean updatePraiseCnt(Long feedId, int cnt);

}
