package com.github.paicoding.forum.web.front.chat.rest;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatMsgTypeEnum;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.web.front.chat.helper.WsLogicExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * STOMP协议的ChatGpt聊天通讯实现方式
 *
 * @author YiHui
 * @date 2023/6/5
 */
@Slf4j
@RestController
public class ChatRestController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private Map<String, Long> cache = new HashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 接收用户发送的消息
     *
     * @param msg
     * @param session
     * @param attrs
     * @return
     * @MessageMapping（"/chat/{session}"）注解的方法将用来接收"/app/chat/xxx路径发送来的消息，<br/> 如果有 @SendTo，则表示将返回结果，转发到其对应的路径上 （这个sendTo的路径，就是前端订阅的路径）
     * @DestinationVariable： 实现路径上的参数解析
     * @Headers 实现请求头格式的参数解析, @Header("headName") 表示获取某个请求头的内容
     */
    @MessageMapping("/chat/{session}")
    public void chat(String msg, @DestinationVariable("session") String session, @Header("simpSessionAttributes") Map<String, Object> attrs) {
        WsLogicExecutor.execute(attrs, () -> {
            log.info("{} 用户开始了对话: {}", ReqInfoContext.getReqInfo().getUser(), msg);
            cache.put(session, System.currentTimeMillis());
            buildRsp(session, "直接返回:" + msg);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // 1s之后，延迟返回信息给之前通讯过的用户
                    buildRsp(session, "上次请求时间：" + DateUtil.time2date(cache.get(session)) + " | 1s 之后自动再次: " + LocalDateTime.now());
                }
            });
        });
    }

    private void buildRsp(String session, String msg) {
        Map map = MapUtils.create("message", msg, "type", ChatMsgTypeEnum.Payload.getType(), "time", LocalDateTimeUtil.formatNormal(LocalDateTime.now()));
        String rsp = JsonUtil.toStr(map);
        // convertAndSendToUser 方法可以发送信给给指定用户,
        // 底层会自动将第二个参数目的地址 /chat/rsp 拼接为
        // /user/username/chat/rsp，其中第二个参数 username 即为这里的第一个参数 session
        // username 也是AuthHandshakeHandler中配置的 Principal 用户识别标志
        simpMessagingTemplate.convertAndSendToUser(session, "/chat/rsp", rsp);
        log.info("结果已返回!");
    }

}
