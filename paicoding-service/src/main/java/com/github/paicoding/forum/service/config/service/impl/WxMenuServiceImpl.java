package com.github.paicoding.forum.service.config.service.impl;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuButtonDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuDetailDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishResDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuSaveReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuTreeDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateResDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.config.property.WxMenuProperties;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.config.service.WxMenuService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 微信菜单管理
 *
 * @author Codex
 * @date 2026/3/23
 */
@Service
public class WxMenuServiceImpl implements WxMenuService {
    private static final String DRAFT_KEY = "wx.menu.default";
    private static final String DEFAULT_COMMENT = "微信自定义菜单草稿";
    private static final String ACCESS_TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String MENU_CREATE_API = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
    private static final String MENU_GET_API = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=%s";

    private static final Set<String> KEY_TYPES = new HashSet<String>();
    private static final Set<String> MEDIA_ID_TYPES = new HashSet<String>();
    private static final Set<String> ARTICLE_ID_TYPES = new HashSet<String>();

    static {
        Collections.addAll(KEY_TYPES,
                "click",
                "scancode_push",
                "scancode_waitmsg",
                "pic_sysphoto",
                "pic_photo_or_album",
                "pic_weixin",
                "location_select");
        Collections.addAll(MEDIA_ID_TYPES, "media_id", "view_limited");
        Collections.addAll(ARTICLE_ID_TYPES, "article_id", "article_view_limited");
    }

    @Resource
    private ConfigDao configDao;

    @Resource
    private WxMenuProperties wxMenuProperties;

    private volatile String accessTokenCache;
    private volatile long accessTokenExpireAt;

    @Override
    public WxMenuDetailDTO getDetail() {
        WxMenuDetailDTO detail = new WxMenuDetailDTO();
        GlobalConfigDO draft = configDao.getGlobalConfigByKey(DRAFT_KEY);
        if (draft != null && StringUtils.isNotBlank(draft.getValue())) {
            detail.setDraftJson(draft.getValue());
            detail.setDraftComment(draft.getComment());
            WxMenuValidateResDTO validateRes = validateMenuJson(draft.getValue());
            detail.setDraftValid(validateRes.getValid());
            detail.setDraftErrors(validateRes.getErrors());
            if (Boolean.TRUE.equals(validateRes.getValid())) {
                detail.setDraftJson(validateRes.getNormalizedMenuJson());
                detail.setDraftMenu(JsonUtil.toObj(validateRes.getNormalizedMenuJson(), WxMenuTreeDTO.class));
            }
        }

        try {
            WxMenuRemoteResult remote = fetchRemoteMenu();
            detail.setRemoteJson(remote.getMenuJson());
            detail.setRemoteMenu(remote.getMenu());
            detail.setConditionalMenuCount(remote.getConditionalMenuCount());
        } catch (Exception e) {
            detail.setRemoteError(resolveErrorMessage(e));
        }
        return detail;
    }

    @Override
    public void saveDraft(WxMenuSaveReq req) {
        if (req == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "请求不能为空");
        }
        WxMenuValidateResDTO validateRes = validateMenuJson(req.getMenuJson());
        raiseWhenInvalid(validateRes);
        upsertDraft(validateRes.getNormalizedMenuJson(), req.getComment());
    }

    @Override
    public WxMenuValidateResDTO validate(WxMenuValidateReq req) {
        String menuJson = req == null ? null : req.getMenuJson();
        if (StringUtils.isBlank(menuJson)) {
            GlobalConfigDO draft = configDao.getGlobalConfigByKey(DRAFT_KEY);
            menuJson = draft == null ? null : draft.getValue();
        }
        return validateMenuJson(menuJson);
    }

    @Override
    public WxMenuPublishResDTO publish(WxMenuPublishReq req) {
        String menuJson = req == null ? null : req.getMenuJson();
        if (StringUtils.isBlank(menuJson)) {
            GlobalConfigDO draft = configDao.getGlobalConfigByKey(DRAFT_KEY);
            menuJson = draft == null ? null : draft.getValue();
        }
        WxMenuValidateResDTO validateRes = validateMenuJson(menuJson);
        raiseWhenInvalid(validateRes);

        if (req != null && StringUtils.isNotBlank(req.getMenuJson())) {
            upsertDraft(validateRes.getNormalizedMenuJson(), null);
        }

        String accessToken = getAccessToken();
        String response = HttpRequest.post(String.format(MENU_CREATE_API, accessToken))
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(validateRes.getNormalizedMenuJson())
                .timeout(5000)
                .execute()
                .body();
        WxBaseResp wxResp = JsonUtil.toObj(response, WxBaseResp.class);
        if (wxResp == null || wxResp.getErrcode() == null) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信菜单发布返回异常:" + response);
        }
        if (wxResp.getErrcode() != 0) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信菜单发布失败:" + wxResp.getErrmsg());
        }

        WxMenuPublishResDTO res = new WxMenuPublishResDTO();
        res.setSuccess(true);
        res.setErrCode(wxResp.getErrcode());
        res.setErrMsg(wxResp.getErrmsg());
        res.setPublishedMenuJson(validateRes.getNormalizedMenuJson());
        return res;
    }

    @Override
    public WxMenuDetailDTO syncRemoteToDraft() {
        WxMenuRemoteResult remote = fetchRemoteMenu();
        if (remote.getMenu() == null || CollectionUtils.isEmpty(remote.getMenu().getButton())) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "微信线上菜单为空");
        }
        String menuJson = JsonUtil.toStr(remote.getMenu());
        upsertDraft(menuJson, "同步微信线上菜单于 " + new Date());
        return getDetail();
    }

    private WxMenuValidateResDTO validateMenuJson(String menuJson) {
        WxMenuValidateResDTO res = new WxMenuValidateResDTO();
        List<String> errors = new ArrayList<String>();
        res.setErrors(errors);
        if (StringUtils.isBlank(menuJson)) {
            errors.add("菜单 JSON 不能为空");
            res.setValid(false);
            return res;
        }

        WxMenuTreeDTO menuTree;
        try {
            menuTree = JsonUtil.toObj(menuJson, WxMenuTreeDTO.class);
        } catch (Exception e) {
            errors.add("菜单 JSON 格式不合法");
            res.setValid(false);
            return res;
        }

        if (menuTree == null || CollectionUtils.isEmpty(menuTree.getButton())) {
            errors.add("一级菜单不能为空");
        } else if (menuTree.getButton().size() > 3) {
            errors.add("一级菜单最多只能有 3 个");
        } else {
            for (int i = 0; i < menuTree.getButton().size(); i++) {
                validateTopButton(menuTree.getButton().get(i), i + 1, errors);
            }
        }

        res.setValid(errors.isEmpty());
        if (errors.isEmpty()) {
            res.setNormalizedMenuJson(JsonUtil.toStr(menuTree));
        }
        return res;
    }

    private void validateTopButton(WxMenuButtonDTO button, int index, List<String> errors) {
        String path = "一级菜单[" + index + "]";
        if (button == null) {
            errors.add(path + "不能为空");
            return;
        }

        if (!CollectionUtils.isEmpty(button.getSubButton())) {
            validateName(button, path, 16, errors);
            if (button.getSubButton().size() > 5) {
                errors.add(path + "的二级菜单最多只能有 5 个");
            }
            if (StringUtils.isNotBlank(button.getType()) ||
                    StringUtils.isNotBlank(button.getKey()) ||
                    StringUtils.isNotBlank(button.getUrl()) ||
                    StringUtils.isNotBlank(button.getAppid()) ||
                    StringUtils.isNotBlank(button.getPagepath()) ||
                    StringUtils.isNotBlank(button.getMediaId()) ||
                    StringUtils.isNotBlank(button.getArticleId())) {
                errors.add(path + "存在二级菜单时不能再配置 type/key/url/appid/pagepath/media_id/article_id");
            }
            for (int i = 0; i < button.getSubButton().size(); i++) {
                validateLeafButton(button.getSubButton().get(i), path + "/二级菜单[" + (i + 1) + "]", 60, errors, true);
            }
            return;
        }

        validateLeafButton(button, path, 16, errors, false);
    }

    private void validateLeafButton(WxMenuButtonDTO button, String path, int maxNameBytes, List<String> errors, boolean childLevel) {
        validateName(button, path, maxNameBytes, errors);
        if (button == null) {
            errors.add(path + "不能为空");
            return;
        }

        if (childLevel && !CollectionUtils.isEmpty(button.getSubButton())) {
            errors.add(path + "不支持三级菜单");
        }
        if (StringUtils.isBlank(button.getType())) {
            errors.add(path + "的 type 不能为空");
            return;
        }

        String type = button.getType().trim();
        if ("view".equals(type)) {
            if (StringUtils.isBlank(button.getUrl())) {
                errors.add(path + "的 view 类型必须配置 url");
            }
        } else if ("miniprogram".equals(type)) {
            if (StringUtils.isBlank(button.getUrl())) {
                errors.add(path + "的小程序类型必须配置 url");
            }
            if (StringUtils.isBlank(button.getAppid())) {
                errors.add(path + "的小程序类型必须配置 appid");
            }
            if (StringUtils.isBlank(button.getPagepath())) {
                errors.add(path + "的小程序类型必须配置 pagepath");
            }
        } else if (KEY_TYPES.contains(type)) {
            if (StringUtils.isBlank(button.getKey())) {
                errors.add(path + "的 " + type + " 类型必须配置 key");
            }
        } else if (MEDIA_ID_TYPES.contains(type)) {
            if (StringUtils.isBlank(button.getMediaId())) {
                errors.add(path + "的 " + type + " 类型必须配置 media_id");
            }
        } else if (ARTICLE_ID_TYPES.contains(type)) {
            if (StringUtils.isBlank(button.getArticleId())) {
                errors.add(path + "的 " + type + " 类型必须配置 article_id");
            }
        } else {
            errors.add(path + "的 type 不受支持:" + type);
        }
    }

    private void validateName(WxMenuButtonDTO button, String path, int maxBytes, List<String> errors) {
        if (button == null || StringUtils.isBlank(button.getName())) {
            errors.add(path + "名称不能为空");
            return;
        }
        if (button.getName().getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            errors.add(path + "名称超过长度限制，最多 " + maxBytes + " 字节");
        }
    }

    private void raiseWhenInvalid(WxMenuValidateResDTO validateRes) {
        if (validateRes == null || !Boolean.TRUE.equals(validateRes.getValid())) {
            String msg = validateRes == null || CollectionUtils.isEmpty(validateRes.getErrors()) ?
                    "菜单校验失败" :
                    StringUtils.join(validateRes.getErrors(), "；");
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, msg);
        }
    }

    private void upsertDraft(String menuJson, String comment) {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(DRAFT_KEY);
        if (config == null) {
            config = new GlobalConfigDO();
            config.setKey(DRAFT_KEY);
            config.setValue(menuJson);
            config.setComment(StringUtils.defaultIfBlank(comment, DEFAULT_COMMENT));
            config.setDeleted(0);
            configDao.save(config);
            return;
        }

        config.setValue(menuJson);
        config.setComment(StringUtils.defaultIfBlank(comment, config.getComment()));
        configDao.updateById(config);
    }

    private WxMenuRemoteResult fetchRemoteMenu() {
        String accessToken = getAccessToken();
        String response = HttpRequest.get(String.format(MENU_GET_API, accessToken))
                .timeout(5000)
                .execute()
                .body();
        WxMenuGetResp remoteResp = JsonUtil.toObj(response, WxMenuGetResp.class);
        if (remoteResp == null) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信菜单查询返回为空");
        }
        if (remoteResp.getErrcode() != null && remoteResp.getErrcode() != 0) {
            throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信菜单查询失败:" + remoteResp.getErrmsg());
        }

        WxMenuRemoteResult result = new WxMenuRemoteResult();
        result.setMenu(remoteResp.getMenu());
        if (remoteResp.getMenu() != null) {
            result.setMenuJson(JsonUtil.toStr(remoteResp.getMenu()));
        }
        result.setConditionalMenuCount(CollectionUtils.isEmpty(remoteResp.getConditionalmenu()) ? 0 : remoteResp.getConditionalmenu().size());
        return result;
    }

    private String getAccessToken() {
        if (StringUtils.isBlank(wxMenuProperties.getAppId()) || StringUtils.isBlank(wxMenuProperties.getAppSecret())) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "未配置 paicoding.login.wx.appId/appSecret");
        }
        long now = System.currentTimeMillis();
        if (StringUtils.isNotBlank(accessTokenCache) && now < accessTokenExpireAt) {
            return accessTokenCache;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (StringUtils.isNotBlank(accessTokenCache) && now < accessTokenExpireAt) {
                return accessTokenCache;
            }
            String response = HttpRequest.get(String.format(ACCESS_TOKEN_API, wxMenuProperties.getAppId(), wxMenuProperties.getAppSecret()))
                    .timeout(5000)
                    .execute()
                    .body();
            WxAccessTokenResp tokenResp = JsonUtil.toObj(response, WxAccessTokenResp.class);
            if (tokenResp == null) {
                throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信 access_token 返回为空");
            }
            if (tokenResp.getErrcode() != null && tokenResp.getErrcode() != 0) {
                throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信 access_token 获取失败:" + tokenResp.getErrmsg());
            }
            if (StringUtils.isBlank(tokenResp.getAccess_token())) {
                throw ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, "微信 access_token 为空:" + response);
            }

            accessTokenCache = tokenResp.getAccess_token();
            long expiresIn = tokenResp.getExpires_in() == null ? 7200L : tokenResp.getExpires_in();
            accessTokenExpireAt = now + Math.max(300L, expiresIn - 300L) * 1000L;
            return accessTokenCache;
        }
    }

    private String resolveErrorMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return StringUtils.defaultIfBlank(t.getMessage(), e.getMessage());
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WxBaseResp {
        private Integer errcode;
        private String errmsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WxAccessTokenResp extends WxBaseResp {
        private String access_token;
        private Long expires_in;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WxMenuGetResp extends WxBaseResp {
        private WxMenuTreeDTO menu;
        private List<Object> conditionalmenu;
    }

    @Data
    private static class WxMenuRemoteResult {
        private String menuJson;
        private WxMenuTreeDTO menu;
        private Integer conditionalMenuCount;
    }
}
