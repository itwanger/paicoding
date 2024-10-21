<template>
  <section class="com-2-panel side J-authorPanel internal">
    <header class="com-2-panel-hd"><h3 class="com-2-panel-title">作者介绍</h3></header>
    <div class="com-2-panel-bd">
      <div class="com-author-intro">
        <div class="com-author-intro-object">
          <a :href="'/user/' + user.userId"
             class="com-author-intro-avatar"
             :style="{ backgroundImage: 'url(' + user.photo + ')'}"></a>
        </div>
        <h3 class="com-author-intro-name">
          <a :href="'/user/' + user.userId"
             class="com-author-intro-name-inner"
             :title="user.userName">
          {{ user.userName}}
          </a>
        </h3>
        <div class="com-author-intro-desc">
          <span>{{'已加入' + user.joinDayCount + '天'}}</span>
          <div class="c-bubble-trigger com-verification">
            <i class="verified"></i>
          </div>
        </div>
        <div class="com-author-intro-btns">
          <el-button @click="follow" :disabled="followBtnDisabled" class="w-20" v-if="global.isLogin && global.user.userId != user.userId">
            {{ userFollowed == true ? '取消关注' : '关注'}}
          </el-button>
          <el-button class="w-20" type="primary" @click="router.push('/column')">
            教程
          </el-button>
        </div>
        <ul class="com-author-intro-infos">
          <li class="com-author-intro-info">
            <a :href="'/user/' + user.userId"
               class="com-author-intro-info-link">
              <div class="com-author-intro-info-title">文章数</div>
              <div class="com-author-intro-info-num">{{user.articleCount}}</div>
            </a>
          </li>
          <li class="com-author-intro-info">
            <a :href="'/user/' + user.userId"
               class="com-author-intro-info-link">
              <div class="com-author-intro-info-title">点赞数</div>
              <div class="com-author-intro-info-num">{{user.praiseCount}}</div>
            </a>
          </li>
          <li class="com-author-intro-info">
            <a :href="'/user/' + user.userId"
               class="com-author-intro-info-link">
              <div class="com-author-intro-info-title">收藏数</div>
              <div class="com-author-intro-info-num">{{user.collectionCount}}</div>
            </a>
          </li>
          <li class="com-author-intro-info">
            <a :href="'/user/' + user.userId"
               class="com-author-intro-info-link">
              <div class="com-author-intro-info-title">粉丝数</div>
              <div class="com-author-intro-info-num">{{user.fansCount}}</div>
            </a>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">

import type { CommonResponse, GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'
import type { UserStatisticInfo } from '@/http/ResponseTypes/UserInfoType/UserStatisticInfoType'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { doPost } from '@/http/BackendRequests'
import { USER_FOLLOW_URL } from '@/http/URL'

const router = useRouter()
const props = defineProps<{
  user: UserStatisticInfo
  global: GlobalResponse
}>()

// 关注/取消关注
const followBtnDisabled = ref(false)
const userFollowed = ref(props.user.followed)

const follow = () => {
  followBtnDisabled.value = true
  doPost<CommonResponse>(USER_FOLLOW_URL, {
    userId: props.user.userId,
    followed: !userFollowed.value
  })
    .then((res) => {
      userFollowed.value = !userFollowed.value
    })
    .catch((err) => {
      console.log(err)
    })
    .finally(() => {
      followBtnDisabled.value = false
    })
}

</script>



<style scoped>

</style>
