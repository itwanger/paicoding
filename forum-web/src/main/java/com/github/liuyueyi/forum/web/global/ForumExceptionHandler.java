package com.github.liuyueyi.forum.web.global;

import com.github.liueyueyi.forum.api.model.exception.ForumException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        ModelAndView mv = new ModelAndView("error/500");
        if (ex instanceof ForumException) {
            mv.getModel().put("toast", ((ForumException) ex).getStatus().getMsg());
        } else {
            mv.getModel().put("toast", ExceptionUtils.getStackTrace(ex));
        }
        response.setStatus(500);
        return mv;
    }
}
