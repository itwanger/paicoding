package com.github.liuyueyi.forum.web.front.notice.view;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.NotifyTypeEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.notify.service.NotifyService;
import com.github.liuyueyi.forum.web.front.notice.vo.NoticeResVo;
import com.github.liuyueyi.forum.web.global.BaseViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 消息通知
 *
 * @author louzai
 * @date : 2022/9/4 10:56
 **/
@Controller
@Permission(role = UserRole.LOGIN)
@RequestMapping(path = "notice")
public class NoticeViewController extends BaseViewController {
    @Autowired
    private NotifyService notifyService;

    @RequestMapping("/{type}")
    public String list(@PathVariable(name = "type", required = false) String type, Model model) {
        NotifyTypeEnum typeEnum = NotifyTypeEnum.typeOf(type);
        if (typeEnum == null) {
            // 默认显示评论消息通知
            typeEnum = NotifyTypeEnum.COMMENT;
        }

        NoticeResVo vo = new NoticeResVo();
        Long loginUserId = ReqInfoContext.getReqInfo().getUserId();
        vo.setList(notifyService.queryUserNotices(loginUserId, typeEnum, PageParam.newPageInstance()));

        vo.setSelectType(typeEnum.name().toLowerCase());
        vo.setUnreadCountMap(notifyService.queryUnreadCounts(loginUserId));
        model.addAttribute("vo", vo);
        return "biz/notice/msg";
    }
}