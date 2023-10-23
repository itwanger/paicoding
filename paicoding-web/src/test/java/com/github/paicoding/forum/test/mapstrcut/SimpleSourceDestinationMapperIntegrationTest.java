package com.github.paicoding.forum.test.mapstrcut;

import com.github.paicoding.forum.web.QuickForumApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 9/21/23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = QuickForumApplication.class)
public class SimpleSourceDestinationMapperIntegrationTest {

    @Test
    public void givenSourceToDestination_whenMaps_thenCorrect() {
        SimpleSource simpleSource = new SimpleSource();
        simpleSource.setName("SourceName");
        simpleSource.setDescription("SourceDescription");
        SimpleDestination destination = SimpleSourceDestinationMapper.INSTANCE.sourceToDestination(simpleSource);
        assertEquals(simpleSource.getName(), destination.getName());
        assertEquals(simpleSource.getDescription(), destination.getDescription());
    }
}
