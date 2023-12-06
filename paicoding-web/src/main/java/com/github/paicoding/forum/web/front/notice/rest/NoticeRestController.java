package com.github.paicoding.forum.web.front.notice.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.NextPageHtmlVo;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.notify.dto.NotifyMsgDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.ws.WebSocketResponseUtil;
import com.github.paicoding.forum.service.notify.service.NotifyChatService;
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.web.component.TemplateEngineHelper;
import com.github.paicoding.forum.web.front.notice.vo.NoticeResVo;
import com.github.paicoding.forum.web.front.notice.vo.NotifyChannelDescResVo;
import com.github.paicoding.forum.web.front.notice.vo.NotifyChatResVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Autowired
    private TemplateEngineHelper templateEngineHelper;

    private NotifyService notifyService;

    private NotifyChatService notifyChatService;

    public NoticeRestController(NotifyService notifyService, NotifyChatService notifyChatService) {
        this.notifyService = notifyService;
        this.notifyChatService = notifyChatService;
    }

    private PageListVo<NotifyMsgDTO> listItems(String type, Long page, Long pageSize) {
        NotifyTypeEnum typeEnum = NotifyTypeEnum.typeOf(type);
        if (typeEnum == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "type" + type + "非法");
        }
        if (pageSize == null) {
            pageSize = PageParam.DEFAULT_PAGE_SIZE;
        }
        return notifyService.queryUserNotices(ReqInfoContext.getReqInfo().getUserId(),
                typeEnum, PageParam.newPageInstance(page, pageSize));

    }

    /**
     * 消息通知列表，用于前后端分离的场景
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
        return ResVo.ok(listItems(type, page, pageSize));
    }

    /**
     * 返回渲染好的分页信息
     *
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(path = "items")
    public ResVo<NextPageHtmlVo> listForView(@RequestParam(name = "type") String type,
                                             @RequestParam("page") Long page,
                                             @RequestParam(name = "pageSize", required = false) Long pageSize) {
        type = type.toLowerCase().trim();
        PageListVo<NotifyMsgDTO> list = listItems(type, page, pageSize);
        NoticeResVo vo = new NoticeResVo();
        vo.setList(list);
        vo.setSelectType(type);
        String html = templateEngineHelper.render("views/notice/tab/notify-" + type, vo);
        return ResVo.ok(new NextPageHtmlVo(html, list.getHasMore()));
    }


    /**
     * 实时在线聊天
     *
     * @param content 发送的内容
     * @param type    群组类型
     * @param channel 目标地址
     */
    @MessageMapping("/msg/{type}/{channel}")
    public void sayHello(String content, @DestinationVariable("type") String type, @DestinationVariable("channel") String channel, SimpMessageHeaderAccessor headerAccessor) {
        ReqInfoContext.ReqInfo user = (ReqInfoContext.ReqInfo) headerAccessor.getUser();
        NotifyChatResVo resVo = new NotifyChatResVo().setUserId(user.getUser().getUserId())
                .setUserName(user.getUser().getUserName())
                .setAvatar(user.getUser().getPhoto())
                .setContent(content)
                .setMsgType(NotifyChatService.NotifyChatMsgType.USER_MSG)
                .setDate(System.currentTimeMillis());
        WebSocketResponseUtil.broadcastMsg("/msg/" + type + "/" + channel, resVo);
    }


    /**
     * 返回临时聊天室的描述
     *
     * @param channelId
     * @return
     */
    @GetMapping(path = "/tmpChannel")
    public ResVo<NotifyChannelDescResVo> getChannelDesc(String channelId) {
        String desc = notifyChatService.getTmpChatChannelInfo(channelId);
        return ResVo.ok(new NotifyChannelDescResVo().setChannel(channelId).setTitle(desc));
    }
}
