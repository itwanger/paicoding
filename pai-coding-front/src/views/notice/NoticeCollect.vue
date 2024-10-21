<script setup lang="ts">
import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import NoticeCollectItem from '@/components/notice/NoticeCollectItem.vue'

const props = defineProps<{
  list: BasicPageType<NoticeMsgType>
}
>()
</script>

<template>
  <div v-for="(item, id) in list.records" :key="id">
    <NoticeCollectItem :msg="item"></NoticeCollectItem>
  </div>
</template>

<style scoped>

</style>
