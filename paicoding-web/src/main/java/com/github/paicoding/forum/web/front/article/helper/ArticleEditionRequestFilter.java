package com.github.paicoding.forum.web.front.article.helper;
import com.github.paicoding.forum.api.model.context.ArticleEditionRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
@Component
public class ArticleEditionRequestFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String articleId = request.getHeader("X-Article-Id");
            if (articleId != null && !request.getRequestURI().contains("/admin/")) {
                ArticleEditionRequest selected = new ArticleEditionRequest();
                try {
                    selected.setArticleId(Long.valueOf(articleId));
                    String id = request.getHeader("X-Article-Edition");
                    String detail = request.getHeader("X-Article-Detail");
                    if (id != null && !id.isEmpty()) selected.setEditionId(Long.valueOf(id));
                    if (detail != null && !detail.isEmpty()) selected.setDetailId(Long.valueOf(detail));
                } catch (NumberFormatException e) { response.sendError(400, "Invalid edition selection"); return; }
                selected.setScope("history".equals(request.getHeader("X-Comment-Scope")) ? "history" : "current");
                selected.setSwitching("1".equals(request.getHeader("X-Article-Switch")));
                ArticleEditionRequest.set(selected);
                response.setHeader("Cache-Control", "private, no-store");
            }
            response.addHeader("Vary", "X-Article-Id, X-Article-Edition, X-Comment-Scope");
            chain.doFilter(request, response);
        } finally { ArticleEditionRequest.clear(); }
    }
}
