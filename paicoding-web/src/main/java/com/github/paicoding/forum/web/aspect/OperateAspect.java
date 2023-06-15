package com.github.paicoding.forum.web.aspect;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.github.paicoding.forum.api.model.constant.KafkaTopicConstant;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.NotifyTypeEnum;
import com.github.paicoding.forum.core.annotation.RecordOperate;
import com.github.paicoding.forum.api.model.dto.ArticleKafkaMessageDTO;
import com.github.paicoding.forum.core.util.IpUtils;
import com.github.paicoding.forum.core.util.ServletUtils;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;

import lombok.extern.slf4j.Slf4j;

/**
 * 操作切面
 *
 * @ClassName: OperateAspect
 * @Author: ygl
 * @Date: 2022/11/18 14:55
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class OperateAspect {

    @Autowired
    ArticleReadService articleReadService;

    @Autowired
    KafkaTemplate kafkaTemplate;

    /**
     * 1、定义切入点
     * 2、横切逻辑
     * 3、织入
     */

    @Pointcut(value = "@annotation(recordOperate)")
    public void pointcut(RecordOperate recordOperate) {
    }

    /**
     * 处理完请求后执行
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, RecordOperate controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, RecordOperate controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, RecordOperate controllerLog, final Exception e,
                             Object jsonResult) {
        try {
            // 请求的地址
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());

            HttpServletRequest request = ServletUtils.getRequest();
            // URL
            String requestURI = "http://xxx.xxx.xxx.xxx/default_url";
            // 设置请求方式
            String method = "defaultMethod";
            if (ObjectUtils.isNotEmpty(request)) {
                requestURI = request.getRequestURI();
                method = request.getMethod();
            }

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            // 处理设置注解上的参数
            // 设置action动作
            String businessType = controllerLog.businessType();
            // 设置标题
            String title = controllerLog.title();
            String[] params = requestValue(joinPoint, request, title).split("&");

            this.sendKafkaMessage(params);


        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    private void sendKafkaMessage(String[] params) {

        // 谁向谁的那篇文章点赞了
        String sourceName = ReqInfoContext.getReqInfo().getUser().getUserName();
        String articleIdStr = params[0].split("=")[1];
        Long articleId = Long.parseLong(articleIdStr);
        String typeStr = params[1].split("=")[1];
        int type = Integer.parseInt(typeStr);
        String typeName = NotifyTypeEnum.typeOf(type).getMsg();
        ArticleDO articleDO = articleReadService.queryBasicArticle(articleId);
        String articleTitle = articleDO.getTitle();
        Long targetUserId = articleDO.getUserId();

        ArticleKafkaMessageDTO articleKafkaMessageDTO = new ArticleKafkaMessageDTO();
        articleKafkaMessageDTO.setType(type);
        articleKafkaMessageDTO.setSourceUserName(sourceName);
        articleKafkaMessageDTO.setTargetUserId(targetUserId);
        articleKafkaMessageDTO.setArticleTitle(articleTitle);
        articleKafkaMessageDTO.setTypeName(typeName);
        kafkaTemplate.send(KafkaTopicConstant.ARTICLE_TOPIC, JSON.toJSONString(articleKafkaMessageDTO));

    }


    /**
     * 获取请求的参数，放到log中
     *
     * @throws Exception 异常
     */
    private String requestValue(JoinPoint joinPoint, HttpServletRequest request, String title) throws Exception {
        String requestMethod = request.getMethod();
        String param = "";
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            param = StringUtils.substring(params, 0, 2000);
        } else {
            if (ObjectUtils.isNotEmpty(request)) {

                Map<String, String[]> parameterMap = request.getParameterMap();
                if (StringUtils.equals(title, "article")) {
                    String articleId = parameterMap.get("articleId")[0];
                    String type = parameterMap.get("type")[0];
                    param = "articleId=" + articleId + "&type=" + type;
                }
            }
        }
        return param;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (ObjectUtils.isNotEmpty(o) && !isFilterObject(o)) {
                    try {
                        Object jsonObj = JSON.toJSON(o);
                        params += jsonObj.toString() + " ";
                    } catch (Exception e) {
                        log.info(e.toString());
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}

