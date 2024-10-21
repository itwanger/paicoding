<template>
  <div v-for="(item, id) in list.records" :key="id">
    <NoticeFollowItem :msg="item"></NoticeFollowItem>
  </div>
</template>

<script setup lang="ts">

import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import NoticeFollowItem from '@/components/notice/NoticeFollowItem.vue'

const props = defineProps<{
  list: BasicPageType<NoticeMsgType>
}
>()

</script>


<style scoped>

</style>
