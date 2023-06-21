package com.github.paicoding.forum.test.ai;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.util.Proxys;

import java.net.Proxy;

public class TestChatGPT {
    public static void main(String[] args) {
        // export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890
        //国内需要代理
        // Proxy proxy = Proxys.http("127.0.0.1", 7892);
        Proxy proxy = Proxys.http("10.3.4.136", 7892);
        //socks5 代理
        // Proxy proxy = Proxys.socks5("127.0.0.1", 1080);

        ChatGPT chatGPT = ChatGPT.builder()
//                .apiKey("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")
                .apiKey("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")
                .proxy(proxy)
                .apiHost("https://api.openai.com/") //反向代理地址
                .build()
                .init();

        String res = chatGPT.chat("写一段七言绝句诗，题目是：火锅！");
        System.out.println(res);
    }
}
