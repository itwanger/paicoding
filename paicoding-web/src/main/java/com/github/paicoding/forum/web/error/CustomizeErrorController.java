package com.github.paicoding.forum.web.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller("/error")
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomizeErrorController implements ErrorController {

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, Model model) {
        HttpStatus status = getStatus(request);

        if (status == HttpStatus.FORBIDDEN) {
            model.addAttribute("toast", "403无权限");
            return new ModelAndView("error/403");
        }

        if (status.is4xxClientError()) {
            model.addAttribute("toast", "请求地址不存在");
            return new ModelAndView("error/404");
        }

        if (status.is5xxServerError()) {
            model.addAttribute("toast", "服务器内部异常");
            return new ModelAndView("error/500");
        }

        model.addAttribute("toast", "请求处理异常");
        return new ModelAndView("error/500");

    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
