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
            <h4 class="correlation-article-title">后续更新计划</h4>
            <el-divider></el-divider>
            <el-card>
              <MdPreview :model-value="planText"></MdPreview>
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
import '@/assets/md-preview.css'
import SideImage from '@/components/layout/SideImage.vue'
import { MdPreview } from 'md-editor-v3'
import {onMounted, provide, ref} from 'vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import type {CommonResponse} from "@/http/ResponseTypes/CommonResponseType";
import {GLOBAL_INFO_URL} from "@/http/URL";
import {doGet} from "@/http/BackendRequests";
import {messageTip} from "@/util/utils";
import {useGlobalStore} from "@/stores/global";

const globalStore = useGlobalStore()
// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

// 获取登录信息
onMounted(async () => {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
      .then((res) => {
        globalStore.setGlobal(res.data.global)
      })
})

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

const planText = ref('### 系统更新\n' +
    '\n' +
    '> 更新时间：2025/03/13\n' +
    '\n' +
    '#### bug修复\n' +
    '\n' +
    '- [x] 修复了大模型对话时输入框被锁定的bug（配了https后新出现的问题）\n' +
    '- [x] 修复了更新文章时忘记删redis导致读到的是旧信息的问题\n' +
    '\n' +
    '#### 其他修改\n' +
    '\n' +
    '- [x] 更新「关于作者」页\n' +
    '\n' +
    '> 更新时间：2024/11/2\n' +
    '\n' +
    '#### bug修复\n' +
    '\n' +
    '- [x] 修复了修改用户信息时没有删redis导致读到的是旧信息的问题\n' +
    '\n' +
    '#### 新增功能\n' +
    '\n' +
    '- [x] 将rabbitmq处理点赞消息的机制的实现使用spring-amqp实现\n' +
    '- [x] 增加测试专用的接口\n' +
    '\n' +
    '### 历史更新\n' +
    '\n' +
    '> 更新时间：2024/10/29\n' +
    '\n' +
    '#### bug修复\n' +
    '\n' +
    '- [x] 获取验证码的逻辑是异步的，会导致线程私有的ReqInfo初始化后可能无法在后面访问到，因此将该映射器排除出拦截器的逻辑\n' +
    '- [x] 修复了某些机器或浏览器下请求不会携带cookie导致无法自动登录\n' +
    '- [x] 修复缓存层中redis中保存中文字符的乱码问题\n' +
    '- [x] 修复前端点击页面时的跳转问题\n' +
    '- [x] 修复了过滤器放行OPTIONS请求导致线程上下文不存在，从而拦截器中报错的问题\n' +
    '- [x] 更新pv、uv计数时不使用异步操作，原因是这里需要使用到ThreadLocal中的ReqInfoContext\n' +
    '\n' +
    '#### 新增功能\n' +
    '\n' +
    '- [x] 对文章的获取流程增加了缓存机制，对文章-专栏关系、文章作者及文章信息缓存到redis\n' +
    '- [x] 将每次请求request_count的统计改为redis统计，减少数据库压力\n' +
    '\n' +
    '> 更新时间：2024/10/21\n' +
    '\n' +
    '#### bug修复\n' +
    '\n' +
    '- [x] 修复了关于页与更新计划页底部不显示站点信息的问题\n' +
    '- [x] 修复了有关pv统计的问题\n' +
    '- [x] 修复了首页下方页面切换时的显示bug\n' +
    '\n' +
    '#### 架构调整\n' +
    '\n' +
    '- [x] 将统计信息的执行修改为异步执行\n' +
    '- [x] 实现了不同策略的在线人数统计，可以通过online.statistics.type配置来切换\n' +
    '- [x] 将前后端两个项目合并至同一仓库\n' +
    '\n' +
    '> 更新时间：2024/08/22\n' +
    '\n' +
    '#### bug修复\n' +
    '\n' +
    '- [x] 修复了专栏页教程数过多时的显示bug\n' +
    '\n' +
    '> 更新时间：2024/08/02\n' +
    '\n' +
    '#### 新增功能\n' +
    '\n' +
    '- [x] 添加了用于后续增加各种功能的工具页面\n' +
    '\n' +
    '\n' +
    '\n' +
    '> 更新时间：2024/07/16\n' +
    '\n' +
    '#### bug修复\n' +
    '\n' +
    '- [x] 修复了文章详情页点赞、收藏数目显示bug\n' +
    '- [x] 修复了部分首页与专栏详情页的tag显示bug\n' +
    '\n' +
    '\n' +
    '\n' +
    '> 更新时间：2024/07/15\n' +
    '\n' +
    '#### 新增功能\n' +
    '\n' +
    '- [x] 文章详情页和专栏详情页中文章基本信息中额外添加了文章的tag信息\n' +
    '\n' +
    '\n' +
    '\n' +
    '> 更新时间：2024/07/13\n' +
    '\n' +
    '**bug修复**\n' +
    '\n' +
    '- [x] 修复在关于页面&更新计划页面无法登录的bug\n' +
    '\n' +
    '**界面修复**\n' +
    '\n' +
    '- [x] 修复在专栏页中如果标题过长时布局出错的问题\n' +
    '- [x] 修复了主页中文章标题过长以及tag数目过多时显示超出规定范围的问题。转而使用省略号代替更多的显示\n' +
    '\n' +
    '---\n' +
    '\n' +
    '> 更新时间：2024/07/12\n' +
    '\n' +
    '#### 新增功能\n' +
    '\n' +
    '- [x] 接入AI聊天功能（目前只支持讯飞星火默认的lite模型，毕竟免费）\n' +
    '\n' +
    '---\n' +
    '\n' +
    '> 日期：2024/07/10\n' +
    '\n' +
    '**新增页面**\n' +
    '\n' +
    '- [x] **首页显示推荐文章**\n' +
    '- [x] **首页显示当前分类下的文章**\n' +
    '- [x] **专栏文章页面**\n' +
    '- [x] **用户详情页面**\n' +
    '- [x] **文章新增/编辑页面**\n' +
    '- [x] **各类通知信息页面**\n' +
    '- [x] **作者的关于页面**\n' +
    '- [x] **更新计划页面**\n' +
    '\n' +
    '**新增功能**\n' +
    '\n' +
    '- [x] **用户更新个人基本信息**\n' +
    '- [x] **用户查看各种通知信息（点赞、收藏、关注等）**\n' +
    '- [x] **文章的新增和编辑**\n' +
    '- [x] **文章的点赞、收藏、评论**\n' +
    '- [x] **接入AI聊天功能**\n' +
    '\n' +
    '**bug修复**\n' +
    '\n' +
    '- [x] 修复了专栏首页中当浏览器窗口非常小时，布局异常的问题\n' +
    '\n' +
    '---\n' +
    '\n' +
    '### 开发计划\n' +
    '\n' +
    '#### 更新计划\n' +
    '\n' +
    '> **计划时间：2024/07/12**\n' +
    '\n' +
    '**计划新增功能与页面如下：**\n' +
    '\n' +
    '- [ ] 用户使用手机短信注册登录\n' +
    '- [ ] 增加重置密码功能\n' +
    '- [ ] 完善在网络阻塞时各个页面加载不友好的情况（骨架屏实现）\n' +
    '\n' +
    '#### 历史计划\n' +
    '\n' +
    '> **计划时间：2024/07/11**\n' +
    '\n' +
    '**计划新增功能与页面如下：**\n' +
    '\n' +
    '- [ ] 用户使用手机短信注册登录\n' +
    '- [ ] 增加重置密码功能\n' +
    '- [ ] 完善在网络阻塞时各个页面加载不友好的情况（骨架屏实现）\n' +
    '- [x] ~~增加AI聊天功能（已完成）~~\n' +
    '\n')


</script>
