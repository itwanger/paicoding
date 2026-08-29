package com.github.paicoding.forum.web.front.article.view;

import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnViewControllerCourseIntroTest {

    private final ColumnViewController controller = new ColumnViewController();

    @Test
    void shouldNotRepeatColumnSummaryWhenReadmeIsMissing() {
        ColumnDTO column = new ColumnDTO();
        column.setIntroduction("顶部已经展示的教程简介");

        String html = ReflectionTestUtils.invokeMethod(controller, "buildCourseIntroHtml", column);

        assertEquals("", html);
    }

    @Test
    void shouldRenderDedicatedReadmeWhenPresent() {
        ColumnDTO column = new ColumnDTO();
        column.setIntroduction("顶部教程简介");
        column.setReadmeContent("## 能学到什么\n\n这是独立的教程说明。");

        String html = ReflectionTestUtils.invokeMethod(controller, "buildCourseIntroHtml", column);

        assertTrue(html.contains("能学到什么"));
        assertTrue(html.contains("这是独立的教程说明"));
    }
}
