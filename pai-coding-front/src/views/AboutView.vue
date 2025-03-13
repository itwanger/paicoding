<template>
  <HeaderBar></HeaderBar>
  <div class="home article-detail flex flex-col">
    <div class="col-body pg-2-article flex" id="article-detail-body-div">
      <div class="com-3-layout" >
        <div class="self-info">

          <!-- 正文 -->
          <!-- 文章内容 -->
          <section class="article-info-wrap com-2-panel col-2-article J-articlePanel">
            <!-- 关联推荐 -->
            <h4 class="correlation-article-title">关于作者</h4>
            <el-divider></el-divider>
            <el-card class="mb-2">
              <template #header>基本信息</template>
              <template #default>
                <p class="flex m-2">
                  <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>作者：</b>许逸飞</span>
                  <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>性别：</b>♂</span>
                  <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>年龄：</b>23</span>
                </p>
                <p class="flex m-2">
                  <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>邮箱：</b>857998989@qq.com</span>
                  <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>籍贯：</b>河北</span>
                </p>
              </template>
            </el-card>
            <el-card class="mb-2">
              <template #header>教育背景</template>
              <template #default>
                <div>
                  <h2 class="text-blue-400"> 本科：2019.09-2023.06 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>学校：</b>华中科技大学</span>
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>学院：</b>电子信息与通信学院</span>
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>专业：</b>电子信息工程</span>
                  </p>
                </div>
                <div>
                  <h2 class="text-blue-400"> 研究生：2023.06 至今 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>学校：</b>南京大学</span>
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>学院：</b>计算机学院</span>
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>专业：</b>计算机科学与技术</span>
                  </p>
                </div>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>获奖/荣誉</template>
              <template #default>
                <div>
<!--                  奖项-->
                  <h2 class="text-blue-400"> 所获奖项 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>2020-2021学年度本专科生<b>国家奖学金</b></span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>2021-2022学年度本专科生<b>国家奖学金</b></span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>美国大学生数学建模竞赛F(Finalist)奖（特等奖提名）</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>蓝桥杯省赛二等奖</span>
                  </p>
<!--                  荣誉-->
                  <h2 class="text-blue-400"> 所获荣誉 </h2>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>南京大学硕士学业一等奖学金</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>华中科技大学2023届<b>优秀毕业生</b></span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon>华中科技大学荣誉学士学位</span>
                  </p>
                </div>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>技能专长</template>
              <template #default>
                <div>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>Java基础：</b>熟悉Java基础知识，具备良好编码能力，能熟练应用常见的设计模式；熟练使用集合等基础框架</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>MySQL：</b>熟练掌握MySQL，对锁机制、索引有深入了解</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>Redis：</b>熟练使用Redis及其底层原理【底层数据结构、数据持久化机制、多种分布式集群实现方式】</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>框架：</b>会使用SpringMVC、SpringBoot、MyBatis等框架</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>JVM：</b>初步理解JVM原理【类加载机制、内存结构、垃圾回收机制等】</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>JUC：</b>熟悉Java多线程与并发底层原理【volatile、synchronized、ReentrantLock】</span>
                  </p>
                  <p class="m-1">
                    <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>MQ：</b>了解消息队列MQ，有使用RabbitMQ经验</span>
                  </p>
                  <p class="m-1">
                      <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>其他：</b>有使用前端框架经验【Vue、React】，了解前后端分离开发模式；掌握版本控制工具Git、Docker的基本操作</span>
                  </p>
                </div>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>实习经历</template>
              <template #default>
                <el-collapse>
                  <el-collapse-item title="小红书 引擎架构 平台化开发">
                    <template #default>
                      <div>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>实习时间：</b>2024.12 —— 2025.03</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>⼯作核⼼：</b>搭建数据⽣产与消费侧的通⽤任务平台，为开发⼈员提供任务作业的提交与管理服务。主要技术栈为 SpringBoot、MySQL，使⽤CI/CD流程集成部署</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;参与设计⽤于⽣成任务作业的DSL配置与图化开发流程，使开发者能通过简易的配置⽣成任务作业，为每⽇近100亿的离线数据与50亿量级的实时消息提供⾼效可靠的数据的同步与索引构建服务</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;负责开发作业的监控模块，通过监控流批作业的运⾏状态，维护⽇均2000+⼤数据作业的运⾏状态，并在出现异常时及时告警</span>
                        </p>
                        <p class="m-1 ml-2">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;升级作业的配置版本管理功能，为开发⼈员提供多配置的回滚及对⽐功能</span>
                        </p>
                      </div>
                    </template>
                  </el-collapse-item>
                </el-collapse>
              </template>
            </el-card>
            <el-card class="mt-2">
              <template #header>项目经验</template>
              <template #default>
                <el-collapse>
                  <el-collapse-item title="编程汇：前后端分离的社区项目、包括前端PC和管理后台">
                    <template #default>
                      <div>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>技术栈：</b>SpringBoot、MyBatis-Plus、MySQL、Redis、RabbitMQ、Docker</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>项目描述：</b>编程汇是一个前后端分离的社区项目、包括前端PC和管理后台，用于帮助开发者成长的社区，面向互联网技术人的内容分享平台</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>责任描述：</b>作为核心研发人员，主要负责登录认证、消息通知以及文章模块的开发工作</span>
                        </p>
                        <p class="m-1">
                          <span><el-icon size="13"><CaretRight /></el-icon><b>核心技术：</b></span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;将⽤⼾相关动态消息的处理机制进⾏异步优化，在⾼负载情况下的M<b>最⼤响应时间降低33%</b></span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;通过<b>缓存+会话session</b>实现计数统计与在线⼈数实时展⽰，使系统的<b>平均响应速度提升了20%</b></span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;基于ThreadLocal在封装线程隔离的全局上下⽂，减少⽤⼾信息的数据库查询次数</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;针对热点⽂章进⾏<b>动态缓存</b>，将访问⾼频⽂章的响应时⻓从1000ms提升⾄700ms，QPS提升25%</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;通过分析系统的响应报告，进⾏简单的JVM参数调整，提升了系统的响应性能</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon>&nbsp;&nbsp;通过验证码和前端保持半⻓链接映射关系，当⽤⼾扫码关注公众号并输⼊验证码后，发起回调，识别⽤⼾信息并找到对应半⻓链接，实现系统⾃动登录</span>
                        </p>
                        <p class="m-1">
                          <span><el-icon size="13"><CaretRight /></el-icon><b>项目链接：</b><a href="http://www.xuyifei.site"><u>就是本项目</u></a></span>
                        </p>
                      </div>
                    </template>
                  </el-collapse-item>
                </el-collapse>
                <el-collapse>
                  <el-collapse-item title="SimpleDB：手搓的简易版MySQL数据库">
                    <template #default>
                      <div>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>项目描述：</b>SimpleDB致力于实现一个简易版本MySQL数据库（基于InnoDB引擎）。该相项目涵盖了数据存储、事务管理、MVCC和索引构建等核心数据库功能，还模拟实现日志管理和事务状态查询等特性</span>
                        </p>
                        <p class="m-1">
                          <span class="mr-3"><el-icon size="13"><CaretRight /></el-icon><b>责任描述：</b>独立开发</span>
                        </p>
                        <p class="m-1">
                          <span><el-icon size="13"><CaretRight /></el-icon><b>核心技术：</b></span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon><b>&nbsp;&nbsp;日志管理：</b>引入数据库日志管理机制，通过日志保障数据一致性，实现故障恢复功能，强化数据的安全性和稳定性</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon><b>&nbsp;&nbsp;MVCC与锁协议：</b>基于两阶段锁（2PL）协议与MVCC实现事务的 读提交、可重复读 隔离级别，并解决了<b>版本跳跃</b>问题</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon><b>&nbsp;&nbsp;死锁检测与预防：</b>通过维护一个依赖等待图来监控并避免系统中出现死锁</span>
                        </p>
                        <p class="m-1 ml-4">
                          <span class="mr-3"><el-icon size="11"><InfoFilled /></el-icon><b>&nbsp;&nbsp;索引与表管理：</b>实现<b>基于B+树的聚簇索引</b>，支持高效的索引查询；并构建表管理器，负责管理表结构和字段信息、解析SQL语句</span>
                        </p>
                        <p class="m-1">
                          <span><el-icon size="13"><CaretRight /></el-icon><b>项目链接：</b><a href="https://github.com/XuYi-fei/SimpleDB"><u>https://github.com/XuYi-fei/SimpleDB</u></a></span>
                        </p>
                      </div>
                    </template>
                  </el-collapse-item>
                </el-collapse>
              </template>
            </el-card>
          </section>
        </div>

      </div>
<!--   右侧的图片   -->
      <SideImage></SideImage>
    </div>

    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<style>

.home{
  min-height: calc(100vh - var(--header-height));
  display: flex;
  align-items: center;
  width: 100%;
}

.photos{
  max-width: 200px;
}

@media (max-width: 768px) {
  .photos{
    display: none;
  }
}

.self-info{
  max-width: 900px;
}

</style>
<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import { CaretRight, InfoFilled } from '@element-plus/icons-vue'
import SideImage from '@/components/layout/SideImage.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import {onMounted, provide, ref} from 'vue'

import {useGlobalStore} from "@/stores/global";
import {doGet} from "@/http/BackendRequests";
import type {CommonResponse} from "@/http/ResponseTypes/CommonResponseType";
import {GLOBAL_INFO_URL} from "@/http/URL";

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

const globalStore = useGlobalStore()
// 获取登录信息
onMounted(async () => {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
      .then((res) => {
        globalStore.setGlobal(res.data.global)
      })
})
</script>
