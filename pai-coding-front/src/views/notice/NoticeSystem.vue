<script setup lang="ts">
import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import NoticeSystemItem from '@/components/notice/NoticeSystemItem.vue'

const props = defineProps<{
  list: BasicPageType<NoticeMsgType>
}
>()

</script>

<template>
  <div v-for="(item, id) in list.records" :key="id">
    <NoticeSystemItem :msg="item"></NoticeSystemItem>
  </div>
</template>

<style scoped>

</style>
