# 技术派生产环境 Redis 内存从 2.55G 降到 128M，顺手把 ReqRecordFilter 卡死问题修了

球友好，我是二哥呀。

今天这篇来聊一个非常真实的生产环境问题：技术派突然访问变慢，甚至首页都打不开，Nginx 频繁报 upstream timeout，应用健康检查却还是 UP。

这种问题最迷惑人的地方就在这里：进程还活着，端口也在监听，健康检查也没挂，但用户就是访问不了。

最后定位下来，根因主要有两个：

1. 请求链路里的 `ReqRecordFilter` 会记录 PV/UV 和请求统计，Redis 一旦变慢，请求线程就会被拖住。
2. Redis 里访问统计 key 膨胀到了 80 多万，内存一度占到 2.55G，里面还有几个异常大的 session meta key。

这类问题非常适合拿来做实战教程，因为它不是简单地“加个缓存”或者“改个参数”，而是完整覆盖了线上排查、代码优化、Redis 数据治理和发版验证。

## 01、先看生产现象

一开始看到的是 Nginx 侧大量超时，请求打到后端 8080 后迟迟没有响应。

同时应用的 actuator health 还是正常的：

```bash
curl http://127.0.0.1:8999/actuator/health
```

返回：

```json
{"status":"UP"}
```

这说明 JVM 进程没有挂，Spring Boot 也没有完全死掉。但业务请求超时，通常意味着 Tomcat 工作线程被某些慢操作占住了。

接着看应用日志，关键错误是 Redis 命令超时：

```text
io.lettuce.core.RedisCommandTimeoutException: Command timed out after 1 minute(s)
```

堆栈主要落在两个地方：

- `ReqRecordFilter`
- `SitemapServiceImpl.saveVisitInfo`

也就是说，每次请求进来，都会尝试写 Redis 访问统计；Redis 慢了，请求线程也跟着慢。

## 02、为什么 ReqRecordFilter 会拖垮请求

技术派里有一个全局过滤器 `ReqRecordFilter`，它负责构建请求上下文、打印请求日志、记录 PV/UV 等。

原来的访问统计逻辑大概是这样：

```java
AsyncUtil.execute(() -> SpringUtil.getBean(SitemapService.class)
        .saveVisitInfo(reqInfo.getClientIp(), reqInfo.getPath()));
```

看起来用了异步，问题不大。

但真正看 `AsyncUtil` 的线程池配置会发现，它用了 `SynchronousQueue` 和 `CallerRunsPolicy`。这组配置在高压场景下有一个副作用：当异步线程池扛不住时，任务会回退到调用线程执行。

在这里，调用线程就是 Tomcat 的请求线程。

于是链路变成了这样：

```text
用户请求
  -> Tomcat 线程
  -> ReqRecordFilter
  -> 提交访问统计任务
  -> 异步池满了
  -> CallerRunsPolicy
  -> Tomcat 线程自己执行 Redis 写入
  -> Redis timeout 60s
  -> 请求线程被占住
```

这就是为什么健康检查还活着，但页面请求打不开。

优化的第一步，就是把“统计类任务”从通用异步池里拿出来，单独建一个有界线程池。

```java
private static final ThreadPoolExecutor REQUEST_STAT_EXECUTOR = new ThreadPoolExecutor(
        1,
        4,
        60,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(2048),
        new NamedDaemonThreadFactory("paicoding-req-stat-"),
        new ThreadPoolExecutor.DiscardPolicy());
```

这里有几个关键点：

- 核心线程数小，避免统计任务抢业务资源。
- 队列有界，防止 Redis 抖动时无限堆积。
- 使用 `DiscardPolicy`，宁愿丢一点统计数据，也不能拖垮主链路。
- 单独线程名前缀，线上排查时一眼能看出来。

然后把访问统计包一层异常保护：

```java
private void recordVisitAsync(ReqInfoContext.ReqInfo reqInfo) {
    REQUEST_STAT_EXECUTOR.execute(() -> {
        try {
            SpringUtil.getBean(SitemapService.class)
                    .saveVisitInfo(reqInfo.getClientIp(), reqInfo.getPath());
        } catch (Throwable e) {
            log.warn("record visit stat failed, ip={}, path={}", reqInfo.getClientIp(), reqInfo.getPath(), e);
        }
    });
}
```

请求计数也同样异步化：

```java
private void recordRequestCountAsync(String clientIp) {
    REQUEST_STAT_EXECUTOR.execute(() -> {
        try {
            statisticsSettingService.saveRequestCount(clientIp);
        } catch (Throwable e) {
            log.warn("record request count failed, ip={}", clientIp, e);
        }
    });
}
```

这类统计数据的原则是：能记就记，记不了就丢，不能影响用户访问。

## 03、哪些请求不应该进 PV/UV 统计

生产环境还有一个细节：`/notify` 是 WebSocket 相关路径，异常扫描或者普通 GET 请求打到这里时，也可能触发一堆无意义统计。

所以统计前加了一层过滤：

```java
private boolean shouldRecordVisit(HttpServletRequest request, ReqInfoContext.ReqInfo reqInfo) {
    if (request == null || reqInfo == null || StringUtils.isBlank(reqInfo.getPath())) {
        return false;
    }
    if (!HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
        return false;
    }
    if (StringUtils.isNotBlank(request.getHeader("Upgrade"))) {
        return false;
    }
    String path = reqInfo.getPath();
    return !StringUtils.equalsAny(path, "/notify", "/gpt", "/subscribe")
            && !StringUtils.startsWithAny(path, "/actuator", "/admin", "/admin-view", "/api/admin");
}
```

这里主要跳过几类请求：

- 非 GET 请求。
- WebSocket Upgrade 请求。
- `/notify`、`/gpt`、`/subscribe` 这类不适合做普通页面 PV 的路径。
- actuator、admin、admin-view、api/admin 等后台路径。

这样 Redis 里只保留真正有意义的页面访问统计。

## 04、Redis 内存为什么会爆

接着看 Redis。

线上当时的情况大概是这样：

```text
used_memory_human:2.55G
used_memory_peak_human:3.95G
maxmemory_human:0B
maxmemory_policy:noeviction
dbsize:903898
pai_visit_info*:857279
pai_visit_info_具体IP:856570
pai_session_meta:*:899
```

90 多万个 key 里，85 万多个都是 `pai_visit_info*`，其中绝大多数是按 IP 维度生成的访问统计。

旧版 `saveVisitInfo` 会维护类似这样的结构：

```text
pai_visit_info
pai_visit_info_20260612
pai_visit_info_192.168.1.1
pai_visit_info_20260612 里的 pv_xxx_ip 字段
```

问题在于：

- 每个 IP 可能生成一个全局统计 key。
- 每天还会写大量 `pv_path_ip` 字段。
- 老 key 没有 TTL。
- path 没有归一化，路径一多，field 也跟着膨胀。

这类统计如果按照“精确 IP 集合”的方式长期保存，Redis 迟早会爆。

## 05、UV 统计改成 HyperLogLog

UV 本质上是去重计数。对站点统计来说，通常不需要精确到 100%。

Redis 的 HyperLogLog 正好适合这个场景：它可以用固定的小内存估算基数，适合做 UV。

先在 `RedisClient` 里封装一个 `pfAdd`：

```java
public static Long pfAdd(String key, String... values) {
    byte[][] bytes = new byte[values.length][];
    IntStream.range(0, values.length).forEach(i -> bytes[i] = valBytes(values[i]));
    return template.execute((RedisCallback<Long>) connection -> connection.pfAdd(keyBytes(key), bytes));
}
```

然后在 `SitemapServiceImpl.saveVisitInfo` 里改造 UV 统计：

```java
String todayKey = globalKey + "_" + day;
String globalUvKey = uvKey(globalKey);
String todayUvKey = uvKey(todayKey);
String todayPathUvKey = pathUvKey(todayKey, path);

Long globalNewUv = RedisClient.pfAdd(globalUvKey, visitIp);
Long todayNewUv = RedisClient.pfAdd(todayUvKey, visitIp);
Long todayPathNewUv = RedisClient.pfAdd(todayPathUvKey, visitIp);
```

`PFADD` 返回 1 时，说明 HyperLogLog 内部基数发生变化，可以认为是一个新的 UV：

```java
if (isNewHllItem(globalNewUv)) {
    pipelineAction.add(globalKey, "uv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
}
if (isNewHllItem(todayNewUv)) {
    pipelineAction.add(todayKey, "uv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
}
if (isNewHllItem(todayPathNewUv)) {
    pipelineAction.add(todayKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
    pipelineAction.add(globalKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
}
```

这里有一个取舍：全量 path UV 采用“每日 path UV 累加”的方式，不再为每个 path 保存永久的 IP 集合。

这会让全量 path UV 从严格精确变成近似统计，但换来的收益很明显：不会再为每个 IP、每个 path 维护大量 Redis key 和 field。

## 06、日统计加 30 天 TTL

生产环境统计数据并不是越久越好。太久的明细统计只会增加 Redis 压力。

这次我们把日级访问统计 TTL 设置为 30 天：

```java
private static final long VISIT_STAT_TTL_SECONDS = 30 * DateUtil.ONE_DAY_SECONDS;
```

然后给当天的 hash 和 HLL key 都加过期时间：

```java
pipelineAction.add(todayKey, (connection, key) -> connection.expire(key, VISIT_STAT_TTL_SECONDS));
pipelineAction.add(todayUvKey, (connection, key) -> connection.expire(key, VISIT_STAT_TTL_SECONDS));
pipelineAction.add(todayPathUvKey, (connection, key) -> connection.expire(key, VISIT_STAT_TTL_SECONDS));
```

路径也做了简单归一化，去掉 query string，并限制长度：

```java
private String normalizeVisitPath(String path) {
    if (StringUtils.isBlank(path)) {
        return null;
    }
    int queryIndex = path.indexOf('?');
    if (queryIndex >= 0) {
        path = path.substring(0, queryIndex);
    }
    path = StringUtils.trimToNull(path);
    if (path == null) {
        return null;
    }
    return path.length() > VISIT_PATH_MAX_LENGTH ? path.substring(0, VISIT_PATH_MAX_LENGTH) : path;
}
```

这样可以避免同一个页面因为 query 参数不同，被统计成大量不同路径。

## 07、Redis timeout 不能再用 60 秒

日志里看到的 Redis 超时时间是 1 分钟，这对请求链路来说太长了。

生产配置里加上 Redis timeout：

```yaml
spring:
  redis:
    host: ${PAICODING_REDIS_HOST:localhost}
    port: ${PAICODING_REDIS_PORT:6379}
    password: ${PAICODING_REDIS_PASSWORD:}
    timeout: ${PAICODING_REDIS_TIMEOUT:2s}
```

这里默认 2 秒。

统计类 Redis 写入失败并不可怕，可怕的是它挂住 60 秒，把请求线程耗光。

## 08、线上 Redis 老数据怎么清

注意，上面的代码只能控制新增数据，已经存在的老 key 不会自动消失。

所以线上需要做一次清理。

先看 Redis 内存和 key 分布：

```bash
redis-cli -h 127.0.0.1 -p 6388 INFO memory | grep -E 'used_memory_human|used_memory_peak_human|maxmemory_human|maxmemory_policy'
redis-cli -h 127.0.0.1 -p 6388 DBSIZE
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info*' | wc -l
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info_*.*' | wc -l
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info_20[0-9][0-9][0-9][0-9][0-9][0-9]' | wc -l
```

删除大量 key 时，建议用 `UNLINK`，不要用 `DEL`。`UNLINK` 会异步释放内存，对 Redis 主线程更友好。

清理按 IP 维度生成的访问统计 key：

```bash
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info_*.*' \
  | xargs -r -n 1000 redis-cli -h 127.0.0.1 -p 6388 UNLINK
```

清理 30 天以前的日统计 key：

```bash
cutoff=$(date -d '30 days ago' +%Y%m%d)

redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info_20[0-9][0-9][0-9][0-9][0-9][0-9]' \
  | awk -v cutoff="$cutoff" '{d=$0; sub(/^pai_visit_info_/, "", d); if (d < cutoff) print $0}' \
  | xargs -r -n 500 redis-cli -h 127.0.0.1 -p 6388 UNLINK
```

清理后再看：

```bash
redis-cli -h 127.0.0.1 -p 6388 INFO memory | grep -E 'used_memory_human|used_memory_rss_human|lazyfree_pending_objects'
redis-cli -h 127.0.0.1 -p 6388 DBSIZE
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info*' | wc -l
```

这次清理后，访问统计 key 从 85 万多个降到了几百个，Redis key 总数从 90 多万降到了 4 万多个。

## 09、还有一个隐藏大户：异常 session meta

访问统计 key 清完后，Redis 内存已经下降了一部分，但仍然偏高。

于是继续跑 `--bigkeys`：

```bash
redis-cli -h 127.0.0.1 -p 6388 --bigkeys
```

结果发现，`pai_session_meta:*` 里有几个异常大的字符串，单个 key 达到 200MB 到 300MB，而正常 session meta 只有几百字节。

这种数据明显不正常，可以先列出来：

```bash
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_session_meta:*' | while read -r k; do
  len=$(redis-cli -h 127.0.0.1 -p 6388 STRLEN "$k" 2>/dev/null || echo 0)
  ttl=$(redis-cli -h 127.0.0.1 -p 6388 TTL "$k" 2>/dev/null || echo -999)
  printf '%s\t%s\t%s\n' "$len" "$ttl" "$k"
done | sort -nr | head -n 20
```

只清理超过 1MB 的异常 session meta：

```bash
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_session_meta:*' | while read -r k; do
  len=$(redis-cli -h 127.0.0.1 -p 6388 STRLEN "$k" 2>/dev/null || echo 0)
  if [ "$len" -gt 1048576 ]; then
    echo "$k"
  fi
done | xargs -r -n 100 redis-cli -h 127.0.0.1 -p 6388 UNLINK
```

清完后，Redis `used_memory` 从 2.55G 降到了 128M。

当然，清 session meta 的副作用也要说清楚：被删除的那几个用户可能需要重新登录。但相比 Redis 被异常大 key 撑爆，这个代价是可以接受的。

## 10、完整上线步骤

代码改完之后，建议按下面的顺序上线。

第一步，先本地编译。

技术派这个项目需要 Java 8，不能直接用本机默认 Java 21：

```bash
JAVA8_HOME=$(/usr/libexec/java_home -v 1.8)
export JAVA_HOME="$JAVA8_HOME"
export PATH="$JAVA8_HOME/bin:$PATH"

/opt/homebrew/Cellar/maven/3.9.9/libexec/bin/mvn -pl paicoding-web -am -DskipTests compile
```

第二步，提交并发布代码。

第三步，生产环境拉最新代码后启动：

```bash
cd /home/www/paicoding
git pull --ff-only
./launch.sh start
```

第四步，启动后验证：

```bash
curl -I https://paicoding.com/
curl http://127.0.0.1:8999/actuator/health
tail -n 200 logs/startup-prod.log
tail -n 200 logs/pai-prod.log
```

第五步，观察 Redis 是否继续产生老的 IP 统计 key：

```bash
redis-cli -h 127.0.0.1 -p 6388 --scan --pattern 'pai_visit_info_*.*' | wc -l
redis-cli -h 127.0.0.1 -p 6388 INFO memory | grep used_memory_human
```

如果代码已经生效，`pai_visit_info_具体IP` 这类 key 就不会继续增长。

## 11、这次优化的收益

这次优化不是单点修复，而是把整条链路都收了一遍。

请求链路上：

- Redis 写统计不再卡住 Tomcat 请求线程。
- 统计任务有独立线程池，队列有上限。
- Redis 异常只打 warn，不影响业务响应。
- 无意义路径不进入 PV/UV 统计。

Redis 数据结构上：

- UV 从按 IP 精确存储改为 HyperLogLog。
- 日统计和 HLL key 加 30 天 TTL。
- path 做归一化，避免 query 参数打爆 field。
- 老的 IP 统计 key 用 `UNLINK` 清理。

线上资源上：

- Redis key 从 90 多万降到 4 万多个。
- Redis 内存从 2.55G 降到 128M。
- 异常大的 session meta 被清理掉。

## 12、简历上可以怎么写

这类实战非常适合写到简历里，因为它体现的不是“会用 Redis”，而是能处理真实生产问题。

可以这样写：

> 负责技术派生产环境 Redis 内存治理与请求链路优化，定位 ReqRecordFilter 中 PV/UV 统计在 Redis 超时时拖垮 Tomcat 请求线程的问题，拆分独立有界线程池并引入降级丢弃策略；将 UV 统计从 IP 明细 key 改造为 Redis HyperLogLog，并为日统计增加 30 天 TTL，最终将 Redis key 数从 90 万降低到 4 万，内存占用从 2.55G 降低到 128M。

面试官如果追问：“为什么用 HyperLogLog？”

可以回答：

> UV 是去重计数，不需要保存每个 IP 明细。HyperLogLog 可以用固定小内存估算基数，适合站点 UV、页面 UV 这类统计场景。这里牺牲了一点精确性，换来 Redis key 数和内存占用的大幅下降。

如果追问：“统计任务丢了怎么办？”

可以回答：

> PV/UV 是非核心链路，统计准确性不能优先于用户请求可用性。高压或 Redis 抖动时宁愿丢一部分统计，也不能让请求线程阻塞。因此使用独立有界线程池和 DiscardPolicy，保证统计任务不会反向拖垮主业务。

如果追问：“为什么不用 DEL？”

可以回答：

> 大量 key 删除时 DEL 会同步释放内存，可能阻塞 Redis 主线程。UNLINK 会先把 key 从 keyspace 摘掉，再异步释放内存，更适合线上批量清理。

## 13、最后总结

这次问题本质上是一个典型的生产系统治理问题。

代码里一个看似不起眼的访问统计，在 Redis 变慢、线程池饱和、key 无 TTL、数据结构不合理这些因素叠加后，就会放大成整站不可用。

所以做性能优化时，不要只盯着某一个点。

要顺着真实链路看：

```text
请求入口
  -> 过滤器
  -> 线程池
  -> Redis 命令
  -> Redis key 设计
  -> TTL 策略
  -> 线上清理
  -> 发版验证
```

这条链路走完，优化才算闭环。
