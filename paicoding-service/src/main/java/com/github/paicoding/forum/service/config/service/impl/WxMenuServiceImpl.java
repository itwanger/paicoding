package com.github.paicoding.forum.service.config.service.impl;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuAiProviderDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuButtonDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuClickReplyDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuConfigDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuDetailDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuKeywordReplyDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewAiReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewAiResDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewMatchReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPreviewMatchResDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuPublishResDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuReplyArticleDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuReplyDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuSaveReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuTreeDTO;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateReq;
import com.github.paicoding.forum.api.model.vo.wx.menu.WxMenuValidateResDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.service.ChatService;
import com.github.paicoding.forum.service.chatai.service.ChatServiceFactory;
import com.github.paicoding.forum.service.chatai.bot.AiBots;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuCodingIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import com.github.paicoding.forum.service.config.property.WxMenuProperties;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.config.service.WxMenuService;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 微信菜单管理
 *
 * @author Codex
 * @date 2026/3/23
 */
@Service
@Slf4j
public class WxMenuServiceImpl implements WxMenuService {
    private static final String DRAFT_KEY = "wx.menu.default";
    private static final String BUNDLE_KEY = "wx.menu.bundle";
    private static final String DEFAULT_COMMENT = "微信自定义菜单草稿";
    private static final String BUNDLE_COMMENT = "微信菜单整套配置";
    private static final String ACCESS_TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String MENU_CREATE_API = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
    private static final String MENU_GET_API = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=%s";

    private static final String FALLBACK_NONE = "none";
    private static final String FALLBACK_FIXED_REPLY = "fixed_reply";
    private static final String FALLBACK_AI_REPLY = "ai_reply";

    private static final String MATCH_EVENT_KEY_EXACT = "event_key_exact";
    private static final String MATCH_CONTENT_EXACT = "content_exact";
    private static final String MATCH_CONTENT_CONTAINS = "content_contains";

    private static final String MENU_JSON_TEMPLATE = "{\n" +
            "  \"button\": [\n" +
            "    {\n" +
            "      \"type\": \"view\",\n" +
            "      \"name\": \"官网\",\n" +
            "      \"url\": \"https://paicoding.com\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"服务\",\n" +
            "      \"sub_button\": [\n" +
            "        {\n" +
            "          \"type\": \"click\",\n" +
            "          \"name\": \"联系我们\",\n" +
            "          \"key\": \"CONTACT_US\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"type\": \"view\",\n" +
            "          \"name\": \"最新教程\",\n" +
            "          \"url\": \"https://paicoding.com/column\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

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

    @Resource
    private ChatServiceFactory chatServiceFactory;

    @Resource
    private AiConfig aiConfig;

    @Resource
    private ZhipuIntegration.ZhipuConfig zhipuConfig;
    @Resource
    private ZhipuCodingIntegration.ZhipuCodingConfig zhipuCodingConfig;

    @Resource
    private AiBots aiBots;

    private volatile String accessTokenCache;
    private volatile long accessTokenExpireAt;

    @Override
    public WxMenuDetailDTO getDetail() {
        WxMenuDetailDTO detail = new WxMenuDetailDTO();
        fillTips(detail);
        detail.setAiProviderOptions(buildAiProviderOptions());

        WxMenuConfigDTO draftConfig = loadDraftConfig();
        if (draftConfig != null) {
            WxMenuValidateResDTO validateRes = validateConfig(draftConfig);
            detail.setDraftConfig(draftConfig);
            detail.setDraftJson(draftConfig.getMenuJson());
            detail.setDraftComment(draftConfig.getComment());
            detail.setSubscribeReply(draftConfig.getSubscribeReply());
            detail.setDefaultReply(draftConfig.getDefaultReply());
            detail.setKeywordReplies(draftConfig.getKeywordReplies());
            detail.setMessageFallbackStrategy(draftConfig.getMessageFallbackStrategy());
            detail.setAiPrompt(draftConfig.getAiPrompt());
            detail.setAiProvider(draftConfig.getAiProvider());
            detail.setAiEnable(draftConfig.getAiEnable());
            detail.setClickReplies(draftConfig.getClickReplies());
            detail.setDraftValid(validateRes.getValid());
            detail.setDraftErrors(validateRes.getErrors());
            detail.setDraftWarnings(validateRes.getWarnings());
            if (StringUtils.isNotBlank(validateRes.getNormalizedMenuJson())) {
                detail.setDraftJson(validateRes.getNormalizedMenuJson());
                detail.getDraftConfig().setMenuJson(validateRes.getNormalizedMenuJson());
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
        WxMenuConfigDTO config = buildConfig(req);
        WxMenuValidateResDTO validateRes = validateConfig(config);
        raiseWhenInvalid(validateRes);

        config.setMenuJson(validateRes.getNormalizedMenuJson());
        upsertBundle(config, req.getComment());
        upsertLegacyDraft(config.getMenuJson(), req.getComment());
    }

    @Override
    public WxMenuValidateResDTO validate(WxMenuValidateReq req) {
        WxMenuConfigDTO config;
        if (req == null || isValidateReqEmpty(req)) {
            config = loadDraftConfig();
        } else {
            config = buildConfig(req);
        }
        return validateConfig(config);
    }

    @Override
    public WxMenuPublishResDTO publish(WxMenuPublishReq req) {
        String menuJson = req == null ? null : req.getMenuJson();
        if (StringUtils.isBlank(menuJson)) {
            WxMenuConfigDTO config = loadDraftConfig();
            menuJson = config == null ? null : config.getMenuJson();
        }

        MenuValidationContext menuValidation = validateMenu(menuJson);
        raiseWhenMenuInvalid(menuValidation);

        if (req != null && StringUtils.isNotBlank(req.getMenuJson())) {
            WxMenuConfigDTO config = loadDraftConfig();
            if (config == null) {
                config = new WxMenuConfigDTO();
            }
            config.setMenuJson(menuValidation.getNormalizedMenuJson());
            upsertBundle(config, config.getComment());
            upsertLegacyDraft(menuValidation.getNormalizedMenuJson(), config.getComment());
        }

        String accessToken = getAccessToken();
        String response = HttpRequest.post(String.format(MENU_CREATE_API, accessToken))
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(menuValidation.getNormalizedMenuJson())
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
        res.setPublishedMenuJson(menuValidation.getNormalizedMenuJson());
        return res;
    }

    @Override
    public WxMenuDetailDTO syncRemoteToDraft() {
        WxMenuRemoteResult remote = fetchRemoteMenu();
        if (remote.getMenu() == null || CollectionUtils.isEmpty(remote.getMenu().getButton())) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "微信线上菜单为空");
        }
        String menuJson = JsonUtil.toStr(remote.getMenu());
        WxMenuConfigDTO config = loadDraftConfig();
        if (config == null) {
            config = new WxMenuConfigDTO();
        }
        String syncComment = "同步微信线上菜单于 " + new Date();
        config.setMenuJson(menuJson);
        if (StringUtils.isBlank(config.getComment())) {
            config.setComment(syncComment);
        }
        upsertBundle(config, syncComment);
        upsertLegacyDraft(menuJson, syncComment);
        return getDetail();
    }

    @Override
    public WxMenuReplyDTO getSubscribeReply() {
        WxMenuConfigDTO config = loadDraftConfig();
        return config == null ? null : config.getSubscribeReply();
    }

    @Override
    public WxMenuReplyDTO getDefaultReply() {
        WxMenuConfigDTO config = loadDraftConfig();
        return config == null ? null : config.getDefaultReply();
    }

    @Override
    public WxMenuReplyDTO matchKeywordReply(String eventType, String eventKey, String content) {
        WxMenuConfigDTO config = loadDraftConfig();
        if (config == null || CollectionUtils.isEmpty(config.getKeywordReplies())) {
            return null;
        }

        List<WxMenuKeywordReplyDTO> rules = new ArrayList<WxMenuKeywordReplyDTO>(config.getKeywordReplies());
        Collections.sort(rules, Comparator.comparingInt(item -> item == null || item.getPriority() == null ? 9999 : item.getPriority()));
        for (WxMenuKeywordReplyDTO rule : rules) {
            if (findMatchedKeyword(rule, eventType, eventKey, content) == null) {
                continue;
            }
            return rule.getReply();
        }
        return null;
    }

    @Override
    public String getMessageFallbackStrategy() {
        WxMenuConfigDTO config = loadDraftConfig();
        return config == null ? FALLBACK_FIXED_REPLY : config.getMessageFallbackStrategy();
    }

    @Override
    public Boolean getAiEnable() {
        WxMenuConfigDTO config = loadDraftConfig();
        return config == null ? null : config.getAiEnable();
    }

    @Override
    public String getAiPrompt() {
        WxMenuConfigDTO config = loadDraftConfig();
        return config == null ? null : config.getAiPrompt();
    }

    @Override
    public String getAiProvider() {
        WxMenuConfigDTO config = loadDraftConfig();
        return config == null ? null : config.getAiProvider();
    }

    @Override
    public WxMenuReplyDTO getClickReply(String eventKey) {
        return matchKeywordReply("CLICK", eventKey, null);
    }

    @Override
    public WxMenuPreviewMatchResDTO previewMatch(WxMenuPreviewMatchReq req) {
        WxMenuConfigDTO config = buildPreviewConfig(req);
        WxMenuValidateResDTO validateRes = validateConfig(config);
        raiseWhenInvalid(validateRes);

        String eventType = req == null ? null : req.getEventType();
        String eventKey = req == null ? null : req.getEventKey();
        String content = req == null ? null : req.getContent();

        WxMenuPreviewMatchResDTO res = new WxMenuPreviewMatchResDTO();
        res.setFallbackStrategy(config.getMessageFallbackStrategy());
        res.setMatched(false);
        res.setUsedFallback(false);

        if (("subscribe".equalsIgnoreCase(eventType) || "scan".equalsIgnoreCase(eventType))
                && config.getSubscribeReply() != null) {
            res.setMatched(true);
            res.setMatchedRuleTitle("subscribeReply");
            res.setMatchedRuleType("subscribe_reply");
            res.setReply(config.getSubscribeReply());
            return res;
        }

        RuleMatchResult matched = matchRule(config, eventType, eventKey, content);
        if (matched != null && matched.getReply() != null) {
            res.setMatched(true);
            res.setMatchedRuleTitle(matched.getRuleTitle());
            res.setMatchedRuleType(matched.getRuleType());
            res.setMatchedKeyword(matched.getMatchedKeyword());
            res.setReply(matched.getReply());
            return res;
        }

        res.setUsedFallback(true);
        if (FALLBACK_FIXED_REPLY.equals(config.getMessageFallbackStrategy()) && config.getDefaultReply() != null) {
            res.setMatchedRuleTitle("defaultReply");
            res.setMatchedRuleType("fallback_fixed_reply");
            res.setReply(config.getDefaultReply());
            return res;
        }

        if (FALLBACK_AI_REPLY.equals(config.getMessageFallbackStrategy())) {
            WxMenuPreviewAiResDTO aiRes = previewAi(buildPreviewAiReq(config, content));
            res.setMatchedRuleTitle("aiFallback");
            res.setMatchedRuleType("fallback_ai_reply");
            if (Boolean.TRUE.equals(aiRes.getSuccess()) && StringUtils.isNotBlank(aiRes.getReplyText())) {
                WxMenuReplyDTO reply = new WxMenuReplyDTO();
                reply.setReplyType("text");
                reply.setContent(aiRes.getReplyText());
                res.setReply(reply);
            }
            return res;
        }

        res.setMatchedRuleTitle("none");
        res.setMatchedRuleType("fallback_none");
        return res;
    }

    @Override
    public WxMenuPreviewAiResDTO previewAi(WxMenuPreviewAiReq req) {
        WxMenuPreviewAiResDTO res = new WxMenuPreviewAiResDTO();
        res.setSuccess(false);
        if (req == null || StringUtils.isBlank(req.getContent())) {
            res.setErrorMsg("content 不能为空");
            return res;
        }
        if (!Boolean.TRUE.equals(req.getAiEnable())) {
            res.setErrorMsg("aiEnable 未开启");
            return res;
        }

        AISourceEnum source = resolveAiSource(req.getAiProvider());
        if (source == null) {
            res.setErrorMsg("无法识别 aiProvider:" + req.getAiProvider());
            return res;
        }
        res.setProvider(source.name());
        if (!source.syncSupport()) {
            res.setErrorMsg("当前 provider 不支持同步预览:" + source.name());
            return res;
        }
        String readyMessage = validateProviderReady(source);
        if (StringUtils.isNotBlank(readyMessage)) {
            res.setErrorMsg(readyMessage);
            return res;
        }

        ChatService chatService = chatServiceFactory.getChatService(source);
        if (chatService == null) {
            res.setErrorMsg("未找到 provider 对应的聊天服务:" + source.name());
            return res;
        }

        ReqInfoContext.ReqInfo oldReq = ReqInfoContext.getReqInfo();
        ReqInfoContext.ReqInfo reqInfo = oldReq == null ? new ReqInfoContext.ReqInfo() : oldReq;
        String oldChatId = reqInfo.getChatId();
        Long oldUserId = reqInfo.getUserId();
        BaseUserInfoDTO oldUser = reqInfo.getUser();
        BaseUserInfoDTO previewUser = aiBots == null ? null : aiBots.getBotUser(AiBotEnum.QA_BOT);
        Long previewUserId = previewUser == null ? oldUserId : previewUser.getUserId();
        try {
            if (previewUserId == null) {
                res.setErrorMsg("未找到 AI 预览用户上下文");
                return res;
            }
            if (oldReq == null) {
                ReqInfoContext.addReqInfo(reqInfo);
            }
            reqInfo.setUserId(previewUserId);
            reqInfo.setUser(previewUser == null ? oldUser : previewUser);
            reqInfo.setChatId("wx-menu-preview-" + UUID.randomUUID().toString().replace("-", ""));

            if (StringUtils.isNotBlank(req.getAiPrompt())) {
                chatService.chat(previewUserId, ChatConstants.PROMPT_TAG + req.getAiPrompt());
            }
            ChatRecordsVo vo = chatService.chat(previewUserId, req.getContent());
            String answer = extractAnswer(vo);
            if (StringUtils.isBlank(answer)) {
                res.setErrorMsg("AI 未返回内容");
                return res;
            }
            res.setSuccess(true);
            res.setReplyText(answer);
            return res;
        } catch (Exception e) {
            log.warn("wx menu preview ai failed, provider={}, reason={}", source.name(), resolveErrorMessage(e));
            res.setErrorMsg(StringUtils.defaultIfBlank(resolveErrorMessage(e), "AI 预览执行失败"));
            return res;
        } finally {
            if (oldReq == null) {
                ReqInfoContext.clear();
            } else {
                reqInfo.setChatId(oldChatId);
                reqInfo.setUserId(oldUserId);
                reqInfo.setUser(oldUser);
            }
        }
    }

    private String validateProviderReady(AISourceEnum source) {
        if (source == null) {
            return "aiProvider 不能为空";
        }
        if (!isWxAiProviderEnabled(source)) {
            return "当前环境未启用 provider:" + source.name();
        }
        if (source == AISourceEnum.ZHI_PU_AI && (zhipuConfig == null || StringUtils.isBlank(zhipuConfig.getApiSecretKey()))) {
            return "未配置 zhipu.apiSecretKey";
        }
        if (source == AISourceEnum.ZHIPU_CODING) {
            if (zhipuCodingConfig == null || StringUtils.isBlank(zhipuCodingConfig.getApiKey())) {
                return "未配置 zhipu.coding.apiKey";
            }
        }
        return null;
    }

    private WxMenuConfigDTO buildPreviewConfig(WxMenuPreviewMatchReq req) {
        WxMenuConfigDTO current = loadDraftConfig();
        WxMenuConfigDTO config = current == null ? new WxMenuConfigDTO() : current;
        if (req == null) {
            return config;
        }

        if (StringUtils.isNotBlank(req.getMenuJson())) {
            config.setMenuJson(req.getMenuJson());
        }
        if (req.getSubscribeReply() != null) {
            config.setSubscribeReply(req.getSubscribeReply());
        }
        if (req.getDefaultReply() != null) {
            config.setDefaultReply(req.getDefaultReply());
        }
        if (req.getKeywordReplies() != null) {
            config.setKeywordReplies(req.getKeywordReplies());
        }
        if (StringUtils.isNotBlank(req.getMessageFallbackStrategy())) {
            config.setMessageFallbackStrategy(req.getMessageFallbackStrategy());
        }
        if (StringUtils.isNotBlank(req.getAiPrompt())) {
            config.setAiPrompt(req.getAiPrompt());
        }
        if (StringUtils.isNotBlank(req.getAiProvider())) {
            config.setAiProvider(req.getAiProvider());
        }
        if (req.getAiEnable() != null) {
            config.setAiEnable(req.getAiEnable());
        }
        if (req.getClickReplies() != null) {
            config.setClickReplies(req.getClickReplies());
        }
        return normalizeConfig(config);
    }

    private WxMenuPreviewAiReq buildPreviewAiReq(WxMenuConfigDTO config, String content) {
        WxMenuPreviewAiReq req = new WxMenuPreviewAiReq();
        req.setContent(content);
        req.setAiEnable(config == null ? null : config.getAiEnable());
        req.setAiPrompt(config == null ? null : config.getAiPrompt());
        req.setAiProvider(config == null ? null : config.getAiProvider());
        return req;
    }

    private RuleMatchResult matchRule(WxMenuConfigDTO config, String eventType, String eventKey, String content) {
        if (config == null || CollectionUtils.isEmpty(config.getKeywordReplies())) {
            return null;
        }

        List<WxMenuKeywordReplyDTO> rules = new ArrayList<WxMenuKeywordReplyDTO>(config.getKeywordReplies());
        Collections.sort(rules, Comparator.comparingInt(item -> item == null || item.getPriority() == null ? 9999 : item.getPriority()));
        for (WxMenuKeywordReplyDTO rule : rules) {
            String matchedKeyword = findMatchedKeyword(rule, eventType, eventKey, content);
            if (matchedKeyword == null) {
                continue;
            }

            RuleMatchResult result = new RuleMatchResult();
            result.setRuleTitle(StringUtils.defaultIfBlank(rule.getTitle(), "keywordReplies"));
            result.setRuleType(rule.getMatchType());
            result.setMatchedKeyword(matchedKeyword);
            result.setReply(rule.getReply());
            return result;
        }
        return null;
    }

    private String findMatchedKeyword(WxMenuKeywordReplyDTO rule, String eventType, String eventKey, String content) {
        if (rule == null || Boolean.FALSE.equals(rule.getEnabled()) || CollectionUtils.isEmpty(rule.getKeywords())) {
            return null;
        }

        String matchType = StringUtils.trim(rule.getMatchType());
        if (MATCH_EVENT_KEY_EXACT.equals(matchType)) {
            if (!"CLICK".equalsIgnoreCase(StringUtils.defaultString(eventType))) {
                return null;
            }
            for (String keyword : rule.getKeywords()) {
                if (StringUtils.equals(StringUtils.trim(keyword), StringUtils.trim(eventKey))) {
                    return keyword;
                }
            }
            return null;
        }

        if (StringUtils.isBlank(content)) {
            return null;
        }

        if (MATCH_CONTENT_EXACT.equals(matchType)) {
            for (String keyword : rule.getKeywords()) {
                if (StringUtils.equalsIgnoreCase(StringUtils.trim(keyword), StringUtils.trim(content))) {
                    return keyword;
                }
            }
            return null;
        }

        if (MATCH_CONTENT_CONTAINS.equals(matchType)) {
            String normalized = content.toLowerCase();
            for (String keyword : rule.getKeywords()) {
                if (StringUtils.isNotBlank(keyword) && normalized.contains(keyword.trim().toLowerCase())) {
                    return keyword;
                }
            }
        }
        return null;
    }

    private AISourceEnum resolveAiSource(String provider) {
        if (StringUtils.isBlank(provider)) {
            return AISourceEnum.PAI_AI;
        }

        String normalized = provider.trim();
        String compact = normalized.toUpperCase()
                .replace("-", "")
                .replace("_", "")
                .replace(".", "")
                .replace(" ", "");
        if ("GLM".equals(compact)
                || "GLM4".equals(compact)
                || "GLM45".equals(compact)
                || "GLM45FLASH".equals(compact)
                || "ZHIPU".equals(compact)
                || "ZHIPUAI".equals(compact)
                || "智谱".equals(normalized)) {
            return AISourceEnum.ZHI_PU_AI;
        }
        if ("ZHIPUCODING".equals(compact)
                || "GLMCODING".equals(compact)
                || "智谱CODING".equalsIgnoreCase(normalized)
                || "智谱编码".equals(normalized)) {
            return AISourceEnum.ZHIPU_CODING;
        }
        if ("PAI".equals(compact)
                || "PAIAI".equals(compact)
                || "技术派".equals(normalized)) {
            return AISourceEnum.PAI_AI;
        }
        if ("CHATGPT".equals(compact)
                || "OPENAI".equals(compact)
                || "GPT35".equals(compact)
                || "CHATGPT35".equals(compact)) {
            return AISourceEnum.CHAT_GPT_3_5;
        }
        if ("GPT4".equals(compact)
                || "CHATGPT4".equals(compact)) {
            return AISourceEnum.CHAT_GPT_4;
        }

        for (AISourceEnum source : AISourceEnum.values()) {
            if (source.name().equalsIgnoreCase(normalized)
                    || source.getName().equalsIgnoreCase(normalized)
                    || String.valueOf(source.getCode()).equals(normalized)) {
                return source;
            }
        }
        return null;
    }

    private String extractAnswer(ChatRecordsVo vo) {
        if (vo == null || CollectionUtils.isEmpty(vo.getRecords())) {
            return null;
        }
        for (ChatItemVo record : vo.getRecords()) {
            if (record != null && StringUtils.isNotBlank(record.getAnswer())) {
                return record.getAnswer();
            }
        }
        return null;
    }

    private WxMenuValidateResDTO validateConfig(WxMenuConfigDTO config) {
        WxMenuValidateResDTO res = new WxMenuValidateResDTO();
        List<String> menuErrors = new ArrayList<String>();
        List<String> replyErrors = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();
        res.setMenuErrors(menuErrors);
        res.setReplyErrors(replyErrors);
        res.setWarnings(warnings);

        MenuValidationContext menuValidation = validateMenu(config == null ? null : config.getMenuJson());
        menuErrors.addAll(menuValidation.getErrors());
        warnings.addAll(menuValidation.getWarnings());
        if (StringUtils.isNotBlank(menuValidation.getNormalizedMenuJson())) {
            res.setNormalizedMenuJson(menuValidation.getNormalizedMenuJson());
        }

        if (config != null) {
            validateReply(config.getSubscribeReply(), "subscribeReply", replyErrors);
            validateReply(config.getDefaultReply(), "defaultReply", replyErrors);
            validateFallbackStrategy(config, replyErrors, warnings);
            validateKeywordReplies(config.getKeywordReplies(), menuValidation.getClickKeys(), replyErrors, warnings);
        }

        List<String> errors = new ArrayList<String>();
        errors.addAll(menuErrors);
        errors.addAll(replyErrors);
        res.setErrors(errors);
        res.setValid(errors.isEmpty());
        return res;
    }

    private MenuValidationContext validateMenu(String menuJson) {
        MenuValidationContext context = new MenuValidationContext();
        List<String> errors = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();
        Set<String> clickKeys = new LinkedHashSet<String>();
        context.setErrors(errors);
        context.setWarnings(warnings);
        context.setClickKeys(clickKeys);

        if (StringUtils.isBlank(menuJson)) {
            errors.add("menuJson 不能为空");
            return context;
        }

        WxMenuTreeDTO menuTree;
        try {
            menuTree = JsonUtil.toObj(menuJson, WxMenuTreeDTO.class);
        } catch (Exception e) {
            errors.add("menuJson 格式不合法");
            return context;
        }

        if (menuTree == null || CollectionUtils.isEmpty(menuTree.getButton())) {
            errors.add("一级菜单不能为空");
            return context;
        }

        if (menuTree.getButton().size() > 3) {
            errors.add("一级菜单最多只能有 3 个");
        } else {
            Set<String> duplicatedClickKeys = new HashSet<String>();
            for (int i = 0; i < menuTree.getButton().size(); i++) {
                validateTopButton(menuTree.getButton().get(i), i + 1, errors, clickKeys, duplicatedClickKeys);
            }
            if (!duplicatedClickKeys.isEmpty()) {
                for (String key : duplicatedClickKeys) {
                    warnings.add("菜单中存在重复的 click key[" + key + "]，建议改为唯一值");
                }
            }
        }

        if (errors.isEmpty()) {
            context.setMenuTree(menuTree);
            context.setNormalizedMenuJson(JsonUtil.toStr(menuTree));
        }
        return context;
    }

    private void validateTopButton(WxMenuButtonDTO button,
                                   int index,
                                   List<String> errors,
                                   Set<String> clickKeys,
                                   Set<String> duplicatedClickKeys) {
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
                validateLeafButton(button.getSubButton().get(i),
                        path + "/二级菜单[" + (i + 1) + "]",
                        60,
                        errors,
                        true,
                        clickKeys,
                        duplicatedClickKeys);
            }
            return;
        }

        validateLeafButton(button, path, 16, errors, false, clickKeys, duplicatedClickKeys);
    }

    private void validateLeafButton(WxMenuButtonDTO button,
                                    String path,
                                    int maxNameBytes,
                                    List<String> errors,
                                    boolean childLevel,
                                    Set<String> clickKeys,
                                    Set<String> duplicatedClickKeys) {
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
            } else if ("click".equals(type)) {
                String key = StringUtils.trim(button.getKey());
                if (!clickKeys.add(key)) {
                    duplicatedClickKeys.add(key);
                }
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

    private void validateReply(WxMenuReplyDTO reply, String path, List<String> replyErrors) {
        if (reply == null) {
            return;
        }

        String replyType = StringUtils.trimToEmpty(reply.getReplyType());
        if (StringUtils.isBlank(replyType)) {
            replyErrors.add(path + ".replyType 不能为空，仅支持 text/news");
            return;
        }

        if ("text".equalsIgnoreCase(replyType)) {
            if (StringUtils.isBlank(reply.getContent())) {
                replyErrors.add(path + ".content 不能为空");
            }
            return;
        }

        if ("news".equalsIgnoreCase(replyType)) {
            if (CollectionUtils.isEmpty(reply.getArticles())) {
                replyErrors.add(path + ".articles 不能为空");
                return;
            }
            if (reply.getArticles().size() > 8) {
                replyErrors.add(path + ".articles 最多 8 条");
            }
            for (int i = 0; i < reply.getArticles().size(); i++) {
                WxMenuReplyArticleDTO article = reply.getArticles().get(i);
                String articlePath = path + ".articles[" + i + "]";
                if (article == null) {
                    replyErrors.add(articlePath + "不能为空");
                    continue;
                }
                if (StringUtils.isBlank(article.getTitle())) {
                    replyErrors.add(articlePath + ".title 不能为空");
                }
                if (StringUtils.isBlank(article.getUrl())) {
                    replyErrors.add(articlePath + ".url 不能为空");
                }
            }
            return;
        }

        replyErrors.add(path + ".replyType 不受支持:" + replyType);
    }

    private void validateKeywordReplies(List<WxMenuKeywordReplyDTO> keywordReplies,
                                        Set<String> clickKeys,
                                        List<String> replyErrors,
                                        List<String> warnings) {
        Set<String> configuredEventKeys = new LinkedHashSet<String>();
        if (!CollectionUtils.isEmpty(keywordReplies)) {
            for (int i = 0; i < keywordReplies.size(); i++) {
                WxMenuKeywordReplyDTO item = keywordReplies.get(i);
                String path = "keywordReplies[" + i + "]";
                if (item == null) {
                    replyErrors.add(path + "不能为空");
                    continue;
                }

                String matchType = StringUtils.trim(item.getMatchType());
                if (!MATCH_EVENT_KEY_EXACT.equals(matchType)
                        && !MATCH_CONTENT_EXACT.equals(matchType)
                        && !MATCH_CONTENT_CONTAINS.equals(matchType)) {
                    replyErrors.add(path + ".matchType 仅支持 event_key_exact/content_exact/content_contains");
                }

                if (CollectionUtils.isEmpty(item.getKeywords())) {
                    replyErrors.add(path + ".keywords 不能为空");
                } else {
                    for (String keyword : item.getKeywords()) {
                        if (StringUtils.isBlank(keyword)) {
                            replyErrors.add(path + " 存在空 keyword");
                            continue;
                        }
                        if (MATCH_EVENT_KEY_EXACT.equals(matchType)) {
                            configuredEventKeys.add(StringUtils.trim(keyword));
                        }
                    }
                }

                if (StringUtils.isBlank(item.getReplyType()) && (item.getReply() == null || StringUtils.isBlank(item.getReply().getReplyType()))) {
                    replyErrors.add(path + ".replyType 不能为空");
                }
                if (item.getReply() == null) {
                    replyErrors.add(path + ".reply 不能为空");
                } else {
                    if (StringUtils.isBlank(item.getReply().getReplyType())) {
                        item.getReply().setReplyType(item.getReplyType());
                    }
                    validateReply(item.getReply(), path + ".reply", replyErrors);
                }
            }
        }

        if (!CollectionUtils.isEmpty(clickKeys)) {
            for (String key : clickKeys) {
                if (!configuredEventKeys.contains(key)) {
                    warnings.add("菜单 click key[" + key + "] 未配置 keywordReplies 规则，将走 messageFallbackStrategy");
                }
            }
        }

        if (!CollectionUtils.isEmpty(configuredEventKeys)) {
            for (String key : configuredEventKeys) {
                if (CollectionUtils.isEmpty(clickKeys) || !clickKeys.contains(key)) {
                    warnings.add("keywordReplies 中的 event_key[" + key + "] 未在当前菜单 JSON 中使用");
                }
            }
        }
    }

    private void validateFallbackStrategy(WxMenuConfigDTO config, List<String> replyErrors, List<String> warnings) {
        String strategy = StringUtils.defaultIfBlank(config.getMessageFallbackStrategy(), FALLBACK_FIXED_REPLY);
        config.setMessageFallbackStrategy(strategy);
        if (!FALLBACK_NONE.equals(strategy)
                && !FALLBACK_FIXED_REPLY.equals(strategy)
                && !FALLBACK_AI_REPLY.equals(strategy)) {
            replyErrors.add("messageFallbackStrategy 仅支持 none/fixed_reply/ai_reply");
            return;
        }

        if (FALLBACK_FIXED_REPLY.equals(strategy) && config.getDefaultReply() == null) {
            warnings.add("messageFallbackStrategy=fixed_reply 但 defaultReply 未配置，未命中时会走系统默认兜底文案");
        }

        if (FALLBACK_AI_REPLY.equals(strategy)) {
            if (!Boolean.TRUE.equals(config.getAiEnable())) {
                warnings.add("messageFallbackStrategy=ai_reply，但 aiEnable 未开启，当前仍会退回默认兜底逻辑");
            }
            if (StringUtils.isBlank(config.getAiProvider())) {
                warnings.add("messageFallbackStrategy=ai_reply，建议补充 aiProvider");
                return;
            }
        }

        if (StringUtils.isBlank(config.getAiProvider())) {
            return;
        }

        AISourceEnum source = resolveAiSource(config.getAiProvider());
        if (source == null) {
            replyErrors.add("aiProvider 不受支持:" + config.getAiProvider());
            return;
        }

        String providerError = validateWxAiProvider(source);
        if (StringUtils.isBlank(providerError)) {
            return;
        }

        if (FALLBACK_AI_REPLY.equals(strategy) || Boolean.TRUE.equals(config.getAiEnable())) {
            replyErrors.add(providerError);
        } else {
            warnings.add(providerError + "，当前 AI 配置不会生效");
        }
    }

    private String validateWxAiProvider(AISourceEnum source) {
        if (source == null) {
            return "aiProvider 不能为空";
        }
        if (!source.syncSupport()) {
            return "当前 provider 不支持微信公众号同步回复:" + source.name();
        }
        if (!isWxAiProviderEnabled(source)) {
            return "aiProvider 未在 AI 模型配置中启用:" + source.name();
        }
        return null;
    }

    private boolean isWxAiProviderEnabled(AISourceEnum source) {
        if (source == null || aiConfig == null || CollectionUtils.isEmpty(aiConfig.getSource())) {
            return false;
        }
        return aiConfig.getSource().contains(source);
    }

    private List<WxMenuAiProviderDTO> buildAiProviderOptions() {
        if (aiConfig == null || CollectionUtils.isEmpty(aiConfig.getSource())) {
            return Collections.emptyList();
        }

        List<WxMenuAiProviderDTO> options = new ArrayList<WxMenuAiProviderDTO>();
        Set<AISourceEnum> dedup = new LinkedHashSet<AISourceEnum>();
        for (AISourceEnum source : aiConfig.getSource()) {
            if (source == null || !source.syncSupport() || !dedup.add(source)) {
                continue;
            }

            WxMenuAiProviderDTO option = new WxMenuAiProviderDTO();
            option.setCode(source.getCode());
            option.setValue(source.name());
            option.setName(source.getName());
            option.setSyncSupport(source.syncSupport());
            options.add(option);
        }
        return options;
    }

    private void raiseWhenInvalid(WxMenuValidateResDTO validateRes) {
        if (validateRes == null || !Boolean.TRUE.equals(validateRes.getValid())) {
            String msg = validateRes == null || CollectionUtils.isEmpty(validateRes.getErrors()) ?
                    "微信菜单配置校验失败" :
                    StringUtils.join(validateRes.getErrors(), "；");
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, msg);
        }
    }

    private void raiseWhenMenuInvalid(MenuValidationContext menuValidation) {
        if (menuValidation == null || !CollectionUtils.isEmpty(menuValidation.getErrors())) {
            String msg = menuValidation == null || CollectionUtils.isEmpty(menuValidation.getErrors()) ?
                    "菜单校验失败" :
                    StringUtils.join(menuValidation.getErrors(), "；");
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, msg);
        }
    }

    private WxMenuConfigDTO buildConfig(WxMenuSaveReq req) {
        WxMenuConfigDTO config = new WxMenuConfigDTO();
        config.setMenuJson(req.getMenuJson());
        config.setComment(req.getComment());
        config.setSubscribeReply(req.getSubscribeReply());
        config.setDefaultReply(req.getDefaultReply());
        config.setKeywordReplies(req.getKeywordReplies());
        config.setMessageFallbackStrategy(req.getMessageFallbackStrategy());
        config.setAiPrompt(req.getAiPrompt());
        config.setAiProvider(req.getAiProvider());
        config.setAiEnable(req.getAiEnable());
        config.setClickReplies(req.getClickReplies());
        return normalizeConfig(config);
    }

    private WxMenuConfigDTO buildConfig(WxMenuValidateReq req) {
        WxMenuConfigDTO current = loadDraftConfig();
        WxMenuConfigDTO config = new WxMenuConfigDTO();
        config.setMenuJson(StringUtils.defaultIfBlank(req.getMenuJson(), current == null ? null : current.getMenuJson()));
        config.setComment(current == null ? null : current.getComment());
        config.setSubscribeReply(req.getSubscribeReply() == null ? (current == null ? null : current.getSubscribeReply()) : req.getSubscribeReply());
        config.setDefaultReply(req.getDefaultReply() == null ? (current == null ? null : current.getDefaultReply()) : req.getDefaultReply());
        config.setKeywordReplies(req.getKeywordReplies() == null ? (current == null ? null : current.getKeywordReplies()) : req.getKeywordReplies());
        config.setMessageFallbackStrategy(StringUtils.defaultIfBlank(req.getMessageFallbackStrategy(), current == null ? null : current.getMessageFallbackStrategy()));
        config.setAiPrompt(StringUtils.defaultIfBlank(req.getAiPrompt(), current == null ? null : current.getAiPrompt()));
        config.setAiProvider(StringUtils.defaultIfBlank(req.getAiProvider(), current == null ? null : current.getAiProvider()));
        config.setAiEnable(req.getAiEnable() == null ? (current == null ? null : current.getAiEnable()) : req.getAiEnable());
        config.setClickReplies(req.getClickReplies() == null ? (current == null ? null : current.getClickReplies()) : req.getClickReplies());
        return normalizeConfig(config);
    }

    private boolean isValidateReqEmpty(WxMenuValidateReq req) {
        return StringUtils.isBlank(req.getMenuJson())
                && req.getSubscribeReply() == null
                && req.getDefaultReply() == null
                && req.getKeywordReplies() == null
                && StringUtils.isBlank(req.getMessageFallbackStrategy())
                && req.getAiEnable() == null
                && StringUtils.isBlank(req.getAiPrompt())
                && StringUtils.isBlank(req.getAiProvider())
                && req.getClickReplies() == null;
    }

    private WxMenuConfigDTO loadDraftConfig() {
        GlobalConfigDO bundle = configDao.getGlobalConfigByKey(BUNDLE_KEY);
        if (bundle != null && StringUtils.isNotBlank(bundle.getValue())) {
            WxMenuConfigDTO config = JsonUtil.toObj(bundle.getValue(), WxMenuConfigDTO.class);
            if (config != null && StringUtils.isBlank(config.getComment())) {
                config.setComment(bundle.getComment());
            }
            return normalizeConfig(config);
        }

        GlobalConfigDO draft = configDao.getGlobalConfigByKey(DRAFT_KEY);
        if (draft == null || StringUtils.isBlank(draft.getValue())) {
            return null;
        }

        WxMenuConfigDTO config = new WxMenuConfigDTO();
        config.setMenuJson(draft.getValue());
        config.setComment(draft.getComment());
        return normalizeConfig(config);
    }

    private WxMenuConfigDTO normalizeConfig(WxMenuConfigDTO config) {
        if (config == null) {
            return null;
        }

        if (StringUtils.isBlank(config.getMessageFallbackStrategy())) {
            config.setMessageFallbackStrategy(FALLBACK_FIXED_REPLY);
        }
        if (StringUtils.isNotBlank(config.getAiProvider())) {
            AISourceEnum source = resolveAiSource(config.getAiProvider());
            if (source != null) {
                config.setAiProvider(source.name());
            }
        }

        if (CollectionUtils.isEmpty(config.getKeywordReplies()) && !CollectionUtils.isEmpty(config.getClickReplies())) {
            List<WxMenuKeywordReplyDTO> keywordReplies = new ArrayList<WxMenuKeywordReplyDTO>();
            for (WxMenuClickReplyDTO item : config.getClickReplies()) {
                if (item == null) {
                    continue;
                }
                WxMenuKeywordReplyDTO rule = new WxMenuKeywordReplyDTO();
                rule.setMatchType(MATCH_EVENT_KEY_EXACT);
                rule.setKeywords(Collections.singletonList(item.getKey()));
                rule.setReplyType(item.getReply() == null ? null : item.getReply().getReplyType());
                rule.setReply(item.getReply());
                rule.setEnabled(Boolean.TRUE);
                rule.setPriority(100);
                rule.setTitle(item.getTitle());
                keywordReplies.add(rule);
            }
            config.setKeywordReplies(keywordReplies);
        }

        if (!CollectionUtils.isEmpty(config.getKeywordReplies())) {
            for (WxMenuKeywordReplyDTO rule : config.getKeywordReplies()) {
                if (rule == null) {
                    continue;
                }
                if (rule.getEnabled() == null) {
                    rule.setEnabled(Boolean.TRUE);
                }
                if (rule.getPriority() == null) {
                    rule.setPriority(100);
                }
                if (rule.getReply() != null && StringUtils.isBlank(rule.getReply().getReplyType())) {
                    rule.getReply().setReplyType(rule.getReplyType());
                }
                if (StringUtils.isBlank(rule.getReplyType()) && rule.getReply() != null) {
                    rule.setReplyType(rule.getReply().getReplyType());
                }
            }
        }
        return config;
    }

    private void upsertBundle(WxMenuConfigDTO config, String comment) {
        String persistedComment = StringUtils.defaultIfBlank(comment, config == null ? null : config.getComment());
        if (config == null) {
            config = new WxMenuConfigDTO();
        }
        config.setComment(StringUtils.defaultIfBlank(persistedComment, DEFAULT_COMMENT));

        GlobalConfigDO bundle = configDao.getGlobalConfigByKey(BUNDLE_KEY);
        if (bundle == null) {
            bundle = new GlobalConfigDO();
            bundle.setKey(BUNDLE_KEY);
            bundle.setValue(JsonUtil.toStr(config));
            bundle.setComment(StringUtils.defaultIfBlank(persistedComment, BUNDLE_COMMENT));
            bundle.setDeleted(0);
            configDao.save(bundle);
            return;
        }

        bundle.setValue(JsonUtil.toStr(config));
        bundle.setComment(StringUtils.defaultIfBlank(persistedComment, bundle.getComment()));
        configDao.updateById(bundle);
    }

    private void upsertLegacyDraft(String menuJson, String comment) {
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

    private void fillTips(WxMenuDetailDTO detail) {
        detail.setMenuJsonTemplate(MENU_JSON_TEMPLATE);
        detail.setMenuJsonTips(Arrays.asList(
                "一级菜单最多 3 个，每个一级菜单最多 5 个二级菜单。",
                "有 sub_button 的一级菜单不要再配置 type/key/url/appid/pagepath/media_id/article_id。",
                "view 类型需要 url；click 类型需要 key；miniprogram 类型需要 url/appid/pagepath。",
                "如果菜单是 click 类型，建议 key 用大写英文或下划线，便于和 keywordReplies 的 event_key_exact 规则对上。"
        ));
        detail.setReplyTips(Arrays.asList(
                "subscribeReply、keywordReplies、defaultReply 在保存后立即生效，不需要发布。",
                "菜单结构本身只有在调用 publish 后才会同步到微信公众号。",
                "keywordReplies 目前支持 event_key_exact、content_exact、content_contains 三种匹配方式。",
                "messageFallbackStrategy 仅支持 none、fixed_reply、ai_reply；replyType 支持 text/news。",
                "aiProviderOptions 会和 AI 模型配置中的启用项保持同步，只展示支持同步回复的 provider。"
        ));
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
        String msg = StringUtils.defaultIfBlank(t.getMessage(), e.getMessage());
        if (StringUtils.isNotBlank(msg)) {
            return msg;
        }
        return t.getClass().getSimpleName();
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

    @Data
    private static class MenuValidationContext {
        private WxMenuTreeDTO menuTree;
        private String normalizedMenuJson;
        private List<String> errors;
        private List<String> warnings;
        private Set<String> clickKeys;
    }

    @Data
    private static class RuleMatchResult {
        private String ruleTitle;
        private String ruleType;
        private String matchedKeyword;
        private WxMenuReplyDTO reply;
    }
}
