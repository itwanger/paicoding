
<template>
  <!-- 侧边栏 -->
  <div v-for="(sidebar, id) in sidebarBarItems"  :key="id">
    <SideNotice v-if="sidebar.style === 1" :side="sidebar"></SideNotice>
    <SideArticles v-else-if="sidebar.style === 2" :side="sidebar"></SideArticles>
    <SideJoin :side="sidebar" v-else-if="sidebar.style === 3"></SideJoin>
    <SideAbout v-else-if="sidebar.style === 4" :side="sidebar"></SideAbout>
    <SideColumn v-else-if="sidebar.style === 5" :side="sidebar"></SideColumn>
<!--    <SidePDF v-else-if="sidebar.style === 6" :side="sidebar"></SidePDF>-->
  </div>
</template>

<script setup lang="ts">
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'
import SideNotice from '@/components/side/SideNotice.vue'
import SideArticles from '@/components/side/SideArticles.vue'
import SideJoin from '@/components/side/SideJoin.vue'
import SideAbout from '@/components/side/SideAbout.vue'
import SideColumn from '@/components/side/SideColumn.vue'
import SidePDF from '@/components/side/SidePDF.vue'

const props = defineProps<{
  sidebarBarItems: SideBarItem[]
}>()

</script>

<style scoped>

</style>
