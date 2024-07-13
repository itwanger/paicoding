package com.github.paicoding.forum.web.front.chat.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "chat")
public class ChatViewController {
    @RequestMapping(path = {"", "/", "home"})
    public String index() {
        return "views/chat-home/index";
    }
}
