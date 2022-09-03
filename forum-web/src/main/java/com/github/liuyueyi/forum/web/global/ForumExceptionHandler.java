package com.github.liuyueyi.forum.web.global;

import com.github.liueyueyi.forum.api.model.exception.ForumException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author YiHui
 * @date 2022/9/3
 */
public class ForumExceptionHandler implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler, Exception ex) {


        ModelAndView mv = new ModelAndView("error/500");
        if (ex instanceof ForumException) {
            mv.getModel().put("toast", ((ForumException) ex).getStatus().getMsg());
        } else {
            mv.getModel().put("toast", ExceptionUtils.getStackTrace(ex));
        }
        return mv;
    }
}
