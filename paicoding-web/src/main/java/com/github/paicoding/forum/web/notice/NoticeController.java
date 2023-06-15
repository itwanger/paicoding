package com.github.paicoding.forum.web.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.service.notice.service.NoticeService;

import io.swagger.annotations.Api;

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
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 查询总消息通知
     */
    @PostMapping(path = "/gettotal")
    public ResVo<Integer> getTotal() {

        Integer num = noticeService.getTotal();

        return ResVo.ok(num);

    }

}
