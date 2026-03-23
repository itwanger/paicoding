package com.github.paicoding.forum.web.front.chat.view;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatSourceOptionDTO;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.user.service.conf.AiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path = "chat")
public class ChatViewController {
    @Resource
    private AiConfig aiConfig;

    @Resource
    private ChatFacade chatFacade;

    @RequestMapping(path = {"", "/", "home"})
    public String index(Model model) {
        List<ChatSourceOptionDTO> options = buildChatSourceOptions();
        model.addAttribute("chatSourceOptions", options);
        model.addAttribute("selectedChatSource", resolveSelectedSource(options));
        return "views/chat-home/index";
    }

    private List<ChatSourceOptionDTO> buildChatSourceOptions() {
        List<ChatSourceOptionDTO> options = new ArrayList<ChatSourceOptionDTO>();
        if (aiConfig == null || CollectionUtils.isEmpty(aiConfig.getSource())) {
            return options;
        }

        for (AISourceEnum source : aiConfig.getSource()) {
            if (source == null) {
                continue;
            }

            ChatSourceOptionDTO option = new ChatSourceOptionDTO();
            option.setCode(source.getCode());
            option.setValue(source.name());
            option.setName(resolveSourceName(source));
            options.add(option);
        }
        return options;
    }

    private String resolveSelectedSource(List<ChatSourceOptionDTO> options) {
        AISourceEnum selected = chatFacade == null ? null : chatFacade.getRecommendAiSource();
        if (selected != null) {
            for (ChatSourceOptionDTO option : options) {
                if (selected.name().equals(option.getValue())) {
                    return option.getValue();
                }
            }
        }
        return CollectionUtils.isEmpty(options) ? null : options.get(0).getValue();
    }

    private String resolveSourceName(AISourceEnum source) {
        if (source == null) {
            return "";
        }

        switch (source) {
            case CHAT_GPT_3_5:
                return "OpenAI";
            case CHAT_GPT_4:
                return "OpenAI GPT-4";
            case ALI_AI:
                return "通义千问";
            case XUN_FEI_AI:
                return "讯飞星火";
            case ZHI_PU_AI:
                return "智谱";
            case ZHIPU_CODING:
                return "智谱Coding";
            case DOU_BAO_AI:
                return "豆包";
            case DEEP_SEEK:
                return "DeepSeek";
            case PAI_AI:
                return "技术派";
            default:
                return source.getName();
        }
    }
}
