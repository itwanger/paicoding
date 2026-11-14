package com.github.paicoding.forum.service.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.article.AiIllustrationGenerateReq;
import com.github.paicoding.forum.api.model.vo.article.AiIllustrationGenerateRes;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.service.article.service.AiIllustrationService;
import com.github.paicoding.forum.service.chatai.service.impl.zhipu.ZhipuIntegration;
import com.github.paicoding.forum.service.image.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class AiIllustrationServiceImpl implements AiIllustrationService {

    private static final long PROVIDER_TIMEOUT_SECONDS = 300L;

    @Autowired
    private ZhipuIntegration zhipuIntegration;

    @Autowired
    private ImageService imageService;

    private static final String ANALYSIS_PROMPT_TEMPLATE =
            "你是一个专业的中文文章配图设计师。请分析以下文章，提炼出适合配图的关键节点。\n\n" +
            "文章标题：%s\n" +
            "文章正文：%s\n\n" +
            "要求：\n" +
            "1. 分析文章的核心观点和认知转折\n" +
            "2. 选择4-%d个最适合用图解释的位置（优先选择核心判断、流程、结构、前后对比、概念隐喻）\n" +
            "3. 每个配图需要包含：\n" +
            "   - theme：配图主题（简短概括）\n" +
            "   - coreIdea：核心意思（这张图要表达什么）\n" +
            "   - placement：建议放置位置（段落序号，从1开始）\n" +
            "   - structureType：结构类型（Workflow/系统局部/前后对比/角色状态/概念隐喻/方法分层/地图路线/小漫画分镜）\n" +
            "   - composition：画面描述（小黑在哪里、正在做什么、主要物件是什么）\n" +
            "   - elements：建议元素（用/分隔，不超过4个）\n" +
            "   - labels：建议中文标注词（用/分隔，不超过5个）\n" +
            "4. 不要平均配图，优先选择认知锚点\n" +
            "5. 返回格式必须是JSON数组，不要添加任何额外文字\n\n" +
            "示例格式：\n" +
            "[{\"theme\":\"主题1\",\"coreIdea\":\"核心意思1\",\"placement\":1,\"structureType\":\"概念隐喻\",\"composition\":\"小黑在...\",\"elements\":\"元素1/元素2/元素3\",\"labels\":\"标注1/标注2\"}]";

    private static final String IMAGE_PROMPT_TEMPLATE =
            "Generate one standalone 16:9 horizontal Chinese article illustration.\n\n" +
            "Visual DNA:\n" +
            "Pure white background. Minimalist black hand-drawn line art. Slightly wobbly pen lines. Lots of empty white space. Sparse red/orange/blue handwritten Chinese annotations. Clean absurd product-sketch feeling. No gradients, no shadows, no paper texture, no complex background, no commercial vector style, no PPT infographic look, no cute mascot poster, no children's illustration, no realistic UI.\n\n" +
            "Recurring IP character required:\n" +
            "小黑, a small solid-black absurd creature with white dot eyes, tiny thin legs, blank serious expression, slightly uneven hand-drawn body shape. 小黑 must perform the core conceptual action, not decorate the scene. Make 小黑 serious, deadpan, and slightly bizarre, not cute.\n\n" +
            "Theme:\n" +
            "%s\n\n" +
            "Structure type:\n" +
            "%s\n\n" +
            "Core idea:\n" +
            "%s\n\n" +
            "Composition:\n" +
            "%s\n\n" +
            "Suggested elements:\n" +
            "%s\n\n" +
            "Chinese handwritten labels:\n" +
            "%s\n\n" +
            "Color use:\n" +
            "Black for main line art and 小黑. Orange for main flow/path/arrows. Red only for key warnings/problems/results. Blue only for secondary notes or feedback/system state.\n\n" +
            "Constraints:\n" +
            "One image explains only one core structure. Keep the main subject around 40%%-60%% of the canvas. Preserve at least 35%% blank white space. Use at most 5-8 short handwritten Chinese labels. Do not write a title in the top-left corner. Do not write the structure type on the image. Do not make it a formal diagram, course slide, or dense explainer. Do not copy prior examples or reuse known case compositions unless explicitly requested; invent a fresh visual metaphor for this specific article. It should be clear but not instructional, interesting but not childish, strange but clean.";

    @Override
    public AiIllustrationGenerateRes generateIllustrations(AiIllustrationGenerateReq req) {
        if (StringUtils.isBlank(req.getTitle()) || StringUtils.isBlank(req.getContent())) {
            throw new IllegalArgumentException("文章标题和正文内容不能为空");
        }

        int count = req.getCount() != null ? req.getCount() : 4;
        if (count < 1 || count > 8) {
            throw new IllegalArgumentException("配图数量必须在1-8之间");
        }

        String content = req.getContent();
        if (content.length() > 3000) {
            content = content.substring(0, 3000);
        }

        List<AiIllustrationGenerateRes.IllustrationItem> plan = analyzeArticle(req.getTitle(), content, count);

        List<AiIllustrationGenerateRes.IllustrationItem> result = new ArrayList<>();
        int index = 1;
        for (AiIllustrationGenerateRes.IllustrationItem item : plan) {
            try {
                String imageUrl = generateImage(item);
                item.setImageUrl(imageUrl);
                item.setIndex(index++);
                result.add(item);
                log.info("配图生成成功: {}", item.getTheme());
            } catch (Exception e) {
                log.warn("配图生成失败: {}, 原因: {}", item.getTheme(), e.getMessage());
            }
        }

        AiIllustrationGenerateRes res = new AiIllustrationGenerateRes();
        res.setIllustrations(result);
        return res;
    }

    private List<AiIllustrationGenerateRes.IllustrationItem> analyzeArticle(String title, String content, int count) {
        String prompt = String.format(ANALYSIS_PROMPT_TEMPLATE, title, content, count);

        log.info("开始分析文章，生成配图策略");
        ChatItemVo zhipuItem = new ChatItemVo().initQuestion(prompt);
        callProviderWithTimeout("智谱AI", () -> zhipuIntegration.directReturn(0L, zhipuItem));
        String answer = zhipuItem.getAnswer();

        if (StringUtils.isBlank(answer)) {
            throw illustrationException("智谱AI返回内容为空");
        }

        try {
            return parseAnalysisResponse(answer);
        } catch (Exception e) {
            log.error("解析智谱AI响应失败", e);
            throw illustrationException("解析智谱AI响应失败: " + e.getMessage());
        }
    }

    private String generateImage(AiIllustrationGenerateRes.IllustrationItem item) {
        String prompt = String.format(IMAGE_PROMPT_TEMPLATE,
                item.getTheme(),
                item.getStructureType(),
                item.getCoreIdea(),
                item.getComposition(),
                item.getElements(),
                item.getLabels());

        log.info("开始生成配图: {}", item.getTheme());

        ChatItemVo zhipuItem = new ChatItemVo().initQuestion(prompt);
        callProviderWithTimeout("智谱AI", () -> zhipuIntegration.directReturn(0L, zhipuItem));
        String answer = zhipuItem.getAnswer();

        if (StringUtils.isBlank(answer)) {
            throw illustrationException("智谱AI图片生成返回为空");
        }

        String imageUrl = extractImageUrl(answer);
        if (StringUtils.isBlank(imageUrl)) {
            throw illustrationException("未从响应中提取到图片URL");
        }

        return imageService.saveImg(imageUrl);
    }

    private List<AiIllustrationGenerateRes.IllustrationItem> parseAnalysisResponse(String answer) {
        log.info("智谱AI分析响应: {}", answer);

        String cleanedAnswer = answer.trim();
        if (cleanedAnswer.startsWith("\"") && cleanedAnswer.endsWith("\"")) {
            cleanedAnswer = JSON.parseObject(cleanedAnswer, String.class);
        }

        cleanedAnswer = cleanedAnswer.replaceAll("\\n", "")
                .replaceAll("\\r", "")
                .trim();

        if (!cleanedAnswer.startsWith("[")) {
            int arrayStart = cleanedAnswer.indexOf("[");
            if (arrayStart >= 0) {
                cleanedAnswer = cleanedAnswer.substring(arrayStart);
            }
        }

        JSONArray array = JSON.parseArray(cleanedAnswer);
        if (array == null || array.isEmpty()) {
            throw illustrationException("智谱AI返回的配图策略为空");
        }

        List<AiIllustrationGenerateRes.IllustrationItem> items = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            AiIllustrationGenerateRes.IllustrationItem item = new AiIllustrationGenerateRes.IllustrationItem();
            item.setTheme(obj.getString("theme"));
            item.setCoreIdea(obj.getString("coreIdea"));
            item.setPlacement(obj.getInteger("placement"));
            item.setStructureType(obj.getString("structureType"));
            item.setComposition(obj.getString("composition"));
            item.setElements(obj.getString("elements"));
            item.setLabels(obj.getString("labels"));
            items.add(item);
        }

        return items;
    }

    private String extractImageUrl(String answer) {
        try {
            String cleanedAnswer = answer.trim();
            if (cleanedAnswer.startsWith("\"") && cleanedAnswer.endsWith("\"")) {
                cleanedAnswer = JSON.parseObject(cleanedAnswer, String.class);
            }

            JSONObject obj = JSON.parseObject(cleanedAnswer);
            if (obj != null && obj.containsKey("url")) {
                return obj.getString("url");
            }

            JSONArray choices = obj != null ? obj.getJSONArray("choices") : null;
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice != null ? choice.getJSONObject("message") : null;
                String content = message != null ? message.getString("content") : null;
                if (content != null) {
                    JSONObject contentObj = JSON.parseObject(content);
                    if (contentObj != null && contentObj.containsKey("url")) {
                        return contentObj.getString("url");
                    }
                    if (content.contains("http") && content.contains("https")) {
                        int start = content.indexOf("http");
                        int end = content.indexOf("\"", start);
                        if (end > start) {
                            return content.substring(start, end);
                        }
                    }
                }
            }

            if (answer.contains("http") && answer.contains(".png")) {
                int start = answer.indexOf("http");
                int end = answer.indexOf(".png") + 4;
                if (end > start) {
                    return answer.substring(start, end);
                }
            }
            if (answer.contains("http") && answer.contains(".jpg")) {
                int start = answer.indexOf("http");
                int end = answer.indexOf(".jpg") + 4;
                if (end > start) {
                    return answer.substring(start, end);
                }
            }
            if (answer.contains("http") && answer.contains(".webp")) {
                int start = answer.indexOf("http");
                int end = answer.indexOf(".webp") + 5;
                if (end > start) {
                    return answer.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.warn("提取图片URL失败: {}", e.getMessage());
        }

        throw illustrationException("无法从响应中提取图片URL");
    }

    private void callProviderWithTimeout(String providerName, Callable<Boolean> callable) {
        boolean success;
        try {
            success = Boolean.TRUE.equals(AsyncUtil.callWithTimeLimit(PROVIDER_TIMEOUT_SECONDS, TimeUnit.SECONDS, callable));
        } catch (TimeoutException e) {
            log.warn("{}生成配图超时，限制：{}秒", providerName, PROVIDER_TIMEOUT_SECONDS, e);
            throw illustrationException(providerName + "调用超时，限制：" + PROVIDER_TIMEOUT_SECONDS + "秒");
        } catch (ForumException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}生成配图异常", providerName, e);
            throw illustrationException(providerName + "调用异常: " + e.getMessage());
        }
        if (!success) {
            throw illustrationException(providerName + "调用失败");
        }
    }

    private RuntimeException illustrationException(String message) {
        return ExceptionUtil.of(StatusEnum.UNEXPECT_ERROR, message);
    }
}