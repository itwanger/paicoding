<template>
  <!-- 文章列表 -->
  <div v-for="(article, id) in articles" :key="id">
    <ArticleCard :article="article"></ArticleCard>
  </div>
</template>

<script setup lang="ts">

import ArticleCard from '@/components/article/ArticleCard.vue'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'

const props = defineProps<{
  articles: ArticleType[]
}>()

</script>

<style scoped>

</style>
