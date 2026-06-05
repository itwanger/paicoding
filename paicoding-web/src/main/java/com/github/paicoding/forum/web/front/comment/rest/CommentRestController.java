package com.github.paicoding.forum.web.front.comment.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.api.model.enums.OperateTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.comment.CommentAiStreamReq;
import com.github.paicoding.forum.api.model.vo.comment.CommentSaveReq;
import com.github.paicoding.forum.api.model.vo.comment.HighlightAiStreamEvent;
import com.github.paicoding.forum.api.model.vo.comment.HighlightAiStreamReq;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.comment.dto.TopCommentDTO;
import com.github.paicoding.forum.api.model.vo.comment.vo.SubCommentListVO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.chatai.bot.AiBots;
import com.github.paicoding.forum.service.comment.repository.entity.CommentDO;
import com.github.paicoding.forum.service.comment.service.CommentReadService;
import com.github.paicoding.forum.service.comment.service.CommentWriteService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.web.component.TemplateEngineHelper;
import com.github.paicoding.forum.web.front.article.vo.ArticleDetailVo;
import com.github.paicoding.forum.web.front.comment.vo.CommentPageVo;
import com.github.paicoding.forum.web.front.comment.vo.HighlightCommentVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 评论
 *
 * @author louzai
 * @date : 2022/4/22 10:56
 **/
@Slf4j
@RestController
@RequestMapping(path = "comment/api")
public class CommentRestController {
    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private CommentReadService commentReadService;

    @Autowired
    private CommentWriteService commentWriteService;

    @Autowired
    private UserFootService userFootService;

    @Autowired
    private TemplateEngineHelper templateEngineHelper;

    @Autowired
    private AiBots aiBots;

    /**
     * 评论列表页
     *
     * @param articleId
     * @return
     */
    @ResponseBody
    @RequestMapping(path = "list")
    public ResVo<List<TopCommentDTO>> list(Long articleId, Long pageNum, Long pageSize) {
        if (NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        pageNum = Optional.ofNullable(pageNum).orElse(PageParam.DEFAULT_PAGE_NUM);
        pageSize = Optional.ofNullable(pageSize).orElse(PageParam.DEFAULT_PAGE_SIZE);
        List<TopCommentDTO> result = commentReadService.getArticleComments(articleId, PageParam.newPageInstance(pageNum, pageSize));
        return ResVo.ok(result);
    }

    /**
     * 保存评论
     *
     * @param req
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "post")
    @ResponseBody
    public ResVo<String> save(@RequestBody CommentSaveReq req) {
        if (req.getArticleId() == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        ArticleDO article = articleReadService.queryBasicArticle(req.getArticleId());
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }

        // 保存评论
        req.setUserId(ReqInfoContext.getReqInfo().getUserId());
        req.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        commentWriteService.saveComment(req);

        // 返回新的评论信息，用于实时更新详情也的评论列表
        ArticleDetailVo vo = new ArticleDetailVo();
        vo.setArticle(ArticleConverter.toDto(article));
        // 评论信息
        List<TopCommentDTO> comments = commentReadService.getArticleComments(req.getArticleId(), PageParam.newPageInstance());
        vo.setComments(comments);
        vo.setTopCommentTotal(commentReadService.queryTopCommentCount(req.getArticleId()));

        // 热门评论
        TopCommentDTO hotComment = commentReadService.queryHotComment(req.getArticleId());
        vo.setHotComment(hotComment);
        String content = templateEngineHelper.render("views/article-detail/comment/index", vo);
        return ResVo.ok(content);
    }


    /**
     * 划线评论
     *
     * @param req
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "highlightComment")
    @ResponseBody
    public ResVo<HighlightCommentVo> highlightComment(@RequestBody CommentSaveReq req) {
        if (req.getArticleId() == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        ArticleDO article = articleReadService.queryBasicArticle(req.getArticleId());
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }

        // 保存评论
        req.setUserId(ReqInfoContext.getReqInfo().getUserId());
        req.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        Long commentId = commentWriteService.saveComment(req);
        TopCommentDTO comments = commentReadService.queryTopComments(commentId);
        String content = templateEngineHelper.render("components/comment/comment-highlight", comments);
        HighlightCommentVo vo = new HighlightCommentVo();
        vo.setCommentId(commentId);
        vo.setHtml(content);
        return ResVo.ok(vo);
    }

    /**
     * 提交带 @ 机器人的划线评论，并实时返回机器人子回复。
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "highlightAiStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter highlightAiStream(@RequestBody HighlightAiStreamReq req) {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> {
            closed.set(true);
            emitter.complete();
        });
        emitter.onError(e -> closed.set(true));

        String requestId = req == null ? String.valueOf(System.currentTimeMillis()) : StringUtils.defaultIfBlank(req.getRequestId(), String.valueOf(System.currentTimeMillis()));
        AiBotEnum bot = req == null ? null : parseAiBot(req.getBot());
        if (req == null) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, null, "划线 AI 请求参数不完整"));
            emitter.complete();
            return emitter;
        }
        if (bot == null) {
            bot = parseAiBotFromContent(req.getCommentContent());
        }
        if (req.getArticleId() == null || req.getHighlight() == null || StringUtils.isBlank(req.getCommentContent()) || bot == null) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, req.getBot(), "划线 AI 请求参数不完整"));
            emitter.complete();
            return emitter;
        }

        ArticleDO article = articleReadService.queryBasicArticle(req.getArticleId());
        if (article == null) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, bot.getNickName(), "文章不存在"));
            emitter.complete();
            return emitter;
        }
        AiBotEnum finalBot = bot;

        Long commentId;
        try {
            commentId = saveHighlightUserComment(req);
            sendAiEvent(emitter, HighlightAiStreamEvent.comment(requestId, finalBot.getNickName(), commentId));
        } catch (Exception e) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, finalBot.getNickName(), "评论发表失败，请稍后再试"));
            emitter.complete();
            return emitter;
        }

        sendAiEvent(emitter, HighlightAiStreamEvent.start(requestId, finalBot.getNickName()));
        AtomicInteger sentLength = new AtomicInteger(0);
        String sourceBizId = "comment:" + commentId + "_" + ReqInfoContext.getReqInfo().getUserId();
        Long finalCommentId = commentId;
        aiBots.triggerStream(finalBot, buildHighlightAiQuestion(finalBot, req), sourceBizId, item -> {
            if (closed.get() || item == null) {
                return;
            }

            String answer = sanitizeAiAnswer(item.getAnswer());
            int lastLength = sentLength.get();
            if (answer.length() > lastLength) {
                String delta = answer.substring(lastLength);
                sentLength.set(answer.length());
                sendAiEvent(emitter, HighlightAiStreamEvent.delta(requestId, finalBot.getNickName(), delta, answer));
            }

            if (isAiAnswerFinished(item)) {
                if (isAiAnswerFailed(answer)) {
                    sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, finalBot.getNickName(), "AI 回复生成失败，请稍后再试")
                            .setCommentId(finalCommentId));
                    closed.set(true);
                    emitter.complete();
                    return;
                }

                try {
                    saveAiHighlightReply(req, finalBot, answer, finalCommentId);
                    sendAiEvent(emitter, HighlightAiStreamEvent.done(requestId, finalBot.getNickName(), finalCommentId, null));
                } catch (Exception e) {
                    log.error("保存划线 AI 回复失败, articleId={}, parentCommentId={}, bot={}", req.getArticleId(), finalCommentId, finalBot, e);
                    sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, finalBot.getNickName(), "AI 回复保存失败，请稍后再试")
                            .setCommentId(finalCommentId));
                } finally {
                    closed.set(true);
                    emitter.complete();
                }
            }
        }, () -> buildHighlightAiSystemPrompt(finalBot, req.getArticleId()));
        return emitter;
    }

    /**
     * 提交带 @ 机器人的普通评论，并实时返回机器人子回复。
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "commentAiStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter commentAiStream(@RequestBody CommentAiStreamReq req) {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        emitter.onCompletion(() -> closed.set(true));
        emitter.onTimeout(() -> {
            closed.set(true);
            emitter.complete();
        });
        emitter.onError(e -> closed.set(true));

        String requestId = req == null ? String.valueOf(System.currentTimeMillis()) : StringUtils.defaultIfBlank(req.getRequestId(), String.valueOf(System.currentTimeMillis()));
        AiBotEnum bot = req == null ? null : parseAiBot(req.getBot());
        if (req == null) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, null, "AI 评论请求参数不完整"));
            emitter.complete();
            return emitter;
        }
        if (bot == null) {
            bot = parseAiBotFromContent(req.getCommentContent());
        }
        if (req.getArticleId() == null || StringUtils.isBlank(req.getCommentContent()) || bot == null) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, req.getBot(), "AI 评论请求参数不完整"));
            emitter.complete();
            return emitter;
        }

        ArticleDO article = articleReadService.queryBasicArticle(req.getArticleId());
        if (article == null) {
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, bot.getNickName(), "文章不存在"));
            emitter.complete();
            return emitter;
        }

        AiBotEnum finalBot = bot;
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        Long commentId;
        Long topCommentId;
        try {
            commentId = saveCommentUserComment(req, userId);
            topCommentId = NumUtil.upZero(req.getTopCommentId()) ? req.getTopCommentId() : commentId;
            sendAiEvent(emitter, HighlightAiStreamEvent.comment(requestId, finalBot.getNickName(), commentId)
                    .setHtml(renderArticleComments(req.getArticleId())));
        } catch (Exception e) {
            log.error("保存 AI 评论用户评论失败, articleId={}, bot={}", req.getArticleId(), finalBot, e);
            sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, finalBot.getNickName(), "评论发表失败，请稍后再试"));
            emitter.complete();
            return emitter;
        }

        sendAiEvent(emitter, HighlightAiStreamEvent.start(requestId, finalBot.getNickName()));
        String sourceBizId = "comment:" + topCommentId + "_" + userId;
        Long finalCommentId = commentId;
        Long finalTopCommentId = topCommentId;
        aiBots.triggerSync(finalBot, buildCommentAiQuestion(req), sourceBizId, answer -> {
            if (closed.get()) {
                return;
            }

            String cleanAnswer = sanitizeAiAnswer(answer);
            if (isAiAnswerFailed(cleanAnswer)) {
                sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, finalBot.getNickName(), "AI 回复生成失败，请稍后再试")
                        .setCommentId(finalCommentId));
                closed.set(true);
                emitter.complete();
                return;
            }

            try {
                saveAiCommentReply(req, finalBot, cleanAnswer, finalCommentId, finalTopCommentId);
                sendAiEvent(emitter, HighlightAiStreamEvent.done(requestId, finalBot.getNickName(), finalCommentId, null));
            } catch (Exception e) {
                log.error("保存评论区 AI 回复失败, articleId={}, parentCommentId={}, bot={}", req.getArticleId(), finalCommentId, finalBot, e);
                sendAiEvent(emitter, HighlightAiStreamEvent.error(requestId, finalBot.getNickName(), "AI 回复保存失败，请稍后再试")
                        .setCommentId(finalCommentId));
            } finally {
                closed.set(true);
                emitter.complete();
            }
        }, () -> buildCommentAiSystemPrompt(finalBot, req.getArticleId()));
        return emitter;
    }

    /**
     * 获取文章的顶级评论列表
     *
     * @param commentId
     * @return
     */
    @Permission(role = UserRole.ALL)
    @GetMapping(path = "listTopComment")
    @ResponseBody
    public ResVo<String> listTopComment(Long commentId) {
        TopCommentDTO comments = commentReadService.queryTopComments(commentId);
        String content = templateEngineHelper.render("components/comment/comment-highlight", comments);
        return ResVo.ok(content);
    }

    /**
     * 获取文章评论区 html 片段
     *
     * @param articleId
     * @return
     */
    @Permission(role = UserRole.ALL)
    @GetMapping(path = "articleCommentsHtml")
    @ResponseBody
    public ResVo<String> articleCommentsHtml(Long articleId) {
        if (NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }
        return ResVo.ok(renderArticleComments(articleId));
    }

    private AiBotEnum parseAiBot(String bot) {
        if (StringUtils.isBlank(bot)) {
            return null;
        }
        try {
            return AiBotEnum.valueOf(bot);
        } catch (Exception e) {
            return null;
        }
    }

    private AiBotEnum parseAiBotFromContent(String content) {
        if (StringUtils.contains(content, "@" + AiBotEnum.HATER_BOT.getNickName())) {
            return AiBotEnum.HATER_BOT;
        }
        if (StringUtils.contains(content, "@" + AiBotEnum.QA_BOT.getNickName())) {
            return AiBotEnum.QA_BOT;
        }
        return null;
    }

    private String buildHighlightAiQuestion(AiBotEnum bot, HighlightAiStreamReq req) {
        String selectedText = req.getHighlight() == null ? "" : StringUtils.defaultString(req.getHighlight().getSelectedText());
        String question = StringUtils.defaultIfBlank(stripAiBotMention(req.getQuestion()), stripAiBotMention(req.getCommentContent()));
        question = StringUtils.defaultIfBlank(question, "请围绕这段划线内容进行回复。");
        if (bot == AiBotEnum.QA_BOT) {
            return "这是我从文章中选择的一段文本：\"" + selectedText + "\"\n" + question;
        }
        return "请围绕下面这段划线内容回复：\"" + selectedText + "\"\n" + question;
    }

    private String stripAiBotMention(String content) {
        return StringUtils.trimToEmpty(content)
                .replace("@" + AiBotEnum.HATER_BOT.getNickName(), "")
                .replace("@" + AiBotEnum.QA_BOT.getNickName(), "")
                .trim();
    }

    private String buildHighlightAiSystemPrompt(AiBotEnum bot, Long articleId) {
        if (bot == AiBotEnum.QA_BOT) {
            String article = articleReadService.queryArticleContentForAI(articleId);
            return bot.getPrompt() + "\n\n" + article;
        }
        return bot.getPrompt() + "\n请直接围绕用户给出的划线内容回复，不要解释自己的角色设定。";
    }

    private boolean isAiAnswerFinished(ChatItemVo item) {
        return item.getAnswerType() == ChatAnswerTypeEnum.JSON
                || item.getAnswerType() == ChatAnswerTypeEnum.TEXT
                || item.getAnswerType() == ChatAnswerTypeEnum.STREAM_END;
    }

    private boolean isAiAnswerFailed(String answer) {
        String content = sanitizeAiAnswer(answer);
        return StringUtils.isBlank(content)
                || StringUtils.contains(content, "AI 回复生成失败")
                || StringUtils.startsWith(content, "Error:")
                || StringUtils.contains(content, "调用失败")
                || StringUtils.contains(content, "未配置")
                || StringUtils.contains(content, "未返回")
                || StringUtils.contains(content, "大模型超时未返回结果");
    }

    private String sanitizeAiAnswer(String answer) {
        String content = StringUtils.trimToEmpty(answer);
        content = content.replaceAll("(?s)(?:\\r?\\n)?Error\\s*:\\s*null\\s*$", "");
        content = content.replaceAll("(?s)(?:\\r?\\n)?Error\\s*:\\s*$", "");
        return StringUtils.trimToEmpty(content);
    }

    private Long saveHighlightUserComment(HighlightAiStreamReq req) {
        CommentSaveReq save = new CommentSaveReq();
        save.setArticleId(req.getArticleId());
        save.setUserId(ReqInfoContext.getReqInfo().getUserId());
        save.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        save.setHighlight(req.getHighlight());
        save.setSkipAiTrigger(true);
        return commentWriteService.saveComment(save);
    }

    private Long saveAiHighlightReply(HighlightAiStreamReq req, AiBotEnum bot, String answer, Long parentCommentId) {
        String content = sanitizeAiAnswer(answer);
        if (StringUtils.isBlank(content)) {
            content = "AI 暂时没有生成有效回复，请稍后再试。";
        }

        CommentSaveReq save = new CommentSaveReq();
        save.setArticleId(req.getArticleId());
        save.setUserId(aiBots.getBotUser(bot).getUserId());
        save.setCommentContent(StringEscapeUtils.escapeHtml3(content));
        save.setParentCommentId(parentCommentId);
        save.setTopCommentId(parentCommentId);
        save.setSkipAiTrigger(true);
        return commentWriteService.saveComment(save);
    }

    private Long saveCommentUserComment(CommentAiStreamReq req, Long userId) {
        CommentSaveReq save = new CommentSaveReq();
        save.setArticleId(req.getArticleId());
        save.setUserId(userId);
        save.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        save.setParentCommentId(req.getParentCommentId());
        save.setTopCommentId(req.getTopCommentId());
        save.setSkipAiTrigger(true);
        return commentWriteService.saveComment(save);
    }

    private Long saveAiCommentReply(CommentAiStreamReq req, AiBotEnum bot, String answer, Long parentCommentId, Long topCommentId) {
        String content = sanitizeAiAnswer(answer);
        if (StringUtils.isBlank(content)) {
            content = "AI 暂时没有生成有效回复，请稍后再试。";
        }

        CommentSaveReq save = new CommentSaveReq();
        save.setArticleId(req.getArticleId());
        save.setUserId(aiBots.getBotUser(bot).getUserId());
        save.setCommentContent(StringEscapeUtils.escapeHtml3(content));
        save.setParentCommentId(parentCommentId);
        save.setTopCommentId(topCommentId);
        save.setSkipAiTrigger(true);
        return commentWriteService.saveComment(save);
    }

    private String buildCommentAiQuestion(CommentAiStreamReq req) {
        return StringUtils.defaultIfBlank(stripAiBotMention(req.getQuestion()), stripAiBotMention(req.getCommentContent()));
    }

    private String buildCommentAiSystemPrompt(AiBotEnum bot, Long articleId) {
        if (bot == AiBotEnum.QA_BOT) {
            String article = articleReadService.queryArticleContentForAI(articleId);
            return bot.getPrompt() + "\n\n" + article;
        }
        return bot.getPrompt();
    }

    private String renderArticleComments(Long articleId) {
        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        ArticleDetailVo vo = new ArticleDetailVo();
        vo.setArticle(ArticleConverter.toDto(article));
        vo.setComments(commentReadService.getArticleComments(articleId, PageParam.newPageInstance()));
        vo.setTopCommentTotal(commentReadService.queryTopCommentCount(articleId));
        vo.setHotComment(commentReadService.queryHotComment(articleId));
        return templateEngineHelper.render("views/article-detail/comment/index", vo);
    }

    private String renderHighlightComment(Long commentId) {
        TopCommentDTO comments = commentReadService.queryTopComments(commentId);
        return templateEngineHelper.render("components/comment/comment-highlight", comments);
    }

    private void sendAiEvent(SseEmitter emitter, HighlightAiStreamEvent event) {
        try {
            synchronized (emitter) {
                emitter.send(SseEmitter.event()
                        .name(event.getType())
                        .data(JsonUtil.toStr(event)));
            }
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 分页加载一级评论 html 片段
     *
     * @param articleId 文章ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论 html 片段
     */
    @Permission(role = UserRole.ALL)
    @GetMapping(path = "listPageHtml")
    @ResponseBody
    public ResVo<CommentPageVo> listPageHtml(Long articleId, Long pageNum, Long pageSize) {
        if (NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }

        pageNum = Optional.ofNullable(pageNum).orElse(PageParam.DEFAULT_PAGE_NUM);
        pageSize = Optional.ofNullable(pageSize).orElse(PageParam.DEFAULT_PAGE_SIZE);

        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }

        List<TopCommentDTO> comments = commentReadService.getArticleComments(articleId, PageParam.newPageInstance(pageNum, pageSize));
        ArticleDetailVo renderVo = new ArticleDetailVo();
        renderVo.setArticle(ArticleConverter.toDto(article));
        renderVo.setComments(comments);

        CommentPageVo result = new CommentPageVo();
        result.setHtml(templateEngineHelper.render("components/comment/comment-page-items", renderVo));
        int topCommentTotal = commentReadService.queryTopCommentCount(articleId);
        result.setHasMore(pageNum * pageSize < topCommentTotal);
        result.setNextPageNum(pageNum + 1);
        return ResVo.ok(result);
    }

    /**
     * 分页加载子评论
     *
     * @param topCommentId 一级评论ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 子评论列表
     */
    @Permission(role = UserRole.ALL)
    @GetMapping(path = "subComments")
    @ResponseBody
    public ResVo<SubCommentListVO> getSubComments(
            @RequestParam Long topCommentId,
            @RequestParam Long pageNum,
            @RequestParam Long pageSize) {
        if (NumUtil.nullOrZero(topCommentId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "一级评论ID为空");
        }
        pageNum = Optional.ofNullable(pageNum).orElse(PageParam.DEFAULT_PAGE_NUM);
        pageSize = Optional.ofNullable(pageSize).orElse(PageParam.DEFAULT_PAGE_SIZE);
        SubCommentListVO result = commentReadService.getSubComments(topCommentId, PageParam.newPageInstance(pageNum, pageSize));
        return ResVo.ok(result);
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @RequestMapping(path = "delete")
    public ResVo<Boolean> delete(Long commentId) {
        commentWriteService.deleteComment(commentId, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }

    /**
     * 收藏、点赞等相关操作
     *
     * @param commendId
     * @param type      取值来自于 OperateTypeEnum#code
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "favor")
    public ResVo<Boolean> favor(@RequestParam(name = "commentId") Long commendId,
                                @RequestParam(name = "type") Integer type) {
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (operate == OperateTypeEnum.EMPTY) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }

        // 要求文章必须存在
        CommentDO comment = commentReadService.queryComment(commendId);
        if (comment == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "评论不存在!");
        }

        userFootService.favorArticleComment(DocumentTypeEnum.COMMENT,
                commendId,
                comment.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(),
                operate);
        return ResVo.ok(true);
    }

}
