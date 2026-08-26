package com.github.paicoding.forum.service.config.property;

import com.github.paicoding.forum.api.model.vo.config.NavbarConfigDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NavbarConfigPropertiesTest {
    @Test
    void shouldPreserveLegacyEntriesAndAppendPaiResumeBeforeFirstAdminSave() {
        NavbarConfigProperties properties = new NavbarConfigProperties();
        properties.setPaiSmartName("派聪明");
        properties.setPaiSmartUrl("https://smart.example.com/");
        properties.setPaiChatName("派聊聊");

        NavbarConfigDTO config = properties.resolve();

        assertThat(config.getItems()).extracting("name")
                .containsExactly("派聪明", "派聊聊", "派简历");
        assertThat(config.getItems().get(0).getUrl()).isEqualTo("https://smart.example.com/");
        assertThat(config.getItems().get(2).getUrl()).isEqualTo("https://resume.paicoding.com/");
    }

    @Test
    void shouldReadStoredAdminConfiguration() {
        NavbarConfigProperties properties = new NavbarConfigProperties();
        properties.setNavbarItemsConfig("{\"items\":[{\"id\":\"docs\",\"name\":\"文档\",\"url\":\"/docs\",\"enabled\":true,\"openInNewWindow\":false}]}");

        NavbarConfigDTO config = properties.resolve();

        assertThat(config.getItems()).hasSize(1);
        assertThat(config.getItems().get(0).getName()).isEqualTo("文档");
    }

    @Test
    void shouldFallBackSafelyWhenStoredJsonIsBroken() {
        NavbarConfigProperties properties = new NavbarConfigProperties();
        properties.setNavbarItemsConfig("not-json");

        assertThat(properties.resolve().getItems()).extracting("name")
                .containsExactly("派聪明", "派聊聊", "派简历");
    }
}
