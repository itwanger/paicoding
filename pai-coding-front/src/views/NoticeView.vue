<template>
  <!-- 导航栏 -->
  <HeaderBar></HeaderBar>

  <!-- 正文内容 -->
  <div class="notice-wrap">

    <!-- 文章列表 -->
    <div class="notice-nav">
      <div class="notice-nav-inner">
        <el-badge :value="unreadCount[NoticeTypeEnum.COMMENT_TYPE]" :offset="[-20, 12]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.COMMENT_TYPE)">
            <template #default>
              <span :class="{'color-text-link-orange': currentTab == NoticeTypeEnum.COMMENT_TYPE}" class="resized-text-size font-bold mr-4 p-2"> 评论 </span>
            </template>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount[NoticeTypeEnum.REPLY_TYPE]" :offset="[-20, 12]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.REPLY_TYPE)">
            <template #default>
              <span :class="{'color-text-link-orange': currentTab == NoticeTypeEnum.REPLY_TYPE}" class="resized-text-size font-bold mr-4 p-2"> 回复 </span>
            </template>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['praise']" :offset="[-20, 12]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.PRAISE_TYPE)">
            <template #default>
              <span :class="{'color-text-link-orange': currentTab == NoticeTypeEnum.PRAISE_TYPE}" class="resized-text-size font-bold mr-4 p-2"> 点赞 </span>
            </template>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['collect']" :offset="[-20, 12]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.COLLECT_TYPE)">
            <template #default>
              <span :class="{'color-text-link-orange': currentTab == NoticeTypeEnum.COLLECT_TYPE}" class="resized-text-size font-bold mr-4 p-2"> 收藏 </span>
            </template>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['follow']" :offset="[-20, 12]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.FOLLOW_TYPE)">
            <template #default>
              <span :class="{'color-text-link-orange': currentTab == NoticeTypeEnum.FOLLOW_TYPE}" class="resized-text-size font-bold mr-4 p-2"> 关注消息 </span>
            </template>
          </el-link>
        </el-badge>
        <el-badge :value="unreadCount['system']" :offset="[-20, 12]" :show-zero="false">
          <el-link @click="changeNoticeType(NoticeTypeEnum.SYSTEM_TYPE)">
            <template #default>
              <span :class="{'color-text-link-orange': currentTab == NoticeTypeEnum.SYSTEM_TYPE}" class="resized-text-size font-bold mr-4 p-2"> 系统消息 </span>
            </template>
          </el-link>
        </el-badge>

      </div>
    </div>
<!--  评论页  -->
    <div class="notice-content">
      <div v-if="currentTab === NoticeTypeEnum.COMMENT_TYPE" id="itemList">
        <el-skeleton :loading="loading" :throttle="200">
          <template #template>
            <div v-for="(item, id) in 5" :key="id" class="center-content notification h-36 w-full">
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
            </div>
          </template>
          <template #default>
            <div v-if="!loading && (!noticeData.comment.list || noticeData.comment.list.records.length == 0)" class="notification">暂无评论消息</div>
            <NoticeComment v-else  :list="noticeData.comment.list"></NoticeComment>
            <!--        分页组件-->
            <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.COMMENT_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.COMMENT_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.COMMENT_TYPE]" :default-current-page="1"
                           @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange">
            </el-pagination>
          </template>
        </el-skeleton>
      </div>
<!--  回复页-->
      <div v-if="currentTab === NoticeTypeEnum.REPLY_TYPE" id="itemList">
        <el-skeleton :loading="loading">
          <template #template>
            <div v-for="(item, id) in 5" :key="id" class="center-content notification h-36 w-full">
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
            </div>
          </template>
          <template #default>
            <div v-if="!loading && (!noticeData.reply.list || noticeData.reply.list.records.length == 0)" class="notification">暂无回复消息</div>
            <NoticeReply v-else  :list="noticeData.reply.list"></NoticeReply>
            <!--        分页组件-->
            <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.REPLY_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.REPLY_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.REPLY_TYPE]" :default-current-page="1"
                           @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange">
            </el-pagination>
          </template>
        </el-skeleton>
      </div>
<!--  点赞通知    -->

      <div v-if="currentTab === NoticeTypeEnum.PRAISE_TYPE" id="itemList">
        <el-skeleton :loading="loading">
          <template #template>
            <div v-for="(item, id) in 5" :key="id" class="center-content notification h-36 w-full">
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
            </div>
          </template>
          <template #default>
            <div v-if="!loading && (!noticeData.praise.list || noticeData.praise.list.records.length == 0)" class="notification">暂无点赞消息</div>
            <NoticePraise v-else  :list="noticeData.praise.list"></NoticePraise>
            <!--        分页组件-->
            <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.PRAISE_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.PRAISE_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.PRAISE_TYPE]" :default-current-page="1"
                           @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange">
            </el-pagination>
          </template>
        </el-skeleton>
      </div>
<!--  收藏通知    -->
      <div v-if="currentTab === NoticeTypeEnum.COLLECT_TYPE" id="itemList">
        <el-skeleton :loading="loading">
          <template #template>
            <div v-for="(item, id) in 5" :key="id" class="center-content notification h-36 w-full">
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
            </div>
          </template>
          <template #default>
            <div v-if="!loading && (!noticeData.collect.list || noticeData.collect.list.records.length == 0)" class="notification">暂无收藏消息</div>
            <NoticeCollect v-else  :list="noticeData.collect.list"></NoticeCollect>
            <!--        分页组件-->
            <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.COLLECT_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.COLLECT_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.COLLECT_TYPE]" :default-current-page="1"
                           @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange">
            </el-pagination>
          </template>
        </el-skeleton>
      </div>
<!--  关注消息    -->
      <div v-if="currentTab === NoticeTypeEnum.FOLLOW_TYPE" id="itemList">
        <el-skeleton :loading="loading">
          <template #template>
            <div v-for="(item, id) in 5" :key="id" class="center-content notification h-36 w-full">
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
            </div>
          </template>
          <template #default>
            <div v-if="!loading && (!noticeData.follow.list || noticeData.follow.list.records.length == 0)" class="notification">暂无关注消息</div>
            <NoticeFollow v-else  :list="noticeData.follow.list"></NoticeFollow>
            <!--        分页组件-->
            <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.FOLLOW_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.FOLLOW_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.FOLLOW_TYPE]" :default-current-page="1"
                           @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange">
            </el-pagination>
          </template>
        </el-skeleton>
      </div>
<!--  系统消息    -->
      <div v-if="currentTab === NoticeTypeEnum.SYSTEM_TYPE" id="itemList">
        <el-skeleton :loading="loading">
          <template #template>
            <div v-for="(item, id) in 5" :key="id" class="center-content notification h-36 w-full">
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
              <el-skeleton-item class="m-2"></el-skeleton-item>
            </div>
          </template>
          <template #default>
            <div v-if="!loading && (!noticeData.system.list || noticeData.system.list.records.length == 0)" class="notification">暂无系统消息</div>
            <NoticeSystem v-else  :list="noticeData.system.list"></NoticeSystem>
            <!--        分页组件-->
            <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage[NoticeTypeEnum.SYSTEM_TYPE]" v-model:page-size="pageSize[NoticeTypeEnum.SYSTEM_TYPE]" layout="sizes, prev, pager, next" :page-count="totalPage[NoticeTypeEnum.SYSTEM_TYPE]" :default-current-page="1"
                           @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange">
            </el-pagination>
          </template>
        </el-skeleton>
      </div>
    </div>
    <!-- 底部信息 -->
    <Footer></Footer>
    <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
  </div>
</template>

<script setup lang="ts">
import Footer from '@/components/layout/Footer.vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { useGlobalStore } from '@/stores/global'
import { computed, onMounted, provide, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { defaultNoticeMsgResponse, type NoticeMsgResponseType } from '@/http/ResponseTypes/NoticeMsgResponseType'
import { UNREAD_NOTICE_URL } from '@/http/URL'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import NoticeComment from '@/views/notice/NoticeComment.vue'
import { NoticeTypeEnum } from '@/constants/NoticeTypeConstants'
import NoticeReply from '@/views/notice/NoticeReply.vue'
import NoticeSystem from '@/views/notice/NoticeSystem.vue'
import NoticeFollow from '@/views/notice/NoticeFollow.vue'
import NoticeCollect from '@/views/notice/NoticeCollect.vue'
import NoticePraise from '@/views/notice/NoticePraise.vue'
const globalStore = useGlobalStore()
const global = globalStore.global
const route = useRoute()
const router = useRouter()

// 骨架屏
const loading = ref(true)

// 分页信息
const currentPage = ref<{[key: string]: number}>({
  comment: 1,
  reply: 1,
  praise: 1,
  collect: 1,
  follow: 1,
  system: 1
})

const pageSize = ref<{[key: string]: number}>({
  comment: 10,
  reply: 10,
  praise: 10,
  collect: 10,
  follow: 10,
  system: 10
})

const totalPage = ref<{[key: string]: number}>({
  comment: 0,
  reply: 0,
  praise: 0,
  collect: 0,
  follow: 0,
  system: 0
})

const noticeData = ref<{[key: string]: NoticeMsgResponseType}>({
  comment: {...defaultNoticeMsgResponse},
  reply: {...defaultNoticeMsgResponse},
  praise: {...defaultNoticeMsgResponse},
  collect: {...defaultNoticeMsgResponse},
  follow: {...defaultNoticeMsgResponse},
  system: {...defaultNoticeMsgResponse}
})

const unreadCount = ref<{[key: string]: number}>({
  comment: 0,
  reply: 0,
  praise: 0,
  collect: 0,
  follow: 0,
  system: 0
})


onMounted(() => {
  getNotices()
  // 清除当前tab下的未读信息状态
  unreadCount.value[`${route.params.noticeType}`] = 0
})

watch(() => route.params.noticeType, (newVal, oldVal) => {
  getNotices()
  // 清除当前tab下的未读信息状态
  unreadCount.value[`${route.params.noticeType}`] = 0
})

const getNotices = () => {
  doGet<CommonResponse<NoticeMsgResponseType>>(UNREAD_NOTICE_URL + '/' + route.params.noticeType, {
    currentPage: currentPage.value[`${route.params.noticeType}`],
    pageSize: pageSize.value[`${route.params.noticeType}`]
  })
    .then((res) => {
      console.log(res)
      globalStore.setGlobal(res.data.global)
      Object.assign(noticeData.value[`${route.params.noticeType}`], res.data.result)
      Object.assign(unreadCount.value, res.data.result.unreadCountMap)
      totalPage.value[`${route.params.noticeType}`] = Number(res.data.result.list.pages)
    })
    .finally(() => {
      loading.value = false
    })
}


// 获取当前的通知类型（好像用处不大，后面可以考虑删掉）
const currentTab = computed(() => {
  return route.params.noticeType
})

// 分页
const onCurrentPageChange = (newCurrentPage: number) => {
  currentPage.value[`${route.params.noticeType}`] = newCurrentPage
  getNotices()
}
const onPageSizeChange = (newPageSize: number) => {
  pageSize.value[`${route.params.noticeType}`] = newPageSize
  getNotices()
}

// 点击各个按钮切换通知类型
const changeNoticeType = (targetNoticeType: string) => {
  if (currentTab.value == targetNoticeType){
    window.location.reload()
  }else{
    router.push('/notice/' + targetNoticeType)
  }
}


// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

</script>



<style scoped>

.resized-text-size{
  font-size: 1.125rem/* 18px */;
  line-height: 1.75rem/* 28px */;
}

@media (max-width: 768px) {
  .resized-text-size{
    font-size: 0.875rem/* 14px */;
    line-height: 1.25rem/* 20px */;
  }
}

</style>
