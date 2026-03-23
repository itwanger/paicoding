package com.github.paicoding.forum.test.ai;

import com.github.paicoding.forum.core.util.DotenvUtil;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.util.Proxys;

import java.net.Proxy;

public class TestChatGPT {
    public static void main(String[] args) {
        // export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890
        //国内需要代理
         Proxy proxy = Proxys.http("127.0.0.1", 7890);
//        Proxy proxy = Proxys.http("10.3.4.136", 7890);
        //socks5 代理
        // Proxy proxy = Proxys.socks5("127.0.0.1", 1080);
        String apiKey = DotenvUtil.requireFirst("OPENAI_API_KEY", "PAICODING_OPENAI_API_KEY");

        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey(apiKey)
//                .proxy(proxy)
                .apiHost("https://api.openai.com/") //反向代理地址
                .build()
                .init();

        String res = chatGPT.chat("写一段七言绝句诗，题目是：火锅！");
        System.out.println(res);
    }
}
