<template>
  <div v-for="(item, id) in list.records" :key="id">
    <NoticePraiseItem :msg="item"></NoticePraiseItem>
  </div>
</template>

<script setup lang="ts">
import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'
import NoticePraiseItem from '@/components/notice/NoticePraiseItem.vue'

const props = defineProps<{
  list: BasicPageType<NoticeMsgType>
}
>()
</script>



<style scoped>

</style>
