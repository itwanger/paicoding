package com.github.paicoding.forum.test.ai;

import cn.hutool.core.util.NumberUtil;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.ChatGPTStream;
import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.listener.ConsoleStreamListener;
import com.plexpt.chatgpt.util.Proxys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


/**
 * open ai 客户端
 *
 * @author plexpt
 */

@Slf4j

public class ConsoleChatGPT {

    public static Proxy proxy = Proxy.NO_PROXY;

    public static void main(String[] args) {

        System.out.println("ChatGPT - Java command-line interface");
        System.out.println("Press enter twice to submit your question.");
        System.out.println();
        System.out.println("按两次回车以提交您的问题！！！");
        System.out.println("按两次回车以提交您的问题！！！");
        System.out.println("按两次回车以提交您的问题！！！");


        System.out.println();
//        System.out.println("Please enter APIKEY, press Enter twice to submit:");
        String key = "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";
        check(key);

        String useProxy = "y";
        if (useProxy.equalsIgnoreCase("y")) {

            // 输入代理地址
            String type = "http";

            // export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890

            // 输入代理地址
            String proxyHost = "http://127.0.0.1";

            // 输入代理端口
            String portStr = "7890";
            Integer proxyPort = Integer.parseInt(portStr);

            if (type.equals("http")) {
                proxy = Proxys.http(proxyHost, proxyPort);
            } else {
                proxy = Proxys.socks5(proxyHost, proxyPort);
            }

        }

        System.out.println("Inquiry balance...");
        System.out.println("查询余额中...");
        BigDecimal balance = getBalance(key);
        System.out.println("API KEY balance: " + balance.toPlainString());

        if (!NumberUtil.isGreater(balance, BigDecimal.ZERO)) {
            System.out.println("API KEY 余额不足: ");
            return;
        }


        while (true) {
            String prompt = getInput("\nYou:\n");

            ChatGPTStream chatGPT = ChatGPTStream.builder()
                    .apiKey(key)
                    .proxy(proxy)
                    .build()
                    .init();

            System.out.println("AI: ");


            //卡住
            CountDownLatch countDownLatch = new CountDownLatch(1);

            Message message = Message.of(prompt);
            ConsoleStreamListener listener = new ConsoleStreamListener() {
                @Override
                public void onError(Throwable throwable, String response) {
                    throwable.printStackTrace();
                    countDownLatch.countDown();
                }
            };

            listener.setOnComplate(msg -> {
                countDownLatch.countDown();
            });
            chatGPT.streamChatCompletion(Arrays.asList(message), listener);

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }

    private static BigDecimal getBalance(String key) {

        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey(key)
                .proxy(proxy)
                .build()
                .init();

        return chatGPT.balance();
    }

    private static void check(String key) {
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("请输入正确的KEY");
        }
    }

    @SneakyThrows
    public static String getInput(String prompt) {
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.stream().collect(Collectors.joining("\n"));
    }

}
