<template>
  <!-- 文章列表 -->
  <div v-for="(article, id) in articles.records" :key="id">
    <ArticleCard :article="article"></ArticleCard>
  </div>
</template>

<script setup lang="ts">

import ArticleCard from '@/components/article/ArticleCard.vue'
import { type BasicPageType, } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

const props = defineProps<{
  articles: BasicPageType<ArticleType>
}>()

</script>

<style scoped>

</style>
