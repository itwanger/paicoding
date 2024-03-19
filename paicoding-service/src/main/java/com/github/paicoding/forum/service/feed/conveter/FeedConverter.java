package com.github.paicoding.forum.service.feed.conveter;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.PushStatusEnum;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.feed.FeedSaveReq;
import com.github.paicoding.forum.service.feed.repository.entity.FeedDO;
import com.google.common.base.Joiner;
import org.springframework.util.CollectionUtils;

/**
 * @author YiHui
 * @date 2024/3/19
 */
public class FeedConverter {

    public static FeedDO toDo(FeedSaveReq saveReq, String extra) {
        FeedDO feed = new FeedDO();
        feed.setContent(saveReq.getContent());
        if (CollectionUtils.isEmpty(saveReq.getImgs())) {
            feed.setImg("");
        } else {
            feed.setImg(Joiner.on(",").join(saveReq.getImgs()));
        }
        feed.setStatus(PushStatusEnum.ONLINE.getCode());
        feed.setType(saveReq.getType());
        feed.setView(saveReq.getView());
        feed.setRefId(saveReq.getRefId());
        feed.setRefUrl(saveReq.getRefUrl());
        feed.setDeleted(YesOrNoEnum.NO.getCode());
        feed.setUserId(ReqInfoContext.getReqInfo().getUserId());
        feed.setId(saveReq.getId());
        feed.setExtend(extra);
        return feed;
    }

}
