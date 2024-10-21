<template>
  <HeaderBar />

  <div class="home article-detail" v-if="ifUsualArticle">
    <div class="col-body pg-2-article" id="article-detail-body-div">
      <div class="com-3-layout" >
        <div class="layout-main">

          <!-- 正文 -->
          <ArticleDetail  :global="global" :articleVo="articleVo"></ArticleDetail>

          <!--  评论  -->
          <CommentList :comments="articleVo.comments" :hot-comment="articleVo.hotComment" :article="articleVo.article"></CommentList>

          <div
            class="correlation-article bg-color-white"
            id="relatedRecommend"
          >
            <!-- 关联推荐 -->
            <h4 class="correlation-article-title">相关推荐</h4>
            <div class="bg-color-white">
              <div id="articleList"></div>
            </div>
          </div>
        </div>

        <div class="layout-side hidden-when-screen-small flex-col flex">

          <!-- 用户相关信息 -->
          <UserCard :global="global" :user="articleVo.author"></UserCard>

          <!-- 活动推荐 -->
          <SideRecommendBar :sidebar-bar-items="articleVo.sideBarItems"></SideRecommendBar>
          <div id="toc-container-position" class="hidden-when-screen-small"></div>
          <!-- 文章菜单  -->

          <div class="sticky top-5 overflow-auto" id="content-menu">
            <el-scrollbar>
              <em>文章目录</em>
              <el-divider></el-divider>
              <MdCatalog :editor-id="'id'" :scroll-element="scrollElement"></MdCatalog>
            </el-scrollbar>
          </div>

        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <Footer></Footer>
  </div>
  <LoginDialog :clicked="clicked"></LoginDialog>

</template>

<script setup lang="ts">
import Footer from '@/components/layout/Footer.vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import { onMounted, provide, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  type CommonResponse,
} from '@/http/ResponseTypes/CommonResponseType'
import { doGet } from '@/http/BackendRequests'
import { ARTICLE_DETAIL_URL } from '@/http/URL'
import {
  type ArticleDetailResponse,
  defaultArticleDetailResponse
} from '@/http/ResponseTypes/ArticleDetailResponseType'
import ArticleDetail from '@/components/article/ArticleDetail.vue'
import { MdCatalog } from 'md-editor-v3'
import UserCard from '@/components/user/UserCard.vue'
import SideRecommendBar from '@/views/article-detail/SideRecommendBar.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { useGlobalStore } from '@/stores/global'
import CommentList from '@/views/article-detail/CommentList.vue'
import { setTitle } from '@/util/utils'

const route = useRoute()
// let global = reactive<GlobalResponse>({...defaultGlobalResponse})
let articleVo = reactive<ArticleDetailResponse>({...defaultArticleDetailResponse})
const globalStore = useGlobalStore()

const global = globalStore.global
const articleId = route.params.articleId

const scrollElement = document.documentElement;

const clicked = ref(false)


const changeClicked = () => {
  clicked.value = !clicked.value
  console.log("clicked: ", clicked.value)
}

provide('loginDialogClicked', changeClicked)

//为了子组件评论时能够无缝更新，重新渲染，提供一个更新函数
const getArticleDetail = (response: ArticleDetailResponse) => {
  Object.assign(articleVo.comments, response.comments)
  console.log(articleVo)
}

provide('updateArticleComment', getArticleDetail)

// ifUsualArticle 为true时，表示文章正常，为false时，表示文章是专栏文章，需要重定向，引入此变量从而避免在重定向过程中的闪烁情况
const ifUsualArticle = ref(false)
const router = useRouter()
onMounted(async () => {
  doGet<CommonResponse>(ARTICLE_DETAIL_URL + `/${articleId}`, {})
    .then((response) => {
      console.log(response)
      if (!response.data.redirect) {
        globalStore.setGlobal(response.data.global)
        console.log("global: ", global)
        Object.assign(articleVo, response.data.result)
        setTitle(articleVo.article.title)
        ifUsualArticle.value = true
      }else{
        router.replace("/column/" + response.data.result.columnId + '/' + response.data.result.sectionId)
      }
    })
})
</script>

<style scoped>

div.layout-main{
  padding: 0 60px 0;
}


@media (max-width: 768px) {
  div.layout-side {
    display: none;
  }
  div.layout-main{
    padding: 0 ;
  }
}

div#content-menu{
  height: calc(100vh - 70px);
}
</style>
