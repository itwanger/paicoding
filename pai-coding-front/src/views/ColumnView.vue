<template>
  <HeaderBar />
  <!-- 专栏首页 -->
  <div class="custom-home">
    <div class="custom-home-wrap">
      <div class="custom-home-body">
<!--        TODO 加上换页的分页按钮-->
        <!-- 文章列表 -->
        <div class="bg-color-white m-2">
          <div id="articleList">
              <ColumnList :columns="vo.columnPage.records"></ColumnList>
          </div>
        </div>
      </div>

      <div class="custom-home-right">
        <!--   侧边推荐栏   -->
        <ColumnSideBar :sidebar-items="vo.sideBarItems"></ColumnSideBar>
      </div>
    </div>
    <!-- 底部信息 -->
    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>


<script setup lang="ts">
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()
import { setTitle } from '@/util/utils'
import { onMounted, provide, reactive, ref } from 'vue'
import {
  type CommonResponse,
} from '@/http/ResponseTypes/CommonResponseType'
import { doGet } from '@/http/BackendRequests'
import { COLUMN_LIST_URL } from '@/http/URL'
import {
  type ColumnListVoTypeResponse,
  defaultColumnVoResponse
} from '@/http/ResponseTypes/ColumnType/ColumnListVoType'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import ColumnList from '@/views/column/ColumnList.vue'
import ColumnSideBar from '@/components/column/ColumnSideBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'

let vo = reactive<ColumnListVoTypeResponse>({...defaultColumnVoResponse})

onMounted(() => {
  setTitle("专栏首页")
  doGet<CommonResponse>(COLUMN_LIST_URL, {})
    .then((response) => {
      console.log(response)
      if(response.data){
        globalStore.setGlobal(response.data.global)
        // @ts-ignore
        Object.assign(vo, response.data.result)
        // vo = response.data.result
        console.log(vo)
      }
    })
    .catch((error) => {
      console.log(error)
    })
    .finally(() => {
      console.log("finally")
    })
})

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)

</script>


<style scoped>

.home{
  min-height: calc(100vh - var(--header-height));
}

</style>
