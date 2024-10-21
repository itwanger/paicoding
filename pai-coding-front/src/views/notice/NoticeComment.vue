
<template>
  <div v-for="(item, id) in list.records" :key="id">
    <NoticeCommentItem :msg="item"></NoticeCommentItem>
  </div>
</template>

<script setup lang="ts">

import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import NoticeCommentItem from '@/components/notice/NoticeCommentItem.vue'

const props = defineProps<{
  list: BasicPageType<NoticeMsgType>
}
>()

</script>


<style scoped>

</style>
