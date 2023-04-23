package com.github.paicoding.forum.core.ai;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;


/**
 * @author YiHui
 * @date 2023/4/20
 */
@Service
public class ChatGptHelper {


    public static void main(String[] args) {
        //国内需要代理 国外不需要
//        Proxy proxy = Proxys.http("212.50.245.101", 9898);
//
//        ChatGPT chatGPTStream = ChatGPT.builder()
//                .apiKey("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")
//                .proxy(proxy)
//                .timeout(900)
//                .apiHost("https://api.openai.com/") //反向代理地址
//                .build()
//                .init();
//
//
//        Message message = Message.of("写一段七言绝句诗，题目是：火锅！");
//        ChatCompletion chatCompletion = ChatCompletion.builder()
//                .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
//                .messages(Arrays.asList(message))
//                .maxTokens(3000)
//                .temperature(0.9)
//                .build();
//        ChatCompletionResponse response = chatGPTStream.chatCompletion(chatCompletion);
//        Message res = response.getChoices().get(0).getMessage();
//        System.out.println(res);


        String url = "https://gwgp-cekvddtwkob.n.bdcloudapi.com/ip/local/geo/v1/district?";
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", HttpRequestHelper.CHROME_UA);
        headers.set("Origin", "https://www.baidu.com");
        headers.set("Referer", "https://www.baidu.com/s?wd=ip&rsv_spt=1&rsv_iqid=0xc734b40a0007fde4&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_dl=tb&rsv_enter=1&oq=i%255B&rsv_btype=t&inputT=424&rsv_t=3086SdJTQ%2Bj2N8VEfxWuzA%2BSi7rduxSlW3kklwLNhUvfICZA%2Fe7EjDVpkAeOdRvurWPs&rsv_sug3=5&rsv_pq=e33624780007aa59&rsv_sug2=0&prefixsug=ip&rsp=5&rsv_sug4=996");
        String ans = HttpRequestHelper.fetchContentWithProxy(url, HttpMethod.GET, new HashMap<>(), headers, String.class);
        System.out.println(ans);
    }
}
