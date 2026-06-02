package com.github.paicoding.forum.api.model.vo.comment;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 划线 AI 实时回复事件
 */
@Data
@Accessors(chain = true)
public class HighlightAiStreamEvent {
    private String type;
    private String requestId;
    private String bot;
    private String delta;
    private String content;
    private Long commentId;
    private String html;
    private String message;

    public static HighlightAiStreamEvent start(String requestId, String bot) {
        return new HighlightAiStreamEvent()
                .setType("start")
                .setRequestId(requestId)
                .setBot(bot);
    }

    public static HighlightAiStreamEvent comment(String requestId, String bot, Long commentId) {
        return new HighlightAiStreamEvent()
                .setType("comment")
                .setRequestId(requestId)
                .setBot(bot)
                .setCommentId(commentId);
    }

    public static HighlightAiStreamEvent delta(String requestId, String bot, String delta, String content) {
        return new HighlightAiStreamEvent()
                .setType("delta")
                .setRequestId(requestId)
                .setBot(bot)
                .setDelta(delta)
                .setContent(content);
    }

    public static HighlightAiStreamEvent done(String requestId, String bot, Long commentId, String html) {
        return new HighlightAiStreamEvent()
                .setType("done")
                .setRequestId(requestId)
                .setBot(bot)
                .setCommentId(commentId)
                .setHtml(html);
    }

    public static HighlightAiStreamEvent error(String requestId, String bot, String message) {
        return new HighlightAiStreamEvent()
                .setType("error")
                .setRequestId(requestId)
                .setBot(bot)
                .setMessage(message);
    }
}
