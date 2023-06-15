package com.github.paicoding.forum.service.chatai.service.impl.chatgpt;

import com.plexpt.chatgpt.entity.chat.ChatChoice;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Accessors(chain = true)
public class ChatRecordWxVo {
    /**
     * 提问的次数
     */
    private int qasIndex;
    /**
     * 提问内容
     */
    private String qas;
    /**
     * 提问时间
     */
    private Long qasTime;
    private List<ChatChoice> res;
    private ChatRecordWxVo next;
    private ChatRecordWxVo pre;
    private volatile boolean lastReturn;
    private String sysErr;

    public ChatRecordWxVo() {
        qasTime = 0L;
        sysErr = null;
        lastReturn = false;
    }

    /**
     * 之前没有回复过，且chatgpt出错，或者有结果了，才能继续回复
     *
     * @return
     */
    public boolean canReply() {
        return !lastReturn && (sysErr != null || res != null);
    }

    public String reply() {
        lastReturn = true;
        if (!CollectionUtils.isEmpty(res)) {
            return buildResPrefix() + buildRes();
        }

        return buildResPrefix() + sysErr;
    }

    private String buildResPrefix() {
        return qasIndex + "/50: " + qas + "\n================\n";
    }

    private String buildRes() {
        StringBuilder builder = new StringBuilder();
        for (ChatChoice choice : res) {
            builder.append(choice.getMessage().getContent()).append("\n--------------\n\n");
        }
        return builder.toString();
    }
}