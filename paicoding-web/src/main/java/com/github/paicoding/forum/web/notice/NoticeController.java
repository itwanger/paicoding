package com.github.paicoding.forum.web.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.RemoveNotice;
import com.github.paicoding.forum.api.model.vo.article.dto.NoticeDTO;
import com.github.paicoding.forum.service.notice.service.NoticeService;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息通知
 *
 * @ClassName: NoticeController
 * @Author: ygl
 * @Date: 2023/6/16 06:58
 * @Version: 1.0
 */
@RestController
@Api(value = "消息通知", tags = "消息通知")
@RequestMapping(path = {"/api/notice"})
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 查询消息通知
     */
    @PostMapping(path = "/gettotal")
    public ResVo<NoticeDTO> getTotal() {

        log.info("查询消息通知");

        NoticeDTO noticeDTO = noticeService.getTotal();

        return ResVo.ok(noticeDTO);

    }

    /**
     * 删除消息数
     * -1-总
     * 2-点赞
     * 3-收藏
     * 6-评论
     * 8-回复
     */
    @PostMapping(path = "/removenum")
    public ResVo<String> removeNum(@RequestBody RemoveNotice param) {

        log.info("删除消息");

        noticeService.removeNum(param);

        return ResVo.ok("ok");

    }


}
