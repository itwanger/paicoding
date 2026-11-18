package com.github.paicoding.forum.service.table;

import com.github.paicoding.forum.api.model.vo.table.TableLearningReq;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration.DeepSeekConf;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TableAiServiceTest {
    private final TableAiService ai = new TableAiService(new DeepSeekConf());

    private TableLearningReq sample() {
        TableLearningReq r = new TableLearningReq();
        r.setTitle("3个月冲刺计划");
        r.setColumns(Arrays.asList("Day", "面渣逆袭"));
        r.setValues(Arrays.asList("Day 4~7", "集合框架"));
        r.setAction("explain");
        return r;
    }

    @Test
    void supportedActionsTeachTechnicalContent() {
        TableLearningReq r = sample();
        assertTrue(ai.validateLearningRequest(r).contains("知识点"));
        r.setAction("order");
        assertTrue(ai.validateLearningRequest(r).contains("顺序"));
        r.setAction("quiz");
        assertTrue(ai.validateLearningRequest(r).contains("原理理解"));
    }

    @Test
    void rejectsMissingMismatchedAndUnknownInputs() {
        assertThrows(TableException.class, () -> ai.validateLearningRequest(null));
        TableLearningReq r = sample();
        r.setValues(Arrays.asList("只有一列"));
        assertThrows(TableException.class, () -> ai.validateLearningRequest(r));
        r.setValues(Arrays.asList("Day 1", "集合"));
        r.setAction("copy");
        assertThrows(TableException.class, () -> ai.validateLearningRequest(r));
        r.setAction("explain");
        r.setValues(Arrays.asList("Day 1", null));
        assertThrows(TableException.class, () -> ai.validateLearningRequest(r));
    }

    @Test
    void boundsContentBeforeAnyProviderCall() {
        TableLearningReq r = sample();
        r.setValues(Arrays.asList("Day 1", new String(new char[4001]).replace('\0', 'x')));
        assertThrows(TableException.class, () -> ai.validateLearningRequest(r));
        r.setValues(Arrays.asList("Day 1", "集合"));
        r.setQuestion(new String(new char[501]).replace('\0', 'x'));
        assertThrows(TableException.class, () -> ai.validateLearningRequest(r));
    }

    @Test
    void customQuestionMustBePresent() {
        TableLearningReq r = sample();
        r.setAction("ask");
        assertThrows(TableException.class, () -> ai.validateLearningRequest(r));
        r.setQuestion("ArrayList 如何扩容？");
        assertDoesNotThrow(() -> ai.validateLearningRequest(r));
    }

    @Test
    void disabledProviderAndAnonymousUserAreRejected() {
        assertFalse(ai.available());
        assertEquals(503, assertThrows(TableException.class, () -> ai.learningHelp(10L, sample())).getHttpStatus());
        assertEquals(401, assertThrows(TableException.class, () -> ai.learningHelp(null, sample())).getHttpStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void upstreamFailureRefundsQuotaAndStarUserGetsHigherLimit() {
        Map<String, AtomicLong> counters = new HashMap<>();
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(RedisCallback.class))).thenAnswer(inv -> {
            // RedisClient.hIncr uses RedisCallback; simulate atomic increment/decrement per user field
            return counters.computeIfAbsent("u", k -> new AtomicLong(0)).incrementAndGet();
        });
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hashOps);
        RedisClient.register(redis);

        // Configure provider with failing HTTP 502 interceptor and verify refund is invoked
        DeepSeekConf conf = new DeepSeekConf();
        conf.setApiHost("https://api.deepseek.com");
        conf.setApiKey("unit-test-placeholder");
        conf.setModel("deepseek-chat");
        TableAiService configured = new TableAiService(conf);
        ReflectionTestUtils.setField(configured, "dailyLimit", 1);
        ReflectionTestUtils.setField(configured, "starDailyLimit", 3);
        OkHttpClient failingClient = new OkHttpClient.Builder().addInterceptor(chain -> new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(502)
                .message("Bad Gateway")
                .body(ResponseBody.create(MediaType.parse("application/json"), "{}"))
                .build()).build();
        ReflectionTestUtils.setField(configured, "client", failingClient);

        TableException ex = assertThrows(TableException.class, () -> configured.learningHelp(10L, false, sample()));
        assertEquals(502, ex.getHttpStatus());
        // Initial reserve + refund on failure => 2 RedisClient.hIncr calls
        verify(redis, atLeast(2)).execute(any(RedisCallback.class));

        // When count reaches 2, normal user (limit=1) gets 429, while star user (limit=3) is still within quota
        counters.get("u").set(1);
        TableException quotaEx = assertThrows(TableException.class, () -> configured.learningHelp(10L, false, sample()));
        assertEquals(429, quotaEx.getHttpStatus());
    }
}
