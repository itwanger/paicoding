<template>
  <!-- 创作历程 -->
  <div class="card">
    <div class="card-body process-wrap">
      <div class="card-title process-title">创作历程</div>
      <div v-if="user.userHome.yearArticleList.length > 0" >
        <div class="card-text" v-for="(year, id) in user.userHome.yearArticleList" :key="id">
          {{year.year + ' 年共发表文章 ' + year.articleCount + ' 篇'}}
        </div>
      </div>
      <div v-else>
        <div class="card-text">暂未发布任何内容哦~</div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">

import type { UserHomeInfoResponseType } from '@/http/ResponseTypes/UserHomeInfoResponseType'

const props = defineProps<{
  user: UserHomeInfoResponseType
}>()


</script>



<style scoped>

</style>
