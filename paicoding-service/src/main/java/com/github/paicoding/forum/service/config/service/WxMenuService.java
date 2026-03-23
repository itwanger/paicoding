package com.github.paicoding.forum.service.config.service;

import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuDetailDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishResDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuSaveReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateResDTO;

/**
 * 微信菜单管理
 *
 * @author Codex
 * @date 2026/3/23
 */
public interface WxMenuService {
    WxMenuDetailDTO getDetail();

    void saveDraft(WxMenuSaveReq req);

    WxMenuValidateResDTO validate(WxMenuValidateReq req);

    WxMenuPublishResDTO publish(WxMenuPublishReq req);

    WxMenuDetailDTO syncRemoteToDraft();
}
