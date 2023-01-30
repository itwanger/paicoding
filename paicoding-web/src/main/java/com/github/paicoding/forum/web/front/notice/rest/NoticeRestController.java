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
import com.github.paicoding.forum.service.notify.service.NotifyService;
import com.github.paicoding.forum.web.component.TemplateEngineHelper;
import com.github.paicoding.forum.web.front.notice.vo.NoticeResVo;
import org.springframework.beans.factory.annotation.Autowired;
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

    public NoticeRestController(NotifyService notifyService) {
        this.notifyService = notifyService;
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
}
