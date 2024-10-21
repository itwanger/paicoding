<template>
  <HeaderBar/>

  <div class="body-content flex">
<!--    <el-radio-group v-model="isCollapse" style="margin-bottom: 2px">-->
<!--      <el-radio-button :value="false">expand</el-radio-button>-->
<!--      <el-radio-button :value="true">collapse</el-radio-button>-->
<!--    </el-radio-group>-->
    <el-menu
      :default-active="defaultActiveTab"
      class="el-menu-vertical-demo body-content"
      :collapse="isCollapse"
      @open="handleOpen"
      @close="handleClose"
      router
    >
      <el-sub-menu index="1">
        <template #title>
          <el-icon><location /></el-icon>
          <span>常用工具</span>
        </template>
        <el-menu-item-group>
          <template #title><span>文件处理</span></template>
          <el-menu-item index="excel">Excel表格处理</el-menu-item>
<!--          <el-menu-item index="1-2">item two</el-menu-item>-->
        </el-menu-item-group>
<!--        <el-menu-item-group title="Group Two">-->
<!--          <el-menu-item index="1-3">item three</el-menu-item>-->
<!--        </el-menu-item-group>-->
<!--        <el-sub-menu index="1-4">-->
<!--          <template #title><span>item four</span></template>-->
<!--          <el-menu-item index="1-4-1">item one</el-menu-item>-->
<!--        </el-sub-menu>-->
      </el-sub-menu>
<!--      <el-menu-item index="2">-->
<!--        <el-icon><icon-menu /></el-icon>-->
<!--        <template #title>Navigator Two</template>-->
<!--      </el-menu-item>-->
<!--      <el-menu-item index="3" disabled>-->
<!--        <el-icon><document /></el-icon>-->
<!--        <template #title>Navigator Three</template>-->
<!--      </el-menu-item>-->
<!--      <el-menu-item index="4">-->
<!--        <el-icon><setting /></el-icon>-->
<!--        <template #title>Navigator Four</template>-->
<!--      </el-menu-item>-->
    </el-menu>

    <router-view></router-view>
  </div>

  <Footer></Footer>

</template>

<script setup lang="ts">

import HeaderBar from '@/components/layout/HeaderBar.vue'
import { useGlobalStore } from '@/stores/global'
import { onMounted, watchEffect } from 'vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { GLOBAL_INFO_URL } from '@/http/URL'
import Footer from '@/components/layout/Footer.vue'
import { ref } from 'vue'
import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { messageTip } from '@/util/utils'
const globalStore = useGlobalStore()

const global = globalStore.global
const route = useRoute()

// 根据地址选中菜单
const path = route.path
const defaultActiveTab = ref('')
defaultActiveTab.value = path.split('/')[2] || 'excel'

const isCollapse = ref(false)
// 根据浏览器宽度动态调整菜单来的打开和关闭
const updateMenuStatus = () => {
  const width = window.innerWidth;
  isCollapse.value = width < 769;
}

watchEffect(() => {
  updateMenuStatus();
});

window.addEventListener('resize', updateMenuStatus);


onMounted(() => {
  doGet<CommonResponse>(GLOBAL_INFO_URL, {})
    .then((response) => {
      if(response.data){
        console.log(response.data.global)
        globalStore.setGlobal(response.data.global)
        if(!global.isLogin){
          messageTip('请先登录', 'warning')
          setTimeout(() => {
            window.location.href = '/'
          }, 1000)
        }
      }
    })
    .catch((error) => {
      console.error(error)
    })
})





const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}


</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
}

</style>
