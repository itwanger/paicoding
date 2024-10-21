<template>
  <!-- 文章的侧边栏点赞收藏评论浮窗 -->
  <div class="article-suspended-panel hidden-when-screen-small">
    <!-- 点赞 -->
    <div class="mb-4">
      <el-badge :offset="[-5, 5]" :type="praised? 'danger': 'primary'" :value="praiseCnt" class="item" :hidden="praiseCnt === 0" >
        <el-button circle round size="large" @click="likeArticle">
          <el-icon v-show="!btnLoading" size="20"><svg t="1719494245215" class="icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" p-id="2214" id="mx_n_1719494245217" width="16" height="18"><path d="M621.674667 408.021333c16.618667-74.24 28.224-127.936 34.837333-161.194666C673.152 163.093333 629.941333 85.333333 544.298667 85.333333c-77.226667 0-116.010667 38.378667-138.88 115.093334l-0.586667 2.24c-13.728 62.058667-34.72 110.165333-62.506667 144.586666a158.261333 158.261333 0 0 1-119.733333 58.965334l-21.909333 0.469333C148.437333 407.808 106.666667 450.816 106.666667 503.498667V821.333333c0 64.8 52.106667 117.333333 116.394666 117.333334h412.522667c84.736 0 160.373333-53.568 189.12-133.92l85.696-239.584c21.802667-60.96-9.536-128.202667-70.005333-150.186667a115.552 115.552 0 0 0-39.488-6.954667H621.674667z" :fill="praised? '#ED722E': '#8a8a8a'" p-id="2215"></path></svg></el-icon>
          <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
        </el-button>
      </el-badge>
    </div>
    <!-- 评论  -->
    <div class="mb-4 p-0">
      <el-badge :offset="[-5, 5]" :type="commented?  'danger': 'primary'" :value="commentCnt" :hidden="commentCnt === 0" class="item">
        <el-button circle round size="large">
          <el-icon v-show="!btnLoading" size="20" :color="commented? '#ED722E': '#8a8a8a'"><Comment/></el-icon>
          <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
        </el-button>
      </el-badge>
    </div>
    <!-- 收藏 -->
    <div class="mb-4">
      <el-badge :offset="[-5, 5]" :type="collected? 'danger': 'primary'" :value="collectCnt" class="item" :hidden="collectCnt === 0">
        <el-button circle round size="large" @click="collectArticle">
          <template #default>
            <el-icon  v-show="!btnLoading" size="20" :color="collected? '#ED722E': '#8a8a8a'"><StarFilled/></el-icon>
            <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
          </template>
        </el-button>
      </el-badge>
    </div>
  </div>
  <!-- 文章内容 -->
  <section class="article-info-wrap com-2-panel col-2-article J-articlePanel">
    <span class="com-2-mark-triangle article-mark">
      <span class="mark-cnt">原创</span>
    </span>
    <div id="postsTitle" class="article-info-title">
      {{articleVo.article.title}}
    </div>
    <div>
      <div class="detail-content-title-other-wrap relative flex justify-between" v-if="articleVo.article && Object.keys(articleVo.article).length !== 0" >
        <div id="article-basic-info" class="flex justify-between" style="color: #999999">
          <div class="flex">
            <p class="center-content mr-2">
              <el-avatar :src="articleVo.article.authorAvatar" size="small"></el-avatar>
            </p>
            <p class="center-content mr-2">
              <el-link
                v-if="articleVo.article.author"
                :href="'/user/' + articleVo.article.author"
                class="detail-content-title-other-name"
                type="primary"
              >
                {{articleVo.article.authorName}}
              </el-link>
            </p>
            <span class="detail-content-title-other-time p-1">
            {{ format(new Date(Number(articleVo.article.createTime)), 'yyyy年MM月dd日')}}
            </span>
            <span class="p-1">{{'阅读 ' + articleVo.article.count.readCount}}</span>
          </div>

          <span class="p-1" v-if="articleVo.article.status !== 1" style="padding-left: 1em; color: red">{{articleVo.article.status == 0? '(草稿)' : '(审核中)'}}</span>
          <div class="flex" v-if="global.isLogin && articleVo.article.author == global.user.id">
            <p @click="() => {router.push('/article/edit/'+articleVo.article.articleId)}" class="p-1 edit-delete-btn flex">
              <span class="center-content">
                <el-icon :size="20">
                  <Edit />
                </el-icon>
              </span>
              <span>编辑</span>
            </p>
            <p @click="deleteDialog=true" class="p-1 edit-delete-btn flex">
              <span class="center-content">
                <el-icon :size="20">
                  <Delete />
                </el-icon>
              </span>
              <span>删除</span>
            </p>
          </div>
        </div>
        <!--    文章的tag    -->
        <div>
          <el-tag class="m-1" v-for="tagItem in articleVo.article.tags" :type="getRandomElTagType()" :key="tagItem.tagId">{{tagItem.tag}}</el-tag>
        </div>
      </div>
    </div>

    <div class="flex">
      <MdPreview :editor-id="'id'" :model-value="articleVo.article.content"></MdPreview>
    </div>

    <!-- 左右切换 -->
    <div class="article-change direction" v-if="articleVo.other && articleVo.other.flip">
      <a class="step-btn--prev"
         :href="articleVo.other.flip.prevHref"
         v-if="articleVo.other.flip.prevShow"
      >
        <div class="article-change-item">
          <svg
            t="1670064682276"
            class="icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            p-id="9458"
            width="32"
            height="32"
          >
            <path
              d="M671.968176 911.99957c-12.287381 0-24.576482-4.67206-33.951566-14.047144L286.048434 545.984249c-18.751888-18.719204-18.751888-49.12028 0-67.872168L638.016611 126.111222c18.751888-18.751888 49.12028-18.751888 67.872168 0 18.751888 18.719204 18.751888 49.12028 0 67.872168l-318.016611 318.047574L705.888778 830.047574c18.751888 18.751888 18.751888 49.12028 0 67.872168C696.544658 907.32751 684.255557 911.99957 671.968176 911.99957z"
              p-id="9459"
              fill="#ffffff"
            ></path>
          </svg>
        </div>
      </a>
      <a class="step-btn--next"
         :href="articleVo.other.flip.nextHref"
         v-if="articleVo.other.flip.nextShow"
      >
        <div class="article-change-item">
          <svg
            t="1670064662589"
            class="icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            p-id="8352"
            width="32"
            height="32"
          >
            <path
              d="M761.055557 532.128047c0.512619-0.992555 1.343475-1.823411 1.792447-2.848649 8.800538-18.304636 5.919204-40.703346-9.664077-55.424808L399.935923 139.743798c-19.264507-18.208305-49.631179-17.344765-67.872168 1.888778-18.208305 19.264507-17.375729 49.631179 1.888778 67.872168l316.960409 299.839269L335.199677 813.631716c-19.071845 18.399247-19.648112 48.767639-1.247144 67.872168 9.407768 9.791372 21.984142 14.688778 34.560516 14.688778 12.000108 0 24.000215-4.479398 33.311652-13.439914l350.048434-337.375729c0.672598-0.672598 0.927187-1.599785 1.599785-2.303346 0.512619-0.479935 1.056202-0.832576 1.567101-1.343475C757.759656 538.879828 759.199462 535.391265 761.055557 532.128047z"
              p-id="8353"
              fill="#ffffff"
            ></path>
          </svg>
        </div>
      </a>
    </div>

    <div v-if="articleVo.other && articleVo.other.readType === 1 && !global.isLogin">
      <div class="needlock">
        <a class="btn-readmore no-login underline" data-target="#loginModal" data-toggle="modal">
          <span>登录之后即可阅读全文</span>
        </a>
      </div>
    </div>

    <div v-if="articleVo.other && articleVo.other.readType === 3 && !(global.user != null && global.user.starStatus == 'FORMAL')">
      <div class="needlock">
        <a class="btn-readmore no-login underline" href="#" target="_blank" data-target="#registerModal" data-toggle="modal">
          <H2>已加入二哥编程星球，即刻绑定星球编号解锁🔐</H2>
        </a>
      </div>
    </div>



    <!-- 删除文章再次确认 Modal -->
    <div class="modal fade"
         id="deleteModal"
         data-backdrop="static"
         data-keyboard="false"
         tabindex="-1"
         role="dialog"
         aria-labelledby="deleteModalDropLabel"
         aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="deleteModalDropLabel">删除提醒</h5>
            <button type="button"
                    class="close"
                    data-dismiss="modal"
                    aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <p >{{'确定删除《' + articleVo.article.title + '》吗'}}</p>
          </div>
          <div class="modal-footer">
            <button id="deleteBtn" type="button" class="btn btn-primary">
              确定
            </button>
            <button type="button"
                    class="btn btn-secondary"
                    data-dismiss="modal">
              取消
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="article-heart bg-color-white">
      <el-button circle round size="large" @click="likeArticle">
        <el-icon v-show="!btnLoading" size="20"><svg t="1719494245215" class="icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" p-id="2214" id="mx_n_1719494245217" width="16" height="18"><path d="M621.674667 408.021333c16.618667-74.24 28.224-127.936 34.837333-161.194666C673.152 163.093333 629.941333 85.333333 544.298667 85.333333c-77.226667 0-116.010667 38.378667-138.88 115.093334l-0.586667 2.24c-13.728 62.058667-34.72 110.165333-62.506667 144.586666a158.261333 158.261333 0 0 1-119.733333 58.965334l-21.909333 0.469333C148.437333 407.808 106.666667 450.816 106.666667 503.498667V821.333333c0 64.8 52.106667 117.333333 116.394666 117.333334h412.522667c84.736 0 160.373333-53.568 189.12-133.92l85.696-239.584c21.802667-60.96-9.536-128.202667-70.005333-150.186667a115.552 115.552 0 0 0-39.488-6.954667H621.674667z" :fill="praised? '#ED722E': '#8a8a8a'" p-id="2215"></path></svg></el-icon>
        <el-icon v-show="btnLoading" class="is-loading" size="20"><Loading /></el-icon>
      </el-button>

      <div class="praise-photos">
        <p class="approval-tips-line"
           id="praiseDesc"
           data-defult-text="真诚点赞 诚不我欺">
          {{praiseCnt > 0? praiseCnt + '人已点赞': '真诚点赞 诚不我欺'}}
        </p>
        <div class="approval-box" id="praiseUsers" >
          <a class="g-user-popover approval-img" :href="'/user/' + item.userId" v-for="(item, id) in praisedUsers" :key="id">
            <img :src="item.avatar">
          </a>
        </div>
      </div>
    </div>
  </section>

  <el-dialog
    v-if="global.user && global.user.id == articleVo.article.author"
    v-model="deleteDialog"
    title="删除提醒"
    width="500"
    center
  >
    <div>
      <strong>确定删除 《{{articleVo.article.title}}》 吗？</strong>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="danger" @click="deleteArticle">
          确认
        </el-button>
        <el-button @click="deleteDialog = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">

import { format } from 'date-fns'
import '@/assets/md-preview.css'
import { inject, ref, watch } from 'vue'
import { Comment, Delete, Edit, Loading, StarFilled } from '@element-plus/icons-vue'
import type { ArticleDetailResponse } from '@/http/ResponseTypes/ArticleDetailResponseType'
import { MdPreview } from 'md-editor-v3'
import { useGlobalStore } from '@/stores/global'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { ARTICLE_DELETE_URL, ARTICLE_LIKE_COLLECT_URL } from '@/http/URL'
import { OperateTypeEnum } from '@/constants/OperateTypeConstants'
import type { SimpleUserInfo } from '@/http/ResponseTypes/UserInfoType/SimpleUserInfoType'
import { useRouter } from 'vue-router'
import { getRandomElTagType } from '@/constants/element-plus-constants/ELTagEnumConstants'

const globalStore = useGlobalStore()
const global = globalStore.global
const router = useRouter()

const showLoginDialog = inject<() => void>('loginDialogClicked')

const props = defineProps<{
  articleVo: ArticleDetailResponse,
}>()


// ========= 删除文章 ========
const deleteDialog = ref(false)

const deleteArticle = () => {
  doGet<CommonResponse>(ARTICLE_DELETE_URL, {
    articleId: props.articleVo.article.articleId,
  })
    .then((response) => {
      router.push('/')
    }).catch((error) => {
    console.error(error)
  })
}

// ========= 点赞、收藏、评论 ============
const btnLoading = ref(false)
const praiseCnt = ref( props.articleVo.article.count.praiseCount)
const commentCnt = ref(props.articleVo.article.count.commentCount)
const collectCnt = ref(props.articleVo.article.count.collectionCount)
const praised = ref(props.articleVo.article.praised)
const commented = ref(props.articleVo.article.commented)
const collected = ref(props.articleVo.article.collected)
const praisedUsers = ref<SimpleUserInfo[]>(props.articleVo.article.praisedUsers? props.articleVo.article.praisedUsers: [])

watch(() => props.articleVo.article, (newVal) => {
  console.log(newVal)
  praiseCnt.value = newVal.count.praiseCount
  commentCnt.value = newVal.count.commentCount
  collectCnt.value = newVal.count.collectionCount
  praised.value = newVal.praised || false
  commented.value = newVal.commented || false
  collected.value = newVal.collected || false
  praisedUsers.value = newVal.praisedUsers || []
})




// ========= 点赞 ============
const likeArticle = () => {
  if (!global.isLogin) {
    if (showLoginDialog) {
      showLoginDialog()
    }else{
      console.error('showLoginDialog is not defined')
    }
    return
  }
  btnLoading.value = true
  if(praised.value){
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.CANCEL_PRAISE,
    })
      .then((response) => {
        praiseCnt.value --
        praised.value = false
        praisedUsers.value = praisedUsers.value?.filter((item) => item.userId !== global.user.id)
      }).catch((error) => {
      console.error(error)
    })
      .finally(() => {
        btnLoading.value = false
      })
  }else {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.PRAISE,
    })
      .then((response) => {
        praiseCnt.value++
        praised.value = true
        praisedUsers.value.push(
          {
            userId: global.user.id,
            avatar: global.user.photo,
            profile: global.user.profile,
            name: global.user.userName,
          }
        )
      })
      .catch((error) => {
        console.error(error)
      })
      .finally(() => {
        btnLoading.value = false
      })
  }
}

// ========= 收藏 ============

const collectArticle = () => {
  if (!global.isLogin) {
    if (showLoginDialog) {
      showLoginDialog()
    }else{
      console.error('showLoginDialog is not defined')
    }
    return
  }
  btnLoading.value = true
  if(collected.value){
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.CANCEL_COLLECTION,
    })
      .then((response) => {
        collectCnt.value --
        collected.value = false
      }).catch((error) => {
      console.error(error)
    })
      .finally(() => {
        btnLoading.value = false
      })
  }else {
    doGet<CommonResponse>(ARTICLE_LIKE_COLLECT_URL, {
      articleId: props.articleVo.article.articleId,
      type: OperateTypeEnum.COLLECTION,
    })
      .then((response) => {
        collectCnt.value++
        collected.value = true
      })
      .catch((error) => {
        console.error(error)
      })
      .finally(() => {
        btnLoading.value = false
      })
  }
}
</script>

<style scoped>
.edit-delete-btn:hover{
  cursor: pointer;
  color: #E9A249;
}

#article-basic-info {
  color: #999999;
  font-size: 13px;
}

.el-badge{
  padding: 0;
}
</style>
