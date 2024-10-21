<template>
  <!-- 侧边栏 -->
  <div class="home-right-item-wrap">
    <div class="home-right-item-title">
      <h5 >{{side.title}}</h5>
    </div>
    <div class="home-right-item-about-content" >
      {{side.content}}
    </div>
  </div>
</template>

<script setup lang="ts">

import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'

const props = defineProps<{
  side: SideBarItem
}>()
</script>



<style scoped>

</style>
