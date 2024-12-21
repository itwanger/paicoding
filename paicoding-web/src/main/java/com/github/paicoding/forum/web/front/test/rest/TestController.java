package com.github.paicoding.forum.web.front.test.rest;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.pay.ThirdPayWayEnum;
import com.github.paicoding.forum.api.model.exception.ForumAdviceException;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.dal.DsAno;
import com.github.paicoding.forum.core.dal.DsSelectExecutor;
import com.github.paicoding.forum.core.dal.MasterSlaveDsEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.util.EmailUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.article.conveter.PayConverter;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.config.service.GlobalConfigService;
import com.github.paicoding.forum.service.pay.model.PrePayInfoResBo;
import com.github.paicoding.forum.service.pay.model.ThirdPayOrderReqBo;
import com.github.paicoding.forum.service.pay.service.ThirdPayHandler;
import com.github.paicoding.forum.service.statistics.converter.StatisticsConverter;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountExcelDO;
import com.github.paicoding.forum.service.statistics.service.RequestCountService;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
import com.github.paicoding.forum.service.statistics.service.impl.CountServiceImpl;
import com.github.paicoding.forum.web.front.test.vo.EmailReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.ProxyUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于一些功能测试的入口，默认都使用从库，不支持修改数据
 *
 * @author YiHui
 * @date 2023/3/19
 */
@Slf4j
@DsAno(MasterSlaveDsEnum.SLAVE)
@RestController
@RequestMapping(path = "test")
public class TestController {
    private AtomicInteger cnt = new AtomicInteger(1);

    /**
     * 测试邮件发送
     *
     * @param req
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @RequestMapping(path = "email")
    public ResVo<String> email(EmailReqVo req) {
        if (StringUtils.isBlank(req.getTo()) || req.getTo().indexOf("@") <= 0) {
            return ResVo.fail(Status.newStatus(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "非法的邮箱接收人"));
        }
        if (StringUtils.isBlank(req.getTitle())) {
            req.setTitle("技术派的测试邮件发送");
        }
        if (StringUtils.isBlank(req.getContent())) {
            req.setContent("技术派的测试发送内容");
        } else {
            // 测试邮件内容，不支持发送邮件正文，避免出现垃圾情况
            req.setContent(StringEscapeUtils.escapeHtml4(req.getContent()));
        }

        boolean ans = EmailUtil.sendMail(req.getTitle(), req.getTo(), req.getContent());
        log.info("测试邮件发送，计数：{}，发送内容：{}", cnt.addAndGet(1), req);
        return ResVo.ok(String.valueOf(ans));
    }

    @RequestMapping(path = "alarm")
    public ResVo<String> alarm(String content) {
        content = StringEscapeUtils.escapeHtml4(content);
        log.error("测试异常报警: {}", content);
        return ResVo.ok("移除日志接收完成！");
    }

    @RequestMapping(path = "testControllerAdvice")
    @ResponseBody
    public String testControllerAdvice() {
        throw new ForumAdviceException(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "测试ControllerAdvice异常");
    }

    @RequestMapping(path = "exception")
    @ResponseBody
    public String unexpect() {
        throw new RuntimeException("非预期异常");
    }

    /**
     * 测试 Knife4j
     *
     * @return
     */
    @RequestMapping(value = "/testKnife4j", method = RequestMethod.POST)
    public String testKnife4j() {
        return "沉默王二又帅又丑";
    }

    // POST 请求，使用 HttpServletRequest 获取请求参数
    @PostMapping(path = "testPost")
    public String testPost(HttpServletRequest request) {
        String name = request.getParameter("name");
        String age = request.getParameter("age");
        return "name=" + name + ", age=" + age;
    }

    // POST 请求，使用 HttpServletRequest 获取请求参数，使用 JSON 把参数转为字符串
    @PostMapping(path = "testPostJson")
    public String testPostJson(HttpServletRequest request) {
        return JsonUtil.toStr(request.getParameterMap());
    }

    // POST 请求，使用 HttpServletRequest 获取 JSON 请求参数
    @PostMapping(path = "testPostJson2")
    public String testPostJson2(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();  // body中即是JSON格式的请求参数
    }

    @PostMapping(path = "testPostJson3")
    public String testPostJson3(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("testPostJson3 第一次: {}", sb);

        StringBuilder sb1 = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb1.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("testPostJson3 第二次: {}", sb1);

        return sb1.toString();  // body中即是JSON格式的请求参数
    }

    @Autowired
    private StatisticsSettingService statisticsSettingService;

    /**
     * 只读测试，如果有更新就会报错
     *
     * @return
     */
    @GetMapping(path = "ds/read")
    public String readOnly() {
        // 保存请求计数
        statisticsSettingService.saveRequestCount(ReqInfoContext.getReqInfo().getClientIp());
        return "使用从库：更新成功!";
    }

    /**
     * 只读测试，如果有更新就会报错
     *
     * @return
     */
    @GetMapping(path = "ds/write2")
    public String write2() {
        log.info("------------------- 业务逻辑进入 ----------------------------");
        long old = statisticsSettingService.getStatisticsCount().getPvCount();
        DsSelectExecutor.execute(MasterSlaveDsEnum.MASTER, () -> statisticsSettingService.saveRequestCount(ReqInfoContext.getReqInfo()
                .getClientIp()));
        // 保存请求计数
        long n = statisticsSettingService.getStatisticsCount().getPvCount();
        log.info("------------------- 业务逻辑结束 ----------------------------");
        return "编程式切换主库：更新成功! old=" + old + " new=" + n;
    }


    @DsAno(MasterSlaveDsEnum.MASTER)
    @GetMapping(path = "ds/write")
    public String write() {
        // 保存请求计数
        long old = statisticsSettingService.getStatisticsCount().getPvCount();
        statisticsSettingService.saveRequestCount(ReqInfoContext.getReqInfo().getClientIp());
        long n = statisticsSettingService.getStatisticsCount().getPvCount();
        return "使用主库：更新成功! old=" + old + " new=" + n;
    }


    /**
     * 打印配置信息
     *
     * @param beanName
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping("print")
    public String printInfo(String beanName) throws Exception {
        Object bean = SpringUtil.getBeanOrNull(beanName);
        if (bean == null) {
            try {
                Class clz = ClassUtils.forName(beanName, this.getClass().getClassLoader());
                bean = SpringUtil.getBeanOrNull(clz);
            } catch (ClassNotFoundException e) {
            }
        }

        if (bean != null && ClassUtils.isCglibProxy(bean)) {
            return printProxyFields(bean);
        }

        return JsonUtil.toStr(bean);
    }

    private String printProxyFields(Object proxy) {
        Class clz = ProxyUtils.getUserClass(proxy);
        Field[] fields = clz.getDeclaredFields();
        JSONObject obj = new JSONObject();
        for (Field f : fields) {
            f.setAccessible(true);
            obj.put(f.getName(), ReflectionUtils.getField(f, proxy));
        }
        return obj.toString();
    }


    /**
     * 刷新global_config动态配置
     *
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping("refresh/config")
    public String refreshConfig() {
        DynamicConfigContainer configContainer = SpringUtil.getBean(DynamicConfigContainer.class);
        configContainer.forceRefresh();
        return JsonUtil.toStr(configContainer.getCache());
    }

    /**
     * 更新启用的AI模型
     *
     * @param ai
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping("ai/update")
    public AISourceEnum updateAi(String ai) {
        ChatFacade chatFacade = SpringUtil.getBean(ChatFacade.class);
        chatFacade.refreshAiSourceCache(AISourceEnum.valueOf(ai));
        return chatFacade.getRecommendAiSource();
    }

    @Autowired
    private SensitiveService sensitiveService;

    /**
     * 敏感词校验
     *
     * @param txt
     * @return
     */
    @GetMapping(path = "sensitive/check")
    public List<String> sensitiveWords(String txt) {
        return sensitiveService.findAll(txt);
    }


    /**
     * 返回所有命中的敏感词
     *
     * @return
     */
    @GetMapping(path = "sensitive/all")
    public Map<String, Integer> showAllHitSensitiveWords() {
        return sensitiveService.getHitSensitiveWords();
    }


    /**
     * 将敏感词添加到白名单内
     *
     * @param word
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "sensitive/addAllowWord")
    public String addSensitiveAllowWord(String word) {
        SpringUtil.getBean(GlobalConfigService.class).addSensitiveWhiteWord(word);
        return "ok";
    }


    @Autowired
    private CountServiceImpl countServiceImpl;

    @GetMapping(path = "autoRefreshUserInfo")
    public String autoRefreshUserInfo() {
        countServiceImpl.autoRefreshAllUserStatisticInfo();
        return "ok";
    }

    // 前端把一些数据发送到这里并打印出来
    @PostMapping(path = "loadmore")
    public void testLoadMore(@RequestBody String loadmore) {
        log.info("loadmore: {}", loadmore);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "h5pay")
    public ResVo<PrePayInfoResBo> testH5Pay(String outTradeNo, int amount) throws Exception {
        ThirdPayOrderReqBo req = new ThirdPayOrderReqBo();
        req.setOutTradeNo("TEST-H5-" + outTradeNo + "-" + ReqInfoContext.getReqInfo().getUserId());
        req.setDescription(ReqInfoContext.getReqInfo().getUser().getUserName() + "-测试h5支付");
        req.setTotal(amount <= 0 ? 1 : amount);
        req.setPayWay(ThirdPayWayEnum.WX_H5);
        ThirdPayHandler payFacade = SpringUtil.getBeanOrNull(ThirdPayHandler.class);
        if (payFacade != null) {
            PrePayInfoResBo res = payFacade.createPayOrder(req);
            log.info("返回结果: {}", res);
            return ResVo.ok(res);
        } else {
            return ResVo.ok(null);
        }
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "nativePay")
    public ResVo<PrePayInfoResBo> testNativePay(String outTradeNo, int amount) throws Exception {
        ThirdPayOrderReqBo req = new ThirdPayOrderReqBo();
        req.setOutTradeNo("TEST-N-" + outTradeNo + "-" + ReqInfoContext.getReqInfo().getUserId());
        req.setDescription(ReqInfoContext.getReqInfo().getUser().getUserName() + "-测试native支付");
        req.setTotal(amount <= 0 ? 1 : amount);
        req.setPayWay(ThirdPayWayEnum.WX_NATIVE);
        ThirdPayHandler thirdPayService = SpringUtil.getBeanOrNull(ThirdPayHandler.class);
        if (thirdPayService != null) {
            PrePayInfoResBo res = thirdPayService.createPayOrder(req);
            log.info("返回结果: {}", res);
            res.setPrePayId(PayConverter.genQrCode(res.getPrePayId()));
            return ResVo.ok(res);
        } else {
            return ResVo.ok(null);
        }
    }

    @Autowired
    private RequestCountService requestCountService;

    // 准备使用 FastExcel 批量导出 500万条数据
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "exportBatch")
    public void exportBatch(HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();

        // 查出总数量
        long total = requestCountService.count();

        // 每页大小
        int pageSize = 100000; // 每页 1 万条数据

        // 每个 Sheet 容纳数据条数
        int sheetSize = 1000000; // 每个 Sheet 100 万条数据
        int sheetCount = (int) (total / sheetSize + (total % sheetSize == 0 ? 0 : 1));

        // 文件名
        String fileName = URLEncoder.encode("批量导出测试.xlsx", "UTF-8").replaceAll("\\+", "%20");

        // 设置响应头
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        // 开始导出
        try (ExcelWriter excelWriter = FastExcel.write(outputStream, RequestCountExcelDO.class).build()) {
            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                // 命名 Sheet
                WriteSheet sheet = FastExcel.writerSheet(sheetIndex, "sheet" + (sheetIndex + 1)).build();

                // 查询数据
                for (int pageIndex = 0; pageIndex < sheetSize / pageSize; pageIndex++) {
                    // 第一页是从 0-9999，第二页是从 10000-19999
                    int offset = sheetIndex * sheetSize + pageIndex * pageSize + 1;
                    // TODO 自定义线程池+ CountDownLatch 进行优化
                    List<RequestCountDO> data = requestCountService.listRequestCount(PageParam.newPageInstance(offset, pageSize));
                    List<RequestCountExcelDO> list = StatisticsConverter.convertToRequestCountExcelDOList(data);
                    excelWriter.write(list, sheet);
                    log.info("导出第 {} 页数据，目前是第{} 条数据", pageIndex, offset);
                }

            }
        }
    }

    // 准备使用 FastExcel 批量导出 500万条数据
    // 自定义线程池，以及 CountDownLatch 进行优化
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "exportBatchPoolCountDownLatch")
    public void exportBatchPoolCountDownLatch(HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();

        // 查出总数量
        long total = requestCountService.count();

        // 每页大小
        int pageSize = 100000; // 每页 1 万条数据

        // 每个 Sheet 容纳数据条数
        int sheetSize = 1000000; // 每个 Sheet 100 万条数据
        int sheetCount = (int) (total / sheetSize + (total % sheetSize == 0 ? 0 : 1));

        // 文件名
        String fileName = URLEncoder.encode("批量导出测试.xlsx", "UTF-8").replaceAll("\\+", "%20");

        // 设置响应头
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        // 开始导出
        int threadPoolSize = Runtime.getRuntime().availableProcessors(); // 根据 CPU 核心数动态分配线程
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(sheetCount * (sheetSize / pageSize));

        try (ExcelWriter excelWriter = FastExcel.write(outputStream, RequestCountExcelDO.class).build()) {
            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                WriteSheet sheet = FastExcel.writerSheet(sheetIndex, "sheet" + (sheetIndex + 1)).build();
                for (int pageIndex = 0; pageIndex < sheetSize / pageSize; pageIndex++) {
                    int finalSheetIndex = sheetIndex;
                    int finalPageIndex = pageIndex;
                    threadPool.submit(() -> {
                        try {
                            // 计算分页偏移量
                            int offset = finalSheetIndex * sheetSize + finalPageIndex * pageSize + 1;
                            List<RequestCountDO> data = requestCountService.listRequestCount(PageParam.newPageInstance(offset, pageSize));
                            List<RequestCountExcelDO> list = StatisticsConverter.convertToRequestCountExcelDOList(data);

                            synchronized (excelWriter) {
                                excelWriter.write(list, sheet); // 写入操作需要同步，避免线程冲突
                            }
                            log.info("导出第 {} 页数据，目前是第 {} 条数据", finalPageIndex, offset);
                        } catch (Exception e) {
                            log.error("导出第 {} 页数据时出错", finalPageIndex, e);
                        } finally {
                            latch.countDown(); // 减少计数器
                        }
                    });
                }
            }
            latch.await(); // 等待所有线程完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("线程中断: ", e);
        } finally {
            threadPool.shutdown(); // 关闭线程池
        }
    }

}
