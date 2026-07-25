package com.github.paicoding.forum.web.front.article.view;

import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.service.ColumnService;
import com.github.paicoding.forum.web.error.ErrorViewFactory;
import com.github.paicoding.forum.web.global.SeoInjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 短地址入口未命中内容时的状态码。
 *
 * <p>返回 200 会让搜索引擎把任意拼错或被外站误链的地址当成真实网页收录，
 * 形成大量互为重复的 soft 404，所以这条行为必须锁住。</p>
 *
 * @author Claude
 */
public class ArticleSlugViewControllerTest {

    private MockMvc mockMvc;
    private ArticleDao articleDao;

    @BeforeEach
    public void setUp() {
        articleDao = Mockito.mock(ArticleDao.class);
        ColumnViewController columnViewController = Mockito.mock(ColumnViewController.class);
        Mockito.when(columnViewController.columnByRootSlug(anyString(), any()))
                .thenThrow(new ForumException(StatusEnum.COLUMN_NOT_EXISTS));

        ErrorViewFactory errorViewFactory = new ErrorViewFactory();
        ReflectionTestUtils.setField(errorViewFactory, "seoInjectService", Mockito.mock(SeoInjectService.class));

        ArticleSlugViewController controller = new ArticleSlugViewController();
        ReflectionTestUtils.setField(controller, "articleDao", articleDao);
        ReflectionTestUtils.setField(controller, "columnService", Mockito.mock(ColumnService.class));
        ReflectionTestUtils.setField(controller, "articleViewController", Mockito.mock(ArticleViewController.class));
        ReflectionTestUtils.setField(controller, "columnViewController", columnViewController);
        ReflectionTestUtils.setField(controller, "errorViewFactory", errorViewFactory);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void unknownSlugShouldReturnRealNotFound() throws Exception {
        Mockito.when(articleDao.getByUrlSlug(anyString())).thenReturn(null);

        mockMvc.perform(get("/this-slug-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }
}
