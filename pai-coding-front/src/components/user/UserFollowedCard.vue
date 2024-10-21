<template>
  <el-card>
    <template #default>
      <div class="flex text-sm justify-between">
        <span class="flex">
          <el-avatar size="large">
            <img :src="user.avatar">
          </el-avatar>
          <span class="center-content ml-2 text-lg">{{user.userName}}</span>
        </span>
        <span class="center-content" v-if="global.user.id == route.params['userId']">
          <el-button @click="follow" :disabled="btnDisabled" round>{{userFollowed? '取消关注': '关注'}}</el-button>
        </span>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">

import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import { doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { USER_FOLLOW_URL } from '@/http/URL'
import { useGlobalStore } from '@/stores/global'
import { useRoute } from 'vue-router'
import { ref } from 'vue'
const globalStore = useGlobalStore()
const global = globalStore.global

const props = defineProps<{
  user: FollowUserInfoType
}>()

const route = useRoute()
const userFollowed = ref(props.user.followed)

// 关注/取消关注
const btnDisabled = ref(false)

const follow = () => {
  btnDisabled.value = true
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
      btnDisabled.value = false
  })
}


</script>

<style scoped>

</style>
