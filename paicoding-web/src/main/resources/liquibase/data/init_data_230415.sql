-- 设置文章置顶
update article set topping_stat = 1 where id in (100, 101, 102);


-- 新增几个官方相关文章

INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(14, 1, 1, '二哥的 Java 进阶之路.pdf 开放下载了，GitHub 星标 7700+，太赞了！', '', '', '小册名字：二哥的Java进阶之路小册作者：沉默王二小册品质：能在GitHub取得7600star自认为品质是有目共睹的，尤其是国内还有不少小伙伴在访问GitHub的时候很不顺利。小册风格：通俗易懂、风趣幽默、深度解析，新手可以拿来入门，老手可以拿来进阶，重要的知识，比如说面试高频的内容会从应用到源码挖个底朝天，还会穿插介绍一些计算机底层知识，力求讲个明白）小册简介：这是一份通俗易懂、风趣幽默的Java学习指南，内容涵', 1, 2, '', 1, 0, 0, 1, 0);
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(15, 1, 1, '官宣：技术派上线了！⭐️一款好用又强大的开源社区，学编程，就上技术派?', '', 'https://cdn.tobebetterjavaer.com/paicoding/e95f60537f490bb96560aae25e2d53f0.jpg', '一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等，代码完全开源，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目? ', 1, 2, '', 1, 0, 0, 1, 0);
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(16, 1, 1, '对标大厂的技术派详细方案设计，务必要看', '', 'https://cdn.tobebetterjavaer.com/paicoding/7f492cb729d944097d6676b7f19d3ff9.png', '这个项目诞生的背景和企业内生的需求不太一样，主要是某一天二哥说，“我们一起搞事吧”，楼仔问，“搞什么”，然后这个项目的需求就来了言归正传，我们主要的目的是希望打造一个切实可用的项目，依托于这个项目，将java从业者所用到的技术栈真实的展现出来，对于经验不是那么足的小伙伴，可以在一个真实的系统上，理解到自己学习的知识点是如何落地的，同时也能真实的了解一个项目是从0到1实现的全过程系统模块介绍系统架构基于社区系统的分层特点，将整个系统架构划分为展示层，应用层，服务层，如下图展示层其中展示层主要为用', 1, 2, '', 1, 0, 0, 1, 0);
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(17, 1, 1, '技术派的知识星球，开通啦！附 120 篇技术派的详细教程！', '', '', '大家好呀，我是楼仔。上周推出了我们的开源项目技术派，大家好评如潮，很多同学都想学习这个项目，为了更好带大家一起飞，我们今天正式推出技术派的知识星球。什么是知识星球呢？你可以理解为高品质社群，方便大家跟着我们一起学习。01星球介绍先来介绍下星球的三位联合创始人：楼仔：8年一线大厂后端经验（百度/小米/美团），技术派团队负责人，擅长高并发、架构、源码，有很强的项目/团队管理、职业规划能力。沉默王二：GitHub星标6400k开源知识库《Java程序员进阶之路》作者，CSDN两届博客之星，掘金/知乎Java领域优', 1, 2, '', 1, 0, 0, 1, 0);



INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(14, 2, '![](https://cdn.tobebetterjavaer.com/stutymore/readme-fengmian.png)

以上就是小册的封面了，自我感觉还不错哈，简洁大方，但包含的信息又足够的丰富：

- 小册名字：二哥的 Java 进阶之路
- 小册作者：沉默王二
- 小册品质：能在 GitHub 取得 7600+ star 自认为品质是有目共睹的，尤其是国内还有不少小伙伴在访问 GitHub 的时候很不顺利。
- 小册风格：通俗易懂、风趣幽默、深度解析，新手可以拿来入门，老手可以拿来进阶，重要的知识，比如说面试高频的内容会从应用到源码挖个底朝天，还会穿插介绍一些计算机底层知识，力求讲个明白）
- 小册简介：这是一份通俗易懂、风趣幽默的Java学习指南，内容涵盖Java基础、Java并发编程、Java虚拟机、Java面试等核心知识点。学Java，就认准二哥的Java进阶之路?
- 小册品位：底部用了梵高 1889 年的《星空》（the starry night），绝美的漩涡星空，耀眼的月亮，宁静的村庄，还有一颗燃烧着火焰的巨大柏树，我想小册的艺术品位也是恰到好处的。
- 小册角色：为了增加小册的趣味性，我特意为此追加了两个虚拟角色，一个二哥，一个三妹，二哥负责教，三妹负责学。这样大家在学习 Java 的时候代入感也会更强烈一些，希望这样的设定能博得大家的欢心。

## 小册包含哪些内容？

三妹出场：“二哥，帮读者朋友们问一下哈，为什么会有《二哥的Java进阶之路》这份小册呢？”

*二哥巴拉巴拉 ing...*

小册的内容主要来源于我的开源知识库《[Java程序员进阶之路](https://github.com/itwanger/toBeBetterJavaer)》，目前在 GitHub 上收获 7600+ star，深受读者喜爱。小册之所以叫《二哥的Java进阶之路》，是因为这样更方便小册的读者知道这份小册的作者是谁，IP 感更强烈一些。

如果有读者是第一次阅读这份小册，肯定又会问，“二哥是哪个鸟人？”

噢噢噢噢，正是鄙人了，一个英俊潇洒的男人（见下图），你可以通过我的微信公众号“**沉默王二**”了解更多关于我的信息，总之，就是一个非常喜欢王小波的程序员了，写得一手风趣幽默的技术文章，所以被读者“尊称”为二哥就对了。现实中，三妹也是真实存在的哦。

![](https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/overview/readme-ece0be3e-d176-447c-bff9-59d9f02c7a65.jpg)

《**二哥的 Java 进阶之路**》是我自学 Java 以来所有原创文章和学习资料的大聚合。[在线网站](https://tobebetterjavaer.com/)和 [GitHub 仓库](https://github.com/itwanger/toBeBetterJavaer)里的内容包括 Java 基础、Java 并发编程、Java 虚拟机、Java 企业级开发（包括开发/构建/测试、JavaWeb、SSM、Spring Boot、Linux、Nginx、Docker、k8s、微服务&分布式、消息队列等）、Java 面试等核心内容。这也是小册最终版会覆盖的内容。

小册旨在为学习 Java 的小伙伴提供一系列：

 - **优质的原创 Java 教程**
 - **全面清晰的 Java 学习路线**
 - **免费但靠谱的 Java 学习资料**
 - **精选的 Java 岗求职面试指南**
 - **Java 企业级开发所需的必备技术**

接下来，送你 4 个“掏心掏肺”的阅读建议：

- 如果你是零基础的小白，可以按照小册的顺序一路读下去，小册的内容安排都是经过我精心安排的；
- 否则，请按照目录按需阅读，该跳过的跳过，该放慢节奏的放慢节奏。
- 小册中会有一个虚拟人物，三妹，当然她的原型也是真实存在的，目的就是通过我们之间的对话，来增强文章的趣味性，以便你能更轻松地获取知识。
- 最重要的一点，“光看不练假把戏”，请在阅读的过程中把该敲的代码敲了，把该记的笔记记了，语雀、思维导图、GitHub 仓库都可以，养成好的学习习惯。

这里展示一下暗黑版的 PDF 视图，大家先感受一下，手绘图都画得非常用心。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411224013.png)

这是 epub 版本的阅读效果，感觉左右翻动的效果好舒服，一次可以看两页，真的就像在读纸质版书籍一样，体验非常棒。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230412002314.png)

如果你喜欢在线阅读，请戳下面这个网址：

> [https://tobebetterjavaer.com](https://tobebetterjavaer.com)

首页见下图，同样简洁、清新、方便沉浸式阅读：

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411102619.png)

你也可以到技术派的[教程栏（戳这里）](https://paicoding.com/column)里阅读，目前正在连载更新中。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230410215012.png)

>技术派是一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，采用主流的互联网技术架构、全新的UI设计、支持一键源码部署，拥有完整的文章&教程发布/搜索/评论/统计流程等，[代码完全开源（可戳）](https://github.com/itwanger/paicoding)，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目? 。

如果你在阅读过程中感觉这份小册写的还不错，甚至有亿点点收获，**请肆无忌惮地把这份小册分享给你的同事、同学、舍友、朋友，让他们也进步亿点点，赠人玫瑰手有余香嘛**。

如果这份小册有幸被更多人看得到，我的虚荣心也会得到恰当的满足，嘿嘿?

## 如何获取最新版？

小册分为 3 个版本，暗黑版（适合夜服）、亮白版（适合打印）、epub 版，可以说凝聚了二哥十多年来学习 Java 的心血，33 万+，绝对不虚市面上任何一本 Java 实体书！

![](https://cdn.tobebetterjavaer.com/stutymore/readme-wecom-temp-cbe8e183acdd8daa542c94ab7f4a7eec.png)

小册会持续保持**更新**，如果想获得最新版，请在我的微信公众号 **沉默王二** 后台回复 **222** 获取（你懂我的意思吧，我肯定是足够二才有这样的勇气定义这样一个关键字）！

![](https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/gongzhonghao.png)

## 面试指南（配套教程）

《Java 面试指南》是[二哥编程星球的](https://tobebetterjavaer.com/zhishixingqiu/)的一个内部小册，和《Java 进阶之路》内容互补。相比开源的版本来说，《Java 面试指南》添加了下面这些板块和内容：

- 面试准备篇（20+篇），手把手教你如何准备面试。
- 职场修炼篇（10+篇），手摸手教你如何在职场中如鱼得水。
- 技术提升篇（30+篇），手拉手教你如何成为团队不可或缺的技术攻坚小能手。
- 面经分享篇（20+篇），手牵手教你如何在面试中知彼知己，百战不殆。
- 场景设计篇（20+篇），手握手教你如何在面试中脱颖而出。

### 内容概览

#### 面试准备篇

所谓临阵磨枪，不快也光。更何况提前做好充足的准备呢？这 20+篇文章会系统地引导你该如何做准备。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411111002.png)

#### 职场修炼篇

如何平滑度过试用期？如何平滑度过 35 岁程序员危机？如何在繁重的工作中持续成长？如何做副业？等等，都是大家迫切关心的问题，这 10+篇文章会一一为你揭晓答案。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411111200.png)

#### 技术提升篇

编程能力、技术功底，是我们程序员安身立命之本，是我们求职/工作的最核心的武器。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411112059.png)

#### 面经分享篇

知彼知己，方能百战不殆，我们必须得站在学长学姐的肩膀上，才能走得更远更快。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411112435.png)

#### 场景设计题篇

这里收录的都是精华，让天底下没有难背的八股文；场景设计题篇页都是面试中经常考察的大项，可以让你和面试官对线半小时（?）

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411112637.png)

### 星球其他资源

除了《Java 面试指南》外，星球还提供了《编程喵实战项目笔记》、《二哥的 LeetCode 刷题笔记》，以及技术派实战项目配套的 120+篇硬核教程。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411113022.png)

这里重点介绍一下技术派吧，这个项目上线后，一直广受好评，读者朋友们的认可度非常高，项目配套的教程也足够的硬核。

![](/forum/image/20230415040007452_12.png)

这是部分目录（共计 120 篇，大厂篇、基础篇、进阶篇、工程篇，全部落地）。

开篇：

- 技术答疑（⭐️）
- 技术派问题反馈及解决方案（⭐️）
- 踩坑实录之本地缓存Caffeine采坑实录（⭐️）
- 技术派系统架构、功能模块一览（⭐️⭐️⭐️⭐️⭐️）

大厂篇：

- 技术派产品调研，让你了解产品诞生背后的故事（⭐️⭐️）
- 技术派产品设计（⭐️）
- 技术派交互视觉设计（⭐️）
- 技术派整体架构方案设计全过程（⭐️⭐️⭐️）
- 技术方案详细设计（⭐️⭐️⭐️⭐️）
- 技术派项目管理流程（⭐️⭐️）
- 技术派项目管理研发阶段（⭐️⭐️⭐️）

基础篇：

- 技术派中实体对象 DO、DTO、VO 到底代表了什么（⭐️）
- 通过技术派项目讲解 MVC 分层架构的应用（⭐️⭐️）
- 技术派整合本地缓存之Guava（⭐️⭐️⭐️）
- 技术派整合本地缓存之Caffeine（⭐️⭐️⭐️⭐️）
- 技术派整合 Redis（⭐️）
- 技术派中基于 Redis 的缓存示例（⭐️⭐️⭐️）
- 技术派中基于Cacheable注解实现缓存示例（⭐️⭐️）
- 技术派中的事务使用实例（⭐️⭐️⭐️）
- 事务使用的 7 条注意事项（⭐️⭐️⭐️）
- 技术派中的多配置文件说明（⭐️）
- 技术派整合 Logback/lombok 配置日志输出（⭐️）
- 技术派整合邮件服务实现邮件发送（⭐️）
- Web 三大组件之 Filter 在技术派中的应用（⭐️）
- Web 三大组件之 Servlet 在技术派中的应用（⭐️）
- Web 三大组件之 listenter 在技术派中的应用（⭐️）
- 技术派实时在线人数统计-单机版（⭐️）

进阶篇：

- 技术派之扫码登录实现原理（⭐️）
- 技术派身份验证之session与 cookie（⭐️）
- 技术派中基于异常日志的报警通知（⭐️）

扩展篇：

- 技术派的数据库表自动初始化实现方案（⭐️⭐️⭐️⭐️⭐️）
- 技术派中基于 filter 实现请求日志记录（⭐️）

工程篇：

- 技术派项目工程搭建手册（⭐️⭐️⭐️⭐️）
- 技术派本地多机器部署开发教程（⭐️⭐️）
- 技术派服务器部署指导手册（⭐️⭐️）
- 技术派的 MVC 分层架构（⭐️⭐️）
- 技术派 Docker 本机部署开发手册（⭐️⭐️⭐️）
- 技术派多环境配置管理（⭐️）

欣赏一下技术派实战项目的首页吧，绝壁清新、高级、上档次！

![](/forum/image/20230415040007550_25.png)

### 星球限时优惠

一年前，星球的定价是 99 元一年，第一批优惠券的额度是 30 元，等于说 69 元的低价就可以加入，再扣除掉星球手续费，几乎就是纯粹做公益。

随着时间的推移，星球积累的干货/资源越来越多，我花在星球上的时间也越来越多，[星球的知识图谱](https://tobebetterjavaer.com/zhishixingqiu/map.html)里沉淀的问题，你可以戳这个[链接](https://tobebetterjavaer.com/zhishixingqiu/map.html)去感受一下。有学习计划啊、有学生党秋招&春招&offer选择&考研&实习&专升本&培训班的问题啊、有工作党方向选择&转行&求职&职业规划的问题啊，还有大大小小的技术细节，我都竭尽全力去帮助球友，并且得到了球友的认可和尊重。

目前星球已经 2100+ 人了，所以星球也涨价到了 119 元，后续会讲星球的价格调整为 139 元/年，所以想加入的小伙伴一定要趁早。

![](https://cdn.tobebetterjavaer.com/stutymore/readme-20230411113706.png)

你可以添加我的微信（没有⼿机号再申请微信，故使⽤企业微信。不过，请放⼼，这个号的消息也是
我本⼈处理，平时最常看这个微信）领取星球专属优惠券(推荐)，限时 89/年 加⼊(续费半价)！

<img src="https://cdn.tobebetterjavaer.com/tobebetterjavaer/images/zhishixingqiu/readme-c773d5ff-4458-4d92-868b-2d1d95d6a409.png" title="二哥的编程星球" width="300" />


或者你也可以微信扫码或者长按自动识别领取 30 元优惠券，**89/年** 加入！

<img src="https://cdn.tobebetterjavaer.com/stutymore/readme-20230411114734.png" title="二哥的编程星球" width="300" />

对了，**加入星球后记得花 10 分钟时间看一下星球的两个置顶贴，你会发现物超所值**！

成功没有一蹴而就，没有一飞冲天，但只要你能够一步一个脚印，就能取得你心满意足的好结果，请给自己一个机会！

最后，把二哥的座右铭送给你：**没有什么使我停留——除了目的，纵然岸旁有玫瑰、有绿荫、有宁静的港湾，我是不系之舟**。

共勉 ⛽️。

## 如何贡献？

对了，如果你在阅读的过程中遇到一些错误，欢迎到我的开源仓库提交 issue、PR（审核通过后可成为 Contributor），我会第一时间修正，感谢你为后来者做出的贡献。

>- GitHub：[https://github.com/itwanger/toBeBetterJavaer](https://github.com/itwanger/toBeBetterJavaer)
>- 码云：[https://gitee.com/itwanger/toBeBetterJavaer](https://gitee.com/itwanger/toBeBetterJavaer)

## 更新记录

### V1.0-2023年04月11日

第一版《二哥的 Java 进阶之路》正式完结发布！', 0);
INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(16, 1, '## 整体介绍
### 背景
> 这个项目诞生的背景和企业内生的需求不太一样，主要是某一天二哥说，“我们一起搞事吧”， 楼仔问，“搞什么”，然后这个项目的需求就来了

言归正传，我们主要的目的是希望打造一个切实可用的项目，依托于这个项目，将java从业者所用到的技术栈真实的展现出来，对于经验不是那么足的小伙伴，可以在一个真实的系统上，理解到自己学习的知识点是如何落地的，同时也能真实的了解一个项目是从0到1实现的全过程
### 系统模块介绍
#### 系统架构
基于社区系统的分层特点，将整个系统架构划分为展示层，应用层，服务层，如下图
![](https://cdn.tobebetterjavaer.com/paicoding/05acc5c76bb87adbb5eb1a3e4e4f5f5c.png)

#### 展示层
其中展示层主要为用户直接接触的视图层，基于用户角色，分别提供为面向普通用户的前台与面向管理员的后台
**前台web**

- 采用Thymleaf模板引擎进行视图渲染
- 对于不关心前端技术栈的小伙伴相对友好，学习成本低，只用会基本的html,css,js即可

**管理后台**

- 采用成熟的前后端分离技术方案
- 前端基于react成熟框架搭建
#### 应用层
应用层，也可以称为业务层，强业务相关，其中每个划分出来的模块有较明显的业务边界，虽然在上图中区分了前台、后台
但是需要注意的是，后台也是同样有文章、评论、用户等业务功能的，前台与后台可使用应用主要是权限粒度管理的差异性，对于技术派系统而言，我们的应用可分为：

- 文章
- 专栏
- 评论
- 用户
- 收藏
- 订阅
- 运营
- 审核
- 类目标签
- 统计
#### 服务层
我们将一些通用的、可抽离业务属性的功能模块，沉淀到服务层，作为一个一个的基础服务进行设计，比如计数服务、消息服务等，通常他们最大特点就是独立与业务之外，适用性更广，并不局限在特定的业务领域内，可以作为通用的技术方案存在
在技术派的项目设计中，我们拟定以下基础服务

- 用户权限管理 (auth)
- 消息中心 (mq)
- 计数 (redis)
- 搜索服务 (es)
- 推荐 (recommend)
- 监控运维 (prometheus)
#### 平台资源层
这一层可以理解为更基础的下层支撑

- 服务资源：数据库、redis、es、mq
- 硬件资源：容器，ecs服务器
### 术语介绍
技术派整个系统中涉及到的术语并不多，也很容易理解，下面针对几个常用的进行说明

- 用户：特指通过微信公众号扫码注册的用户，可以发布文章、阅读文章等
- 管理员：可以登录后台的特殊用户
- 文章：即博文
- 专栏：由一系列相关的文章组成的一个合集
- 订阅：专指关注用户

### 技术架构
![](https://cdn.tobebetterjavaer.com/paicoding/e0484ed3a9a48b4a3977fccdd7cebcf5.png)
## 系统模块设计
针对前面技术派的业务架构拆分，技术派的实际项目划分，主要是五个模块，相反并没由将上面的每个应用、服务抽离为独立的模块，主要是为了避免过渡设计，粒度划分太细会增加整个项目的理解维护成本

这里设置五个相对独立的模块，则主要是基于边界特别清晰这一思考点进行，后续做微服务演进时，下面每个模块可以作为独立的微服务存在

### 用户模块
在技术派中，整个用户模块从功能角度可以分为

- 注册登录
- 权限管理（是的，权限管理也放在这里了）
- 业务逻辑

#### 注册登录
##### 方案设计
注册登录除了常见的用户名+密码的登录方式之外，现在也有流行的手机号+验证，第三方授权登录；我们最终选择微信公众号登录方式（其最主要的目的，相信大家也知道...）
对于个人公众号，很多权限没有；因此这个登录的具体实现，有两种实现策略

- 点击登录，登录页显示二维码 + 输入框 -> 用户关注公众号，输入 "login" 获取登录验证码 -> 在登录界面输入验证码实现登录
- 点击登录，登录页显示二维码 + 验证码 -> 用户关注公众号，将登录页面上的验证码输入到微信公众号 -> 自动登录

其中第一种策略，类似于手机号/验证码的登录方式，主要是根据系统返回的验证码来主动登录

**优点：**

- 代码实现简单，逻辑清晰

**缺点：**

- 操作流程复杂，用户需要输入两次

对于第二种策略，如果是企业公众号，是可以省略输入验证码这一步骤的，借助动态二维码来直接实现扫码登录；对于我们这种个人公众号，则需要多来一步，通过输入验证码来将微信公众号的用户与需要登录的用户绑定起来
登录工作流程如下：

![](https://cdn.tobebetterjavaer.com/paicoding/5f1dd8c83ce13bfb4f413df27e674246.png)

##### 库表设计
基于公众号的登录方式，看一下用户登录表的设计
```sql
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `third_account_id` varchar(128) NOT NULL DEFAULT '''' COMMENT ''第三方用户ID'',
  `user_name` varchar(64) NOT NULL DEFAULT '''' COMMENT ''用户名'',
  `password` varchar(128) NOT NULL DEFAULT '''' COMMENT ''密码'',
  `login_type` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''登录方式: 0-微信登录，1-账号密码登录'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `key_third_account_id` (`third_account_id`),
  KEY `key_user_name` (`user_name`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT=''用户登录表'';
```

注意上面的表结构设计，我们冗余了 `user_name`, `password` 用户名密码的登录方式，主要是给管理员登录后台使用

用户首次登录之后，会在user表中插入一条数据，主要关注 `third_account_id` 这个字段，它记录的是微信开放平台返回的唯一用户id
#### 权限管理
权限管理会分为两块：用户身份识别 + 鉴权
##### 方案设计
**用户身份识别：**
现在用户的身份识别有非常多的方案，我们现在采用的是最基础、历史最悠久的方案，cookie + session 方式（后续会迭代为分布式session + jwt）
整体流程：

- 用户登录成功，服务器生成sessionId -> userId 映射关系
- 服务器返回sessionId，写到客户端的浏览器cookie
- 后续用户请求，携带cookie
- 服务器从cookie中获取sessionId，然后找到uesrId

![](https://cdn.tobebetterjavaer.com/paicoding/e1ced86bedf29384ec8492519dd4256f.png)

服务内部身份传递：
另外一个需要考虑的点则是用户的身份如何在整个系统内传递？ 对于一期我们采用的单体架构而言，借助ThreadLocal来实现

- 自定义Filter，实现用户身份识别（即上面的流程，从cookie中拿到SessionId，转userId)
- 定义全局上下文ReqInfoContext：将用户信息，写入全局共享的ThreadLocal中
- 在系统内，需要获取当前用户的地方，直接通过访问 ReqInfoContext上下文获取用户信息
- 请求返回前，销毁上下文中当前登录用户信息

**鉴权**
根据用户角色与接口权限要求进行判定，我们设计三种权限点类型

- ADMIN：只有管理员才能访问的接口
- LOGIN：只有登录了才能访问的接口
- ALL：默认，没有权限限制

我们在需要权限判定的接口上，添加上对应的权限要求，然后借助AOP来实现权限判断

- 当接口上有权限点要求时（除ALL之外）
- 首先获取用户信息，如果没有登录，则直接报403
- 对于ADMIN限制的接口，要求查看用户角色，必须为admin

##### 库表设计
我们将用户角色信息写入用户基本信息表中，没有单独抽出一个角色表，然后进行映射，主要是因为这个系统逻辑相对清晰，没有太复杂的角色关系，因此采用了轻量级的设计方案

```sql
-- pai_coding.user_info definition

CREATE TABLE `user_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''用户ID'',
  `user_name` varchar(50) NOT NULL DEFAULT '''' COMMENT ''用户名'',
  `photo` varchar(128) NOT NULL DEFAULT '''' COMMENT ''用户图像'',
  `position` varchar(50) NOT NULL DEFAULT '''' COMMENT ''职位'',
  `company` varchar(50) NOT NULL DEFAULT '''' COMMENT ''公司'',
  `profile` varchar(225) NOT NULL DEFAULT '''' COMMENT ''个人简介'',
  `user_role` int(4) NOT NULL DEFAULT ''0'' COMMENT ''0 普通用户 1 超管'',
  `extend` varchar(1024) NOT NULL DEFAULT '''' COMMENT ''扩展字段'',
  `ip` json NOT NULL COMMENT ''用户的ip信息'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `key_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT=''用户个人信息表'';
```

#### 业务逻辑
在业务模块，主要说两块，一个是用户的轨迹，一个是订阅关注

##### 订阅关注
订阅关注这块业务主要是用户可以相互关注，核心点就在于维护用户与用户之间的订阅关系

业务逻辑上没有太复杂的东西，核心就是需要一张表来记录关注与被关注情况
```sql
-- pai_coding.user_relation definition

CREATE TABLE `user_relation` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''作者用户ID'',
  `follow_user_id` int(10) unsigned NOT NULL COMMENT ''关注userId的用户id，即粉丝userId'',
  `follow_state` tinyint(2) unsigned NOT NULL DEFAULT ''0'' COMMENT ''阅读状态: 0-未关注，1-已关注，2-取消关注'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`,`follow_user_id`),
  KEY `key_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT=''用户关系表'';
```

##### 用户轨迹
在技术派的整体设计中，我们希望记录用户的阅读历史、关注列表、收藏列表、评价的文章列表，对于这种用户行为轨迹的诉求，我们采用设计一张大宽表的策略，其主要目的在于

1. 记录用户的关键动作
2. 便于文章的相关计数

接下来看一下表结构设计
```sql
-- pai_coding.user_foot definition

CREATE TABLE `user_foot` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''用户ID'',
  `document_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''文档ID（文章/评论）'',
  `document_type` tinyint(4) NOT NULL DEFAULT ''1'' COMMENT ''文档类型：1-文章，2-评论'',
  `document_user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''发布该文档的用户ID'',
  `collection_stat` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''收藏状态: 0-未收藏，1-已收藏，2-取消收藏'',
  `read_stat` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''阅读状态: 0-未读，1-已读'',
  `comment_stat` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''评论状态: 0-未评论，1-已评论，2-删除评论'',
  `praise_stat` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''点赞状态: 0-未点赞，1-已点赞，2-取消点赞'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_doucument` (`user_id`,`document_id`,`document_type`),
  KEY `idx_doucument_id` (`document_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT=''用户足迹表'';
```

我们将用户 + 文章设计唯一键，用来记录用户对自己阅读过的文章的行为，因此可以直接通过这个表获取用户的历史轨迹
同时也可以从文章的角度出发，查看被哪些用户点赞、收藏过

#### 小结
用户模块的核心支撑在上面几块，请重点关注上面的示意图与表结构；当然用户的功能点不止于上面几个，比如基础的个人主页、用户信息等也属于用户模块的业务范畴

### 文章模块
我们将文章和专栏都放在一起，同样也将类目管理、标签管理等也都放在这个模块中，实际上若文章模块过于庞大，也是可以按照最开始的划分进行继续拆分的；这里放在一起的主要原因在于他们都是围绕基本的文章这一业务属性来的，可以聚合在一起

#### 文章
文章的核心就在于发布、查看

基本的发布流程：

1. 用户登录，进入发布页面
2. 输入标题、文章
3. 选择分类、标签，封面、简介
4. 提交文章，进入待审核状态，仅用户可看详情
5. 管理员审核通过，所有人可看详情

![](https://cdn.tobebetterjavaer.com/paicoding/8894262819716a1e62c988e752b859d7.png)

#### 文章库表设计
考虑到文章的内容通常较大，在很多的业务场景中，我们实际上是不需要文章内容的，如首页、推荐列表等都只需要文章的标题等信息；此外我们也希望对文章做一个版本管理（比如上线之后，再修改则新生成一个版本）
因此我们对文章设计了两张表
```sql
-- pai_coding.article definition

CREATE TABLE `article` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''用户ID'',
  `article_type` tinyint(4) NOT NULL DEFAULT ''1'' COMMENT ''文章类型：1-博文，2-问答'',
  `title` varchar(120) NOT NULL DEFAULT '''' COMMENT ''文章标题'',
  `short_title` varchar(120) NOT NULL DEFAULT '''' COMMENT ''短标题'',
  `picture` varchar(128) NOT NULL DEFAULT '''' COMMENT ''文章头图'',
  `summary` varchar(300) NOT NULL DEFAULT '''' COMMENT ''文章摘要'',
  `category_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''类目ID'',
  `source` tinyint(4) NOT NULL DEFAULT ''1'' COMMENT ''来源：1-转载，2-原创，3-翻译'',
  `source_url` varchar(128) NOT NULL DEFAULT ''1'' COMMENT ''原文链接'',
  `offical_stat` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''官方状态：0-非官方，1-官方'',
  `topping_stat` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''置顶状态：0-不置顶，1-置顶'',
  `cream_stat` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''加精状态：0-不加精，1-加精'',
  `status` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''状态：0-未发布，1-已发布'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_title` (`title`),
  KEY `idx_short_title` (`short_title`)
) ENGINE=InnoDB AUTO_INCREMENT=173 DEFAULT CHARSET=utf8mb4 COMMENT=''文章表'';


-- pai_coding.article_detail definition

CREATE TABLE `article_detail` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `article_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''文章ID'',
  `version` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''版本号'',
  `content` longtext COMMENT ''文章内容'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_article_version` (`article_id`,`version`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4 COMMENT=''文章详情表'';
```

文章对应的分类，我们要求一个文章只能挂在一个分类下
```sql
-- pai_coding.category definition

CREATE TABLE `category` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `category_name` varchar(64) NOT NULL DEFAULT '''' COMMENT ''类目名称'',
  `status` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''状态：0-未发布，1-已发布'',
  `rank` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''排序'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT=''类目管理表'';
```

文章对应的标签属性，一个文章可以有多个标签
```sql
-- pai_coding.tag definition

CREATE TABLE `tag` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `tag_name` varchar(120) NOT NULL COMMENT ''标签名称'',
  `tag_type` tinyint(4) NOT NULL DEFAULT ''1'' COMMENT ''标签类型：1-系统标签，2-自定义标签'',
  `category_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''类目ID'',
  `status` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''状态：0-未发布，1-已发布'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4 COMMENT=''标签管理表'';

-- pai_coding.article_tag definition

CREATE TABLE `article_tag` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `article_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''文章ID'',
  `tag_id` int(11) NOT NULL DEFAULT ''0'' COMMENT ''标签'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=145 DEFAULT CHARSET=utf8mb4 COMMENT=''文章标签映射'';
```

#### 专栏
专栏主要是一系列文章的合集，基于此最简单的设计方案就是加一个专栏表，然后再加一个专栏与文章的映射表

但是需要注意的是专栏中文章的顺序，支持调整
#### 专栏库表设计
专栏表
```sql
-- pai_coding.column_info definition

CREATE TABLE `column_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''专栏ID'',
  `column_name` varchar(64) NOT NULL DEFAULT '''' COMMENT ''专栏名'',
  `user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''作者id'',
  `introduction` varchar(256) NOT NULL DEFAULT '''' COMMENT ''专栏简述'',
  `cover` varchar(128) NOT NULL DEFAULT '''' COMMENT ''专栏封面'',
  `state` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''状态: 0-审核中，1-连载，2-完结'',
  `publish_time` timestamp NOT NULL DEFAULT ''1970-01-02 00:00:00'' COMMENT ''上线时间'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  `section` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''排序'',
  `nums` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''专栏预计的更新的文章数'',
  `type` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''专栏类型 0-免费 1-登录阅读 2-限时免费'',
  `free_start_time` timestamp NOT NULL DEFAULT ''1970-01-02 00:00:00'' COMMENT ''限时免费开始时间'',
  `free_end_time` timestamp NOT NULL DEFAULT ''1970-01-02 00:00:00'' COMMENT ''限时免费结束时间'',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT=''专栏'';
```

专栏文章表
```sql
-- pai_coding.column_article definition

CREATE TABLE `column_article` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `column_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''专栏ID'',
  `article_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''文章ID'',
  `section` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''章节顺序，越小越靠前'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_column_id` (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COMMENT=''专栏文章列表'';
```

#### 点赞收藏
再技术派中，对于文章提供了点赞、收藏、评论三种交互，这里重点看一下点赞与收藏；

点赞与收藏，实际上就是用户与文章之间的操作行为，再前面的`user_foot`表就已经介绍具体的表结构, 文章的统计计数就是根据这个表数据来的，当前用户与文章的点赞、收藏关系，同样是根据这个表来的

唯一需要注意的点，就是这个数据的插入、更新策略：

- 首次阅读文章时：插入一条数据
- 点赞：若记录存在，则更新状态，之前时点赞的，设置为取消点赞；若记录不存在，则插入一条点赞的记录
- 收藏：同上

### 评论模块
评论可以是针对文章进行，也可以是针对另外一个评论进行回复，我们将回复也当作是一个评论

![](https://cdn.tobebetterjavaer.com/paicoding/a98f50b4cdfa7a2a71b7829ed1efe966.png)

#### 评论
我们将评论和回复都当成普通的评论，只是主体不同而已，因此一篇文章的评论列表，我们需要重点关注的就是，如何构建评论与其回复之间的层级关系

对于这种评论与回复的层级关系，可以是建辅助表来处理；也可以是表内的父子关系来处理，这里我们采用第二种策略

- 每个评论记录它的上一级评论id（若只是针对文章的评论，那么上一级评论id = 0）
- 我们通过父子关系，在业务层进行逻辑还原

#### 库表设计
针对上面的策略，核心的评论库表设计如下
```sql
-- pai_coding.comment definition

CREATE TABLE `comment` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `article_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''文章ID'',
  `user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''用户ID'',
  `content` varchar(300) NOT NULL DEFAULT '''' COMMENT ''评论内容'',
  `top_comment_id` int(11) NOT NULL DEFAULT ''0'' COMMENT ''顶级评论ID'',
  `parent_comment_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''父评论ID'',
  `deleted` tinyint(4) NOT NULL DEFAULT ''0'' COMMENT ''是否删除'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COMMENT=''评论表'';
```

**注意：**

- 为什么再表中需要冗余一个顶级评论id ？
- 主要的目的是简化业务层评论关系还原的复杂性

通过上面的表结构，关系还原的策略：

- 先查出文章的顶级评论（parent_comment_id = 0）
- 接下来就是针对每个顶级评论，查询它下面的所有回复 ( top_comment_id = comment_id)
   - 构建顶级评论下的回复父子关系（根据parent_comment_id来构建依赖关系）

拓展：如果不存在top_comment_id，那么要实现上面这个还原，要怎么做呢？

#### 评论点赞
技术派中同样支持对评论进行点赞，取消点赞；对于点赞的整体业务逻辑操作，实际上与文章的点赞一致，因此我们直接复用了文章的点赞逻辑，借助 `user_foot` 来实现的

**说明**

- 上面这种实现并不是一种优雅的选择，从`user_foot`的设计也能看出，它实际上与评论点赞这个业务是有些隔离的
- 采用上面这个方案的主要原因在于，点赞这种属于通用的服务，使用mysql来维系点赞与否以及计数统计，再数据量大了之后，基本上玩不转；后续会介绍如何设计一个通用的点赞服务，以此来替换技术派中当前的点赞实现
- 这种设计思路也经常体现在一个全新项目的设计中，最开始的设计并不会想着一蹴而就，整一个非常完美的系统出来，我们需要的是在最开始搭好基座、方便后续扩展；另外一点就是，如何在当前系统的基础上，最小成本的支持业务需求（相信各位小伙伴在日常工作中，这些事情不会陌生）

### 消息模块
消息模块主要是记录一些定义的事件，用于同步给用户；我们整体采用Event/Listener的异步方案来进行
在单机应用中，借助`Spring Event/Listener`机制来实现；在集群中，将借助MQ消息中间件来实现

#### 消息通知
我们主要定义以下五种消息类型

- 评论
- 点赞
- 收藏
- 关注
- 系统消息

![](https://cdn.tobebetterjavaer.com/paicoding/bd7c10d61a3249edaf9191e7a9733d41.png)

当发生方面的行为之后，再相应的地方进行主动埋点，手动发送一个消息事件，然后异步消费事件，生成消息通知

需要注意一点：

- 当用户点赞了一个文章，产生一个点赞消息之后；又取消了点赞，这个消息会怎样？
- 撤销还是依然保留？（技术派中选择的方案是撤销）

#### 库表设计
```sql
-- pai_coding.notify_msg definition

CREATE TABLE `notify_msg` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `related_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''关联的主键'',
  `notify_user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''通知的用户id'',
  `operate_user_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''触发这个通知的用户id'',
  `msg` varchar(1024) NOT NULL DEFAULT '''' COMMENT ''消息内容'',
  `type` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''类型: 0-默认，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-系统'',
  `state` tinyint(3) unsigned NOT NULL DEFAULT ''0'' COMMENT ''阅读状态: 0-未读，1-已读'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  KEY `key_notify_user_id_type_state` (`notify_user_id`,`type`,`state`)
) ENGINE=InnoDB AUTO_INCREMENT=1086 DEFAULT CHARSET=utf8mb4 COMMENT=''消息通知列表'';
```

### 通用模块
关于技术派中的通用模块大致有下面几种，相关的技术方案也比较简单，将配合库表进行简单说明

#### 统计计数
针对文章的阅读计数，没访问一次计数+1， 因此前面的`user_foot`不能使用（因为未登录的用户是不会生成user_foot记录的）

我们当前设计的一个简单的计数表如下
```sql
-- pai_coding.read_count definition

CREATE TABLE `read_count` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `document_id` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''文档ID（文章/评论）'',
  `document_type` tinyint(4) NOT NULL DEFAULT ''1'' COMMENT ''文档类型：1-文章，2-评论'',
  `cnt` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''访问计数'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_document_id_type` (`document_id`,`document_type`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COMMENT=''计数表'';
```

注意，上面这个计数表中的cnt的更新，使用 `cnt = cnt + 1` 而不是 `cnt = xxx`的方案

#### pv/uv
每天的请求pv/uv计数统计，直接再filter层中记录

```sql
-- pai_coding.request_count definition

CREATE TABLE `request_count` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `host` varchar(32) NOT NULL DEFAULT '''' COMMENT ''机器IP'',
  `cnt` int(10) unsigned NOT NULL DEFAULT ''0'' COMMENT ''访问计数'',
  `date` date NOT NULL COMMENT ''当前日期'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_unique_id_date` (`date`,`host`)
) ENGINE=InnoDB AUTO_INCREMENT=8708 DEFAULT CHARSET=utf8mb4 COMMENT=''请求计数表'';
```

#### 全局字典
统一配置、全局字典相关的，主要是减少代码中的硬编码

```sql
-- pai_coding.dict_common definition

CREATE TABLE `dict_common` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
  `type_code` varchar(100) NOT NULL DEFAULT '''' COMMENT ''字典类型，sex, status 等'',
  `dict_code` varchar(100) NOT NULL DEFAULT '''' COMMENT ''字典类型的值编码'',
  `dict_desc` varchar(200) NOT NULL DEFAULT '''' COMMENT ''字典类型的值描述'',
  `sort_no` int(8) unsigned NOT NULL DEFAULT ''0'' COMMENT ''排序编号'',
  `remark` varchar(500) DEFAULT '''' COMMENT ''备注'',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最后更新时间'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code_dict_code` (`type_code`,`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COMMENT=''通用数据字典'';
```

#### 图片上传
文章的图片上传，我们支持服务器本地存储和oss存储，其中dev开发环境，默认是本地存储，即图片传到本地的一个目录下；prod生产环境，会将图片上传到阿里云的oss（其他厂商的oss也没有什么本质区别，都是一个post请求，将文件上传而已）

注意：

- 再具体的实现中，需要自动检测文章中的图片，进行转存，避免直接引入外部的资源，导致失效问题
- 下载外网资源，是否会有安全问题？
   - 采用资源类型限制、校验
   - 生产环境中不存储资源到本地服务器/或者限制本地存储的文件名
- 下载外网资源，转存是否会导致整个文章发布过程很慢？
   - 并发转存策略

#### 搜索推荐
技术派当前的搜索推荐主要是基于数据库来实现，后续再介绍es相关教程时，会同步引入ES进行替换当前的数据库方案

## 迭代排期
再详细设计这一阶段，一般来说会预估一下整体搞完需要多少人天，鉴于实际情况分几个迭代版本进行，每个版本的主要功能点有哪些；这一块就通过下面几张图简单给大家介绍下，详情推荐查看项目管理流程的内容

关于技术派当前覆盖的功能点如：

![](https://cdn.tobebetterjavaer.com/paicoding/e0fa4870e1a19c61dba7b4c8f76de757.png)
开发进度与后期版本迭代计划：
![](https://cdn.tobebetterjavaer.com/paicoding/c47c62e298647a98ce96a96ae4270336.png)

## 技术派编程星球

当然了，这些教程会优先开放给[技术派编程星球](https://t.zsxq.com/0buCVQ3qQ)的球友们，毕竟这群家伙都是氪金过的 VIP，一直在等这个项目的官宣，等的嗷嗷叫。

![](https://cdn.tobebetterjavaer.com/paicoding/207544feafb505be2dacdf22510de9f6.png)

如果你也想加入技术派的编程星球，现在送出 30 元的优惠券，原价 129 元，等于说优惠完**只需要 99 年就可以加入，每天不到 0.27 元**，超级划算！



![](https://cdn.tobebetterjavaer.com/paicoding/2d8158a75714bdba3facaec0ba7b9610.png)



要知道，这还只是星球的一小部分服务，我们还会提供以下这些服务

1. **技术派项目学习教程**，后续会采用连载的方式，让你从 0 到 1 也能搭建一套自己的网站
2. 技术派项目答疑解惑，让你快速上手该项目，小白也能懂
3. **向楼仔、二哥和大厂嘉宾 1 对 1 交流提问**，告别迷茫
4. 个人成长路线、职业规划和建议，帮助你有计划学习
5. 简历修改建议，让你的简历也能脱颖而出，收获更多面试机会
6. 分享硬核技术学习资料，比如 **Spring 源码、高并发教程、JVM、架构选型**等
7. 分享面试资料，都是一些高频面试题
8. 分享工作中好用的开发小工具，助你提升开发效率
9. 分享工作中的踩坑经历，让你快速获取工作经验，少走很多弯路
10. 需求方案、技术架构设计提供参考建议，对标大厂
11. 回答每天球友的问题
12. 一起学习打卡，楼仔帮你分析学习进度


**比如说星球分享的后端技术栈知识汇总**，全面系统的带你成为一名优秀的 Java 后端工程师。

![](https://cdn.tobebetterjavaer.com/paicoding/0397a6a7eb5b4d8fe3c7ca9a00e76355.png)

**像简历修改，绝不放过任何一个细节**，至今已经修改超过 100 份，所有的简历修改建议也都会第一时间同步到星球里。

![](https://cdn.tobebetterjavaer.com/paicoding/77dd551194c05e090b24d0b7b3adfbfd.png)


星球刚开始运营，所以设置的门槛非常低，为的就是给所有人提供一个可持续的学习环境，不过随着人数的增多，**肯定会涨价**，今天这批 30 元的优惠券是 2023 年最大的优惠力度了，现在入手就是最划算的，再犹豫就只能等着涨价了。

原价 **129元**，优惠完只需**99元**就能上车，星球不仅能开阔你的视野，还能跟一群优秀的人交流学习，如果工作学习中遇到难题也有人给你出谋划策，这个价格绝对超值！

![](https://cdn.tobebetterjavaer.com/paicoding/2d8158a75714bdba3facaec0ba7b9610.png)

想想，QQ音乐听歌连续包年需要 **88元**，腾讯视频连续包年需要 **178元**，腾讯体育包年 **233元**。我相信，知识星球回馈给你的，将是 10 倍甚至百倍的价值。

最后，希望球友们，能紧跟我们的步伐！不要掉队。兔年，和我们技术派一起翻身、一起逆袭、一起晋升、一起拿高薪 offer！

', 0);
INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(17, 1, '大家好呀，我是楼仔。

上周推出了我们的开源项目「技术派」，大家好评如潮，很多同学都想学习这个项目，为了更好带大家一起飞，我们今天正式推出技术派的知识星球。

什么是知识星球呢？你可以理解为高品质社群，方便大家跟着我们一起学习。

![](https://cdn.tobebetterjavaer.com/paicoding/a87da08c67100c0755228c66dbbbf43a.png)

## 01 星球介绍

先来介绍下星球的三位联合创始人：

*   **楼仔**：8 年一线大厂后端经验（百度/小米/美团），技术派团队负责人，擅长高并发、架构、源码，有很强的项目/团队管理、职业规划能力。
*   **沉默王二**：GitHub 星标 6400k+开源知识库《 Java 程序员进阶之路》作者，CSDN 两届博客之星，掘金/知乎 Java 领域优质创作者。
*   **一灰**：担任过技术总监，大厂里搞架构、创业团队冲过浪的资深后端，主研 Java 技术栈，擅长架构设计、高并发、微服务等领域。

再来介绍一下星球提供的服务内容：

1.  技术派项目学习教程，后续会采用连载的方式，让你从 0 到 1 也能搭建一套自己的网站
2.  技术派项目答疑解惑，让你快速上手该项目，小白也能懂
3.  向楼仔、二哥和大厂嘉宾 1 对 1 交流提问，告别迷茫
4.  个人成长路线、职业规划和建议，帮助你有计划学习
5.  简历修改建议，让你的简历也能脱颖而出，收获更多面试机会
6.  分享硬核技术学习资料，比如 Spring 源码、高并发教程、JVM、架构选型等
7.  分享面试资料，都是一些高频面试题
8.  分享工作中好用的开发小工具，助你提升开发效率
9.  分享工作中的踩坑经历，让你快速获取工作经验，少走很多弯路
10.  需求方案、技术架构设计提供参考建议，对标大厂
11.  回答每天球友的问题
12.  一起学习打卡，楼仔帮你分析学习进度

## 02 技术派教程&答疑

技术派教程是我们星球推出的主打服务项目。

整个系列教程，会教你如何从 0 到 1 去完成一个对标大厂的项目，预计会出 100 篇文章，共划分为 6 个模块。

![](https://cdn.tobebetterjavaer.com/paicoding/61015e67dd9ef8b4a30baaeec0167c05.png)

大厂篇：

![](https://cdn.tobebetterjavaer.com/paicoding/50990ffeae270da255ec53116efdb24b.png)

进阶篇：

![](https://cdn.tobebetterjavaer.com/paicoding/610fdc02afa49b58854520b836e4ef0d.png)

由于教程目录太长，就不一一罗列，知识星球中有完整的教程目录。

由于教程内容较多，不可能一次性写完，所以**会采用连载的方式，将教程发布到知识星球中**，该教程由 3 位合伙人一起撰写，我们会先选取里面最重要的 20 篇，在本月全部输出，也方面大家能快速入门学习。

对于技术派项目中遇到的问题，**大家可以加入技术派的知识星球群，我们会给大家一一解答**，即使你是小白，也完全不用担心。

## 03 成长答疑解惑

其实楼仔在学习和成长的过程中，也曾焦虑过、迷茫过，如果你恰好和我一样，这里需要重点关注，就比如下面这位粉丝。

![](https://cdn.tobebetterjavaer.com/paicoding/2414cd97c01c934100e8134619d36853.png)

如果你也一样，找楼仔不就得了。。。给大家看看楼仔这几年的学习计划（目前也都放到星球中）：

![](https://cdn.tobebetterjavaer.com/paicoding/bb86e1d293c9ec50f224ea0ae26958dc.png)

很多同学会问，楼哥，你怎么知道要学习这些内容呢？星球中其实已经给大家分享了后端技术栈需要掌握的全部知识，以及对应的学习资料，让你学习更有章法。

对于还在迷茫和焦虑，不知道如何规划自己学习路径、不知如何进行时间管理、或者有其它疑惑的同学，都可以在星球中给楼仔提问，我都会耐心回复大家。

![](https://cdn.tobebetterjavaer.com/paicoding/cf240a7981b73afa3969346119387449.png)

对于制定好学习计划的同学，可以在星球中打卡，定期同步学习进度，楼仔也会对你的学习进度进行纠偏哈。

![](https://cdn.tobebetterjavaer.com/paicoding/d391ae8331aa97744d33306e8706ca4c.png)

## 04 有价值的资料

星球中会提供大量有价值的学习资料，比如我之前面试大厂的一些笔记，都是我这几年实战的大厂面试题，真枪实弹！就靠这些面试题，拿到过百度、新浪、小米、美团、滴滴、陌陌的 Offer。

![](https://cdn.tobebetterjavaer.com/paicoding/9219e1288cc55100380a2846158b337c.png)

星球中还会提供其它大量有价值的学习资料，之前有一部分已经免费发放给大家，但是有一部分属于星球专属。

![](https://cdn.tobebetterjavaer.com/paicoding/aff8dce6bb9b894595e7e8b5ca51f0fe.png)

![](https://cdn.tobebetterjavaer.com/paicoding/985bd5d963587c9b5aa5d45b4351e84e.png)

## 05 简历&大厂项目文档

马上就到招聘季，我们也会帮大家一起修改简历，大家可以按照这个模板，将改好的简历发给我邮箱，我们会给你简历修改建议，让你能拿到更多面试机会。

![](https://cdn.tobebetterjavaer.com/paicoding/2bd598f5fe732055e82f96b7ca89e3f5.png)

楼仔这边也有很多大厂的资料，需求文档、方案设计文档、架构设计文档等，如果你需要这些大厂资料，我都会给你提供，包括你自己进行方案&架构设计时，我们也会给你提供指导和建议。

## 06 如何加入星球？

技术派的知识星球原价是 **129** 元，特地给大家申请了一波 30 元的优惠券，最后的优惠价是 **99** 元。

![](https://cdn.tobebetterjavaer.com/paicoding/c485a03da98e567cfcd18aacd329c9ce.png)

可能有同学会说，你的星球有点贵，这个还真不一样，**你需要看服务内容，并不是所有的星球，都有技术派这样的项目，都有我们这样专业的团队。**

之前就有粉丝报培训班，花费近 2 万，为了就是能在简历上能多一些项目经验，最后效果也不太理想。

我们这个星球，有项目、有技术、有个人计划、甚至连简历修改都包括，仅单简历修改这一项，外面至少也要 300 RMB，不信？大家可以自行百度。

之前我也经常给同事说，在你这个年龄，但凡有人像我指导你一样，去指导我，我就可以少走 3 年弯路，人的黄金时间，又有多少个 3 年呢？

**可以这么说，对于技术派提供的服务，只要有一项你需要，基本能赚回票价，绝对不会让粉丝们吃亏。**

大家时间都很宝贵，早上车一天，就少浪费一天时间。

![](https://cdn.tobebetterjavaer.com/paicoding/c485a03da98e567cfcd18aacd329c9ce.png)

一起加油，共勉！??', 0);
INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(15, 2, '大家好，我是二哥呀。

给大家官宣一件大事，我们搞了近半年的实战项目——[**技术派**](https://paicoding.com/)，终于上线了！瞅瞅这首页，清新、高级、上档次！

![](https://cdn.tobebetterjavaer.com/paicoding/bdfa153fc82310f9ab862a1b3db0d0d7.png)

瞅瞅我们的文章详情页的楼仔，帅气、文雅，气质拿捏的死死的。

![](https://cdn.tobebetterjavaer.com/paicoding/b925179a4afb567c34e09e6117ce1346.png)

文章底部的点赞、留言、文章目录，都是妥妥的细节控。

![](https://cdn.tobebetterjavaer.com/paicoding/88c1fb27d5239c8071f70cff6b31ddfa.png)

我们的教程，写得特别用心，这篇《高并发限流》近万字，手绘图也是毫不吝啬。

![](https://cdn.tobebetterjavaer.com/paicoding/4fad6764cdff9859d6479326a1ad6c11.png)

[admin 端](https://paicoding.com/admin-view)也是开源的，可以对文章/教程进行管理配置，并且加入了游客/管理员账户，方便大家在线体验。

![](https://cdn.tobebetterjavaer.com/paicoding/9a2c15310630f172989ac8589ce9702a.png)

好了，接下来，就由我来给大家“隆重”地介绍一下技术派的整个生态圈子。

## 技术派是做什么的？

这是一个基于 Spring Boot、MyBatis-Plus、MySQL、Redis、ElasticSearch、MongoDB、Docker、RabbitMQ 等技术栈实现的社区系统，**采用主流的互联网技术架构、全新的UI设计、支持一键源码部署**，拥有完整的文章&教程发布/搜索/评论/统计流程等，代码完全开源，没有任何二次封装，是一个非常适合二次开发/实战的现代化社区项目? 。

>- 首页地址：[https://paicoding.com](https://paicoding.com)
>- GitHub 仓库：[https://github.com/itwanger/paicoding](https://github.com/itwanger/paicoding)
>- 码云仓库（国内访问更快）：[https://gitee.com/itwanger/paicoding](https://gitee.com/itwanger/paicoding)

对于这个项目我们是有野心的：

1、国内的不少社区不思进取，你发个文章各种限制你，不让你干这个不让你干那个（我就不点名批评了，比如说某乎放个 B站视频链接就不给你流量），我们就是要打破这种条条框框，给开发者一个自由创作的平台。

2、到了找工作的季节，很多小伙伴简历上没有项目经验可写，这个很吃亏。虽然 GitHub 和码云上已经有不少优秀的开源项目，但**大多数没有成熟且体系化的教程**，总不能直接下载到本地跑一下 main 方法就算学习了吧？

我们要负责到底！接下来，我们会更新一系列的教程，不仅包含项目的开发文档，还会包括 Java、Go 语言、Spring、MySQL、Redis、微服务&分布式、消息队列、操作系统、计算机网络、数据结构与算法等内容。

总之一句话：**学编程，就上技术派**?。

## 技术派能让你学到什么？

这绝不是我在口嗨哈，给大家看一下我们的系统架构图，就知道我们有多用心。

![](https://cdn.tobebetterjavaer.com/paicoding/3da165adfcad0f03d40e13e941ed4afb.png)

再用文字详细地描述下，方便大家做笔记，也方便大家监督我们，这些技术栈最终都将以专栏/教程的方式和大家见面，让天下没有难学的技术（?）！

- 构建工具：后端（Maven、Gradle）、前端（Webpack、Vite）
- 单元测试：[Junit](https://tobebetterjavaer.com/gongju/junit.html)
- 开发框架：SpringMVC、Spring、Spring Boot
- Web 服务器：Tomcat、Caddy、Nginx
- 微服务：Spring Cloud
- 数据层：JPA、MyBatis、MyBatis-Plus
- 模板引擎：thymeleaf
- 容器：Docker（镜像仓库服务Harbor、图形化工具Portainer）、k8s、Podman
- 分布式 RPC 框架：Dubbo
- 消息队列：Kafka（图形化工具Eagle）、RocketMQ、RabbitMQ、Pulsar
- 持续集成：Jenkins、Drone
- 压力测试：Jmeter
- 数据库：MySQL（数据库中间件Gaea、同步数据canal、数据库迁移工具Flyway）
- 缓存：Redis（增强模块RedisMod、ORM框架RedisOM）
- nosql：MongoDB
- 对象存储服务：minio
- 日志：[Log4j](https://tobebetterjavaer.com/gongju/log4j.html)、[Logback](https://tobebetterjavaer.com/gongju/logback.html)、[SF4J](https://tobebetterjavaer.com/gongju/slf4j.html)、[Log4j2](https://tobebetterjavaer.com/gongju/log4j2.html)
- 搜索引擎：ES
- 日志收集：ELK（日志采集器Filebeat）、EFK（Fluentd）、LPG（Loki+Promtail+Grafana）
- 大数据：Spark、Hadoop、HBase、Hive、Storm、Flink
- 分布式应用程序协调：Zookeeper
- token 管理：jwt（nimbus-jose-jwt）
- 诊断工具：arthas
- 安全框架：Shiro、SpringSecurity
- 权限框架：Keycloak、Sa-Token
- JSON 处理：fastjson2、[Jackson](https://tobebetterjavaer.com/gongju/jackson.html)、[Gson](https://tobebetterjavaer.com/gongju/gson.html)
- office 文档操作：EasyPoi、EasyExcel
- 文件预览：kkFileView
- 属性映射：mapStruct
- Java硬件信息库：oshi
- Java 连接 SSH 服务器：ganymed
- 接口文档：Swagger-ui、Knife4j、Spring Doc、Torna、YApi
- 任务调度框架：Spring Task、Quartz、PowerJob、XXL-Job
- Git服务：Gogs
- 低代码：LowCodeEngine、Yao、Erupt、magic-api
- API 网关：Gateway、Zuul、apisix
- 数据可视化（Business Intelligence，也就是 BI）：DataEase、Metabase
- 项目文档：Hexo、VuePress
- 应用监控：SpringBoot Admin、Grafana、SkyWalking、Elastic APM
- 注解：lombok
- jdbc连接池：Druid
- Java 工具包：hutool、Guava
- 数据检查：hibernate validator
- 代码生成器：Mybatis generator
- Web 自动化测试：selenium
- HTTP客户端工具：Retrofit
- 脚手架：sa-plus

我们希望通过**技术派**这个项目打造一个闭环，既能帮大家提升项目经验、升职加薪，又能提升我们的技术影响力，还能增加我们原创教程的流量（典型的既要又要还要，有没有?）。

![](https://cdn.tobebetterjavaer.com/paicoding/d7c691d9c748ba4980fb14b7132929e8.png)

为了做好这个项目，我们付出了巨大的努力。先来看源码，分支 30 个，提交 595 次，这还不包括 admin 端的，已经推出，就广受好评，这才第一周，就收获了 100+ star，这还只是码云上。


![](https://cdn.tobebetterjavaer.com/paicoding/ca229cfe9dd66d9733190a32c1622b7c.png)


代码严格按照大厂的规范要求来，组织结构清晰、项目文档齐全、代码注释到位，你想学不到知识都难！

![](https://cdn.tobebetterjavaer.com/paicoding/4fb5290e729c6ad7d851d06c09cfd3bc.png)

只要你本地安装好 JDK 8（以上版本均可），MySQL（5.x/8.x+），配置好 Maven，导入项目源码后，直接运行 main 方法就可以轻松在本地跑起来，你甚至不需要额外手动创建数据库，不用在浏览器地址栏键入 `localhost:8080`，只要轻轻一点控制台提供的链接就可以访问了。

![](https://cdn.tobebetterjavaer.com/paicoding/505cf19c993cda5b251b23bbecce2dd0.png)

这些琐事我们已经帮你做好了，省心吧？

## 技术派的成长过程

这个项目并不是二哥一时兴起发起的，而是做了充分的准备和调研。来介绍一下我们技术派的联合创始人，前后端我们三个人均有参与：

- **楼仔**，8 年一线大厂后端经验（百度/小米/美团），技术派团队负责人，擅长高并发、架构、源码，有很强的项目/团队管理、职业规划能力
- **一灰**，国企里莫过鱼、大厂里拧过螺丝、创业团队冲过浪的资深后端，主研Java技术栈，擅长架构设计、高并发、微服务等领域
- **沉默王二**，GitHub 星标 6400k+开源知识库《Java 程序员进阶之路》作者，CSDN 两届博客之星，掘金/知乎 Java 领域优质创作者

前期的需求调研、开发中的进度管理、上线后的文档教程，也都是不能少的，后期我们也会把这些开源出来，先截图给大家看看。

1、整体设计草图

![](https://cdn.tobebetterjavaer.com/paicoding/a2e2af3453a677ac69848d72c632cc16.png)

2、库表设计

![](https://cdn.tobebetterjavaer.com/paicoding/9d8b1b3ff29736a3a8952cefb6c78a33.png)

3、产品方案

![](https://cdn.tobebetterjavaer.com/paicoding/a868cfbf17b57a155d809f9f0ad5b304.png)

4、UI设计

![](https://cdn.tobebetterjavaer.com/paicoding/cc0c36ddc05604671c0ab657837c5ca4.png)

5、接口文档

![](https://cdn.tobebetterjavaer.com/paicoding/976463cffcb46247f6c6c83425bdf454.png)

6、进度排期

![](https://cdn.tobebetterjavaer.com/paicoding/9b09f64bbac80b633da2255896304bfe.png)

7、bug&优化

![](https://cdn.tobebetterjavaer.com/paicoding/9dce5d6ed0689e82a8119438de4ffcc7.png)

8、年度复盘

![](https://cdn.tobebetterjavaer.com/paicoding/bfbef7833225d5da24ebec60f2f01c9d.png)

## 技术派的后期打算

项目上线后，最重要的两件事，一个是持续迭代，修复线上问题，并且把需求池中 p3 的任务开发掉；另外一个就是完成开发文档的编写，我们计划每周更新三篇。

先是大厂篇，由我们技术派团队的楼仔负责。

![](https://cdn.tobebetterjavaer.com/paicoding/146e84171eabca58fc7323f309fb4273.png)

然后是基础篇，由二哥来负责。

![](https://cdn.tobebetterjavaer.com/paicoding/5ffd29de49299b4cc6f286beba519203.png)

接着是进阶篇，由我们技术派团队的一灰来负责。

![](https://cdn.tobebetterjavaer.com/paicoding/ac90ac17e69e0ad0a5b262c6b6b608b2.png)

后面还会推出扩展篇、前端篇、工程篇，把整个 Java 后端的技术栈全部搞定。

![](https://cdn.tobebetterjavaer.com/paicoding/c21bc3e9d6e64bfc89ed2ebf5e038478.png)

## 技术派编程星球

当然了，这些教程会优先开放给[技术派编程星球](https://t.zsxq.com/0buCVQ3qQ)的球友们，毕竟这群家伙都是氪金过的 VIP，一直在等这个项目的官宣，等的嗷嗷叫。

![](https://cdn.tobebetterjavaer.com/paicoding/207544feafb505be2dacdf22510de9f6.png)

如果你也想加入技术派的编程星球，现在送出 30 元的优惠券，原价 129 元，等于说优惠完**只需要 99 年就可以加入，每天不到 0.27 元**，超级划算！



![](https://cdn.tobebetterjavaer.com/paicoding/2d8158a75714bdba3facaec0ba7b9610.png)



要知道，这还只是星球的一小部分服务，我们还会提供以下这些服务

1. **技术派项目学习教程**，后续会采用连载的方式，让你从 0 到 1 也能搭建一套自己的网站
2. 技术派项目答疑解惑，让你快速上手该项目，小白也能懂
3. **向楼仔、二哥和大厂嘉宾 1 对 1 交流提问**，告别迷茫
4. 个人成长路线、职业规划和建议，帮助你有计划学习
5. 简历修改建议，让你的简历也能脱颖而出，收获更多面试机会
6. 分享硬核技术学习资料，比如 **Spring 源码、高并发教程、JVM、架构选型**等
7. 分享面试资料，都是一些高频面试题
8. 分享工作中好用的开发小工具，助你提升开发效率
9. 分享工作中的踩坑经历，让你快速获取工作经验，少走很多弯路
10. 需求方案、技术架构设计提供参考建议，对标大厂
11. 回答每天球友的问题
12. 一起学习打卡，楼仔帮你分析学习进度


**比如说星球分享的后端技术栈知识汇总**，全面系统的带你成为一名优秀的 Java 后端工程师。

![](https://cdn.tobebetterjavaer.com/paicoding/0397a6a7eb5b4d8fe3c7ca9a00e76355.png)

**像简历修改，绝不放过任何一个细节**，至今已经修改超过 100 份，所有的简历修改建议也都会第一时间同步到星球里。

![](https://cdn.tobebetterjavaer.com/paicoding/77dd551194c05e090b24d0b7b3adfbfd.png)


星球刚开始运营，所以设置的门槛非常低，为的就是给所有人提供一个可持续的学习环境，不过随着人数的增多，**肯定会涨价**，今天这批 30 元的优惠券是 2023 年最大的优惠力度了，现在入手就是最划算的，再犹豫就只能等着涨价了。

原价 **129元**，优惠完只需**99元**就能上车，星球不仅能开阔你的视野，还能跟一群优秀的人交流学习，如果工作学习中遇到难题也有人给你出谋划策，这个价格绝对超值！

![](https://cdn.tobebetterjavaer.com/paicoding/2d8158a75714bdba3facaec0ba7b9610.png)

想想，QQ音乐听歌连续包年需要 **88元**，腾讯视频连续包年需要 **178元**，腾讯体育包年 **233元**。我相信，知识星球回馈给你的，将是 10 倍甚至百倍的价值。

最后，希望球友们，能紧跟我们的步伐！不要掉队。兔年，和我们技术派一起翻身、一起逆袭、一起晋升、一起拿高薪 offer！



', 0);


-- 侧边栏推荐调整
delete from config where id <= 6;
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(1, 5, '高并发手册', 'https://img11.360buyimg.com/ddimg/jfs/t1/159287/38/34144/95370/63c7ee9aFc184be3d/94e07dc5dd5b573f.png', 'https://paicoding.com/article/detail/149', '内容肝、配图美、可读性高，高并发经典之作！', 1, 1, '', '{}', 0, '2023-01-13 19:15:57', '2023-04-15 15:05:22');
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(2, 1, '加入社区2', 'https://imgs.hhui.top/forum/banner/01.png', 'https://hhui.top/', '', 2, 1, '', '{}', 0, '2023-01-13 19:15:57', '2023-01-13 19:15:57');
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(3, 4, '官宣：技术派上线了！', '11', 'https://paicoding.com/article/detail/169', '学编程，就上技术派?！', 1, 1, '2', '{}', 0, '2023-01-13 19:15:57', '2023-04-15 14:55:38');
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(4, 4, 'Java进阶之路.pdf来了！', ' 2', 'https://paicoding.com/column/5/1', '学 Java，就认准二哥的 Java 进阶之路。第一版 PDF 开放下载了！技术派团队出品。', 2, 1, '2', '{}', 0, '2023-01-13 19:15:57', '2023-04-15 16:04:49');
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(5, 6, 'JVM 核心手册', ' https://img14.360buyimg.com/ddimg/jfs/t1/184999/39/32111/443189/63c7fbbbF78e720ff/7e878308d3d27dff.png', 'https://paicoding.com/article/detail/151', '楼仔原创的 JVM 手册，带你成为 Java 高手！技术派团队出品。', 2, 1, '1', '{"visit":110252,"download":12121,"rate":"9.1"}', 0, '2023-01-13 19:15:58', '2023-04-15 21:23:09');
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(6, 6, 'Spring源码解析手册', 'https://img13.360buyimg.com/ddimg/jfs/t1/114223/5/31528/3308443/63c7f65eFdb3a20f2/91c8c191152d82c2.png', 'https://paicoding.com/article/detail/150', '楼仔原创的 Spring 源码解读手册，硬核，带你成为 Spring 高手！技术派团队出品。', 2, 1, '1', '{"visit":120248,"download":212103,"rate":"9.3"}', 0, '2023-01-13 19:15:58', '2023-04-15 21:23:09');


update article set summary = '技术派（paicoding）是一个前后端分离的 Java 社区实战项目，基于 SpringBoot+MyBatis-Plus 实现，采用 Docker 容器化部署。包括前台社区系统和后台管理系统。前台社区系统包括社区首页、文章推荐、文章搜索、文章发布、文章详情、优质教程、个人中心等模块；后台管理系统包括文章管理、教程管理、统计报表、权限菜单管理、设置等模块。' where id = 1;

update article set summary = '天天说分布式分布式，那么我们是否知道什么是分布式，分布式会遇到什么问题，有哪些理论支撑，有哪些经典的应对方案，业界是如何设计并保证分布式系统的高可用呢？

1.架构设计
这一节将从一些经典的开源系统架构设计出发，来看一下，如何设计一个高质量的分布式系统；' where id = 100;

update article set summary = '你在分布式系统上工作吗？微服务，Web API，SOA，Web服务器，应用服务器，数据库服务器，缓存服务器，负载均衡器 - 如果这些描述了系统设计中的组件，那么答案是肯定的。分布式系统由许多计算机组成，这些计算机协调以实现共同的目标。

20多年前，Peter Deutsch和James Gosling定义了分布式计算的8个谬误。这些是许多开发人员对分布式系统做出的错误假设。从长远来看，这些通常被证明是错误的，导致难以修复错误。' where id = 101;



update article set summary = '分布式的概念存在年头有点久了，在正式进入我们《分布式专栏》之前，感觉有必要来聊一聊，什么是分布式，分布式特点是什么，它又有哪些问题，在了解完这个概念之后，再去看它的架构设计，理论奠基可能帮助会更大' where id = 102;

