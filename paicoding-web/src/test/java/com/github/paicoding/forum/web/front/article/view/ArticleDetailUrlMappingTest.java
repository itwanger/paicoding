package com.github.paicoding.forum.web.front.article.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /article/detail/{articleId} 的路径约束。
 *
 * <p>外站（含仓库 README 的相对链接）长期在打 /article/detail/forum-web、
 * /article/detail/list.html 这类地址，articleId 不限定数字时会走到 Long 转换失败，
 * 对爬虫返回 500；生产日志里十天有 383 次 5xx 就是这么来的。</p>
 *
 * <p>这里只验证映射层：依赖全为 null，能进方法体就说明地址匹配上了。</p>
 *
 * @author Claude
 */
public class ArticleDetailUrlMappingTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ArticleViewController()).build();
    }

    @Test
    public void nonNumericArticleIdShouldNotMatch() throws Exception {
        mockMvc.perform(get("/article/detail/forum-web")).andExpect(status().isNotFound());
        mockMvc.perform(get("/article/detail/list.html")).andExpect(status().isNotFound());
        mockMvc.perform(get("/article/detail/docs/install.md")).andExpect(status().isNotFound());
    }

    @Test
    public void numericArticleIdShouldStillMatch() {
        // 匹配到处理器后因依赖为 null 抛异常，说明约束没有误伤正常地址
        assertThrows(Exception.class, () -> mockMvc.perform(get("/article/detail/123")));
        assertThrows(Exception.class, () -> mockMvc.perform(get("/article/detail/123/some-slug")));
    }
}
