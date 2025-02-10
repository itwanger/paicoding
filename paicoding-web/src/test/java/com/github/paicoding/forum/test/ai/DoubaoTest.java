package com.github.paicoding.forum.test.ai;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.subscribers.TestSubscriber;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;


import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DoubaoTest {
    static String apiKey = "测试用api";
    static String endPoint = "测试用接入点";
    static ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
    static Dispatcher dispatcher = new Dispatcher();
    static ArkService service = ArkService.builder().dispatcher(dispatcher).connectionPool(connectionPool).baseUrl("https://ark.cn-beijing.volces.com/api/v3").apiKey(apiKey).build();


    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content("常见的十字花科植物有哪些？").build();
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(endPoint)
                .messages(messages)
                .build();

        if(apiKey.equals("测试用api") ) {
            System.out.println("请填写 apiKey");
            return;
        }
        if(endPoint.equals("测试用接入点") ) {
            System.out.println("请填写 endPoint");
            return;

        }
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> System.out.println(choice.getMessage().getContent()));

        System.out.println("\n----- streaming request -----");
        final List<ChatMessage> streamMessages = new ArrayList<>();
        final ChatMessage streamSystemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build();
        final ChatMessage streamUserMessage = ChatMessage.builder().role(ChatMessageRole.USER).content("常见的十字花科植物有哪些？").build();
        streamMessages.add(streamSystemMessage);
        streamMessages.add(streamUserMessage);

        ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                .model("ep-20250208191823-mpjm8")
                .messages(streamMessages)
                .build();

        TestSubscriber<ChatCompletionChunk> testSubscriber =  new TestSubscriber<ChatCompletionChunk>() {
            @Override
            public void onNext(ChatCompletionChunk choice) {
                // 必须调用 super.onNext，以保持 TestSubscriber 的内部状态
                super.onNext(choice);
                if (!choice.getChoices().isEmpty()) {
                    System.out.print(choice.getChoices().get(0).getMessage().getContent());
                }
            }
        };

        // 使用testSubscriber 订阅 以使用断言等方法
        service.streamChatCompletion(streamChatCompletionRequest)
                .doOnSubscribe(d -> System.out.println("Subscription started"))
                .doFinally(() -> System.out.println("Stream completed or errored"))
                .subscribe(testSubscriber);



        testSubscriber.assertSubscribed();

        testSubscriber.awaitTerminalEvent(); // 等待流结束

        // 打印并检查结果
        StringBuilder  contentBuilder = new StringBuilder();
        testSubscriber.values().forEach(choice -> {
            if (!choice.getChoices().isEmpty()) {
                // 此处转型参考 package com.volcengine.ark.runtime.model.completion.chat
                // 的 ChatCompletionChunk 类 的 stringContent 方法
                String content = (String) choice.getChoices().get(0).getMessage().getContent();
                contentBuilder.append(content);
            }
        });
        System.out.println("\nStream test finished.");


        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        // 检查contentBuilder的内容是不是空的
        if(contentBuilder.length() == 0){
            System.err.println("No content received!");
        }

        service.shutdownExecutor();

    }
}