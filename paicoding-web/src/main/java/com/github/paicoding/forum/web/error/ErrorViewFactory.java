package com.github.paicoding.forum.web.error;

import com.github.paicoding.forum.web.global.SeoInjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * 前台错误页视图的统一构造入口。
 *
 * <p>视图控制器直接 {@code new ModelAndView("error/404")} 时状态码仍是 200，
 * 搜索引擎会把任意拼错的、被外站误链的、被扫描器构造出来的地址都当成一个真实网页收录，
 * 形成大量彼此重复的 soft 404，拖累整站质量评分。这里统一补上真实状态码与 noindex。</p>
 *
 * @author Claude
 */
@Component
public class ErrorViewFactory {

    @Autowired
    private SeoInjectService seoInjectService;

    /**
     * 内容不存在：真 404 + noindex。
     */
    public ModelAndView notFound() {
        return render("error/404", HttpStatus.NOT_FOUND);
    }

    /**
     * 内容存在但当前访客无权访问（如教程未发布）：真 403 + noindex。
     */
    public ModelAndView forbidden() {
        return render("error/403", HttpStatus.FORBIDDEN);
    }

    private ModelAndView render(String viewName, HttpStatus status) {
        seoInjectService.initNoindexSeo();
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        return modelAndView;
    }
}
