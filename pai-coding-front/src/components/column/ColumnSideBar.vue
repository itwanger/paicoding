<template>
  <div v-for="(sidebar, id) in sidebarItems" :key="id">
    <SideNotice v-if="sidebar.style === 1" :side="sidebar"></SideNotice>
    <SideArticles v-else-if="sidebar.style === 2 && sidebar.items && sidebar.items.length > 0" :side="sidebar"></SideArticles>
<!--    <SideJoin v-else-if="sidebar.style === 3" :side="sidebar"></SideJoin>-->
    <SideAbout v-else-if="sidebar.style === 4" :side="sidebar"></SideAbout>
    <SideColumn v-else-if="sidebar.style === 5" :side="sidebar"></SideColumn>
<!--    <SidePDF v-else-if="sidebar.style === 6" :side="sidebar"></SidePDF>-->
<!--    <SideSubscribe v-else-if="sidebar.style === 7" :side="sidebar"></SideSubscribe>-->
  </div>
</template>
<script setup lang="ts">

import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'
import SideNotice from '@/components/side/SideNotice.vue'
import SideArticles from '@/components/side/SideArticles.vue'
import SideAbout from '@/components/side/SideAbout.vue'
import SideColumn from '@/components/side/SideColumn.vue'

const props = defineProps<{
  sidebarItems: SideBarItem[]
}>()

</script>



<style scoped>

</style>
