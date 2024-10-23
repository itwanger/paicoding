
<template>
    <HeaderBar />

<!--  正文内容-->
    <div class="home mt-2">

      <el-skeleton class="hidden-when-screen-small" :loading="contentLoading" animated :throttle="200">
        <template #template>
          <div style="height: 400px">
            <el-skeleton-item class="mt-2 mb-4 p-4"></el-skeleton-item>
            <div class="flex justify-between">
              <el-skeleton-item v-for="(item, id) in 4" :key="id" class="ml-4" variant="image" style="width: 285px; height: 290px" />
            </div>
          </div>
        </template>
        <template #default>
          <!--      类别筛选-->
          <NavBar :categories="vo.categories"></NavBar>
          <!--      推荐文章-->
          <RecommendArticle v-if="!contentLoading && vo.topArticles.length > 0" :top-articles="vo.topArticles" id="recommend-article-component"></RecommendArticle>
        </template>
      </el-skeleton>

      <el-skeleton class="hidden-when-screen-small" :loading="articlesLoading" animated :throttle="200">
        <template #template>
          <div class="home-wrap bg-color">
            <div class="home-inter-wrap">
              <div class="home-body">
                <div v-for="(item, id) in 10" :key="id" class="center-content" style="height: 168px">
                  <el-skeleton-item class="mt-2 ml-2 mb-4 mr-4 p-16"></el-skeleton-item>
                </div>
              </div>
              <div class="home-right">
                <el-skeleton-item class="mt-1 mb-4 p-40"></el-skeleton-item>
              </div>
            </div>
          </div>

        </template>
        <template #default>
          <div class="home-wrap bg-color" >
            <div class="home-inter-wrap">
              <div class="home-body">
                <div id="articleList" class="cdc-article-panel__list">
                  <ArticleList :articles="articles.records"></ArticleList>
                </div>

                <!--        分页组件-->
                <el-pagination :page-sizes="[10, 20]" hide-on-single-page v-model:current-page="currentPage" v-model:page-size="pageSize" layout="sizes, prev, pager, next" :page-count="totalPage" :default-current-page="1"
                               @update:page-size="onPageSizeChange" @update:current-page="onCurrentPageChange"
                />

              </div>

              <div class="home-right">
                <!-- 侧边公告 -->
<!--                <div v-if="vo.sideBarItems">-->
<!--                  <SideBar :sidebar-items="vo.sideBarItems"></SideBar>-->
<!--                </div>-->
              </div>
            </div>
          </div>
        </template>
      </el-skeleton>


      <!-- 底部信息 -->
      <Footer></Footer>
    </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>

</template>

<script setup lang="ts">
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { onMounted, provide, reactive, ref } from 'vue'
import { doGet } from '@/http/BackendRequests'
import { CATEGORY_ARTICLE_LIST_URL, INDEX_URL } from '@/http/URL'
import {
  type CommonResponse, defaultGlobalResponse, type GlobalResponse
} from '@/http/ResponseTypes/CommonResponseType'
import NavBar from '@/views/home/navbar/NavBar.vue'
import { defaultIndexVoResponse, type IndexVoResponse } from '@/http/ResponseTypes/IndexVoType'
import RecommendArticle from '@/views/home/recommend/RecommendArticle.vue'
import ArticleList from '@/views/home/article/ArticleList.vue'
import Footer from '@/components/layout/Footer.vue'
import SideBar from '@/views/home/sidebar/SideBar.vue'
import { useGlobalStore } from '@/stores/global'
import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import { useRoute } from 'vue-router'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
const globalStore = useGlobalStore()
const route = useRoute()

let global = reactive<GlobalResponse>({...defaultGlobalResponse})
let vo = reactive<IndexVoResponse>({...defaultIndexVoResponse})
let articles = reactive<BasicPageType<ArticleType>>({...defaultBasicPage})
onMounted(() => {
  // 获取文章列表
  doGet<CommonResponse>(CATEGORY_ARTICLE_LIST_URL, {
    category: route.query['category']
  })
    .then((response) => {
      console.log(response)
      if(response.data){
        globalStore.setGlobal(response.data.global)
        Object.assign(vo.topArticles, response.data.result.topArticles)
        Object.assign(vo.categories, response.data.result.categories)
        // @ts-ignore
        Object.assign(articles, response.data.result.articles)
        totalPage.value = Number(response.data.result.articles.pages)
        currentPage.value = Number(response.data.result.articles.current)
        console.log(articles)
        // 取消骨架屏显示
        articlesLoading.value = false
        contentLoading.value = false
      }
    })
})

// 如下是分页操作
const currentPage = ref(1)
const totalPage = ref(0)
const pageSize = ref(10)

const onPageSizeChange = (newPageSize: number) => {
  doGet<CommonResponse>(CATEGORY_ARTICLE_LIST_URL, {
    category: route.query['category'],
    currentPage: currentPage.value,
    pageSize: pageSize.value
  })
    .then((response) => {
      console.log(response)
      if(response.data){
        globalStore.setGlobal(response.data.global)
        Object.assign(vo.topArticles, response.data.result.topArticles)
        Object.assign(vo.categories, response.data.result.categories)
        Object.assign(articles, response.data.result.articles)
        totalPage.value = Number(response.data.result.articles.pages)
        currentPage.value = Number(response.data.result.articles.current)
        console.log(articles)
      }
    })
}

const onCurrentPageChange = (newCurrentPage: number) => {
  doGet<CommonResponse>(CATEGORY_ARTICLE_LIST_URL, {
    category: route.query['category'],
    currentPage: newCurrentPage,
    pageSize: pageSize.value
  })
    .then((response) => {
      console.log(response)
      if(response.data){
        globalStore.setGlobal(response.data.global)
        Object.assign(vo.topArticles, response.data.result.topArticles)
        Object.assign(vo.categories, response.data.result.categories)
        Object.assign(articles, response.data.result.articles)
        totalPage.value = Number(response.data.result.articles.pages)
        currentPage.value = Number(response.data.result.articles.current)
        console.log(articles)
      }
    })
}

// 骨架屏显示
const contentLoading = ref(true)
const articlesLoading = ref(true)

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

