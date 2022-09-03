-- MySQL dump 10.13  Distrib 8.0.29, for Linux (x86_64)
--
-- Host: localhost    Database: forum
-- ------------------------------------------------------
-- Server version	8.0.29-0ubuntu0.20.04.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
                           `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `user_id` int unsigned NOT NULL COMMENT '用户ID',
                           `article_type` tinyint NOT NULL DEFAULT '1' COMMENT '文章类型：1-博文，2-问答',
                           `title` varchar(120) NOT NULL COMMENT '文章标题',
                           `short_title` varchar(120) NOT NULL COMMENT '短标题',
                           `picture` varchar(128) NOT NULL DEFAULT '' COMMENT '文章头图',
                           `summary` varchar(300) NOT NULL DEFAULT '' COMMENT '文章摘要',
                           `category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '类目ID',
                           `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源：1-转载，2-原创，3-翻译',
                           `source_url` varchar(128) NOT NULL DEFAULT '1' COMMENT '原文链接',
                           `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
                           `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (3,1,1,'Java小技巧：巧用函数方法实现二维数组遍历','巧用函数方法实现二维数组遍历','','对于数组遍历，基本上每个开发者都写过，遍历本身没什么好说的，但是当我们在遍历的过程中，有一些复杂的业务逻辑时，将会发现代码的层级会逐渐加深',1,2,'',1,0,'2022-08-06 11:52:13','2022-08-07 02:11:42'),(4,1,1,'SpringBoot系列之xml传参与返回实战演练','xml传参与返回实战','','asd',1,2,'',1,0,'2022-08-06 11:55:04','2022-08-07 03:53:37');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_detail`
--

DROP TABLE IF EXISTS `article_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_detail` (
                                  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `article_id` int unsigned NOT NULL COMMENT '文章ID',
                                  `version` int unsigned NOT NULL COMMENT '版本号',
                                  `content` text COMMENT '文章内容',
                                  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                                  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `idx_article_version` (`article_id`,`version`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_detail`
--

LOCK TABLES `article_detail` WRITE;
/*!40000 ALTER TABLE `article_detail` DISABLE KEYS */;
INSERT INTO `article_detail` VALUES (3,3,1,'对于数组遍历，基本上每个开发者都写过，遍历本身没什么好说的，但是当我们在遍历的过程中，有一些复杂的业务逻辑时，将会发现代码的层级会逐渐加深\r\n\r\n如一个简单的case，将一个二维数组中的偶数找出来，保存到一个列表中\r\n\r\n二维数组遍历，每个元素判断下是否为偶数，很容易就可以写出来，如\r\n\r\n```java\r\npublic void getEven() {\r\n    int[][] cells = new int[][]{{1, 2, 3, 4}, {11, 12, 13, 14}, {21, 22, 23, 24}};\r\n    List<Integer> ans = new ArrayList<>();\r\n    for (int i = 0; i < cells.length; i ++) {\r\n        for (int j = 0; j < cells[0].length; j++) {\r\n            if ((cells[i][j] & 1) == 0) {\r\n                ans.add(cells[i][j]);\r\n            }\r\n        }\r\n    }\r\n    System.out.println(ans);\r\n}\r\n```\r\n\r\n上面这个实现没啥问题，但是这个代码的深度很容易就有三层了；当上面这个if中如果再有其他的判定条件，那么这个代码层级很容易增加了；二维数组还好，如果是三维数组，一个遍历就是三层；再加点逻辑，四层、五层不也是分分钟的事情么\r\n\r\n那么问题来了，代码层级变多之后会有什么问题呢？\r\n\r\n> 只要代码能跑，又能有什么问题呢？！\r\n\r\n## 1. 函数方法消减代码层级\r\n\r\n由于多维数组的遍历层级天然就很深，那么有办法进行消减么？\r\n\r\n要解决这个问题，关键是要抓住重点，遍历的重点是什么？获取每个元素的坐标！那么我们可以怎么办？\r\n\r\n> 定义一个函数方法，输入的就是函数坐标，在这个函数体中执行我们的遍历逻辑即可\r\n\r\n基于上面这个思路，相信我们可以很容易写一个二维的数组遍历通用方法\r\n\r\n```java\r\npublic static void scan(int maxX, int maxY, BiConsumer<Integer, Integer> consumer) {\r\n    for (int i = 0; i < maxX; i++) {\r\n        for (int j = 0; j < maxY; j++) {\r\n            consumer.accept(i, j);\r\n        }\r\n    }\r\n}\r\n```\r\n\r\n主要上面的实现，函数方法直接使用了JDK默认提供的BiConsumer，两个传参，都是int 数组下表；无返回值\r\n\r\n那么上面这个怎么用呢？\r\n\r\n同样是上面的例子，改一下之后，如\r\n\r\n```java\r\npublic void getEven() {\r\n    int[][] cells = new int[][]{{1, 2, 3, 4}, {11, 12, 13, 14}, {21, 22, 23, 24}};\r\n    List<Integer> ans = new ArrayList<>();\r\n    scan(cells.length, cells[0].length, (i, j) -> {\r\n        if ((cells[i][j] & 1) == 0) {\r\n            ans.add(cells[i][j]);\r\n        }\r\n    });\r\n    System.out.println(ans);\r\n}\r\n```\r\n\r\n相比于前面的，貌似也就少了一层而已，好像也没什么了不起的\r\n\r\n但是，当数组变为三维、四维、无维时，这个改动的写法层级都不会变哦\r\n\r\n## 2. 遍历中return支持\r\n\r\n前面的实现对于正常的遍历没啥问题；但是当我们在遍历过程中，遇到某个条件直接返回，能支持么？\r\n\r\n如一个遍历二维数组，我们希望判断其中是否有偶数，那么可以怎么整？\r\n\r\n仔细琢磨一下我们的scan方法，希望可以支持return，主要的问题点就是这个函数方法执行之后，我该怎么知道是继续循环还是直接return呢?\r\n\r\n很容易想到的就是执行逻辑中，添加一个额外的返回值，用于标记是否中断循环直接返回\r\n\r\n基于此思路，我们可以实现一个简单的demo版本\r\n\r\n定义一个函数方法，接受循环的下标 + 返回值\r\n\r\n```java\r\n@FunctionalInterface\r\npublic interface ScanProcess<T> {\r\n    ImmutablePair<Boolean, T> accept(int i, int j);\r\n}\r\n```\r\n\r\n循环通用方法就可以相应的改成\r\n\r\n```java\r\npublic static <T> T scanReturn(int x, int y, ScanProcess<T> func) {\r\n    for (int i = 0; i < x; i++) {\r\n        for (int j = 0; j < y; j++) {\r\n            ImmutablePair<Boolean, T> ans = func.accept(i, j);\r\n            if (ans != null && ans.left) {\r\n                return ans.right;\r\n            }\r\n        }\r\n    }\r\n    return null;\r\n}\r\n```\r\n\r\n基于上面这种思路，我们的实际使用姿势如下\r\n\r\n```java\r\n@Test\r\npublic void getEven() {\r\n    int[][] cells = new int[][]{{1, 2, 3, 4}, {11, 12, 13, 14}, {21, 22, 23, 24}};\r\n    List<Integer> ans = new ArrayList<>();\r\n    scanReturn(cells.length, cells[0].length, (i, j) -> {\r\n        if ((cells[i][j] & 1) == 0) {\r\n            return ImmutablePair.of(true, i + \"_\" + j);\r\n        }\r\n        return ImmutablePair.of(false, null);\r\n    });\r\n    System.out.println(ans);\r\n}\r\n```\r\n\r\n上面这个实现可满足我们的需求，唯一有个别扭的地方就是返回，总有点不太优雅；那么除了这种方式之外，还有其他的方式么？\r\n\r\n既然考虑了返回值，那么再考虑一下传参呢？通过一个定义的参数来装在是否中断以及返回结果，是否可行呢？\r\n\r\n\r\n基于这个思路，我们可以先定义一个参数包装类\r\n\r\n```java\r\npublic static class Ans<T> {\r\n    private T ans;\r\n    private boolean tag = false;\r\n\r\n    public Ans<T> setAns(T ans) {\r\n        tag = true;\r\n        this.ans = ans;\r\n        return this;\r\n    }\r\n\r\n    public T getAns() {\r\n        return ans;\r\n    }\r\n}\r\n\r\npublic interface ScanFunc<T> {\r\n    void accept(int i, int j, Ans<T> ans)\r\n}\r\n```\r\n\r\n我们希望通过Ans这个类来记录循环结果，其中tag=true，则表示不用继续循环了，直接返回ans结果吧\r\n\r\n与之对应的方法改造及实例如下\r\n\r\n```java\r\npublic static <T> T scanReturn(int x, int y, ScanFunc<T> func) {\r\n    Ans<T> ans = new Ans<>();\r\n    for (int i = 0; i < x; i++) {\r\n        for (int j = 0; j < y; j++) {\r\n            func.accept(i, j, ans);\r\n            if (ans.tag) {\r\n                return ans.ans;\r\n            }\r\n        }\r\n    }\r\n    return null;\r\n}\r\n    \r\npublic void getEven() {\r\n    int[][] cells = new int[][]{{1, 2, 3, 4}, {11, 12, 13, 14}, {21, 22, 23, 24}};\r\n    String ans = scanReturn(cells.length, cells[0].length, (i, j, a) -> {\r\n        if ((cells[i][j] & 1) == 0) {\r\n            a.setAns(i + \"_\" + j);\r\n        }\r\n    });\r\n    System.out.println(ans);\r\n}\r\n```\r\n\r\n这样看起来就比前面的要好一点了\r\n\r\n实际跑一下，看下输出是否和我们预期的一致；\r\n\r\n![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/546a699ae4334df4b6525332da4e5770~tplv-k3u1fbpfcp-watermark.image?)\r\n\r\n## 3.小结\r\n\r\n到此一个小的技巧就分享完毕了，各位感兴趣的小伙伴可以关注我的公众号“一灰灰blog”\r\n\r\n最近正在整理的 * [分布式设计模式综述 | 一灰灰Learning](https://hhui.top/%E5%88%86%E5%B8%83%E5%BC%8F/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F/01.%E5%88%86%E5%B8%83%E5%BC%8F%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E7%BB%BC%E8%BF%B0/) 欢迎各位大佬点评\r\n\r\n* [万字总结：分布式系统的38个知识点 - 掘金](https://juejin.cn/post/7125383856651239432)\r\n* [万字详解：MySql,Redis,Mq,ES的高可用方案解析 - 掘金](https://juejin.cn/post/7126864114806177822)\r\n\r\n\r\n\r\n\r\n',0,'2022-08-06 11:52:13','2022-08-07 02:13:51'),(4,4,8,'![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7224ef4796684b6b8ba597716f8cc172~tplv-k3u1fbpfcp-zoom-1.image)\n\n> SpringBoot系列之xml传参与返回实战演练\n\n最近在准备使用微信公众号来做个人站点的登录，发现微信的回调协议居然是xml格式的，之前使用json传输的较多，结果发现换成xml之后，好像并没有想象中的那么顺利，比如回传的数据始终拿不到，返回的数据对方不认等\n\n接下来我们来实际看一下，一个传参和返回都是xml的SpringBoot应用，究竟是怎样的\n\n<!-- more -->\n\n## I. 项目搭建\n\n\n本文创建的实例工程采用`SpringBoot 2.2.1.RELEASE` + `maven 3.5.3` + `idea`进行开发\n\n### 1. pom依赖\n\n具体的SpringBoot项目工程创建就不赘述了，对于pom文件中，需要重点关注下面两个依赖类\n\n```xml\n<dependencies>\n    <dependency>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-web</artifactId>\n    </dependency>\n    <dependency>\n        <groupId>com.fasterxml.jackson.dataformat</groupId>\n        <artifactId>jackson-dataformat-xml</artifactId>\n    </dependency>\n</dependencies>\n```\n\n### 2. 接口调研\n\n我们直接使用微信公众号的回调传参、返回来搭建项目服务，微信开发平台文档如: [基础消息能力](https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_standard_messages.html)\n\n\n其定义的推送参数如下\n\n```xml\n<xml>\n  <ToUserName><![CDATA[toUser]]></ToUserName>\n  <FromUserName><![CDATA[fromUser]]></FromUserName>\n  <CreateTime>1348831860</CreateTime>\n  <MsgType><![CDATA[text]]></MsgType>\n  <Content><![CDATA[this is a test]]></Content>\n  <MsgId>1234567890123456</MsgId>\n  <MsgDataId>xxxx</MsgDataId>\n  <Idx>xxxx</Idx>\n</xml>\n```\n\n要求返回的结果如下\n\n```xml\n<xml>\n  <ToUserName><![CDATA[toUser]]></ToUserName>\n  <FromUserName><![CDATA[fromUser]]></FromUserName>\n  <CreateTime>12345678</CreateTime>\n  <MsgType><![CDATA[text]]></MsgType>\n  <Content><![CDATA[你好]]></Content>\n</xml>\n```\n\n上面的结构看起来还好，但是需要注意的是外层标签为`xml`，内层标签都是大写开头的；而微信识别返回是大小写敏感的\n\n## II. 实战\n\n项目工程搭建完毕之后，首先定义一个接口，用于接收xml传参，并返回xml对象；\n\n那么核心的问题就是如何定义传参为xml，返回也是xml呢？\n\n> 没错：就是请求头 + 返回头\n\n### 1.REST接口\n\n```java\n@RestController\npublic class XmlRest {\n\n    /**\n     * curl -X POST \'http://localhost:8080/xml/callback\' -H \'content-type:application/xml\' -d \'<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[123]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[测试]]></Content><MsgId>11111111</MsgId></xml>\' -i\n     *\n     * @param msg\n     * @param request\n     * @return\n     */\n    @PostMapping(path = \"xml/callback\",\n            consumes = {\"application/xml\", \"text/xml\"},\n            produces = \"application/xml;charset=utf-8\")\n    public WxTxtMsgResVo callBack(@RequestBody WxTxtMsgReqVo msg, HttpServletRequest request) {\n        WxTxtMsgResVo res = new WxTxtMsgResVo();\n        res.setFromUserName(msg.getToUserName());\n        res.setToUserName(msg.getFromUserName());\n        res.setCreateTime(System.currentTimeMillis() / 1000);\n        res.setMsgType(\"text\");\n        res.setContent(\"hello: \" + LocalDateTime.now());\n        return res;\n    }\n}\n```\n\n注意上面的接口定义，POST传参，请求头和返回头都是 `application/xml`\n\n### 2.请求参数与返回结果对象定义\n\n上面的接口中定义了`WxTxtMsgReqVo`来接收传参，定义`WxTxtMsgResVo`来返回结果，由于我们采用的是xml协议传输数据，这里需要借助`JacksonXmlRootElement`和`JacksonXmlProperty`注解；它们的实际作用与json传输时，使用`JsonProperty`来指定json key的作用相仿\n\n\n下面是具体的实体定义\n\n```java\n@Data\n@JacksonXmlRootElement(localName = \"xml\")\npublic class WxTxtMsgReqVo {\n    @JacksonXmlProperty(localName = \"ToUserName\")\n    private String toUserName;\n    @JacksonXmlProperty(localName = \"FromUserName\")\n    private String fromUserName;\n    @JacksonXmlProperty(localName = \"CreateTime\")\n    private Long createTime;\n    @JacksonXmlProperty(localName = \"MsgType\")\n    private String msgType;\n    @JacksonXmlProperty(localName = \"Content\")\n    private String content;\n    @JacksonXmlProperty(localName = \"MsgId\")\n    private String msgId;\n    @JacksonXmlProperty(localName = \"MsgDataId\")\n    private String msgDataId;\n    @JacksonXmlProperty(localName = \"Idx\")\n    private String idx;\n}\n\n@Data\n@JacksonXmlRootElement(localName = \"xml\")\npublic class WxTxtMsgResVo {\n\n    @JacksonXmlProperty(localName = \"ToUserName\")\n    private String toUserName;\n    @JacksonXmlProperty(localName = \"FromUserName\")\n    private String fromUserName;\n    @JacksonXmlProperty(localName = \"CreateTime\")\n    private Long createTime;\n    @JacksonXmlProperty(localName = \"MsgType\")\n    private String msgType;\n    @JacksonXmlProperty(localName = \"Content\")\n    private String content;\n}\n```\n\n重点说明：\n\n\n- JacksonXmlRootElement 注解，定义返回的xml文档中最外层的标签名\n- JacksonXmlProperty 注解，定义每个属性值对应的标签名\n- 无需额外添加`<![CDATA[...]]>`，这个会自动添加，防转义\n\n\n\n### 3.测试\n\n然后访问测试一下，直接通过curl来发送xml请求\n\n```bash\ncurl -X POST \'http://localhost:8080/xml/callback\' -H \'content-type:application/xml\' -d \'<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[123]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[测试]]></Content><MsgId>11111111</MsgId></xml>\' -i\n```\n\n实际响应如下\n\n```bash\nHTTP/1.1 200\nContent-Type: application/xml;charset=utf-8\nTransfer-Encoding: chunked\nDate: Tue, 05 Jul 2022 01:20:32 GMT\n\n<xml><ToUserName>123</ToUserName><FromUserName>一灰灰blog</FromUserName><CreateTime>1656984032</CreateTime><MsgType>text</MsgType><Content>hello: 2022-07-05T09:20:32.155</Content></xml>%   \n```\n\n\n### 4.问题记录\n\n#### 4.1 HttpMediaTypeNotSupportedException异常\n\n通过前面的方式搭建项目之后，在实际测试时，可能会遇到下面的异常情况`Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content type \'application/xml;charset=UTF-8\' not supported]`\n\n\n当出现这个问题时，表明是没有对应的Convert来处理`application/xml`格式的请求头\n\n对应的解决方案则是主动注册上\n\n```java\n@Configuration\npublic class XmlWebConfig implements WebMvcConfigurer {\n    @Override\n    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {\n        converters.add(new MappingJackson2XmlHttpMessageConverter());\n    }\n}\n```\n\n#### 4.2 其他json接口也返回xml数据\n\n另外一个场景则是配置了前面的xml之后，导致项目中其他正常的json传参、返回的接口也开始返回xml格式的数据了，此时解决方案如下\n\n```java\n@Configuration\npublic class XmlWebConfig implements WebMvcConfigurer {\n    /**\n     * 配置这个，默认返回的是json格式数据；若指定了xml返回头，则返回xml格式数据\n     *\n     * @param configurer\n     */\n    @Override\n    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {\n        configurer.defaultContentType(MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML);\n    }\n}\n```\n\n#### 4.3 微信实际回调参数一直拿不到\n\n这个问题是在实际测试回调的时候遇到的，接口定义之后始终拿不到结果，主要原因就在于最开始没有在定义的实体类上添加 `@JacksonXmlProperty`\n\n当我们没有指定这个注解时，接收的xml标签名与实体对象的fieldName完全相同，既区分大小写\n\n所以为了解决这个问题，就是老老实实如上面的写法，在每个成员上添加注解，如下\n\n```java\n@JacksonXmlProperty(localName = \"ToUserName\")\nprivate String toUserName;\n@JacksonXmlProperty(localName = \"FromUserName\")\nprivate String fromUserName;\n```\n\n### 5.小结\n\n本文主要介绍的是SpringBoot如何支持xml格式的传参与返回，大体上使用姿势与json格式并没有什么区别，但是在实际使用的时候需要注意上面提出的几个问题，避免采坑\n\n关键知识点提炼如下：\n\n- Post接口上，指定请求头和返回头：\n  - `consumes = {\"application/xml\", \"text/xml\"},`\n  - `produces = \"application/xml;charset=utf-8\"`\n- 实体对象，通过`JacksonXmlRootElement`和`JacksonXmlProperty`来重命名返回的标签名\n- 注册`MappingJackson2XmlHttpMessageConverter`解决HttpMediaTypeNotSupportedException异常\n- 指定`ContentNegotiationConfigurer.defaultContentType` 避免出现所有接口返回xml文档\n\n\n## III. 其他\n\n### 0. 项目与源码\n\n- 工程：[https://github.com/liuyueyi/spring-boot-demo](https://github.com/liuyueyi/spring-boot-demo)\n- 源码：[https://github.com/liuyueyi/spring-boot-demo/tree/master/spring-boot/204-web-xml](https://github.com/liuyueyi/spring-boot-demo/tree/master/spring-boot/204-web-xml)\n\n\n',0,'2022-08-06 11:55:04','2022-08-07 03:53:37');
/*!40000 ALTER TABLE `article_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_tag`
--

DROP TABLE IF EXISTS `article_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_tag` (
                               `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `article_id` int unsigned NOT NULL DEFAULT '0' COMMENT '文章ID',
                               `tag_id` int NOT NULL DEFAULT '0' COMMENT '标签',
                               `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签映射';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_tag`
--

LOCK TABLES `article_tag` WRITE;
/*!40000 ALTER TABLE `article_tag` DISABLE KEYS */;
INSERT INTO `article_tag` VALUES (1,3,1,0,'2022-08-06 11:52:13','2022-08-06 11:52:13'),(9,4,1,0,'2022-08-07 03:52:14','2022-08-07 03:52:14');
/*!40000 ALTER TABLE `article_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
                            `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `category_name` varchar(64) NOT NULL COMMENT '类目名称',
                            `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
                            `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                            `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='类目管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'后端',1,0,'2022-07-20 06:58:20','2022-07-20 06:58:20'),(2,'前端',1,0,'2022-07-28 03:35:56','2022-07-28 03:35:56'),(3,'大数据',1,0,'2022-07-28 03:36:03','2022-07-28 03:36:03'),(4,'Android',1,0,'2022-07-28 03:36:19','2022-07-28 03:36:19'),(5,'IOS',1,0,'2022-07-28 03:36:24','2022-07-28 03:37:26'),(6,'人工智能',1,0,'2022-07-28 03:36:30','2022-07-28 03:37:26'),(7,'开发工具',1,0,'2022-07-28 03:36:33','2022-07-28 03:37:27'),(8,'代码人生',1,0,'2022-07-28 03:36:37','2022-07-28 03:37:27'),(9,'阅读',1,0,'2022-07-28 03:36:40','2022-07-28 03:37:27');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
                           `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `article_id` int unsigned NOT NULL COMMENT '文章ID',
                           `user_id` int unsigned NOT NULL COMMENT '用户ID',
                           `content` varchar(300) NOT NULL DEFAULT '' COMMENT '评论内容',
                           `parent_comment_id` int unsigned NOT NULL COMMENT '父评论ID',
                           `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_article_id` (`article_id`),
                           KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
                       `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                       `tag_name` varchar(120) NOT NULL COMMENT '标签名称',
                       `tag_type` tinyint NOT NULL DEFAULT '1' COMMENT '标签类型：1-系统标签，2-自定义标签',
                       `category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '类目ID',
                       `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
                       `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                       `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                       PRIMARY KEY (`id`),
                       KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (1,'Java',1,1,0,0,'2022-07-20 07:03:56','2022-07-20 07:03:56'),(2,'Go',1,1,0,0,'2022-07-28 03:38:30','2022-07-28 03:38:30'),(3,'Python',1,1,0,0,'2022-07-28 03:38:36','2022-07-28 03:38:36'),(4,'Spring Boot',1,1,0,0,'2022-07-28 03:38:48','2022-07-28 03:38:48'),(5,'Spring',1,1,0,0,'2022-07-28 03:39:01','2022-07-28 03:39:01'),(6,'Redis',1,1,0,0,'2022-07-28 03:39:05','2022-07-28 03:39:05'),(7,'Linux',1,1,0,0,'2022-07-28 03:39:10','2022-07-28 03:39:10'),(8,'JavaScript',1,2,0,0,'2022-07-28 03:39:37','2022-07-28 03:39:37'),(9,'React.js',1,2,0,0,'2022-07-28 03:39:41','2022-07-28 03:39:41'),(10,'Vue.js',1,2,0,0,'2022-07-28 03:41:37','2022-07-28 03:41:37'),(11,'Angular.js',1,2,0,0,'2022-07-28 03:41:51','2022-07-28 03:41:51'),(12,'小程序',1,2,0,0,'2022-07-28 03:42:44','2022-07-28 03:42:44');
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `third_account_id` varchar(128) NOT NULL DEFAULT '' COMMENT '第三方用户ID',
                        `login_type` tinyint NOT NULL DEFAULT '0' COMMENT '登录方式: 0-微信登录，1-账号密码登录',
                        `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                        `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                        PRIMARY KEY (`id`),
                        KEY `key_third_account_id` (`third_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'a7cb7228-0f85-4dd5-845c-7c5df3746e92',0,0,'2022-08-06 12:45:22','2022-08-06 12:45:22');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_foot`
--

DROP TABLE IF EXISTS `user_foot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_foot` (
                             `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` int unsigned NOT NULL COMMENT '用户ID',
                             `doucument_id` int unsigned NOT NULL COMMENT '文档ID（文章/评论）',
                             `doucument_type` tinyint NOT NULL DEFAULT '1' COMMENT '文档类型：1-文章，2-评论',
                             `doucument_user_id` int unsigned NOT NULL COMMENT '发布该文档的用户ID',
                             `comment_id` int unsigned NOT NULL DEFAULT '0' COMMENT '当前发起评论的ID',
                             `collection_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '收藏状态: 0-未收藏，1-已收藏，2-取消收藏',
                             `read_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未读，1-已读',
                             `comment_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '评论状态: 0-未评论，1-已评论，2-删除评论',
                             `praise_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '点赞状态: 0-未点赞，1-已点赞，2-取消点赞',
                             `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `idx_user_doucument` (`user_id`,`doucument_id`,`doucument_type`,`comment_id`),
                             KEY `idx_doucument_id` (`doucument_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户足迹表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_foot`
--

LOCK TABLES `user_foot` WRITE;
/*!40000 ALTER TABLE `user_foot` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_foot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
                             `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` int unsigned NOT NULL COMMENT '用户ID',
                             `user_name` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
                             `photo` varchar(128) NOT NULL DEFAULT '' COMMENT '用户图像',
                             `position` varchar(50) NOT NULL DEFAULT '' COMMENT '职位',
                             `company` varchar(50) NOT NULL DEFAULT '' COMMENT '公司',
                             `profile` varchar(225) NOT NULL DEFAULT '' COMMENT '个人简介',
                             `extend` varchar(1024) NOT NULL DEFAULT '' COMMENT '扩展字段',
                             `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
                             `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                             PRIMARY KEY (`id`),
                             KEY `key_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户个人信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_info`
--

LOCK TABLES `user_info` WRITE;
/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` VALUES (1,1,'一灰灰','https://spring.hhui.top/spring-blog/css/images/avatar.jpg','java','xm','码农','',0,'2022-08-06 12:45:22','2022-08-06 12:45:22');
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_relation`
--

DROP TABLE IF EXISTS `user_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_relation` (
                                 `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` int unsigned NOT NULL COMMENT '用户ID',
                                 `follow_user_id` int unsigned NOT NULL COMMENT '关注用户ID',
                                 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_follow` (`user_id`,`follow_user_id`),
                                 KEY `key_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_relation`
--

LOCK TABLES `user_relation` WRITE;
/*!40000 ALTER TABLE `user_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_relation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-08-07 11:59:37
