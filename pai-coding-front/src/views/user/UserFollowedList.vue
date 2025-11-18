<template>
  <div class="flex flex-col" v-if="user.records && user.records.length > 0">
    <div v-for="userItem in user.records" :key="userItem.userId" class="mb-2">
      <UserFollowedCard :user="userItem"></UserFollowedCard>
    </div>
  </div>
  <!-- 空状态 -->
  <el-empty v-else :description="emptyDescription" :image-size="200">
    <template #image>
      <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
        <!-- 背景圆 -->
        <circle cx="100" cy="100" r="90" fill="#F5F7FA"/>

        <!-- 主要用户头像 -->
        <g transform="translate(100, 100)">
          <!-- 头像背景圆 -->
          <circle r="35" fill="url(#userGrad1)"/>

          <defs>
            <linearGradient id="userGrad1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#409EFF;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#79BBFF;stop-opacity:1" />
            </linearGradient>
          </defs>

          <!-- 用户图标 -->
          <!-- 头部 -->
          <circle cy="-8" r="10" fill="white" opacity="0.9"/>
          <!-- 身体 -->
          <path d="M -18 20 Q -18 8 -12 5 Q 0 0 12 5 Q 18 8 18 20" fill="white" opacity="0.9"/>
        </g>

        <!-- 左侧小头像 -->
        <g transform="translate(50, 80)">
          <circle r="20" fill="url(#userGrad2)"/>
          <defs>
            <linearGradient id="userGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#67C23A;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#95D475;stop-opacity:1" />
            </linearGradient>
          </defs>
          <circle cy="-5" r="6" fill="white" opacity="0.85"/>
          <path d="M -10 12 Q -10 5 -7 3 Q 0 0 7 3 Q 10 5 10 12" fill="white" opacity="0.85"/>
        </g>

        <!-- 右侧小头像 -->
        <g transform="translate(150, 120)">
          <circle r="20" fill="url(#userGrad3)"/>
          <defs>
            <linearGradient id="userGrad3" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#E6A23C;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#F3C17A;stop-opacity:1" />
            </linearGradient>
          </defs>
          <circle cy="-5" r="6" fill="white" opacity="0.85"/>
          <path d="M -10 12 Q -10 5 -7 3 Q 0 0 7 3 Q 10 5 10 12" fill="white" opacity="0.85"/>
        </g>

        <!-- 装饰性的加号 -->
        <g transform="translate(140, 60)" opacity="0.4">
          <circle r="8" fill="#909399" opacity="0.2"/>
          <line x1="0" y1="-5" x2="0" y2="5" stroke="#909399" stroke-width="2" stroke-linecap="round"/>
          <line x1="-5" y1="0" x2="5" y2="0" stroke="#909399" stroke-width="2" stroke-linecap="round"/>
        </g>

        <!-- 漂浮装饰点 -->
        <circle cx="35" cy="45" r="3" fill="#F56C6C" opacity="0.5"/>
        <circle cx="165" cy="170" r="4" fill="#409EFF" opacity="0.4"/>
        <circle cx="60" cy="160" r="3" fill="#67C23A" opacity="0.5"/>
      </svg>
    </template>
  </el-empty>
</template>

<script setup lang="ts">
import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
import UserFollowedCard from '@/components/user/UserFollowedCard.vue'
import { computed } from 'vue'

const props = defineProps<{
  user: BasicPageType<FollowUserInfoType>
  emptyDescription?: string
}>()

// 设置默认的空状态描述
const emptyDescription = computed(() => {
  return props.emptyDescription || '暂无用户'
})

</script>



<style scoped>

</style>
