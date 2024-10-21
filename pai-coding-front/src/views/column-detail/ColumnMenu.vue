<template>
  <el-menu
    :default-active="`/column/` + vo.column + '/' + vo.section"
    class="el-menu-vertical-demo overflow-auto"
    @open="handleOpen"
    @close="handleClose"
    style="width: 315px; padding: 18px 5px 12px"
    active-text-color="var(--active-element-color)"
  >
    <a :href="`/column/` + vo.column + '/' + `${id+1}`" v-for="(menu, id) in vo.articleList" :key="id" class="mb-4">
      <el-menu-item  :index="`/column/` + vo.column + '/' + `${id+1}`">
        <template #default>
          <div :title="menu.title" style="font-size: 16px; width: 210px" >
            <span class="mr-2 font-bold">{{id + 1}}</span>
            <span > {{menu.title.length > 15? menu.title.substring(0, 15) + '....': menu.title}}</span>
          </div>
          <div class="right center-content">
            <div class="label label-free" v-if="menu.readType === 0">免费</div>
            <div class="label label-free" v-else-if="menu.readType === 1 || !global.isLogin">登录</div>
            <div class="label" v-else-if="menu.readType === 2">限免</div>
            <div class="label label-star" v-else-if="menu.readType === 3">星球</div>
            <div class="label" v-else>限免</div>
          </div>
        </template>
      </el-menu-item>
      <p class="ml-3.5 pl-4 text-xs" style="color: #999999;">
        更新时间: {{menu.createTime.substring(0, menu.createTime.indexOf('T'))}}
      </p>
    </a>
  </el-menu>
</template>

<script setup lang="ts">

import type { ColumnArticlesResponseType } from '@/http/ResponseTypes/ColumnDetailType/ColumnArticlesResponseType'

import { useGlobalStore } from '@/stores/global'
import { useRouter } from 'vue-router'

// 全局的用户信息
const globalStore = useGlobalStore()
const global = globalStore.global

const router = useRouter()
const props = defineProps<{
  vo: ColumnArticlesResponseType
}>()

const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}

</script>

<style scoped>

.el-menu-item{
  height: 45px;
}


</style>
