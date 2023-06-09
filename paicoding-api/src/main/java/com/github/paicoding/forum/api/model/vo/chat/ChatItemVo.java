package com.github.paicoding.forum.api.model.vo.chat;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 一次qa的聊天记录
 *
 * @author YiHui
 * @date 2023/6/9
 */
@Data
@Accessors(chain = true)
public class ChatItemVo implements Serializable, Cloneable {
    private static final long serialVersionUID = 7230339040247758226L;

    /**
     * 提问的内容
     */
    private String question;

    /**
     * 提问的时间点
     */
    private String questionTime;

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 回答的时间点
     */
    private String answerTime;

    /**
     * 记录问题及记录时间
     *
     * @param question
     * @return
     */
    public ChatItemVo initQuestion(String question) {
        this.question = question;
        this.questionTime = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss").format(LocalDateTime.now());
        return this;
    }

    /**
     * 记录返回结果及回答时间
     *
     * @param answer
     * @return
     */
    public ChatItemVo initAnswer(String answer) {
        this.answer = answer;
        this.answerTime = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss").format(LocalDateTime.now());
        return this;
    }

    @Override
    public ChatItemVo clone() {
        ChatItemVo item = new ChatItemVo();
        item.question = question;
        item.questionTime = questionTime;
        return item;
    }
}
