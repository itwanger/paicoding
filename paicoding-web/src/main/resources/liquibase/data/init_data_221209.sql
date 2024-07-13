-- 初始化类目
insert into `category` (`id`, `category_name`, `status`, `deleted`)
values ('1', '后端', '1', '0'),
       ('2', '前端', '1', '0'),
       ('3', '大数据', '1', '0'),
       ('4', 'Android', '1', '0'),
       ('5', 'IOS', '1', '0'),
       ('6', '人工智能', '1', '0'),
       ('7', '开发工具', '1', '0'),
       ('8', '代码人生', '1', '0'),
       ('9', '阅读', '1', '0'),
       ('10', '编程语言', '1', '0'),
       ('11', '数据结构与算法', '1', '0'),
       ('12', '领域知识', '1', '0'),
       ('13', '面试八股', '1', '0'),
       ('14', '数据库', '1', '0'),
       ('15', '运维', '1', '0');


-- 初始化标签
insert into `tag` (`id`, `tag_name`, `tag_type`, `category_id`, `status`, `deleted`)
values ('1', 'Java', '1', '10', '1', '0'),
       ('2', 'Go', '1', '10', '1', '0'),
       ('3', '算法', '1', '11', '1', '0'),
       ('4', 'Python', '1', '10', '1', '0'),
       ('5', 'Spring Boot', '1', '1', '1', '0'),
       ('6', '面试', '1', '1', '13', '0'),
       ('7', 'MySQL', '1', '1', '14', '0'),
       ('8', '数据库', '1', '1', '14', '0'),
       ('9', 'Spring', '1', '1', '1', '0'),
       ('10', '架构', '1', '1', '1', '0'),
       ('11', 'LeetCode', '1', '11', '1', '0'),
       ('12', 'Redis', '1', '14', '1', '0'),
       ('13', 'Linux', '1', '15', '1', '0'),
       ('14', 'JavaScript', '1', '10', '1', '0'),
       ('15', 'Vue.js', '1', '2', '1', '0'),
       ('16', 'React.js', '1', '2', '1', '0'),
       ('17', 'CSS', '1', '2', '1', '0'),
       ('18', 'TypeScript', '1', '10', '1', '0'),
       ('19', '后端', '1', '1', '1', '0'),
       ('20', 'Node.js', '1', '10', '1', '0'),
       ('21', '前端框架', '1', '2', '1', '0'),
       ('22', 'Webpack', '1', '2', '1', '0'),
       ('23', '架构', '1', '2', '1', '0'),
       ('24', '微信小程序', '1', '2', '1', '0'),
       ('25', 'GitHub', '1', '7', '1', '0'),
       ('26', 'Kotlin', '1', '10', '1', '0'),
       ('27', 'Flutter', '1', '5', '1', '0'),
       ('28', 'Android Jetpack', '1', '4', '1', '0'),
       ('29', 'APP', '1', '4', '1', '0'),
       ('30', 'Android Studio', '1', '4', '1', '0'),
       ('31', '源码', '1', '4', '1', '0'),
       ('32', '性能优化', '1', '4', '1', '0'),
       ('33', '面试', '1', '4', '1', '0'),
       ('34', '架构', '1', '4', '1', '0'),
       ('35', 'gradle', '1', '4', '1', '0'),
       ('36', '程序员', '1', '4', '1', '0'),
       ('37', 'Swift', '1', '5', '1', '0'),
       ('38', 'SwiftUI', '1', '5', '1', '0'),
       ('39', 'Flutter', '1', '5', '1', '0'),
       ('40', 'LeetCode', '1', '5', '1', '0'),
       ('41', 'Objective-C', '1', '5', '1', '0'),
       ('42', 'Mac', '1', '5', '1', '0'),
       ('43', '计算机视觉', '1', '5', '1', '0'),
       ('44', 'Apple', '1', '5', '1', '0'),
       ('45', '音视频开发', '1', '5', '1', '0'),
       ('46', '深度学习', '1', '6', '1', '0'),
       ('47', '机器学习', '1', '6', '1', '0'),
       ('48', 'PyTorch', '1', '6', '1', '0'),
       ('49', 'NLP', '1', '6', '1', '0'),
       ('50', '数据分析', '1', '6', '1', '0'),
       ('51', '神经网络', '1', '6', '1', '0'),
       ('52', 'TensorFlow', '1', '6', '1', '0'),
       ('53', '数据可视化', '1', '6', '1', '0'),
       ('54', '数据挖掘', '1', '6', '1', '0'),
       ('55', '开源', '1', '7', '1', '0'),
       ('56', 'Git', '1', '7', '1', '0'),
       ('57', 'Linux', '1', '7', '1', '0'),
       ('58', '测试', '1', '7', '1', '0'),
       ('59', '数据库', '1', '7', '1', '0'),
       ('60', 'JavaScript', '1', '7', '1', '0'),
       ('61', 'Unity3D', '1', '7', '1', '0'),
       ('62', 'Rust', '1', '7', '1', '0'),
       ('63', '大数据', '1', '7', '1', '0'),
       ('64', '架构', '1', '8', '1', '0'),
       ('65', '开源', '1', '8', '1', '0'),
       ('66', '面试', '1', '8', '1', '0'),
       ('67', '数据结构', '1', '8', '1', '0'),
       ('68', '云原生', '1', '9', '1', '0'),
       ('69', '笔记', '1', '9', '1', '0'),
       ('70', 'Serverless', '1', '9', '1', '0'),
       ('71', '容器', '1', '9', '1', '0'),
       ('72', '微服务', '1', '9', '1', '0'),
       ('73', '产品经理', '1', '9', '1', '0'),
       ('74', 'RocketMQ', '1', '9', '1', '0'),
       ('75', 'sqlite', '1', '3', '1', '0'),
       ('76', 'sql', '1', '3', '1', '0'),
       ('77', 'spark', '1', '3', '1', '0'),
       ('78', 'hive', '1', '3', '1', '0'),
       ('79', 'hbase', '1', '3', '1', '0'),
       ('80', 'hdfs', '1', '3', '1', '0'),
       ('81', 'hadoop', '1', '3', '1', '0'),
       ('82', 'rabbitmq', '1', '3', '1', '0'),
       ('83', 'postgresql', '1', '3', '1', '0'),
       ('84', '数据仓库', '1', '3', '1', '0'),
       ('85', 'oracle', '1', '3', '1', '0'),
       ('86', 'flink', '1', '3', '1', '0'),
       ('87', 'nosql', '1', '3', '1', '0'),
       ('88', 'eureka', '1', '3', '1', '0'),
       ('89', 'mongodb', '1', '3', '1', '0'),
       ('90', 'zookeeper', '1', '3', '1', '0'),
       ('91', 'elasticsearch', '1', '3', '1', '0'),
       ('92', 'kafka', '1', '3', '1', '0'),
       ('93', 'json', '1', '3', '1', '0'),
       ('94', '计算机网络', '1', '12', '1', '0'),
       ('95', 'JVM', '1', '13', '1', '0'),
       ('96', 'Java基础', '1', '13', '1', '0'),
       ('97', '操作系统', '1', '12', '1', '0');


-- 配置相关信息
insert into config(`type`,`name`,`banner_url`,`jump_url`,`content`,`rank`, `status`) values(1, '加入社区1', 'https://imgs.hhui.top/forum/banner/01.png', 'https://blog.csdn.net/qing_gee', '', 1, 1);
insert into config(`type`,`name`,`banner_url`,`jump_url`,`content`,`rank`, `status`) values(1, '加入社区2', 'https://imgs.hhui.top/forum/banner/01.png', 'https://hhui.top/', '', 2, 1);
insert into config(`type`,`name`,`banner_url`,`jump_url`,`content`,`rank`, `status`, `tags`) values(4, '社区上线公告', '', '', '技术社区正式上线啦！', 1, 1, '1,2,3');
insert into config(`type`,`name`,`banner_url`,`jump_url`,`content`,`rank`, `status`, `tags`) values(4, '二哥博客公告', '', 'https://blog.csdn.net/qing_gee/category_9264687.html', '戳这里，访问二哥的博客！', 2, 1, '1,2,3');

-- 初始化文章
-- fixme 下面这个文章后续使用论坛的介绍进行替换

INSERT INTO `user`
(id, third_account_id, `user_name`, `password`, login_type, deleted)
VALUES(1, 'a7cb7228-0f85-4dd5-845c-7c5df3746e92', 'admin', 'df3a4143b663a086d1c006c8084db1b1', 0, 0);

INSERT INTO user_info
(id, user_id, user_name, photo, `position`, company, profile, extend, deleted)
VALUES(1, 1, '管理员', 'https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/avatar/B0FBE96A0309EF08D329E782434894E9.jpg', 'java', 'xm', '码农', '', 0);



-- 准备数据
INSERT INTO column_info
(id, column_name, user_id, introduction, cover, state, publish_time, create_time, update_time)
VALUES(1, '小灰飞的专栏', 1, '这里是小灰飞的踩坑记录，欢迎关注', 'https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/cover/column-avatar1.png', 1, '2022-09-15 01:00:00', '2022-09-15 01:00:00', '2022-09-15 01:00:00');


-- 添加专栏文章

INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, status, deleted, create_time, update_time)
VALUES(100, 1, 1, 'Mybatis-Plus的安装配置', '配置安装', '', 'mybatis-plus配置', 1, 2, '', 1, 0, '2024-07-10 19:12:32', '2024-07-10 19:12:32');
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, status, deleted, create_time, update_time)
VALUES(101, 1, 1, 'JVM学习——初识JVM', '初识JVM', '', '# 1、初始JVM
## 1.1 什么是JVM
JVM 全称是 Java Virtual Machine，中文译名Java虚拟机。JVM 本质上是一个运行在计算机上的程序，他的职责是运行Java字节码文件。
Java源代码执行流程如下：', 1, 2, '', 1, 0, '2024-07-10 19:13:38', '2024-07-10 19:13:38');
INSERT INTO article
(id, user_id, article_type, title, short_title, picture, summary, category_id, source, source_url, status, deleted, create_time, update_time, offical_stat)
VALUES(102, 1, 1, 'JVM基本——JAVA虚拟机与字节码文件', 'JVM与字节码文件', '', 'JVM包括什么？字节码文件又是什么？', 1, 2, '', 1, 0, '2024-07-10 22:45:17', '2024-07-10 22:45:17', 1);

INSERT INTO article_tag
(article_id, tag_id, deleted, create_time, update_time)
VALUES(100, 1, 0, '2024-07-10 19:23:14', '2024-07-10 19:23:14');
INSERT INTO article_tag
(article_id, tag_id, deleted, create_time, update_time)
VALUES(101, 1, 0, '2024-07-10 19:23:14', '2024-07-10 19:23:14');
INSERT INTO article_tag
(article_id, tag_id, deleted, create_time, update_time)
VALUES(102, 1, 0, '2024-07-10 19:23:14', '2024-07-10 19:23:14');
INSERT INTO article_tag
(article_id, tag_id, deleted, create_time, update_time)
VALUES(14, 1, 0, '2024-07-10 19:23:14', '2024-07-10 19:23:14');
INSERT INTO article_tag
(article_id, tag_id, deleted, create_time, update_time)
VALUES(15, 97, 0, '2024-07-10 19:23:14', '2024-07-10 19:23:14');
INSERT INTO article_tag
(article_id, tag_id, deleted, create_time, update_time)
VALUES(16, 10, 0, '2024-07-10 19:23:14', '2024-07-10 19:23:14');



INSERT INTO article_detail
(article_id, version, content, deleted, create_time, update_time)
VALUES(100, 2, '
### 引入依赖

官网：
[MyBatis-Plus](https://baomidou.com/)

```xml
  <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-boot-starter</artifactId>
      <version>3.5.3.1</version>
  </dependency>
```

版本则看情况

### 如何使用

1. 编写与数据库对应的model/entity

```java
@Data
@Builder
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(updateStrategy=FieldStrategy.NOT_EMPTY, insertStrategy=FieldStrategy.NOT_EMPTY)
    private String userName;

    @TableField(updateStrategy=FieldStrategy.NOT_EMPTY, insertStrategy=FieldStrategy.NOT_EMPTY)
    private String userAccount;

    @TableField(updateStrategy=FieldStrategy.NOT_EMPTY, insertStrategy=FieldStrategy.NOT_EMPTY)
    private String password;

    @TableField(updateStrategy=FieldStrategy.NOT_EMPTY, insertStrategy=FieldStrategy.NOT_EMPTY)
    private String userEmail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

```

   - `@TableName`指定对应的表名
   - `@TableId`指明是主键，后面的参数则表明主键id自增，这样在新插入数据时就不需要手动给他赋值了
   - `@TableField`指明了字段对应数据库的哪个字段，可以不写，那么默认就是从数据库的蛇形命名到java中的驼峰命名对应。
     - 其中的`updateStrategy`和`insertStrategy`分别指定了更新和插入值时的一些限制，具体可见：

[注解 | MyBatis-Plus](https://baomidou.com/pages/223848/#tablefield)

      - `fill`与插入或更新字段值时所做的自动填充策略

2. 编写与model对应的mapper

```java
package com.xyf.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyf.blogbackend.models.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

   - 这里需要注意的是如果给`Mapper`添加了`@Mapper`注解，那么在springboot的启动类上方就不需要加`@MapperScan`注解了，否则就要加

3. 在需要进行数据库操作的类中使用

```java
@SpringBootTest
class BlogBackendApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testMybatisAutoUpdateCreateTime() {
        User user = User.builder().userAccount("test_account").userEmail("test_email").password("test_password").userName("test_name").build();
        userMapper.insert(user);
    }

}
```

   - 关键是使用`@Autowired`引入就好了




', 0, '2024-07-08 19:12:32', '2024-07-08 19:23:14');

INSERT INTO article_detail
(article_id, version, content, deleted, create_time, update_time)
VALUES(101, 2, '
# 1、初识JVM

## 1.1 什么是JVM

JVM 全称是 Java Virtual Machine，中文译名Java虚拟机。JVM 本质上是一个运行在计算机上的程序，他的职责是运行Java字节码文件。
Java源代码执行流程如下：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/378a97d805347c7a94f4e8955d7b933a.png)
分为三个步骤：
1、编写Java源代码文件。
2、使用Java编译器（javac命令）将源代码编译成Java字节码文件。
3、使用Java虚拟机加载并运行Java字节码文件，此时会启动一个新的进程。

## 1.2 JVM的功能

- 1 - 解释和运行
- 2 - 内存管理
- 3 - 即时编译

### 1.2.1 解释和运行

对字节码文件中的指令，实时的解释成机器码，让计算机执行。
字节码文件中包含了字节码指令，计算器无法直接执行，Java虚拟机会将字节码文件中的字节码指令实时地解释成机器码，机器码是计算机可以运行的指令。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/2a82bb72c6868bc1a6af3c11257b946c.png)

### 1.2.2 内存管理

- 自动为对象、方法等分配内存
- 自动的垃圾回收机制，回收不再使用的对象

Java虚拟机会帮助程序员为对象分配内存，同时将不用的对象使用垃圾回收器回收掉，这是对比C和C++这些语言的一个优势。在C/C++语言中，对象的回收需要程序员手动去编写代码完成，如果遗漏了这段删除对象的代码，这个对象就会永远占用内存空间，不会再回收。所以JVM的这个功能降低了程序员编写代码的难度。

### 1.2.3 即时编译

对热点代码进行优化，提升执行效率。即时编译可以说是提升Java程序性能最核心的手段。

#### Java性能低的主要原因和跨平台特性

Java语言如果不做任何的优化，性能其实是不如C和C++语言的。主要原因是：
在程序运行过程中，Java虚拟机需要将字节码指令实时地解释成计算机能识别的机器码，这个过程在运行时可能会反复地执行，所以效率较低。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/2a82bb72c6868bc1a6af3c11257b946c.png)

C和C++语言在执行过程中，只需要将源代码编译成可执行文件，就包含了计算机能识别的机器码，无需在运行过程中再实时地解释，所以性能较高。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/56579da5307f70965964599e82251a7c.png)

Java为什么要选择一条执行效率比较低的方式呢？主要是为了实现跨平台的特性。Java的字节码指令，如果希望在不同平台（操作系统+硬件架构），比如在windows或者linux上运行。可以使用同一份字节码指令，交给windows和linux上的Java虚拟机进行解释，这样就可以获得不同平台上的机器码了。这样就实现了Write Once，Run Anywhere 编写一次，到处运行 的目标。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/a14f500a7d347cc06544bbf98f1c5330.png)

但是C/C++语言，如果要让程序在不同平台上运行，就需要将一份源代码在不同平台上分别进行编译，相对来说比较麻烦。
再回到即时编译，在JDK1.1的版本中就推出了即时编译去优化对应的性能。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/b21254144e39d8a32784b6c6b4bb8f8d.png)

虚拟机在运行过程中如果发现某一个方法甚至是循环是热点代码（被非常高频调用），即时编译器会优化这段代码并将优化后的机器码保存在内存中，如果第二次再去执行这段代码。Java虚拟机会将机器码从内存中取出来直接进行调用。这样节省了一次解释的步骤，同时执行的是优化后的代码，效率较高。
Java通过即时编译器获得了接近C/C++语言的性能，在某些特定的场景下甚至可以实现超越。

## 1.3 常见的JVM

### 1.3.1 Java虚拟机规范

- 《Java虚拟机规范》由Oracle制定，内容主要包含了Java虚拟机在设计和实现时需要遵守的规范，主要包含class字节码文件的定义、类和接口的加载和初始化、指令集等内容。
- 《Java虚拟机规范》是对虚拟机设计的要求，而不是对Java设计的要求，也就是说虚拟机可以运行在其他的语言比如Groovy、Scala生成的class字节码文件之上。
- 官网地址：https://docs.oracle.com/javase/specs/index.html

### 1.3.2 Java虚拟机规范

平时我们最常用的，就是Hotspot虚拟机。

| **名称**                   | **作者** | **支持版本**              | **社区活跃度（github star）** | **特性**                                                     | **适用场景**                         |
| -------------------------- | -------- | ------------------------- | ----------------------------- | ------------------------------------------------------------ | ------------------------------------ |
| HotSpot (Oracle JDK版)     | Oracle   | 所有版本                  | 高(闭源)                      | 使用最广泛，稳定可靠，社区活跃JIT支持Oracle JDK默认虚拟机    | 默认                                 |
| HotSpot (Open JDK版)       | Oracle   | 所有版本                  | 中(16.1k)                     | 同上开源，Open JDK默认虚拟机                                 | 默认对JDK有二次开发需求              |
| GraalVM                    | Oracle   | 11, 17,19企业版支持8      | 高（18.7k）                   | 多语言支持高性能、JIT、AOT支持                               | 微服务、云原生架构需要多语言混合编程 |
| Dragonwell JDK龙井         | Alibaba  | 标准版 8,11,17扩展版11,17 | 低(3.9k)                      | 基于OpenJDK的增强高性能、bug修复、安全性提升JWarmup、ElasticHeap、Wisp特性支持 | 电商、物流、金融领域对性能要求比较高 |
| Eclipse OpenJ9 (原 IBM J9) | IBM      | 8,11,17,19,20             | 低(3.1k)                      | 高性能、可扩展JIT、AOT特性支持                               | 微服务、云原生架构                   |

### 1.3.3 HotSpot的发展历程

##### 初出茅庐 - 1999年4月

源自1997年收购的SmallTalk语言的虚拟机，HotSpot虚拟机初次在JDK中使用。在JDK1.2中作为附加功能存在，
JDK1.3之后作为默认的虚拟机。

##### 野蛮生长 - 2006年12月

JDK 6发布，并在虚拟机层面做了大量的优化，这些优化对后续虚拟机的发展产生了深远的影响。

##### 稳步前进 - 2009-2013

JDK7中首次推出了G1垃圾收集器。收购了Sun公司之后，吸纳了JRockIt虚拟机的一些设计思想，JDK8中引入了JMC等工具，去除了永久代。

##### 百家争鸣 - 2018-2019

JDK11优化了G1垃圾收集器的性能,同时推出了ZGC新一代的垃圾回收器，JDK12推出Shenan-doah垃圾回收器。

##### 拥抱云原生 - 2019-至今

以Hotspot为基础的GraalVM虚拟机诞生，不仅让解决了单体应用中多语言整合的难题，同时也提升了这些语言运行时的效率。极高的性能、极快的启动速度也更适用于当下的云原生架构。
', 0, '2024-07-10 19:13:38', '2024-07-10 19:23:35');

INSERT INTO article_detail
(article_id, version, content, deleted, create_time, update_time)
VALUES(102, 2, '## 2 Java虚拟机的组成

Java虚拟机主要分为以下几个组成部分：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/e2e15cec89b785c02b8d465e722aab7c.png)


- 类加载子系统：核心组件类加载器，负责将字节码文件中的内容加载到内存中。
- 运行时数据区：JVM管理的内存，创建出来的对象、类的信息等等内容都会放在这块区域中。
- 执行引擎：包含了即时编译器、解释器、垃圾回收器，执行引擎使用解释器将字节码指令解释成机器码，使用即时编译器优化性能，使用垃圾回收器回收不再使用的对象。
- 本地接口：调用本地使用C/C++编译好的方法，本地方法在Java中声明时，都会带上native关键字，如下图所示。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/8bf944342c3180567a595506c6785602.png)


## 3 字节码文件的组成

### 3.1 以正确的姿势打开文件

字节码文件中保存了源代码编译之后的内容，以二进制的方式存储，无法直接用记事本打开阅读。
通过NotePad++使用十六进制插件查看class文件：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/7e119baa0eaf0bedced10d76323f7784.png)

无法解读出文件里包含的内容，推荐使用 jclasslib工具查看字节码文件。 Github地址： https://github.com/ingokegel/jclasslib

![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/717347ff910bd7744087566ebeec4465.png)


### 3.2 字节码文件的组成

字节码文件总共可以分为以下几个部分：

- **基础信息**：魔数、字节码文件对应的Java版本号、访问标识(public final等等)、父类和接口信息
- **常量池： **保存了字符串常量、类或接口名、字段名，主要在字节码指令中使用
- **字段：** 当前类或接口声明的字段信息
- **方法： **当前类或接口声明的方法信息，核心内容为方法的字节码指令
- **属性： **类的属性，比如源码的文件名、内部类的列表等

#### 3.2.1 基本信息

基本信息包含了jclasslib中能看到的两块内容：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/63b3035c8c2cd03be9fe696066d9a9e2.png)


##### Magic魔数

每个Java字节码文件的前四个字节是固定的，用16进制表示就是0xcafebabe。文件是无法通过文件扩展名来确定文件类型的，文件扩展名可以随意修改不影响文件的内容。软件会使用文件的头几个字节（文件头）去校验文件的类型，如果软件不支持该种类型就会出错。
比如常见的文件格式校验方式如下：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/2096d15baba88e02c2606fda429ce715.png)

Java字节码文件中，将文件头称为magic魔数。Java虚拟机会校验字节码文件的前四个字节是不是0xcafebabe，如果不是，该字节码文件就无法正常使用，Java虚拟机会抛出对应的错误。

##### 主副版本号

主副版本号指的是编译字节码文件时使用的JDK版本号，主版本号用来标识大版本号，JDK1.0-1.1使用了45.0-45.3，JDK1.2是46之后每升级一个大版本就加1；副版本号是当主版本号相同时作为区分不同版本的标识，一般只需要关心主版本号。
1.2之后大版本号计算方法就是 : 主版本号 – 44，比如主版本号52就是JDK8。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/f78bd566b2c88815ab19253e560e2817.png)

版本号的作用主要是判断当前字节码的版本和运行时的JDK是否兼容。如果使用较低版本的JDK去运行较高版本JDK的字节码文件，无法使用会显示错误
有两种方案：
1.升级JDK版本，将图中使用的JDK6升级至JDK8即可正常运行，容易引发其他的兼容性问题，并且需要大量的测试。
2.将第三方依赖的版本号降低或者更换依赖，以满足JDK版本的要求。建议使用这种方案

##### 其他基础信息

其他基础信息包括访问标识、类和接口索引，如下：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/88da89e7709e87bd76b7540e0030251a.png)


#### 3.2.2 常量池

字节码文件中常量池的作用：避免相同的内容重复定义，节省空间。如下图，常量池中定义了一个字符串，字符串的字面量值为123。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/2914192760d83b9d86f6f1bf9a9d79ad.png)

比如在代码中，编写了两个相同的字符串“我爱北京天安门”，字节码文件甚至将来在内存中使用时其实只需要保存一份，此时就可以将这个字符串以及字符串里边包含的字面量，放入常量池中以达到节省空间的作用。

```
String str1 = "我爱北京天安门";
String str2 = "我爱北京天安门";
```

常量池中的数据都有一个编号，编号从1开始。比如“我爱北京天安门”这个字符串，在常量池中的编号就是7。在字段或者字节码指令中通过编号7可以快速的找到这个字符串。
字节码指令中通过编号引用到常量池的过程称之为符号引用。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/182824136eec5204c355730a80edefb5.png)


#### 3.2.3 字段

字段中存放的是当前类或接口声明的字段信息。
如下图中，定义了两个字段a1和a2，这两个字段就会出现在字段这部分内容中。同时还包含字段的名字、描述符（字段的类型）、访问标识（public/private static final等）。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/afe53c01bc789b38da5dfd5605863860.png)


#### 3.2.4 方法

字节码中的方法区域是存放**字节码指令**的核心位置，字节码指令的内容存放在方法的Code属性中。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/fba1504737081e51f24beb41f267579e.png)

通过分析方法的字节码指令，可以清楚地了解一个方法到底是如何执行的。先来看如下案例：

```
int i = 0;
int j = i + 1;
```

这段代码编译成字节码指令之后是如下内容：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/000d65dd9aef2b3af9e1124a5de5c3fb.png)

要理解这段字节码指令是如何执行的，我们需要先理解两块内存区域：操作数栈和局部变量表。
**操作数栈**是用来存放临时数据的内容，是一个栈式的结构，先进后出。
**局部变量表**是存放方法中的局部变量，包含方法的参数、方法中定义的局部变量，在编译期就已经可以确定方法有多少个局部变量。
1、iconst_0，将常量0放入操作数栈。此时栈上只有0。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/eefd483b3fb4c0dd4c82d163bb1551c1.png)

2、istore_1会从操作数栈中，将栈顶的元素弹出来，此时0会被弹出，放入局部变量表的1号位置。局部变量表中的1号位置，在编译时就已经确定是局部变量i使用的位置。完成了对局部变量i的赋值操作。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/a44f6aa0a6c6b863c9e7c5cdb5a96584.png)

3、iload_1将局部变量表1号位置的数据放入操作数栈中，此时栈中会放入0。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/778dd2dd513630fb7ec3e68fb4f08b2a.png)

4、iconst_1会将常量1放入操作数栈中。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/0fb353711da21cd4122ab18aa2dc463b.png)

5、iadd会将操作数栈顶部的两个数据相加，现在操作数栈上有两个数0和1，相加之后结果为1放入操作数栈中，此时栈上只有一个数也就是相加的结果1。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/78bc101711b04573516cbb74b759b2c2.png)

6、istore_2从操作数栈中将1弹出，并放入局部变量表的2号位置，2号位置是j在使用。完成了对局部变量j的赋值操作。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/f4eb3313b48d00a118adb3cfcad64386.png)

7、return语句执行，方法结束并返回。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/d941f189636fd844212be8429a733ccf.png)

同理，同学们可以自行分析下i++和++i的字节码指令执行的步骤。
i++的字节码指令如下，其中iinc 1 by 1指令指的是将局部变量表1号位置增加1，其实就实现了i++的操作。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/3eabbe2fb68d584a147916714d4a9b33.png)

而++i只是对两个字节码指令的顺序进行了更改：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/24d6938804c52a1928800229d792cbcd.png)


##### 面试题：

问：int i = 0; i = i++; 最终i的值是多少？
答：答案是0，我通过分析字节码指令发现，i++先把0取出来放入临时的操作数栈中，
接下来对i进行加1，i变成了1，最后再将之前保存的临时值0放入i，最后i就变成了0。

####  2.2.2.5 属性

属性主要指的是类的属性，比如源码的文件名、内部类的列表等。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/da80d7543580cc80f7952cf6708e507f.png)


### 2.2.3 玩转字节码常用工具

#### 2.2.3.1 javap

javap是JDK自带的反编译工具，可以通过控制台查看字节码文件的内容。适合在服务器上查看字节码文件内容。
直接输入javap查看所有参数。输入javap -v 字节码文件名称 查看具体的字节码信息。如果jar包需要先使用 jar –xvf 命令解压。

#### 2.2.3.2 jclasslib插件

jclasslib也有Idea插件版本，建议开发时使用Idea插件版本，可以在代码编译之后实时看到字节码文件内容。
安装方式：
1、打开idea的插件页面，搜索jclasslib
2、选中要查看的源代码文件，选择 视图(View) - Show Bytecode With Jclasslib
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/1310c6b7b98e7e43101f586476d693ab.png)

右侧会展示对应源代码编译后的字节码文件内容：

tips:
1、一定要选择文件再点击视图(view)菜单，否则菜单项不会出现。
2、文件修改后一定要重新编译之后，再点击刷新按钮。

#### 2.2.3.3 Arthas

Arthas 是一款线上监控诊断产品，通过全局视角实时查看应用 load、内存、gc、线程的状态信息，并能在不修改应用代码的情况下，对业务问题进行诊断，大大提升线上问题排查效率。 官网：https://arthas.aliyun.com/doc/ Arthas的功能列表如下：
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/9d85b83fceb86113b11dd56fd439fa76.png)

**安装方法：**
1、使用java -jar arthas-boot.jar  启动程序。
2、输入需要Arthas监控的进程id。
![](https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/0725e762274911d14b5a5de407a496eb.png)

4、输入命令即可使用。
**dump**
命令详解：https://arthas.aliyun.com/doc/dump.html
dump命令可以将字节码文件保存到本地，如下将java.lang.String 的字节码文件保存到了/tmp/output目录下：

```
$ dump -d /tmp/output java.lang.String

 HASHCODE  CLASSLOADER  LOCATION
 null                   /tmp/output/java/lang/String.class
Affect(row-cnt:1) cost in 138 ms.
```

**jad**
命令详解：https://arthas.aliyun.com/doc/jad.html
jad命令可以将类的字节码文件进行反编译成源代码，用于确认服务器上的字节码文件是否是最新的，如下将demo.MathGame的源代码进行了显示。

```
$ jad --source-only demo.MathGame
/*
 * Decompiled with CFR 0_132.
 */
package demo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MathGame {
    private static Random random = new Random();
    public int illegalArgumentCount = 0;
...
```

## ', 0, '2024-07-10 16:13:17', '2024-07-10 16:13:17');

-- 将文章绑定到专栏

# INSERT INTO column_article
# (column_id, article_id, `section`)
# VALUES(1, 100, 1);

INSERT INTO column_article
(column_id, article_id, `section`)
VALUES(1, 101, 1);

INSERT INTO column_article
(column_id, article_id, `section`)
VALUES(1, 102, 2);



-- 通用字典数据
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ConfigType','1','首页Banner',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ConfigType','2','侧边Banner',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ConfigType','3','广告Banner',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ConfigType','4','公告',4);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ConfigType','5','教程',5);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ConfigType','6','电子书',6);

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('PushStatus','0','未发布',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('PushStatus','1','已发布',2);

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ArticleTag','1','热门',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ArticleTag','2','官方',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ArticleTag','3','推荐',3);

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ArticleSource','1','转载',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ArticleSource','2','原创',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ArticleSource','3','翻译',3);

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('OfficalStatus','0','非官方',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('OfficalStatus','1','官方',2);

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ToppingStatus','0','不置顶',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ToppingStatus','1','置顶',2);

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('CreamStatus','0','不加精',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('CreamStatus','1','加精',2);