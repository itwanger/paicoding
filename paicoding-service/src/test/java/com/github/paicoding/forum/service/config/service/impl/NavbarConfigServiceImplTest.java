package com.github.paicoding.forum.service.config.service.impl;

import com.github.paicoding.forum.api.model.exception.ForumException;
import com.github.paicoding.forum.api.model.vo.config.NavbarConfigDTO;
import com.github.paicoding.forum.api.model.vo.config.NavbarItemDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NavbarConfigServiceImplTest {
    private final NavbarConfigServiceImpl service = new NavbarConfigServiceImpl();

    @Test
    void shouldNormalizeFlagsAndKeepConfiguredOrder() {
        NavbarConfigDTO request = new NavbarConfigDTO();
        request.setItems(Arrays.asList(
                item("one", " 派聪明 ", " https://smart.paicoding.com/ ", null, true),
                item("two", "派简历", "https://resume.paicoding.com/", false, null)
        ));

        NavbarConfigDTO normalized = service.normalizeAndValidate(request);

        assertThat(normalized.getItems()).extracting("name").containsExactly("派聪明", "派简历");
        assertThat(normalized.getItems().get(0).getEnabled()).isTrue();
        assertThat(normalized.getItems().get(1).getEnabled()).isFalse();
        assertThat(normalized.getItems().get(1).getOpenInNewWindow()).isFalse();
        assertThat(JsonUtil.toStr(normalized)).doesNotContain("activeDomain");
    }

    @Test
    void shouldRejectUnsafeAndFixedUrls() {
        assertThrows(ForumException.class, () -> normalize(item("one", "脚本", "javascript:alert(1)", true, false)));
        assertThrows(ForumException.class, () -> normalize(item("one", "重复教程", "/column", true, false)));
    }

    @Test
    void shouldRejectDuplicateNames() {
        NavbarConfigDTO request = new NavbarConfigDTO();
        request.setItems(Arrays.asList(
                item("one", "派简历", "https://resume.paicoding.com/", true, true),
                item("two", "派简历", "https://resume.example.com/", true, true)
        ));

        assertThrows(ForumException.class, () -> service.normalizeAndValidate(request));
    }

    private NavbarConfigDTO normalize(NavbarItemDTO item) {
        NavbarConfigDTO request = new NavbarConfigDTO();
        request.setItems(Arrays.asList(item));
        return service.normalizeAndValidate(request);
    }

    private NavbarItemDTO item(String id, String name, String url, Boolean enabled, Boolean newWindow) {
        NavbarItemDTO item = new NavbarItemDTO();
        item.setId(id);
        item.setName(name);
        item.setUrl(url);
        item.setEnabled(enabled);
        item.setOpenInNewWindow(newWindow);
        return item;
    }
}
