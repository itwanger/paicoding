package com.github.paicoding.forum.api.model.context;
import lombok.Data;
/** Optional page selection, scoped to one HTTP request, not a global default or access grant. */
@Data
public class ArticleEditionRequest {
    private static final ThreadLocal<ArticleEditionRequest> LOCAL = new ThreadLocal<>();
    private Long articleId;
    private Long editionId;
    private Long detailId;
    private String scope = "current";
    private boolean switching;
    public static ArticleEditionRequest get() { return LOCAL.get(); }
    public static void set(ArticleEditionRequest value) { LOCAL.set(value); }
    public static void clear() { LOCAL.remove(); }
    public static ArticleEditionRequest forArticle(Long id) {
        ArticleEditionRequest value = get();
        return value != null && java.util.Objects.equals(id, value.articleId) ? value : null;
    }
}
