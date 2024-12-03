import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { UserHomeTabTypeEnum } from '@/constants/UserHomeTabTypeConstants'
import {useGlobalSize} from "element-plus";
import {getGlobalStore, useGlobalStore} from "@/stores/global";
import {doGet} from "@/http/BackendRequests";
import type {CommonResponse} from "@/http/ResponseTypes/CommonResponseType";
import {GLOBAL_INFO_URL} from "@/http/URL";
import {messageTip} from "@/util/utils";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: HomeView,
    },
    {
      path: '/index',
      name: 'index',
      component: HomeView,
    },
    {
      path: '/article/detail/:articleId',
      name: "articleDetail",
      component: () => import('@/views/ArticleDetailView.vue')
    },
    {
      path: '/article/edit',
      name: "articleNew",
      component: () => import('@/views/ArticleEditView.vue')
    },
    {
      path: '/article/edit/:articleId',
      name: "articleEdit",
      component: () => import('@/views/ArticleEditView.vue')
    },
    {
      path: '/article/tag/:tagId',
      name: "tagArticles",
      component: () => import('@/views/TagArticlesView.vue')
    },
    // 作者个人信息页面
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/views/AboutView.vue')
    },
    // 后续更新计划
    {
      path: '/plan',
      name: 'plan',
      component: () => import('@/views/PlanView.vue')
    },
    {
      path: '/column',
      name: 'column',
      component: () => import('@/views/ColumnView.vue')
    },
    // ai聊天页
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/ChatView.vue')
    },
    // 工具页
    {
      path: '/tools',
      name: 'tools',
      component: () => import('@/views/ToolsView.vue'),
      redirect(to) {
        return { name: 'excel' }
      },
      children: [
        {
          path: 'excel',
          name: 'excel',
          component: () => import('@/views/tools/ToolsExcel.vue')
        }
      ]
    },
    {
      path: '/column/:columnId/:sectionId',
      name: 'columnDetail',
      component: () => import('@/views/ColumnDetailView.vue')
    },
    {
      path: '/user/:userId',
      redirect(to) {
        return { name: 'userHome', params: { userId: to.params.userId, typeName: UserHomeTabTypeEnum.ArticlesTab } }
      },
    },
    {
      path: '/user/:userId/:typeName',
      name: 'userHome',
      component: () => import('@/views/UserHomeView.vue'),
      meta: {
        loginRequired: true
      }
    },
    {
      path: '/notice',
      redirect(to) {
        return { name: 'notice', params: { noticeType: 'comment' } }
      },
    },
    {
      path: '/notice/:noticeType',
      name: "notice",
      component: () => import('@/views/NoticeView.vue'),
    }
  ]
})

router.beforeEach(async (to, from, next) => {
    // console.log(to)
    // console.log(from)
  if(to.meta.loginRequired) {
    const globalStore = await getGlobalStore()
    await checkLoginStatus(globalStore)
    if(!globalStore.global.isLogin){
      messageTip("请先登录", "warning")
      await router.replace("/")
    }else{
      next()
    }
  }
  next()
})

async function checkLoginStatus(globalStore: any)  {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
      .then((res) => {
        globalStore.setGlobal(res.data.global)
      })
}

export default router
