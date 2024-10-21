<template>
  <HeaderBar></HeaderBar>


  <div class="article-wrap">
    <el-skeleton :rows="5" animated :loading="contentLoading">
      <template #template>
        <div class="flex h-full">
          <el-menu class="hidden-when-screen-small" style="width: 275px; height: 100%">
            <el-skeleton-item class="mt-2 mb-3 p-3" v-for="(item, id) in 5" :key="id"></el-skeleton-item>
          </el-menu>
          <div class="flex-grow center-content flex">
            <el-skeleton-item class="center-content mt-2 mb-3 p-3" v-for="(item, id) in 5" :key="id" style="max-width: 800px" :loading="contentLoading"></el-skeleton-item>
          </div>
        </div>
      </template>

      <template #default>
        <!-- 目录 -->
        <ColumnMenu class="hidden-when-screen-small" :vo="vo"></ColumnMenu>
        <!-- 内容 -->
        <div class="article-content-wrap bg-color">
          <!--  增加一个搜索的 Form 表单和左侧目录折叠的按钮  -->
          <div class="for-menu">
            <form class="bd-search d-flex align-items-center">
              <input class="form-control mr-sm-2" type="search" placeholder="Search" aria-label="Search">
              <!-- 折叠目录按钮 -->
              <button class="btn bd-search-docs-toggle d-md-none pl-1 pr-0"
                      type="button"
                      data-toggle="collapse"
                      data-target="#collapseMenu"
                      aria-controls="collapseMenu"
                      aria-expanded="false"
                      aria-label="Toggle docs navigation">
                <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" viewBox="0 0 30 30" role="img" focusable="false"><title>Menu</title><path stroke="currentColor" stroke-linecap="round" stroke-miterlimit="10" stroke-width="2" d="M4 7h22M4 15h22M4 23h22"/></svg>
              </button>
            </form>
          </div>

          <!--  正文 -->
          <div class="article-content-inter-wrap">
            <ColumnArticleDetail :article-vo="vo"></ColumnArticleDetail>
            <!--  评论  -->
            <div id="commentDiv">
              <CommentList :comments="vo.comments" :hot-comment="vo.hotComment" :article="vo.article"></CommentList>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
  <Footer></Footer>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>

</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import ColumnMenu from '@/views/column-detail/ColumnMenu.vue'
import { onMounted, provide, reactive, ref } from 'vue'
import {
  type ColumnArticlesResponseType,
  defaultColumnArticlesResponse
} from '@/http/ResponseTypes/ColumnDetailType/ColumnArticlesResponseType'
import { doGet } from '@/http/BackendRequests'
import { COLUMN_DETAIL_URL } from '@/http/URL'
import { useRoute } from 'vue-router'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { useGlobalStore } from '@/stores/global'
import ColumnArticleDetail from '@/components/column/ColumnArticleDetail.vue'
import CommentList from '@/views/article-detail/CommentList.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'

const globalStore = useGlobalStore()

const vo = reactive({...defaultColumnArticlesResponse})


//为了子组件评论时能够无缝更新，重新渲染，提供一个更新函数
const getArticleDetail = (response: ColumnArticlesResponseType) => {
  Object.assign(vo.comments, response.comments)
  // console.log(articleVo)
}

provide('updateArticleComment', getArticleDetail)

const route = useRoute()
onMounted(() => {
  doGet<CommonResponse>(COLUMN_DETAIL_URL + `/${route.params['columnId']}/${route.params['sectionId']}`, {})
    .then((res) => {
      globalStore.setGlobal(res.data.global)
      Object.assign(vo, res.data.result)
      console.log("column detail: ", vo)
      contentLoading.value = false
    })
    .catch((err) => {
      console.log(err)
    })
})

// 骨架屏的设置
const contentLoading = ref(true)

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>

</style>
