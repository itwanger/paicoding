package com.github.paicoding.forum.web.front.chat.helper;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.core.mdc.MdcUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.user.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

/**
 * @author YiHui
 * @date 2023/6/9
 */
@Slf4j
public class WsLogicExecutor {

    public static void execute(Map<String, Object> attributes, Runnable func) {
        try {
            ReqInfoContext.ReqInfo reqInfo = (ReqInfoContext.ReqInfo) attributes.get(SessionService.SESSION_KEY);
            ReqInfoContext.addReqInfo(reqInfo);
            String traceId = (String) attributes.get(MdcUtil.TRACE_ID_KEY);
            MdcUtil.add(MdcUtil.TRACE_ID_KEY, traceId);


            // 执行具体的业务逻辑
            func.run();

        } finally {
            ReqInfoContext.clear();
            MdcUtil.clear();
        }
    }


    public static void sendMsgToUser(String session, ChatItemVo chat) {
        // convertAndSendToUser 方法可以发送信给给指定用户,
        // 底层会自动将第二个参数目的地址 /chat/rsp 拼接为
        // /user/username/chat/rsp，其中第二个参数 username 即为这里的第一个参数 session
        // username 也是AuthHandshakeHandler中配置的 Principal 用户识别标志
        SpringUtil.getBean(SimpMessagingTemplate.class).convertAndSendToUser(session, "/chat/rsp", chat);
        log.info("结果已返回!");
    }
}
