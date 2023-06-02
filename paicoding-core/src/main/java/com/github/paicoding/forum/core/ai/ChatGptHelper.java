package com.github.paicoding.forum.core.ai;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;


/**
 * @author YiHui
 * @date 2023/4/20
 */
@Service
public class ChatGptHelper {

    @Autowired
    private ChatGptFactory chatGptFactory;

    public String simpleGptReturn(String content) {
        ChatGPT gpt = chatGptFactory.simpleGPT();
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                .messages(Arrays.asList(Message.of(content)))
                .maxTokens(3000)
                .temperature(0.9)
                .build();
        ChatCompletionResponse response = gpt.chatCompletion(chatCompletion);
        Message res = response.getChoices().get(0).getMessage();
        return res.getContent();
    }
}
