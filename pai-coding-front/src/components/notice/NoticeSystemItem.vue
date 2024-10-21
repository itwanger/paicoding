
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
        <div class="comment" v-html="msg.msg"></div>
      </div>
      <div class="notification-bottom">
        <span
          class="notification-time"
        >
         {{format(new Date(msg.createTime), 'yyyy-MM-dd HH:mm:ss')}}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import { format } from 'date-fns'

const props = defineProps<{
  msg: NoticeMsgType
}>()
</script>


<style scoped>

</style>
