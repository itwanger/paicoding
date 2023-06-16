package com.github.paicoding.forum.service.notice.service;

import com.github.paicoding.forum.api.model.vo.article.RemoveNotice;
import com.github.paicoding.forum.api.model.vo.article.dto.NoticeDTO;

/**
 * NoticeService
 *
 * @ClassName: NoticeService
 * @Author: ygl
 * @Date: 2023/6/16 07:03
 * @Version: 1.0
 */
public interface NoticeService {

    NoticeDTO getTotal();

    void removeNum(RemoveNotice param);
}
