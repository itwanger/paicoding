<template>
  <div v-for="(item, id) in list.records" :key="id">
    <NoticeReplyItem :msg="item"></NoticeReplyItem>
  </div>
</template>

<script setup lang="ts">

import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import NoticeReplyItem from '@/components/notice/NoticeReplyItem.vue'

const props = defineProps<{
  list: BasicPageType<NoticeMsgType>
}
>()

</script>


<style scoped>

</style>
