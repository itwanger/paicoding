<template>
  <!-- 消息通知 -->
  <div class="notification">
    <a
      :href="'/user/' + msg.operateUserId"
      class=""
      target="_blank"
    >
      <img
        :src="msg.operateUserPhoto"
        class="notification-img"
        loading="lazy"
      />
    </a>
    <div class="notification-right">
      <div class="notification-content">
        <div class="profile">
          <a
            :href="'/user/' + msg.operateUserId"
            class=""
            target="_blank"
          >
            <span class="name">{{msg.operateUserName}}</span>
          </a>
          关注了你
        </div>
      </div>
      <div class="notification-bottom">
        <span
          class="notification-time"
        >
          {{format(new Date(msg.createTime), 'yyyy-MM-dd HH:mm:ss')}}
        </span>
      </div>
    </div>
    <el-button @click="follow" :disabled="followBtnDisabled" class="w-20" v-if="global.isLogin && global.user.userId != String(msg.operateUserId)">
      {{ userFollowed == true ? '取消关注' : '关注'}}
    </el-button>

  </div>
</template>
<script setup lang="ts">
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import { format } from 'date-fns'
import { useGlobalStore } from '@/stores/global'
import { ref } from 'vue'
import { doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { USER_FOLLOW_URL } from '@/http/URL'
const globalStore = useGlobalStore()
const global = globalStore.global
const props = defineProps<{
  msg: NoticeMsgType
}>()

// 关注/取消关注
const followBtnDisabled = ref(false)
const userFollowed = ref(props.msg.msg != 'false')

const follow = () => {
  followBtnDisabled.value = true
  doPost<CommonResponse>(USER_FOLLOW_URL, {
    userId:props.msg.operateUserId,
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
