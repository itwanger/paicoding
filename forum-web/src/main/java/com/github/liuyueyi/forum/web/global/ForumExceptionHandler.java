package com.github.liuyueyi.forum.web.global;

import com.github.liueyueyi.forum.api.model.exception.ForumException;
import com.github.liueyueyi.forum.api.model.vo.ResVo;
import com.github.liueyueyi.forum.api.model.vo.constants.StatusEnum;
import com.github.liuyueyi.forum.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局异常处理
 * fixme: 除了这种姿势之外，还可以使用 ControllerAdvice 注解方式
 *
 * @author YiHui
 * @date 2022/9/3
 */
@Slf4j
public class ForumExceptionHandler implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler, Exception ex) {
        log.error("unexpect error", ex);
        String errMsg = ex instanceof ForumException ? ((ForumException) ex).getStatus().getMsg() : ExceptionUtils.getStackTrace(ex);
        if (response.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            // 若是rest接口请求异常时，返回json格式的异常数据；而不是专门的500页面
            // 访问需要登录的rest接口
            response.setContentType("application/json");
            try {
                response.getWriter().println(JsonUtil.toStr(ResVo.fail(StatusEnum.UNEXPECT_ERROR, errMsg)));
                response.getWriter().flush();
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ModelAndView mv = new ModelAndView("error/500");
        mv.getModel().put("toast", errMsg);
        response.setStatus(500);
        return mv;
    }
}
