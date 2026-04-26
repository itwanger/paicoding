package com.github.paicoding.forum.service.chatai.config.impl;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.ai.config.AiConfigAdminReq;
import com.github.paicoding.forum.api.model.vo.ai.config.AiConfigTestReq;
import com.github.paicoding.forum.api.model.vo.ai.config.dto.AiConfigAdminDTO;
import com.github.paicoding.forum.api.model.vo.ai.config.dto.AiConfigTestDTO;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.chatai.config.AiConfigAdminService;
import com.github.paicoding.forum.service.chatai.service.impl.ali.AliIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.chatgpt.ChatGptIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.doubao.DoubaoConfig;
import com.github.paicoding.forum.service.chatai.service.impl.doubao.DoubaoIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.pai.PaiAiDemoServiceImpl;
import com.github.paicoding.forum.service.chatai.service.impl.xunfei.XunFeiIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuCodingIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 配置管理服务
 *
 * @author Codex
 * @date 2026/3/23
 */
@Service
public class AiConfigAdminServiceImpl implements AiConfigAdminService {
    private static final String COMMENT_PREFIX = "AI 配置 - ";
    private static final String DEFAULT_TEST_PROMPT = "请只回复“连接成功”四个字";

    private static final String KEY_AI_SOURCE = "ai.source";

    private static final String KEY_CHATGPT_MAIN = "chatgpt.main";
    private static final String KEY_CHATGPT_35_KEYS = "chatgpt.conf.CHAT_GPT_3_5.keys";
    private static final String KEY_CHATGPT_35_PROXY = "chatgpt.conf.CHAT_GPT_3_5.proxy";
    private static final String KEY_CHATGPT_35_API_HOST = "chatgpt.conf.CHAT_GPT_3_5.apiHost";
    private static final String KEY_CHATGPT_35_TIMEOUT = "chatgpt.conf.CHAT_GPT_3_5.timeOut";
    private static final String KEY_CHATGPT_35_MAX_TOKEN = "chatgpt.conf.CHAT_GPT_3_5.maxToken";
    private static final String KEY_CHATGPT_4_KEYS = "chatgpt.conf.CHAT_GPT_4.keys";
    private static final String KEY_CHATGPT_4_PROXY = "chatgpt.conf.CHAT_GPT_4.proxy";
    private static final String KEY_CHATGPT_4_API_HOST = "chatgpt.conf.CHAT_GPT_4.apiHost";
    private static final String KEY_CHATGPT_4_TIMEOUT = "chatgpt.conf.CHAT_GPT_4.timeOut";
    private static final String KEY_CHATGPT_4_MAX_TOKEN = "chatgpt.conf.CHAT_GPT_4.maxToken";

    private static final String KEY_ZHIPU_API_SECRET = "zhipu.apiSecretKey";
    private static final String KEY_ZHIPU_REQUEST_ID_TEMPLATE = "zhipu.requestIdTemplate";
    private static final String KEY_ZHIPU_MODEL = "zhipu.model";
    private static final String KEY_ZHIPU_CODING_API_KEY = "zhipu.coding.apiKey";
    private static final String KEY_ZHIPU_CODING_API_HOST = "zhipu.coding.apiHost";
    private static final String KEY_ZHIPU_CODING_MODEL = "zhipu.coding.model";
    private static final String KEY_ZHIPU_CODING_TIMEOUT = "zhipu.coding.timeout";

    private static final String KEY_XUNFEI_HOST_URL = "xunfei.hostUrl";
    private static final String KEY_XUNFEI_DOMAIN = "xunfei.domain";
    private static final String KEY_XUNFEI_APP_ID = "xunfei.appId";
    private static final String KEY_XUNFEI_API_KEY = "xunfei.apiKey";
    private static final String KEY_XUNFEI_API_SECRET = "xunfei.apiSecret";
    private static final String KEY_XUNFEI_API_PASSWORD = "xunfei.APIPassword";

    private static final String KEY_DEEPSEEK_API_KEY = "deepseek.apiKey";
    private static final String KEY_DEEPSEEK_API_HOST = "deepseek.apiHost";
    private static final String KEY_DEEPSEEK_MODEL = "deepseek.model";
    private static final String KEY_DEEPSEEK_TIMEOUT = "deepseek.timeout";

    private static final String KEY_DOUBAO_API_KEY = "doubao.api-key";
    private static final String KEY_DOUBAO_API_HOST = "doubao.api-host";
    private static final String KEY_DOUBAO_END_POINT = "doubao.end-point";

    private static final String KEY_ALI_MODEL = "ali.model";

    @Resource
    private ConfigDao configDao;

    @Resource
    private DynamicConfigContainer dynamicConfigContainer;

    @Resource
    private ChatFacade chatFacade;

    @Resource
    private AiConfig aiConfig;

    @Resource
    private ChatGptIntegration.ChatGptConfig chatGptConfig;

    @Resource
    private ZhipuIntegration.ZhipuConfig zhipuConfig;
    @Resource
    private ZhipuCodingIntegration.ZhipuCodingConfig zhipuCodingConfig;

    @Resource
    private XunFeiIntegration.XunFeiConfig xunFeiConfig;

    @Resource
    private DeepSeekIntegration.DeepSeekConf deepSeekConfig;

    @Resource
    private DoubaoConfig doubaoConfig;

    @Resource
    private AliIntegration.AliConfig aliConfig;
    @Resource
    private ChatGptIntegration chatGptIntegration;
    @Resource
    private ZhipuIntegration zhipuIntegration;
    @Resource
    private ZhipuCodingIntegration zhipuCodingIntegration;
    @Resource
    private XunFeiIntegration xunFeiIntegration;
    @Resource
    private DeepSeekIntegration deepSeekIntegration;
    @Resource
    private DoubaoIntegration doubaoIntegration;
    @Resource
    private AliIntegration aliIntegration;
    @Resource
    private PaiAiDemoServiceImpl paiAiDemoService;

    @Override
    public AiConfigAdminDTO getConfig() {
        AiConfigAdminDTO dto = new AiConfigAdminDTO();
        dto.setSources(aiConfig.getSource() == null ? Collections.emptyList() : new ArrayList<>(aiConfig.getSource()));
        dto.setChatGpt(buildChatGptDto());
        dto.setZhipu(buildZhipuDto());
        dto.setZhipuCoding(buildZhipuCodingDto());
        dto.setXunFei(buildXunFeiDto());
        dto.setDeepSeek(buildDeepSeekDto());
        dto.setDoubao(buildDoubaoDto());
        dto.setAli(buildAliDto());
        return dto;
    }

    @Override
    public void save(AiConfigAdminReq req) {
        if (req == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "AI 配置不能为空");
        }

        saveAiSource(req.getSources());
        saveChatGpt(req.getChatGpt());
        saveZhipu(req.getZhipu());
        saveZhipuCoding(req.getZhipuCoding());
        saveXunFei(req.getXunFei());
        saveDeepSeek(req.getDeepSeek());
        saveDoubao(req.getDoubao());
        saveAli(req.getAli());

        dynamicConfigContainer.forceRefresh();
        chatFacade.refreshAiSourceCache(Collections.<AISourceEnum>emptySet());
    }

    @Override
    public AiConfigTestDTO test(AiConfigTestReq req) {
        if (req == null || req.getSource() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "测试模型不能为空");
        }

        long start = System.currentTimeMillis();
        String prompt = StringUtils.defaultIfBlank(req.getPrompt(), DEFAULT_TEST_PROMPT);
        AISourceEnum source = req.getSource();
        try {
            String answer = doTest(source, prompt);
            return buildTestResult(source, true, "连通性测试成功", answer, start);
        } catch (Exception e) {
            return buildTestResult(source, false, StringUtils.defaultIfBlank(e.getMessage(), "连通性测试失败"), null, start);
        }
    }

    private void saveAiSource(List<AISourceEnum> sources) {
        if (sources == null) {
            return;
        }
        upsertConfig(KEY_AI_SOURCE, joinEnumList(sources), "启用的 AI 模型源");
    }

    private void saveChatGpt(AiConfigAdminReq.ChatGptConfig chatGpt) {
        if (chatGpt == null) {
            return;
        }

        if (chatGpt.getMain() != null) {
            upsertConfig(KEY_CHATGPT_MAIN, chatGpt.getMain().name(), "ChatGPT 默认模型");
        }
        saveGptModel("3.5", chatGpt.getGpt35(), KEY_CHATGPT_35_KEYS, KEY_CHATGPT_35_PROXY,
                KEY_CHATGPT_35_API_HOST, KEY_CHATGPT_35_TIMEOUT, KEY_CHATGPT_35_MAX_TOKEN);
        saveGptModel("4", chatGpt.getGpt4(), KEY_CHATGPT_4_KEYS, KEY_CHATGPT_4_PROXY,
                KEY_CHATGPT_4_API_HOST, KEY_CHATGPT_4_TIMEOUT, KEY_CHATGPT_4_MAX_TOKEN);
    }

    private void saveGptModel(String modelName, AiConfigAdminReq.GptModelConfig config, String keysKey,
                              String proxyKey, String apiHostKey, String timeoutKey, String maxTokenKey) {
        if (config == null) {
            return;
        }

        if (config.getKeys() != null) {
            upsertConfig(keysKey, joinStringList(config.getKeys()), "ChatGPT " + modelName + " API Keys");
        }
        if (config.getProxy() != null) {
            upsertConfig(proxyKey, String.valueOf(config.getProxy()), "ChatGPT " + modelName + " 是否启用代理");
        }
        if (config.getApiHost() != null) {
            upsertConfig(apiHostKey, config.getApiHost(), "ChatGPT " + modelName + " API Host");
        }
        if (config.getTimeOut() != null) {
            upsertConfig(timeoutKey, String.valueOf(config.getTimeOut()), "ChatGPT " + modelName + " 超时时间");
        }
        if (config.getMaxToken() != null) {
            upsertConfig(maxTokenKey, String.valueOf(config.getMaxToken()), "ChatGPT " + modelName + " 最大 Token");
        }
    }

    private void saveZhipu(AiConfigAdminReq.ZhipuConfig zhipu) {
        if (zhipu == null) {
            return;
        }

        if (zhipu.getApiSecretKey() != null) {
            upsertConfig(KEY_ZHIPU_API_SECRET, zhipu.getApiSecretKey(), "智谱 API Secret");
        }
        if (zhipu.getRequestIdTemplate() != null) {
            upsertConfig(KEY_ZHIPU_REQUEST_ID_TEMPLATE, zhipu.getRequestIdTemplate(), "智谱请求 ID 模板");
        }
        if (zhipu.getModel() != null) {
            upsertConfig(KEY_ZHIPU_MODEL, zhipu.getModel(), "智谱模型");
        }
    }

    private void saveZhipuCoding(AiConfigAdminReq.ZhipuCodingConfig zhipuCoding) {
        if (zhipuCoding == null) {
            return;
        }

        if (zhipuCoding.getApiKey() != null) {
            upsertConfig(KEY_ZHIPU_CODING_API_KEY, zhipuCoding.getApiKey(), "智谱 Coding API Key");
        }
        if (zhipuCoding.getApiHost() != null) {
            upsertConfig(KEY_ZHIPU_CODING_API_HOST, zhipuCoding.getApiHost(), "智谱 Coding API Host");
        }
        if (zhipuCoding.getModel() != null) {
            upsertConfig(KEY_ZHIPU_CODING_MODEL, zhipuCoding.getModel(), "智谱 Coding 模型");
        }
        if (zhipuCoding.getTimeout() != null) {
            upsertConfig(KEY_ZHIPU_CODING_TIMEOUT, String.valueOf(zhipuCoding.getTimeout()), "智谱 Coding 超时时间");
        }
    }

    private void saveXunFei(AiConfigAdminReq.XunFeiConfig xunFei) {
        if (xunFei == null) {
            return;
        }

        if (xunFei.getHostUrl() != null) {
            upsertConfig(KEY_XUNFEI_HOST_URL, xunFei.getHostUrl(), "讯飞 Host URL");
        }
        if (xunFei.getDomain() != null) {
            upsertConfig(KEY_XUNFEI_DOMAIN, xunFei.getDomain(), "讯飞领域");
        }
        if (xunFei.getAppId() != null) {
            upsertConfig(KEY_XUNFEI_APP_ID, xunFei.getAppId(), "讯飞 AppId");
        }
        if (xunFei.getApiKey() != null) {
            upsertConfig(KEY_XUNFEI_API_KEY, xunFei.getApiKey(), "讯飞 API Key");
        }
        if (xunFei.getApiSecret() != null) {
            upsertConfig(KEY_XUNFEI_API_SECRET, xunFei.getApiSecret(), "讯飞 API Secret");
        }
        if (xunFei.getApiPassword() != null) {
            upsertConfig(KEY_XUNFEI_API_PASSWORD, xunFei.getApiPassword(), "讯飞 API Password");
        }
    }

    private void saveDeepSeek(AiConfigAdminReq.DeepSeekConfig deepSeek) {
        if (deepSeek == null) {
            return;
        }

        if (deepSeek.getApiKey() != null) {
            upsertConfig(KEY_DEEPSEEK_API_KEY, deepSeek.getApiKey(), "DeepSeek API Key");
        }
        if (deepSeek.getApiHost() != null) {
            upsertConfig(KEY_DEEPSEEK_API_HOST, deepSeek.getApiHost(), "DeepSeek API Host");
        }
        if (deepSeek.getModel() != null) {
            upsertConfig(KEY_DEEPSEEK_MODEL, deepSeek.getModel(), "DeepSeek 模型");
        }
        if (deepSeek.getTimeout() != null) {
            upsertConfig(KEY_DEEPSEEK_TIMEOUT, String.valueOf(deepSeek.getTimeout()), "DeepSeek 超时时间");
        }
    }

    private void saveDoubao(AiConfigAdminReq.DoubaoConfig doubao) {
        if (doubao == null) {
            return;
        }

        if (doubao.getApiKey() != null) {
            upsertConfig(KEY_DOUBAO_API_KEY, doubao.getApiKey(), "豆包 API Key");
        }
        if (doubao.getApiHost() != null) {
            upsertConfig(KEY_DOUBAO_API_HOST, doubao.getApiHost(), "豆包 API Host");
        }
        if (doubao.getEndPoint() != null) {
            upsertConfig(KEY_DOUBAO_END_POINT, doubao.getEndPoint(), "豆包 EndPoint");
        }
    }

    private void saveAli(AiConfigAdminReq.AliConfig ali) {
        if (ali == null) {
            return;
        }

        if (ali.getModel() != null) {
            upsertConfig(KEY_ALI_MODEL, ali.getModel(), "阿里模型");
        }
    }

    private void upsertConfig(String key, String value, String comment) {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(key);
        if (config == null) {
            GlobalConfigDO globalConfigDO = new GlobalConfigDO();
            globalConfigDO.setKey(key);
            globalConfigDO.setValue(StringUtils.defaultString(value));
            globalConfigDO.setComment(COMMENT_PREFIX + comment);
            configDao.save(globalConfigDO);
            return;
        }

        config.setValue(StringUtils.defaultString(value));
        config.setComment(COMMENT_PREFIX + comment);
        configDao.updateById(config);
    }

    private AiConfigAdminDTO.ChatGptConfig buildChatGptDto() {
        AiConfigAdminDTO.ChatGptConfig dto = new AiConfigAdminDTO.ChatGptConfig();
        dto.setMain(chatGptConfig.getMain());
        dto.setGpt35(buildGptModelDto(chatGptConfig.getConf() == null ? null : chatGptConfig.getConf().get(AISourceEnum.CHAT_GPT_3_5)));
        dto.setGpt4(buildGptModelDto(chatGptConfig.getConf() == null ? null : chatGptConfig.getConf().get(AISourceEnum.CHAT_GPT_4)));
        return dto;
    }

    private AiConfigAdminDTO.GptModelConfig buildGptModelDto(ChatGptIntegration.GptConf conf) {
        AiConfigAdminDTO.GptModelConfig dto = new AiConfigAdminDTO.GptModelConfig();
        if (conf == null) {
            dto.setKeys(Collections.<String>emptyList());
            return dto;
        }
        dto.setKeys(conf.getKeys() == null ? Collections.<String>emptyList() : new ArrayList<String>(conf.getKeys()));
        dto.setProxy(conf.isProxy());
        dto.setApiHost(conf.getApiHost());
        dto.setTimeOut(conf.getTimeOut());
        dto.setMaxToken(conf.getMaxToken());
        return dto;
    }

    private AiConfigAdminDTO.ZhipuConfig buildZhipuDto() {
        AiConfigAdminDTO.ZhipuConfig dto = new AiConfigAdminDTO.ZhipuConfig();
        dto.setApiSecretKey(zhipuConfig.getApiSecretKey());
        dto.setRequestIdTemplate(zhipuConfig.getRequestIdTemplate());
        dto.setModel(zhipuConfig.getModel());
        return dto;
    }

    private AiConfigAdminDTO.ZhipuCodingConfig buildZhipuCodingDto() {
        AiConfigAdminDTO.ZhipuCodingConfig dto = new AiConfigAdminDTO.ZhipuCodingConfig();
        dto.setApiKey(zhipuCodingConfig.getApiKey());
        dto.setApiHost(zhipuCodingConfig.getApiHost());
        dto.setModel(zhipuCodingConfig.getModel());
        dto.setTimeout(zhipuCodingConfig.getTimeout());
        return dto;
    }

    private AiConfigAdminDTO.XunFeiConfig buildXunFeiDto() {
        AiConfigAdminDTO.XunFeiConfig dto = new AiConfigAdminDTO.XunFeiConfig();
        dto.setHostUrl(xunFeiConfig.getHostUrl());
        dto.setDomain(xunFeiConfig.getDomain());
        dto.setAppId(xunFeiConfig.getAppId());
        dto.setApiKey(xunFeiConfig.getApiKey());
        dto.setApiSecret(xunFeiConfig.getApiSecret());
        dto.setApiPassword(xunFeiConfig.getAPIPassword());
        return dto;
    }

    private AiConfigAdminDTO.DeepSeekConfig buildDeepSeekDto() {
        AiConfigAdminDTO.DeepSeekConfig dto = new AiConfigAdminDTO.DeepSeekConfig();
        dto.setApiKey(deepSeekConfig.getApiKey());
        dto.setApiHost(deepSeekConfig.getApiHost());
        dto.setModel(deepSeekConfig.getModel());
        dto.setTimeout(deepSeekConfig.getTimeout());
        return dto;
    }

    private AiConfigAdminDTO.DoubaoConfig buildDoubaoDto() {
        AiConfigAdminDTO.DoubaoConfig dto = new AiConfigAdminDTO.DoubaoConfig();
        dto.setApiKey(doubaoConfig.getApiKey());
        dto.setApiHost(doubaoConfig.getApiHost());
        dto.setEndPoint(doubaoConfig.getEndPoint());
        return dto;
    }

    private AiConfigAdminDTO.AliConfig buildAliDto() {
        AiConfigAdminDTO.AliConfig dto = new AiConfigAdminDTO.AliConfig();
        dto.setModel(aliConfig.getModel());
        return dto;
    }

    private String joinEnumList(List<AISourceEnum> values) {
        if (CollectionUtils.isEmpty(values)) {
            return "";
        }
        return values.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private String joinStringList(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return "";
        }
        return values.stream()
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private String doTest(AISourceEnum source, String prompt) {
        switch (source) {
            case CHAT_GPT_3_5:
            case CHAT_GPT_4:
                return testChatGpt(source, prompt);
            case ZHI_PU_AI:
                return testZhipu(prompt);
            case ZHIPU_CODING:
                return testZhipuCoding(prompt);
            case XUN_FEI_AI:
                return xunFeiIntegration.testConnection(prompt);
            case ALI_AI:
                return testAli(prompt);
            case DEEP_SEEK:
                return testDeepSeek(prompt);
            case DOU_BAO_AI:
                return testDoubao(prompt);
            case PAI_AI:
                return testPai(prompt);
            default:
                throw new IllegalStateException("暂不支持该模型的连通性测试: " + source);
        }
    }

    private String testChatGpt(AISourceEnum model, String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!chatGptIntegration.directReturn(0L, item, model)) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "ChatGPT 连通性测试失败"));
        }
        return item.getAnswer();
    }

    private String testZhipu(String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!zhipuIntegration.directReturn(0L, item)) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "智谱连通性测试失败"));
        }
        return item.getAnswer();
    }

    private String testZhipuCoding(String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!zhipuCodingIntegration.directReturn(item)) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "智谱 Coding 连通性测试失败"));
        }
        return item.getAnswer();
    }

    private String testAli(String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!aliIntegration.directReturn(0L, item)) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "阿里连通性测试失败"));
        }
        return item.getAnswer();
    }

    private String testDeepSeek(String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!deepSeekIntegration.directReturn(item)) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "DeepSeek 连通性测试失败"));
        }
        return item.getAnswer();
    }

    private String testDoubao(String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        if (!com.github.paicoding.forum.api.model.enums.ai.AiChatStatEnum.END.equals(doubaoIntegration.directAnswer(0L, item))) {
            throw new IllegalStateException(StringUtils.defaultIfBlank(item.getAnswer(), "豆包连通性测试失败"));
        }
        return item.getAnswer();
    }

    private String testPai(String prompt) {
        ChatItemVo item = new ChatItemVo().initQuestion(prompt);
        paiAiDemoService.doAnswer(0L, item);
        return item.getAnswer();
    }

    private AiConfigTestDTO buildTestResult(AISourceEnum source, boolean success, String message, String answer, long start) {
        AiConfigTestDTO dto = new AiConfigTestDTO();
        dto.setSource(source);
        dto.setSuccess(success);
        dto.setMessage(message);
        dto.setAnswer(answer);
        dto.setCostMs(System.currentTimeMillis() - start);
        return dto;
    }
}
