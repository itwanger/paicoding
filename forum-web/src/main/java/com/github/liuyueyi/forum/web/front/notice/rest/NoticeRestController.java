package com.github.liuyueyi.forum.web.front.notice.rest;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.NotifyTypeEnum;
import com.github.liueyueyi.forum.api.model.exception.ExceptionUtil;
import com.github.liueyueyi.forum.api.model.vo.PageListVo;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liueyueyi.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.notify.service.NotifyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 消息通知
 *
 * @author louzai
 * @date : 2022/9/4 10:56
 **/
@Permission(role = UserRole.LOGIN)
@RestController
@RequestMapping(path = "notice/api")
public class NoticeRestController {
    private NotifyService notifyService;

    public NoticeRestController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    /**
     * 消息通知列表
     *
     * @param type     @link NotifyTypeEnum
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(path = "list")
    public ResVo<PageListVo<NotifyMsgDTO>> list(@RequestParam(name = "type") String type,
                                                @RequestParam("page") Long page,
                                                @RequestParam(name = "pageSize", required = false) Long pageSize) {
        NotifyTypeEnum typeEnum = NotifyTypeEnum.typeOf(type);
        if (typeEnum == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "type" + type + "非法");
        }
        if (pageSize == null) {
            pageSize = PageParam.DEFAULT_PAGE_SIZE;
        }
        PageListVo<NotifyMsgDTO> vo = notifyService.queryUserNotices(ReqInfoContext.getReqInfo().getUserId(),
                typeEnum, PageParam.newPageInstance(page, pageSize));
        return ResVo.ok(vo);
    }
}
