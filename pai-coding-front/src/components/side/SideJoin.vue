
<template>
  <!-- 侧边栏 -->
  <div class="home-right-item-wrap">
    <div class="home-QR-title">
      <div class="home-QR-title-wrap">
        <div class="home-QR-title-content">
          <img :src="side.icon? side.icon: 'https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fdpic.tiankong.com%2Fet%2Fjg%2FQJ9108977402.jpg&refer=http%3A%2F%2Fdpic.tiankong.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1664861850&t=281d7109272f158ecb9fbee9c0fd0efb'" v-if="side.icon"
               alt="" class="home-QR-title-img">
          <span>扫码进群</span>
        </div>
        <div class="home-QR-title-content-other">{{side.title}}</div>
      </div>
      <img :src="side.img? side.img: 'https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fdpic.tiankong.com%2Fet%2Fjg%2FQJ9108977402.jpg&refer=http%3A%2F%2Fdpic.tiankong.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1664861850&t=281d7109272f158ecb9fbee9c0fd0efb'"
           alt="" class="home-QR-title-code">
    </div>
    <div class="home-QR-content">
        <span>
            {{side.content}}
        </span>
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
