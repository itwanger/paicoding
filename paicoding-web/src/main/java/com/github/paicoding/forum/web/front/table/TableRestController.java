package com.github.paicoding.forum.web.front.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.table.TableLearningReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.service.table.TableAiService;
import com.github.paicoding.forum.service.table.TableException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping(value = "/api/tables", produces = "application/json")
public class TableRestController {
    private final TableAiService ai;
    private final ObjectMapper json = new ObjectMapper();

    public TableRestController(TableAiService ai) {
        this.ai = ai;
    }

    @ModelAttribute
    public void protect(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, no-store");
        if ("GET".equals(request.getMethod())) {
            return;
        }
        if (!"1".equals(request.getHeader("X-Pai-Table")) || "cross-site".equals(request.getHeader("Sec-Fetch-Site"))) {
            throw new TableException(403, "请从本站表格页面提交修改");
        }
        String origin = request.getHeader("Origin");
        if (origin != null) {
            String targetAuthority = resolveEffectiveAuthority(request);
            try {
                URI originUri = URI.create(origin);
                String originAuthority = normalizeAuthority(originUri.getScheme(), originUri.getRawAuthority());
                String expectedAuthority = normalizeAuthority(resolveScheme(request, originUri.getScheme()), targetAuthority);
                if (originAuthority == null || expectedAuthority == null || !originAuthority.equalsIgnoreCase(expectedAuthority)) {
                    throw new TableException(403, "请求来源无效");
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new TableException(403, "请求来源无效");
            }
        }
    }

    @GetMapping("/capabilities")
    public ResVo<Map<String, Object>> capabilities() {
        Map<String, Object> result = new HashMap<>();
        result.put("ai", ai.available());
        result.put("loggedIn", currentUserId() != null);
        return ResVo.ok(result);
    }

    @PostMapping("/learning-help")
    public ResVo<String> learningHelp(@RequestBody TableLearningReq request) {
        return ResVo.ok(ai.learningHelp(actor(), isStarMember(), request));
    }

    @PostMapping("/agent")
    public ResVo<Map<String, Object>> agent(@RequestBody TableLearningReq request) {
        return ResVo.ok(ai.agentHelp(actor(), isStarMember(), request));
    }

    @PostMapping(value = "/learning-help/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter learningHelpStream(@RequestBody TableLearningReq request, HttpServletResponse response) {
        Long userId = actor();
        boolean starMember = isStarMember();
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(70_000L);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<Runnable> cancelUpstream = new AtomicReference<>();

        Runnable cleanup = () -> {
            closed.set(true);
            Runnable cancel = cancelUpstream.getAndSet(null);
            if (cancel != null) {
                cancel.run();
            }
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            cleanup.run();
            emitter.complete();
        });
        emitter.onError(err -> cleanup.run());

        Runnable cancel = ai.learningHelpStream(
                userId,
                starMember,
                request,
                delta -> {
                    if (closed.get()) {
                        return;
                    }
                    Map<String, Object> event = new LinkedHashMap<>();
                    event.put("type", "delta");
                    event.put("delta", delta);
                    if (!sendEvent(emitter, event)) {
                        cleanup.run();
                    }
                },
                () -> {
                    if (closed.compareAndSet(false, true)) {
                        Map<String, Object> event = new LinkedHashMap<>();
                        event.put("type", "done");
                        sendEvent(emitter, event);
                        emitter.complete();
                    }
                },
                error -> {
                    if (closed.compareAndSet(false, true)) {
                        Map<String, Object> event = new LinkedHashMap<>();
                        event.put("type", "error");
                        event.put("message", error.getMessage());
                        sendEvent(emitter, event);
                        emitter.complete();
                    }
                });
        cancelUpstream.set(cancel);
        if (closed.get()) {
            cancel.run();
        }
        return emitter;
    }

    private boolean sendEvent(SseEmitter emitter, Map<String, Object> payload) {
        try {
            emitter.send(SseEmitter.event().data(payload, MediaType.APPLICATION_JSON_UTF8));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveEffectiveAuthority(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.trim().isEmpty()) {
            int comma = forwardedHost.indexOf(',');
            return (comma >= 0 ? forwardedHost.substring(0, comma) : forwardedHost).trim();
        }
        return request.getHeader("Host");
    }

    private String resolveScheme(HttpServletRequest request, String fallbackScheme) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.trim().isEmpty()) {
            int comma = forwardedProto.indexOf(',');
            return (comma >= 0 ? forwardedProto.substring(0, comma) : forwardedProto).trim();
        }
        String scheme = request.getScheme();
        return (scheme == null || scheme.trim().isEmpty()) ? fallbackScheme : scheme;
    }

    private String normalizeAuthority(String scheme, String authority) {
        if (authority == null || authority.trim().isEmpty()) {
            return null;
        }
        String value = authority.trim();
        if ("https".equalsIgnoreCase(scheme) && value.endsWith(":443")) {
            return value.substring(0, value.length() - 4);
        }
        if ("http".equalsIgnoreCase(scheme) && value.endsWith(":80")) {
            return value.substring(0, value.length() - 3);
        }
        return value;
    }

    @ExceptionHandler(TableException.class)
    public ResponseEntity<ResVo<Object>> error(TableException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResVo.fail(Status.newStatus(e.getHttpStatus(), e.getMessage())));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ResVo<Object>> malformed() {
        return error(new TableException(400, "表格请求格式错误"));
    }

    private Long currentUserId() {
        return ReqInfoContext.getReqInfo() == null ? null : ReqInfoContext.getReqInfo().getUserId();
    }

    private Long actor() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new TableException(401, "请先登录");
        }
        return userId;
    }

    private boolean isStarMember() {
        if (ReqInfoContext.getReqInfo() == null) {
            return false;
        }
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        return user != null && user.getStarStatus() == UserAIStatEnum.FORMAL;
    }
}
