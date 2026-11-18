package com.github.paicoding.forum.web.front.table;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.user.UserAIStatEnum;
import com.github.paicoding.forum.api.model.vo.table.TableLearningReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.web.global.ForumExceptionHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/** Opt-in localhost browser fixture: real controller, ForumExceptionHandler, and static frontend assets. */
@EnabledIfSystemProperty(named = "tables.browser", matches = "true")
class TableBrowserHarnessTest extends TableIntegrationTest {

    private String demoMarkdownFor(TableLearningReq req) {
        String action = req.getAction();
        List<String> values = req.getValues();
        String day = values != null && !values.isEmpty() ? values.get(0) : "当前任务";
        String topic = values != null && values.size() > 2 ? values.get(2) : "核心知识点";
        if ("explain".equals(action)) {
            return "围绕 **" + day + " · " + topic + "**，重点掌握以下两个底层机制：\n\n"
                    + "1. **动态扩容与连续内存布局**：`ArrayList` 底层基于 `Object[]` 数组，随机访问复杂度为 `O(1)`，触发扩容时按 1.5 倍（`oldCapacity + (oldCapacity >> 1)`）分配新数组。\n"
                    + "2. **哈希冲突与树化阈值**：`HashMap` 在链表长度达到 `8` 且桶数组容量不小于 `64` 时转为红黑树，避免极端哈希冲突退化为 `O(n)`。\n\n"
                    + "```java\n"
                    + "Map<String, Integer> counter = new HashMap<>(16);\n"
                    + "for (String word : Arrays.asList(\"ArrayList\", \"HashMap\", \"ArrayList\")) {\n"
                    + "    counter.merge(word, 1, Integer::sum);\n"
                    + "}\n"
                    + "System.out.println(counter.get(\"ArrayList\")); // 输出 2\n"
                    + "```";
        }
        if ("order".equals(action)) {
            return "建议按先原理、后源码、再手写验证的三步推进：\n\n"
                    + "1. **先通读核心结构（约 40% 时间）**：对比数组、链表与哈希表的时间复杂度边界，重点梳理扩容与负载因子 `0.75` 的取舍。\n"
                    + "2. **再结合调试看关键源码（约 35% 时间）**：单步跟踪 `putVal` 与 `resize` 两个方法，画一遍高低位链表拆分过程。\n"
                    + "3. **最后独立完成算法题闭环（约 25% 时间）**：不看题解手写一遍哈希表应用题，检验边界判空与复杂度分析。";
        }
        if ("quiz".equals(action)) {
            return "1. **原理理解**：为什么 `HashMap` 链表长度达到 8 时不一定立刻转成红黑树？还需要满足什么前置条件？\n"
                    + "2. **代码推理**：向初始容量为 `10` 的 `ArrayList` 连续添加第 `11` 个元素时，底层数组容量会变成多少？\n"
                    + "3. **实际应用**：已知要往 `HashMap` 一次性放入 `100` 个键值对且不希望触发扩容，构造器初始容量应传多少？\n\n"
                    + "【参考答案】\n"
                    + "1. 还需满足桶数组长度 `table.length >= 64`（`MIN_TREEIFY_CAPACITY`），否则优先执行 `resize()` 扩容而非树化。\n"
                    + "2. 容量变为 `15`，计算逻辑为 `10 + (10 >> 1) = 15`。\n"
                    + "3. 传入 `134` 或 `256`（按 `100 / 0.75 + 1 = 134`，`HashMap` 会自动向上取整为 2 的幂 `256`）。";
        }
        return "针对你问的「**" + req.getQuestion() + "**」：\n\n"
                + "结合 **" + day + "** 的学习范围，建议先确认数据规模与读写比例：读多写少优先用连续内存结构（如 `ArrayList`），高频按建查找用 `HashMap` 并预设容量 `expectedSize / 0.75 + 1`。\n\n"
                + "```java\n"
                + "int expectedSize = 100;\n"
                + "int initialCapacity = (int) (expectedSize / 0.75f) + 1;\n"
                + "Map<String, Object> cache = new HashMap<>(initialCapacity);\n"
                + "```";
    }

    @Test
    @SuppressWarnings("unchecked")
    void serveForBrowserAcceptance() throws Exception {
        Path root = Paths.get("..").toAbsolutePath().normalize();
        Path resources = root.resolve("paicoding-ui/src/main/resources");
        AtomicBoolean mockLoggedIn = new AtomicBoolean(true);

        when(ai.available()).thenReturn(true);
        when(ai.learningHelp(any(), anyBoolean(), any(TableLearningReq.class)))
                .thenAnswer(inv -> demoMarkdownFor(inv.getArgument(2)));
        when(ai.agentHelp(any(), anyBoolean(), any(TableLearningReq.class)))
                .thenAnswer(inv -> {
                    TableLearningReq req = inv.getArgument(2);
                    String q = req.getQuestion() == null ? "" : req.getQuestion();
                    java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
                    if (q.contains("并发") || q.contains("筛选") || q.contains("保留") || q.contains("导出")) {
                        java.util.List<Integer> matchedRows = new java.util.ArrayList<>();
                        if (req.getRows() != null) {
                            for (int idx = 0; idx < req.getRows().size(); idx++) {
                                String joined = String.join(" ", req.getRows().get(idx));
                                if (!q.contains("并发") || joined.contains("并发")) matchedRows.add(idx + 1);
                            }
                        }
                        java.util.List<Integer> keepCols = new java.util.ArrayList<>();
                        if (q.contains("保留") && req.getColumns() != null) {
                            for (int c = 0; c < req.getColumns().size(); c++) {
                                String colName = req.getColumns().get(c);
                                if (q.contains(colName)) keepCols.add(c);
                            }
                        }
                        if (keepCols.isEmpty() && req.getColumns() != null) {
                            for (int c = 0; c < req.getColumns().size(); c++) keepCols.add(c);
                        }
                        java.util.Map<String, Object> view = new java.util.LinkedHashMap<>();
                        view.put("rowIds", matchedRows);
                        view.put("columns", keepCols);
                        java.util.List<String> steps = new java.util.ArrayList<>();
                        steps.add("调整表格视图");
                        result.put("view", view);
                        if (q.contains("导出") || q.toLowerCase().contains("csv")) {
                            steps.add("生成 CSV");
                            result.put("export", view);
                        }
                        result.put("steps", steps);
                        result.put("answer", "已为你筛选出 **" + matchedRows.size() + "** 条匹配记录，并更新当前表格显示列。"
                                + (result.containsKey("export") ? "点击下方按钮即可下载对应的 CSV 文件。" : ""));
                        return result;
                    }
                    result.put("steps", java.util.Collections.emptyList());
                    result.put("answer", demoMarkdownFor(req));
                    return result;
                });
        when(ai.learningHelpStream(any(), anyBoolean(), any(TableLearningReq.class), any(Consumer.class), any(Runnable.class), any(Consumer.class)))
                .thenAnswer(inv -> {
                    TableLearningReq req = inv.getArgument(2);
                    Consumer<String> onDelta = inv.getArgument(3);
                    Runnable onComplete = inv.getArgument(4);
                    String full = demoMarkdownFor(req);
                    new Thread(() -> {
                        try {
                            int step = 28;
                            for (int i = 0; i < full.length(); i += step) {
                                onDelta.accept(full.substring(i, Math.min(full.length(), i + step)));
                                Thread.sleep(25);
                            }
                            onComplete.run();
                        } catch (Exception ignored) {
                            onComplete.run();
                        }
                    }).start();
                    return (Runnable) () -> {};
                });

        org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory liveRedis = null;
        if (Boolean.getBoolean("tables.browser.live-ai")) {
            org.springframework.core.env.StandardEnvironment env = new org.springframework.core.env.StandardEnvironment();
            for (org.springframework.core.env.PropertySource<?> source : new org.springframework.boot.env.YamlPropertySourceLoader().load("dev-ai",
                    new org.springframework.core.io.FileSystemResource(root.resolve("paicoding-web/src/main/resources-env/dev/application-ai.yml").toFile()))) {
                env.getPropertySources().addLast(source);
            }
            com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration.DeepSeekConf config =
                    org.springframework.boot.context.properties.bind.Binder.get(env)
                            .bind("deepseek", org.springframework.boot.context.properties.bind.Bindable.of(
                                    com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration.DeepSeekConf.class)).get();
            org.junit.jupiter.api.Assertions.assertTrue(config.getApiKey() != null && !config.getApiKey().trim().isEmpty(),
                    "Live AI requires configured DeepSeek credentials");
            int port = Integer.parseInt(System.getProperty("tables.browser.redis-port"));
            org.junit.jupiter.api.Assertions.assertTrue(port >= 1024 && port != 6379, "Use a disposable local Redis port for this fixture");
            liveRedis = new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory("127.0.0.1", port);
            liveRedis.afterPropertiesSet();
            org.springframework.data.redis.core.StringRedisTemplate redis = new org.springframework.data.redis.core.StringRedisTemplate(liveRedis);
            com.github.paicoding.forum.core.cache.RedisClient.register(redis);
            ai = new com.github.paicoding.forum.service.table.TableAiService(config);
            mvc = MockMvcBuilders.standaloneSetup(new TableRestController(ai))
                    .setHandlerExceptionResolvers(new ForumExceptionHandler())
                    .build();
            System.out.println("TABLE_LIVE_AI_READY model=" + config.getModel() + " host=" + config.getApiHost());
        }

        int listenPort = Integer.getInteger("tables.browser.port", 18086);
        int holdMinutes = Integer.getInteger("tables.browser.minutes", 30);
        Path stop = Paths.get(System.getProperty("java.io.tmpdir"), "paicoding-table-browser.stop");
        Files.deleteIfExists(stop);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", listenPort), 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);

        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                byte[] bytes;
                int status = 200;
                if (path.equals("/fixture/auth")) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.contains("login=false")) mockLoggedIn.set(false);
                    else if (query != null && query.contains("login=true")) mockLoggedIn.set(true);
                    bytes = ("{\"loggedIn\":" + mockLoggedIn.get() + "}").getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
                } else if (path.startsWith("/api/tables")) {
                    if (mockLoggedIn.get()) {
                        ReqInfoContext.ReqInfo info = new ReqInfoContext.ReqInfo();
                        info.setUserId(10L);
                        BaseUserInfoDTO user = new BaseUserInfoDTO();
                        user.setUserId(10L);
                        user.setStarStatus(UserAIStatEnum.FORMAL);
                        info.setUser(user);
                        ReqInfoContext.addReqInfo(info);
                    }
                    MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                            .request(HttpMethod.valueOf(exchange.getRequestMethod()), exchange.getRequestURI())
                            .content(org.springframework.util.StreamUtils.copyToByteArray(exchange.getRequestBody()));
                    exchange.getRequestHeaders().forEach((name, values) -> values.forEach(value -> request.header(name, value)));
                    MvcResult mvcResult = mvc.perform(request).andReturn();
                    if (mvcResult.getRequest().isAsyncStarted()) {
                        mvcResult.getAsyncResult(65_000L);
                        mvcResult = mvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult)).andReturn();
                    }
                    org.springframework.mock.web.MockHttpServletResponse response = mvcResult.getResponse();
                    status = response.getStatus();
                    bytes = response.getContentAsByteArray();
                    String contentType = response.getContentType();
                    exchange.getResponseHeaders().set("Content-Type",
                            contentType != null ? contentType : "application/json;charset=UTF-8");
                } else if (path.equals("/article-fixture") || path.equals("/")) {
                    bytes = buildPreviewHtml().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html;charset=UTF-8");
                } else {
                    Path file = resources.resolve("static").resolve(path.substring(1)).normalize();
                    if (!file.startsWith(resources.resolve("static")) || !Files.isRegularFile(file)) {
                        status = 404;
                        bytes = new byte[0];
                    } else {
                        bytes = Files.readAllBytes(file);
                    }
                    exchange.getResponseHeaders().set("Content-Type",
                            path.endsWith(".js") ? "application/javascript;charset=UTF-8"
                                    : path.endsWith(".css") ? "text/css;charset=UTF-8" : "application/octet-stream");
                }
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
            } finally {
                ReqInfoContext.clear();
                exchange.close();
            }
        });

        try {
            server.start();
            System.out.println("TABLE_BROWSER_READY http://127.0.0.1:" + listenPort + "/article-fixture");
            long deadline = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(holdMinutes);
            while (!Files.exists(stop) && System.currentTimeMillis() < deadline) {
                Thread.sleep(500);
            }
        } finally {
            server.stop(0);
            executor.shutdownNow();
            if (liveRedis != null) liveRedis.destroy();
            Files.deleteIfExists(stop);
            System.out.println("TABLE_BROWSER_CLOSED");
        }
    }

    private String buildPreviewHtml() {
        return "<!doctype html><html lang='zh-CN'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>3个月冲刺计划 - 技术派多维表格验收预览</title>"
                + "<link rel='stylesheet' href='/css/components/article-tables.css?v=20261118'>"
                + "<style>"
                + "body{margin:0;background:#f4f6f9;color:#2c3e50;font:15px/1.75 -apple-system,BlinkMacSystemFont,'PingFang SC',sans-serif}"
                + ".demo-nav{position:sticky;top:0;z-index:200;display:flex;align-items:center;gap:20px;padding:0 28px;height:56px;background:#1f242d;color:#fff}"
                + ".demo-brand{font-weight:700;font-size:17px;color:#ff8721;margin-right:12px}"
                + ".demo-nav-controls{margin-left:auto;display:flex;align-items:center;gap:10px;font-size:13px}"
                + ".demo-nav-controls button{border:1px solid rgba(255,255,255,.25);background:rgba(255,255,255,.08);color:#fff;border-radius:6px;padding:5px 12px;cursor:pointer;font-size:13px}"
                + ".demo-nav-controls button.active{background:#f5771e;border-color:#f5771e}"
                + ".demo-shell{display:grid;grid-template-columns:240px minmax(0,1fr) 240px;gap:20px;max-width:1420px;margin:20px auto;padding:0 16px}"
                + ".demo-side,.demo-toc{background:#fff;border-radius:10px;padding:16px;height:fit-content;border:1px solid #e8ecf2;font-size:13.5px}"
                + ".demo-side h4,.demo-toc h4{margin:0 0 10px;font-size:14px;color:#1e293b}"
                + ".demo-side ul,.demo-toc ul{list-style:none;padding:0;margin:0;display:grid;gap:8px;color:#526176}"
                + ".demo-side li.active{color:#e96d1c;font-weight:600;background:#fff5ec;padding:6px 10px;border-radius:6px}"
                + ".demo-article{background:#fff;border-radius:10px;padding:28px 34px;border:1px solid #e8ecf2;min-width:0}"
                + "#postsTitle{margin:0 0 12px;font-size:26px;color:#1a2433}"
                + ".demo-edition-bar{display:flex;align-items:center;gap:8px;margin-bottom:18px;padding-bottom:14px;border-bottom:1px solid #eef1f5;font-size:13px;color:#64748b}"
                + ".demo-edition-bar button{border:1px solid #dfe5ed;background:#fff;border-radius:6px;padding:4px 10px;cursor:pointer;font-size:12.5px;color:#475569}"
                + ".demo-edition-bar button.active{border-color:#f5771e;color:#d95f0e;background:#fff6ee;font-weight:600}"
                + ".article-content h2{color:#e96d1c;font-size:20px;margin:22px 0 10px}"
                + ".article-content h3{font-size:16px;margin:24px 0 8px;color:#243447}"
                + ".article-content table{border-collapse:collapse;width:100%}"
                + ".article-content th,.article-content td{border:1px solid #e5e9f0;padding:10px 12px;text-align:left;font-size:13.5px}"
                + ".article-content th{background:#f8fafc}"
                + "#loginModal{display:none;position:fixed;inset:0;z-index:2000;background:rgba(15,23,42,.5);place-items:center}"
                + "#loginModal.is-open{display:grid}"
                + ".login-card{background:#fff;border-radius:12px;padding:24px 28px;width:340px;box-shadow:0 20px 40px rgba(0,0,0,.2);text-align:center}"
                + ".login-card h3{margin:0 0 8px}.login-card p{color:#64748b;font-size:13.5px;margin:0 0 18px}"
                + ".login-card button{border:0;border-radius:6px;padding:8px 16px;cursor:pointer;font-size:14px;margin:0 6px}"
                + ".login-primary{background:#f5771e;color:#fff}.login-cancel{background:#f1f5f9;color:#475569}"
                + "@media(max-width:960px){.demo-shell{grid-template-columns:1fr}.demo-side,.demo-toc{display:none}.demo-article{padding:18px}}"
                + "</style></head><body>"
                + "<nav class='demo-nav'><span class='demo-brand'>技术派 · 教程</span><span>面试冲刺专栏</span>"
                + "<div class='demo-nav-controls'><span>当前身份：</span>"
                + "<button id='authToggleBtn' class='active' type='button'>已登录（星球用户）</button>"
                + "</div></nav>"
                + "<div class='demo-shell'>"
                + "<aside class='demo-side'><h4>面试准备篇</h4><ul>"
                + "<li class='active'>1. 3个月冲刺计划</li><li>2. 如何准备技术面试</li><li>3. 如何打磨项目简历</li><li>4. 高效刷算法路线</li>"
                + "</ul></aside>"
                + "<main class='demo-article'>"
                + "<h1 id='postsTitle' data-id='101'>3个月冲刺计划</h1>"
                + "<div class='demo-edition-bar'><span>教程版本切换验证：</span>"
                + "<button type='button' class='active' data-ed='v2'>v2.0 · 秋招冲刺版（当前版）</button>"
                + "<button type='button' data-ed='v1'>v1.0 · 基础精简版（历史版）</button>"
                + "</div>"
                + "<div id='articleContent' class='article-content'>" + editionV2Html() + "</div>"
                + "</main>"
                + "<aside class='demo-toc'><h4>本节验收要点</h4><ul>"
                + "<li>• 勾选左侧复选框测试打卡进度保存</li>"
                + "<li>• 单击任意单元格展开完整长文本</li>"
                + "<li>• 点击 AI 学习助教切换 3 个按钮对比缓存</li>"
                + "<li>• 点击全屏查看验证视口浮层不破坏正文</li>"
                + "<li>• 对比下方 2×2 小表保持紧凑原生样式</li>"
                + "</ul></aside>"
                + "</div>"
                + "<div id='loginModal'><div class='login-card'><h3>登录技术派</h3>"
                + "<p>未登录点击 AI 学习助教时自动拉起此登录弹窗，避免生硬报错。</p>"
                + "<button type='button' class='login-primary' id='confirmLoginBtn'>一键模拟登录</button>"
                + "<button type='button' class='login-cancel' id='closeLoginBtn'>暂不登录</button>"
                + "</div></div>"
                + "<script src='/js/biz/selection.js?v=20261118'></script>"
                + "<script src='/js/biz/table-model.js?v=20261118'></script>"
                + "<script id='paiArticleTablesScript' src='/js/biz/article-tables.js?v=20261118'></script>"
                + "<script>"
                + "window.jQuery = function(sel){ if(sel==='#loginModal') return { modal(act){ document.getElementById('loginModal').classList.toggle('is-open', act==='show'); } }; return {}; };"
                + "const v2Html = " + toJsString(editionV2Html()) + ";"
                + "const v1Html = " + toJsString(editionV1Html()) + ";"
                + "document.querySelectorAll('[data-ed]').forEach(btn => btn.onclick = () => {"
                + "  document.querySelectorAll('[data-ed]').forEach(b => b.classList.toggle('active', b===btn));"
                + "  window.articleReadingContext = { articleId: '101', selectedEditionId: btn.dataset.ed };"
                + "  window.resetArticleTables();"
                + "  document.getElementById('articleContent').innerHTML = btn.dataset.ed === 'v1' ? v1Html : v2Html;"
                + "  window.initArticleTables();"
                + "});"
                + "const authBtn = document.getElementById('authToggleBtn');"
                + "let isLogged = true;"
                + "async function setAuth(next){ isLogged = next; await fetch('/fixture/auth?login='+next); authBtn.textContent = next ? '已登录（星球用户）' : '未登录访客（点此切回登录）'; authBtn.classList.toggle('active', next); location.reload(); }"
                + "authBtn.onclick = () => setAuth(!isLogged);"
                + "fetch('/api/tables/capabilities').then(r=>r.json()).then(r=>{ isLogged = Boolean(r.result?.loggedIn); authBtn.textContent = isLogged ? '已登录（星球用户）' : '未登录访客（点此切回登录）'; authBtn.classList.toggle('active', isLogged); });"
                + "document.getElementById('confirmLoginBtn').onclick = () => setAuth(true);"
                + "document.getElementById('closeLoginBtn').onclick = () => document.getElementById('loginModal').classList.remove('is-open');"
                + "</script></body></html>";
    }

    private static String toJsString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private String editionV2Html() {
        return "<p id='introPara'>这份冲刺计划把 Java 核心、算法题单和 AI Agent 实战串联起来，点击表格任意一行即可查看完整学习建议与 AI 学习助教。</p>"
                + "<h2>第一个月</h2>"
                + "<table>"
                + "<tr><td>Day</td><td>每天时长</td><td>面渣逆袭</td><td>算法</td><td>MYDB</td><td>PaiCLI</td><td>PaiAgent</td><td>Java 进阶之路</td><td>重要程度</td><td>学习建议</td></tr>"
                + "<tr><td>Day 1</td><td>3 小时</td><td>Java 基础篇 1~6</td><td>两数之和、两数相加</td><td>无</td><td>环境初始化</td><td>无</td><td><a href='#introPara'>1.1 面向对象与基础语法</a></td><td>★★★</td><td>梳理值传递、包装类缓存池与 String 不可变设计，配合手写 2 道链表基础题巩固引用理解。</td></tr>"
                + "<tr><td>Day 2</td><td>6 小时</td><td>Java 基础篇 7~16</td><td>链表反转、合并有序链表</td><td>无</td><td>命令解析模块</td><td>无</td><td><a href='https://paicoding.com'>1.2 异常、泛型与反射机制</a></td><td>★★</td><td>搞清楚泛型擦除边界与反射调用开销，完成 CLI 参数解析练习。</td></tr>"
                + "<tr><td>Day 3</td><td>5 小时</td><td>Java 基础篇 17~32</td><td>有效的括号、最小栈</td><td>无</td><td>工具注册抽象</td><td>无</td><td><a href='https://paicoding.com'>1.3 IO 模型与序列化</a></td><td>★★</td><td>对比 BIO / NIO 核心差异，画一遍栈帧进出过程。</td></tr>"
                + "<tr><td>Day 4 ~ 7</td><td>4 小时</td><td><a href='https://paicoding.com'>集合框架篇（全部内容）</a></td><td>哈希表与滑动窗口 11~20 题</td><td>无</td><td>无</td><td><a href='https://paicoding.com'>Vibe Coding 项目实战：从零搭建工作流编排与工具调用</a></td><td><a href='https://paicoding.com'>2.5 集合框架源码精讲</a></td><td>★★★</td><td>掌握 ArrayList 扩容、LinkedList 双链表、HashMap 哈希扰动与红黑树转换阈值（8 和 64），同步串联 Spring AI 与 MCP 工具调用流程。</td></tr>"
                + "<tr><td>Day 8 ~ 14</td><td>5 小时</td><td><a href='https://paicoding.com'>并发编程篇（全部内容）</a></td><td>二叉树与回溯 21~40 题</td><td>事务隔离初探</td><td><a href='https://paicoding.com'>CLI 异步任务执行器与流式终端输出</a></td><td>无</td><td><a href='https://paicoding.com'>3.1 JUC 线程池与 AQS 源码</a></td><td>★★★</td><td>重点攻克 volatile 内存屏障、synchronized 锁升级、ThreadPoolExecutor 七大核心参数及拒绝策略，手写生产者消费者阻塞队列。</td></tr>"
                + "</table>"
                + "<h3>附：常见学习节奏对照（小型说明表保持原生紧凑样式）</h3>"
                + "<table>"
                + "<tr><th>人群分类</th><th>每日建议投入</th><th>侧重点</th></tr>"
                + "<tr><td>在职跳槽</td><td>2.5 ~ 3 小时</td><td>优先攻克高频集合、并发与手头主项目复盘</td></tr>"
                + "<tr><td>全职备战 / 应届生</td><td>5 ~ 6 小时</td><td>源码推导 + 算法每日 3 题 + 完整跑通 1 个 AI 实战项目</td></tr>"
                + "</table>"
                + "<p id='outroPara'>建议每完成一阶段就在表格第一列打勾记录，点击「只看未完成」聚焦剩余任务。</p>";
    }

    private String editionV1Html() {
        return "<p>这是 v1.0 历史版本的精简计划表，用于验证切换文章版本后表格组件能够自动销毁并重新挂载。</p>"
                + "<h2>第一阶段（v1.0 历史版）</h2>"
                + "<table>"
                + "<tr><th>阶段</th><th>核心主题</th><th>必刷题单</th><th>预计耗时</th><th>阶段目标</th></tr>"
                + "<tr><td>第 1 周</td><td>Java 语法与集合源码</td><td>数组与哈希表 10 题</td><td>20 小时</td><td>熟练掌握 ArrayList 与 HashMap 底层实现</td></tr>"
                + "<tr><td>第 2 周</td><td>JUC 并发编程</td><td>多线程打印与阻塞队列</td><td>25 小时</td><td>理解 AQS、线程池参数配置与常见并发容器</td></tr>"
                + "<tr><td>第 3 周</td><td>JVM 内存与垃圾回收</td><td>线上 OOM 排查演练</td><td>18 小时</td><td>掌握内存区域划分、双亲委派与 G1 回收流程</td></tr>"
                + "<tr><td>第 4 周</td><td>MySQL 索引与事务锁</td><td>高频 SQL 慢查询优化</td><td>22 小时</td><td>掌握 B+ 树索引失效场景、MVCC 与间隙锁</td></tr>"
                + "</table>";
    }
}
