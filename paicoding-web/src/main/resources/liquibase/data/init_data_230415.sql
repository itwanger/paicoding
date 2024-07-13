-- 设置文章置顶
update article set topping_stat = 1 where id in (100, 101, 102);


-- 新增几个官方相关文章

INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(14, 1, 1, 'JUC编程（一）进程与线程', '', '', '了解JUC，那么就是全程围绕线程与进程来讨论问题了，那么在正式开始学习JUC的相关知识之前，不妨再简单地回顾一下进程与线程的基本知识，为之后进一步的学习打好基础', 1, 2, '', 1, 0, 0, 1, 0);
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(15, 1, 1, '字典树/前缀树的结构', '字典树/前缀树', '', '一次做题后的经历，从而学习并简单总结字典树的基本结构', 11, 2, '', 1, 0, 0, 1, 0);
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(16, 1, 1, '技术派的系统设计方案', '', 'https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/cover/dee73c8810cb699ae1ec774a54612080.jpg', '这个项目诞生的背景主要是个人学习使用，鉴于本人还是个在校生，因此项目的规模一定不会很大，并且存在诸多问题。主要的目的是希望打造一个切实可用的项目，和各位学习的小伙伴分享自己的学习经历，并希望能够展示自己，并结识其他厉害的大佬', 9, 2, '', 1, 0, 0, 1, 0);
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, offical_stat, topping_stat, cream_stat, status, deleted)
VALUES(17, 1, 1, '小灰飞的语雀花园', '', '', '大家好，我是小灰飞。小灰飞是谁？其实是一个平平无奇的大学森，不过也是老年人马上就要研二了。大概从大四开始，我才开始尝试日常做笔记，之前总是写在markdown文件上，然后换电脑或者时间一长就不好管理了，后来开始用语雀，语雀花园中有一些个人的小小笔记希望能和大家分享吧，也就是一边学一边记的', 8, 2, '', 1, 0, 0, 1, 0);



INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(14, 2, '#### 进程与线程

**进程相关概念**

- 程序由指令和数据组成，但这些指令要运行，数据要读写，就必须将指令加载至 CPU，数据加载至内存。在指令运行过程中还需要用到磁盘、网络等设备。进程就是用来加载指令、管理内存、管理 IO 的。
- 当一个程序被运行，从磁盘加载这个程序的代码至内存，这时就开启了一个进程。
- 进程就可以视为程序的一个实例。大部分程序可以同时运行多个实例进程（例如记事本、画图、浏览器等），也有的程序只能启动一个实例进程（例如网易云音乐、360 安全卫士等）。

**线程**

- 一个进程之内可以分为一到多个线程。
- 一个线程就是一条指令流，将指令流中的一条条指令以一定的顺序交给 CPU 执行。
- Java 中，线程作为最小调度单位，进程作为资源分配的最小单位。在 windows 中进程是不活动的，只是作为线程的容器。

**二者对比**

- 进程基本上相互独立的，而线程存在于进程内，是进程的一个子集
- 进程拥有共享的资源，如内存空间等，供其内部的线程共享
- 进程间通信较为复杂
  - 同一台计算机的进程通信称为 IPC（Inter-process communication）
  - 不同计算机之间的进程通信，需要通过网络，并遵守共同的协议，例如 HTTP
- 线程通信相对简单，因为它们共享进程内的内存，一个例子是多个线程可以访问同一个共享变量
- 线程更轻量，线程上下文切换成本一般比进程上下文切换低

---

#### 并发与并行

说白了，并行就是真的同时在执行任务；并发则是串行执行的，只是CPU来回调度，宏观上好像在同时做，“微观串行，宏观并行”

---

#### 同步与异步

需要等待结果返回，才能继续运行就是**同步**；不需要等待结果返回就可以继续运行就是**异步**。
JUC就是在研究异步

---

#### 多线程一定更快吗？

1. 单核 CPU 下，多线程不能实际提高程序运行效率，只是为了能够在不同的任务之间切换，不同线程轮流使用 CPU，不至于一个线程总占用 CPU，别的线程没法干活
2. 多核 CPU 可以并行跑多个线程，但能否提高程序运行效率还是要分情况的
   1. 有些任务，经过精心设计，将任务拆分，并行执行，当然可以提高程序的运行效率。但不是所有计算任务都能拆分（参考后文的【阿姆达尔定律】）
   2. 也不是所有任务都需要拆分，任务的目的如果不同，谈拆分和效率没啥意义
3. IO 操作不占用 CPU，只是我们一般拷贝文件使用的是【阻塞 IO】，这时相当于线程虽然不用 CPU，但需要一直等待 IO 结束，没能充分利用线程。所以才有后面的【非阻塞 IO】和【异步 IO】优化

> **阿姆达尔定律：**
> 阿姆达尔定律（Amdahl’s Law）是由计算机科学家吉恩·阿姆达尔（Gene Amdahl）提出的一条关于并行计算的定律。它描述了通过增加并行度（如增加处理器数量）来提高系统性能的理论极限。该定律表明，即使是计算中可以并行化的部分再多，并行化带来的加速效果也是有限的，因为总有一部分计算任务是无法并行化的。
> 阿姆达尔定律的数学表达式为：
> ![](https://cdn.nlark.com/yuque/__latex/e3b0ba891660a61dc509fa91ebe812eb.svg#card=math&code=S%3D%5Cfrac%7B1%7D%7B%281-P%29%2B%5Cfrac%7BP%7D%7BN%7D%7D&id=KA9et)
> 其中：
>
> - ( S ) 是加速比（Speedup），表示使用 ( N ) 个处理器时系统性能的提升倍数。
> - ( P ) 是程序中可以并行化的部分所占的比例。
> - ( N ) 是处理器的数量
>
> 根据阿姆达尔定律：
>
> - 当 ( N ) 很大时，性能提升的上限趋近于 ![](https://cdn.nlark.com/yuque/__latex/28963c9b26dba52235a64d2cb68e48b2.svg#card=math&code=%5Cfrac%7B1%7D%7B1-P%7D&id=lm3IU)。
> - 如果程序中并行化的部分 ( P ) 越接近于 1，理论上可以获得的加速效果越好。
> - 但是在实际应用中，程序总有一些部分是无法并行化的（即  1 - P  的部分），这限制了并行计算的加速效果。
>
> 这一定律的重要性在于它提醒我们，虽然增加处理器数量可以提高计算性能，但实际的性能提升是受限的，开发者应考虑程序的并行性和串行部分对性能的影响。

', 0);
INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(16, 1, '## 整体介绍
### 背景
> 这个项目诞生的背景和什么黑马的项目外卖、商城项目不同，本质来说也是一个个人的项目，同时本人还是个在校的学生，因此体量不会很大

我主要的目的是希望打造一个切实可用的平台，依托于这个平台，将我自己的个人学习笔记进行展示，同时希望有志同道合的小伙伴能一起学习交流，也是一个展示自己的平台
### 系统模块介绍
#### 系统架构
基于论坛或者说博客系统的分层特点，将整个系统架构划分为展示层，应用层，服务层，如下图
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/8671f216f2cd6fa675d2ee5071bf577a.png)

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
但是需要注意的是，后台也是同样有文章、评论、用户等业务功能的，前台与后台可使用应用主要是权限粒度管理的差异性，对于本系统而言，应用可分为：

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
将一些通用的、可抽离业务属性的功能模块，沉淀到服务层，作为一个一个的基础服务进行设计，比如计数服务、消息服务等，通常他们最大特点就是独立与业务之外，适用性更广，并不局限在特定的业务领域内，可以作为通用的技术方案存在
在本系统的项目设计中，拟定以下基础服务

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
整个系统中涉及到的术语并不多，也很容易理解，下面针对几个常用的进行说明

- 用户：特指通过微信公众号扫码注册的用户，可以发布文章、阅读文章等
- 管理员：可以登录后台的特殊用户
- 文章：即博文
- 专栏：由一系列相关的文章组成的一个合集
- 订阅：专指关注用户

> 需要注意的是，由于各种政策限制，我们知道对于这种能够公开发表文章的网站会进行严格的审查，为此大部分用户的文章都需要经过审核才能发布，因此在系统中会有一个审核的状态，用户的文章会有审核中、审核通过、审核未通过等状态。这是通过具有管理员权限的后台控制来保证的

## 系统模块设计
针对前面业务架构拆分，系统的实际项目划分，主要是五个模块，相反并没有将上面的每个应用、服务抽离为独立的模块，主要是为了避免过渡设计，粒度划分太细会增加整个项目的理解维护成本

这里设置五个相对独立的模块，则主要是基于边界特别清晰这一思考点进行，后续做微服务演进时，下面每个模块可以作为独立的微服务存在

### 用户模块
在本系统中，整个用户模块从功能角度可以分为

- 注册登录
- 权限管理（权限管理也放在此）
- 业务逻辑

#### 注册登录
##### 方案设计
注册登录除了常见的用户名+密码的登录方式之外，现在也有流行的手机号+验证，第三方授权登录；而这里则是选择了微信公众号登录方式，主要是为了能够提高自己的展示机会吧
对于个人公众号，很多权限没有；因此这个登录的具体实现，有两种实现策略

- 点击登录，登录页显示二维码 + 输入框 -> 用户关注公众号，输入 "login" 获取登录验证码 -> 在登录界面输入验证码实现登录
- 点击登录，登录页显示二维码 + 验证码 -> 用户关注公众号，将登录页面上的验证码输入到微信公众号 -> 自动登录

其中第一种策略，类似于手机号/验证码的登录方式，主要是根据系统返回的验证码来主动登录

**优点：**

- 代码实现简单，逻辑清晰

**缺点：**

- 操作流程复杂，用户需要输入两次

对于第二种策略，如果是企业公众号，是可以省略输入验证码这一步骤的，借助动态二维码来直接实现扫码登录；对于个人公众号，则需要多来一步，通过输入验证码来将微信公众号的用户与需要登录的用户绑定起来
登录工作流程如下：

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/1ccd749c1b3e8e90cafad25a3e8eaa88.png)

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

注意上面的表结构设计，冗余了 `user_name`, `password` 用户名密码的登录方式，主要是给管理员登录后台使用，同时保留了后续扩展的可能性

用户首次登录之后，会在user表中插入一条数据，主要关注 `third_account_id` 这个字段，它记录的是微信开放平台返回的唯一用户id
#### 权限管理
权限管理会分为两块：用户身份识别 + 鉴权
##### 方案设计
**用户身份识别：**
现在用户的身份识别有非常多的方案，现在采用的是最基础、历史最悠久的方案，cookie + session 方式（后续会迭代为分布式session + jwt）
整体流程：

- 用户登录成功，服务器生成sessionId -> userId 映射关系
- 服务器返回sessionId，写到客户端的浏览器cookie
- 后续用户请求，携带cookie
- 服务器从cookie中获取sessionId，然后找到uesrId

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/515bdaece38d3664d0c7a57d92304387.png)

服务内部身份传递：
另外一个需要考虑的点则是用户的身份如何在整个系统内传递？ 就现在采用的单体架构而言，借助ThreadLocal来实现

- 自定义Filter，实现用户身份识别（即上面的流程，从cookie中拿到SessionId，转userId)
- 定义全局上下文ReqInfoContext：将用户信息，写入全局共享的ThreadLocal中
- 在系统内，需要获取当前用户的地方，直接通过访问 ReqInfoContext上下文获取用户信息
- 请求返回前，销毁上下文中当前登录用户信息

**鉴权**
根据用户角色与接口权限要求进行判定，设计三种权限点类型

- ADMIN：只有管理员才能访问的接口
- LOGIN：只有登录了才能访问的接口
- ALL：默认，没有权限限制

在需要权限判定的接口上，添加上对应的权限要求，然后借助AOP来实现权限判断

- 当接口上有权限点要求时（除ALL之外）
- 首先获取用户信息，如果没有登录，则直接报403
- 对于ADMIN限制的接口，要求查看用户角色，必须为admin

##### 库表设计
将用户角色信息写入用户基本信息表中，没有单独抽出一个角色表，然后进行映射，主要是因为这个系统逻辑相对清晰，没有太复杂的角色关系，因此采用了轻量级的设计方案

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

> 这里需要注意的是上表可以通过查询用户的id来知道哪些人关注了自己，但是一次普通查询是无法得知是不是自己也关注了他们的，后续会讲这里是如何实现的

##### 用户轨迹
在系统的整体设计中，我希望能够记录用户的阅读历史、关注列表、收藏列表、评价的文章列表，对于这种用户行为轨迹的诉求，采用设计一张大宽表的策略，其主要目的在于

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

将用户 + 文章设计唯一键，用来记录用户对自己阅读过的文章的行为，因此可以直接通过这个表获取用户的历史轨迹
同时也可以从文章的角度出发，查看被哪些用户点赞、收藏过

#### 小结
用户模块的核心支撑在上面几块，请重点关注上面的示意图与表结构；当然用户的功能点不止于上面几个，比如基础的个人主页、用户信息等也属于用户模块的业务范畴

### 文章模块
将文章和专栏都放在一起，同样也将类目管理、标签管理等也都放在这个模块中，实际上若文章模块过于庞大，也是可以按照最开始的划分进行继续拆分的；这里放在一起的主要原因在于他们都是围绕基本的文章这一业务属性来的，可以聚合在一起

#### 文章
文章的核心就在于发布、查看

基本的发布流程：

1. 用户登录，进入发布页面
2. 输入标题、文章
3. 选择分类、标签，封面、简介
4. 提交文章，进入待审核状态，仅用户可看详情
5. 管理员审核通过，所有人可看详情

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/0ea39950b441a2ed9b0e14060f952e1e.png)

#### 文章库表设计
考虑到文章的内容通常较大，在很多的业务场景中，实际上是不需要文章内容的，如首页、推荐列表等都只需要文章的标题等信息；此外我也希望对文章做一个版本管理（比如上线之后，再修改则新生成一个版本）
因此对文章设计了两张表
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

文章对应的分类，要求一个文章只能挂在一个分类下
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
在整个系统中，对于文章提供了点赞、收藏、评论三种交互，这里重点看一下点赞与收藏；

点赞与收藏，实际上就是用户与文章之间的操作行为，再前面的`user_foot`表就已经介绍具体的表结构, 文章的统计计数就是根据这个表数据来的，当前用户与文章的点赞、收藏关系，同样是根据这个表来的

唯一需要注意的点，就是这个数据的插入、更新策略：

- 首次阅读文章时：插入一条数据
- 点赞：若记录存在，则更新状态，之前时点赞的，设置为取消点赞；若记录不存在，则插入一条点赞的记录
- 收藏：同上

### 评论模块
评论可以是针对文章进行，也可以是针对另外一个评论进行回复，于是将回复也当作是一个评论

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/3d97fdb8b0b599cf22d3f771067c94e3.png)

#### 评论
将评论和回复都当成普通的评论，只是主体不同而已，因此一篇文章的评论列表，需要重点关注的就是，如何构建评论与其回复之间的层级关系

对于这种评论与回复的层级关系，可以是建辅助表来处理；也可以是表内的父子关系来处理，这里采用第二种策略

- 每个评论记录它的上一级评论id（若只是针对文章的评论，那么上一级评论id = 0）
- 通过父子关系，在业务层进行逻辑还原

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
同样支持对评论进行点赞，取消点赞；对于点赞的整体业务逻辑操作，实际上与文章的点赞一致，因此直接复用了文章的点赞逻辑，借助 `user_foot` 来实现的

**说明**

- 上面这种实现并不是一种优雅的选择，从`user_foot`的设计也能看出，它实际上与评论点赞这个业务是有些隔离的
- 采用上面这个方案的主要原因在于，点赞这种属于通用的服务，使用mysql来维系点赞与否以及计数统计，再数据量大了之后，基本上玩不转；后续会介绍如何设计一个通用的点赞服务，以此来替换目前的点赞实现
- 这种设计思路也经常体现在一个全新项目的设计中，最开始的设计并不会想着一蹴而就，整一个非常完美的系统出来，需要的是在最开始搭好基座、方便后续扩展；另外一点就是，如何在当前系统的基础上，最小成本的支持业务需求

### 消息模块
消息模块主要是记录一些定义的事件，用于同步给用户；整体采用Event/Listener的异步方案来进行
在单机应用中，借助`Spring Event/Listener`机制来实现；在集群中，将借助MQ消息中间件来实现

#### 消息通知
主要定义以下五种消息类型

- 评论
- 点赞
- 收藏
- 关注
- 系统消息

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/63e69441489c53ad29570b584e3ffb44.png)

当发生方面的行为之后，再相应的地方进行主动埋点，手动发送一个消息事件，然后异步消费事件，生成消息通知

需要注意一点：

- 当用户点赞了一个文章，产生一个点赞消息之后；又取消了点赞，这个消息会怎样？
- 撤销还是依然保留？（本项目中选择的方案是撤销）

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
关于通用模块大致有下面几种，相关的技术方案也比较简单，将配合库表进行简单说明

#### 统计计数
针对文章的阅读计数，没访问一次计数+1， 因此前面的`user_foot`不能使用（因为未登录的用户是不会生成user_foot记录的）

当前设计的一个简单的计数表如下
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
每天的请求pv/uv计数统计，直接在filter层中记录

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
文章的图片上传，支持服务器本地存储和oss存储，其中dev开发环境，默认是本地存储，即图片传到本地的一个目录下；prod生产环境，会将图片上传到阿里云的oss（其他厂商的oss也没有什么本质区别，都是一个post请求，将文件上传而已）

注意：

- 在具体的实现中，需要自动检测文章中的图片，进行转存，避免直接引入外部的资源，导致失效问题
- 下载外网资源，是否会有安全问题？
   - 采用资源类型限制、校验
   - 生产环境中不存储资源到本地服务器/或者限制本地存储的文件名
- 下载外网资源，转存是否会导致整个文章发布过程很慢？
   - 并发转存策略

#### 搜索推荐
当前的搜索推荐主要是基于数据库来实现，后续在介绍es相关教程时，会同步引入ES进行替换当前的数据库方案

### 小结
本系统主要是本人在学习过程中用于提升自己实践机会的一个系统，一定还有很多不足之处，希望大家多多包涵，及时指出
', 0);
INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(17, 1, '大家好呀，我是小灰飞。

大家好，我是小灰飞，是一个平平无奇的大学森，不过也是老年人了，马上就要研二。大概从大四开始，我才开始尝试日常做笔记，之前总是写在markdown文件上，然后换电脑或者时间一长就不好管理了，后来开始用语雀，语雀花园中有一些个人的小小笔记希望能和大家分享吧，也就是一边学一边记的。

很希望能有志同道合的小伙伴能一起学习交流，讨论各种技术。自己在学习的时候资源都“不会说话”，不是b站就是各种文档，偶尔能讨论下的也就是gpt，一些比较坑的问题他有时候还说不对......

我的笔记语雀花园也不是什么特别成系统的东西，甚至很多是在看其他人的视频、文档总结的，还有一些甚至就是公开的资料我整合进去便于之后查阅了

大概分布也就如下：

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/b07571a56b8964969a8c2cde615f1542.png)

目前整理的还不够完善和合理，就浅浅的希望有看完之后能够对我感兴趣的大佬来指点迷津吧！
', 0);
INSERT INTO article_detail
(article_id, version, content, deleted)
VALUES(15, 2, '
字典树（Trie），也称为前缀树或基数树，是一种用于高效存储和检索字符串的数据结构，特别适用于查找前缀匹配的场景。字典树的每个节点代表一个字符串中的字符，节点之间的路径代表字符串的前缀。

### 字典树的基本结构

字典树由节点（通常称为 TrieNode）组成，每个节点包含以下内容：

- **子节点（children）**：一个数组或映射，表示所有可能的子节点。对于字母表为26个英文字母的情况，可以使用长度为26的数组来表示子节点。
- **是否为单词结尾（isEndOfWord）**：一个布尔值，表示从根节点到当前节点的路径是否构成一个有效单词。

### 字典树的主要操作

1.  **插入（Insert）**：
    插入一个单词到字典树中。
    - 从根节点开始，对每个字符检查是否存在相应的子节点。
    - 如果不存在，则创建一个新的子节点。
    - 移动到子节点，继续处理下一个字符。
    - 在最后一个字符的节点，标记该节点为单词的结尾。
2.  **查找（Search）**：
    查找一个单词是否存在于字典树中。
    - 从根节点开始，对每个字符检查是否存在相应的子节点。
    - 如果不存在，返回 `false`。
    - 如果存在，移动到子节点，继续处理下一个字符。
    - 在最后一个字符的节点，检查是否标记为单词的结尾。
3.  **前缀查找（StartsWith）**：
    查找是否存在以某个前缀开头的单词。
    - 类似于查找操作，但不需要检查最后一个字符节点是否为单词的结尾。

### 示例代码

下面是一个简单的字典树实现示例（Java）：

```java
import java.util.HashMap;
import java.util.Map;

class TrieNode {
    Map<Character, TrieNode> children;
    boolean isEndOfWord;

    public TrieNode() {
        children = new HashMap<>();
        isEndOfWord = false;
    }
}

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // 插入一个单词到字典树中
    public void insert(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            node = node.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    // 查找一个单词是否在字典树中
    public boolean search(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }

    // 查找是否存在以某个前缀开头的单词
    public boolean startsWith(String prefix) {
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Trie trie = new Trie();

        trie.insert("apple");
        System.out.println(trie.search("apple"));   // 返回 true
        System.out.println(trie.search("app"));     // 返回 false
        System.out.println(trie.startsWith("app")); // 返回 true

        trie.insert("app");
        System.out.println(trie.search("app"));     // 返回 true
    }
}
```

### 字典树的优点

- **高效的前缀查找**：字典树能够快速地进行前缀匹配和自动补全操作。
- **插入和查找效率高**：插入和查找操作的时间复杂度为 O(m)，其中 m 为字符串的长度。

### 字典树的缺点

- **空间消耗大**：每个节点都可能包含多个子节点，尤其是在处理大规模字典或长字符串时，空间消耗较大。

字典树在需要频繁进行字符串前缀查找和自动补全的应用中非常有用，例如搜索引擎的自动补全、拼写检查、IP 路由等。


', 0);


-- 侧边栏推荐调整
delete from config where id <= 6;
# INSERT INTO config
# (id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
# VALUES(1, 5, '高并发手册', 'https://img11.360buyimg.com/ddimg/jfs/t1/159287/38/34144/95370/63c7ee9aFc184be3d/94e07dc5dd5b573f.png', 'https://paicoding.com/article/detail/149', '内容肝、配图美、可读性高，高并发经典之作！', 1, 1, '', '{}', 0, '2023-01-13 19:15:57', '2023-04-15 15:05:22');
# INSERT INTO config
# (id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
# VALUES(2, 1, '加入社区2', 'https://imgs.hhui.top/forum/banner/01.png', 'https://hhui.top/', '', 2, 1, '', '{}', 0, '2023-01-13 19:15:57', '2023-01-13 19:15:57');
INSERT INTO config
(id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
VALUES(3, 4, '技术派上线啦！', '11', 'http://xuyifei.site/article/detail/15', '学编程，就上技术派?！', 1, 1, '2', '{}', 0, '2023-01-13 19:15:57', '2023-04-15 14:55:38');
# INSERT INTO config
# (id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
# VALUES(4, 4, 'Java进阶之路.pdf来了！', ' 2', 'https://paicoding.com/column/5/1', '学 Java，就认准二哥的 Java 进阶之路。第一版 PDF 开放下载了！技术派团队出品。', 2, 1, '2', '{}', 0, '2023-01-13 19:15:57', '2023-04-15 16:04:49');
# INSERT INTO config
# (id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
# VALUES(5, 6, 'JVM 核心手册', ' https://img14.360buyimg.com/ddimg/jfs/t1/184999/39/32111/443189/63c7fbbbF78e720ff/7e878308d3d27dff.png', 'https://paicoding.com/article/detail/151', '楼仔原创的 JVM 手册，带你成为 Java 高手！技术派团队出品。', 2, 1, '1', '{"visit":110252,"download":12121,"rate":"9.1"}', 0, '2023-01-13 19:15:58', '2023-04-15 21:23:09');
# INSERT INTO config
# (id, `type`, name, banner_url, jump_url, content, `rank`, status, tags, extra, deleted, create_time, update_time)
# VALUES(6, 6, 'Spring源码解析手册', 'https://img13.360buyimg.com/ddimg/jfs/t1/114223/5/31528/3308443/63c7f65eFdb3a20f2/91c8c191152d82c2.png', 'https://paicoding.com/article/detail/150', '楼仔原创的 Spring 源码解读手册，硬核，带你成为 Spring 高手！技术派团队出品。', 2, 1, '1', '{"visit":120248,"download":212103,"rate":"9.3"}', 0, '2023-01-13 19:15:58', '2023-04-15 21:23:09');


# update article set summary = '技术派（paicoding）是一个前后端分离的 Java 社区实战项目，基于 SpringBoot+MyBatis-Plus 实现，采用 Docker 容器化部署。包括前台社区系统和后台管理系统。前台社区系统包括社区首页、文章推荐、文章搜索、文章发布、文章详情、优质教程、个人中心等模块；后台管理系统包括文章管理、教程管理、统计报表、权限菜单管理、设置等模块。' where id = 1;

update article set summary = 'mybatis-plus配置' where id = 100;

# update article set summary = '你在分布式系统上工作吗？微服务，Web API，SOA，Web服务器，应用服务器，数据库服务器，缓存服务器，负载均衡器 - 如果这些描述了系统设计中的组件，那么答案是肯定的。分布式系统由许多计算机组成，这些计算机协调以实现共同的目标。

# 20多年前，Peter Deutsch和James Gosling定义了分布式计算的8个谬误。这些是许多开发人员对分布式系统做出的错误假设。从长远来看，这些通常被证明是错误的，导致难以修复错误。' where id = 101;



# update article set summary = '分布式的概念存在年头有点久了，在正式进入我们《分布式专栏》之前，感觉有必要来聊一聊，什么是分布式，分布式特点是什么，它又有哪些问题，在了解完这个概念之后，再去看它的架构设计，理论奠基可能帮助会更大' where id = 102;

