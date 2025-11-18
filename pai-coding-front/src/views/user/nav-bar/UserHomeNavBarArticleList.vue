<template>
  <!-- 文章列表 -->
  <div v-if="articles.records && articles.records.length > 0">
    <div v-for="(article, id) in articles.records" :key="id">
      <ArticleCard :article="article"></ArticleCard>
    </div>
  </div>
  <!-- 空状态 -->
  <el-empty v-else :description="emptyDescription" :image-size="200">
    <template #image>
      <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
        <!-- 背景圆 -->
        <circle cx="100" cy="100" r="90" fill="#F5F7FA"/>

        <!-- 文档图标 -->
        <g transform="translate(60, 50)">
          <!-- 文档背景 -->
          <rect x="0" y="0" width="80" height="100" rx="8" fill="#E6F7FF"/>
          <rect x="0" y="0" width="80" height="100" rx="8" fill="url(#grad1)" opacity="0.6"/>

          <!-- 渐变定义 -->
          <defs>
            <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#409EFF;stop-opacity:0.3" />
              <stop offset="100%" style="stop-color:#66B1FF;stop-opacity:0.1" />
            </linearGradient>
          </defs>

          <!-- 文档页角折叠 -->
          <path d="M 64 0 L 80 16 L 64 16 Z" fill="#B3D8FF"/>

          <!-- 文本行 -->
          <rect x="12" y="24" width="56" height="4" rx="2" fill="#409EFF" opacity="0.4"/>
          <rect x="12" y="34" width="48" height="4" rx="2" fill="#409EFF" opacity="0.3"/>
          <rect x="12" y="44" width="52" height="4" rx="2" fill="#409EFF" opacity="0.3"/>
          <rect x="12" y="54" width="44" height="4" rx="2" fill="#409EFF" opacity="0.2"/>

          <!-- 装饰性图标 -->
          <circle cx="40" cy="75" r="12" fill="#409EFF" opacity="0.15"/>
          <path d="M 34 75 L 38 79 L 46 71" stroke="#409EFF" stroke-width="2" fill="none" stroke-linecap="round"/>
        </g>

        <!-- 漂浮的小点点装饰 -->
        <circle cx="30" cy="40" r="3" fill="#67C23A" opacity="0.4"/>
        <circle cx="170" cy="60" r="4" fill="#E6A23C" opacity="0.4"/>
        <circle cx="160" cy="140" r="3" fill="#409EFF" opacity="0.4"/>
        <circle cx="40" cy="160" r="4" fill="#F56C6C" opacity="0.3"/>
      </svg>
    </template>
  </el-empty>
</template>

<script setup lang="ts">

import ArticleCard from '@/components/article/ArticleCard.vue'
import { type BasicPageType, } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import { computed } from 'vue'

const props = defineProps<{
  articles: BasicPageType<ArticleType>
  emptyDescription?: string
}>()

// 设置默认的空状态描述
const emptyDescription = computed(() => {
  return props.emptyDescription || '暂无内容'
})

</script>

<style scoped>

</style>
