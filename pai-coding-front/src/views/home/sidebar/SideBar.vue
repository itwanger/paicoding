<template>
  <!-- 侧边栏 -->
  <div v-for="(sidebar, id) in sidebarItems" :key="id">
<!--    <SideNotice v-if="sidebar.style === 1" :side="sidebar"></SideNotice>-->
    <SideArticles v-if="sidebar.style === 2" :side="sidebar"></SideArticles>
    <SideJoin v-if="sidebar.style === 3" :side="sidebar" ></SideJoin>
    <SideAbout v-if="sidebar.style === 4" :side="sidebar"></SideAbout>
    <SideColumn v-if="sidebar.style === 5" :side="sidebar"></SideColumn>
<!--    <SidePDF v-if="sidebar.style === 6" :side="sidebar"></SidePDF>-->
    <SideRank v-if="sidebar.style === 8" :side="sidebar"></SideRank>
  </div>

</template>

<script setup lang="ts">

import SideNotice from '@/components/side/SideNotice.vue'
import SideArticles from '@/components/side/SideArticles.vue'
import SideJoin from '@/components/side/SideJoin.vue'
import SideAbout from '@/components/side/SideAbout.vue'
import SideColumn from '@/components/side/SideColumn.vue'
import SidePDF from '@/components/side/SidePDF.vue'
import SideRank from '@/components/side/SideRank.vue'
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'

const props = defineProps<{
  sidebarItems: SideBarItem[]
}>()

</script>


<style scoped>

</style>
