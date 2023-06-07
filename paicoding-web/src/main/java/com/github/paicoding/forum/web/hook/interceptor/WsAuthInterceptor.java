package com.github.paicoding.forum.web.hook.interceptor;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.util.IpUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.web.global.GlobalInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 长连接的登录校验拦截器
 *
 * @author YiHui
 * @date 2023/6/6
 */
@Slf4j
public class WsAuthInterceptor extends HttpSessionHandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        // 初始化登录信息
        // todo websocket 用户身份识别方案待补充
//        SpringUtil.getBean(GlobalInitService.class).initLoginUser(reqInfo);
//        ReqInfoContext.addReqInfo(reqInfo);
//        if (reqInfo.getUserId() == null) {
//            // 未登录，拒绝链接
//            log.info("用户未登录，拒绝聊天!");
//            response.setStatusCode(HttpStatus.FORBIDDEN);
//            return false;
//        }
//        log.info("{} 开始了聊天!", reqInfo);
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        ReqInfoContext.clear();
        super.afterHandshake(request, response, wsHandler, ex);
    }
}
