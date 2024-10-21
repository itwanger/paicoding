<template>

  <!-- 子评论的回复 -->
  <div class="action-box">
      <div
        :class="{'item': true, 'dig-item': true, 'active': commentPraised}" @click="likeComment"
      >
        <el-button text bg style="padding: 2px" :loading="btnLoading">
          <svg width="16" height="16">
            <use xlink:href="#icon-zan"></use>
          </svg>
          <span>{{praiseCnt > 0 ? praiseCnt: '点赞'}}</span>
        </el-button>
    </div>
    <div
      class="item reply-comment hf-con-block"
      @click="replyStatusChange"
    >
      <el-button text bg style="padding: 2px">
        <svg width="16" height="16">
          <use xlink:href="#icon-comment"></use>
        </svg>
        <span v-show="!replyEnabled" class="reply-comment-text">回复</span>
        <span v-show="replyEnabled" class="reply-comment-text">取消回复</span>
      </el-button>
    </div>
  </div>
  <div v-if="replyEnabled" class="mt-2">
    <el-input
      v-model="textarea"
      :rows="2"
      resize="none"
      type="textarea"
      :placeholder="'回复@' + reply.userName + (reply.userId == article.author ? '（作者）' : '')"
    />
    <p class="flex justify-end m-2">
      <el-button @click="commentSubmit" :disabled="textarea.length === 0 || isCommenting">
        评论<el-icon class="el-icon--right"><ChatSquare /></el-icon>
      </el-button>
    </p>
  </div>

</template>

<script setup lang="ts">
import type {ArticleDetailResponse } from '@/http/ResponseTypes/ArticleDetailResponseType'
import { inject, ref } from 'vue'
import { ChatSquare } from '@element-plus/icons-vue'
import { useGlobalStore } from '@/stores/global'
import { doGet, doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { COMMENT_LIKE_URL, COMMENT_SUBMIT_URL } from '@/http/URL'
import { OperateTypeEnum } from '@/constants/OperateTypeConstants'
import { messageTip } from '@/util/utils'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'
import type { SubCommentType } from '@/http/ResponseTypes/CommentType/SubCommentType'
const globalStore = useGlobalStore()
const global = globalStore.global
const showLoginDialog = inject<() => void>('loginDialogClicked')

const props = defineProps<{
  // comment是顶级的父评论
  comment: ArticleCommentType,
  reply: SubCommentType
  article: ArticleType
}>()

const textarea = ref('')

const replyEnabled = ref(false)

const replyStatusChange = () => {
  replyEnabled.value = !replyEnabled.value
}

// 评论点赞状态
const commentPraised = ref(props.reply.praised)

// 点赞相关变量
const btnLoading = ref(false)
const praiseCnt = ref( props.reply.praiseCount)

// ========= 点赞 ============
const likeComment = () => {
  if (!global.isLogin) {
    if (showLoginDialog) {
      showLoginDialog()
    }else{
      console.error('showLoginDialog is not defined')
    }
    return
  }
  btnLoading.value = true
  if(commentPraised.value){
    doGet<CommonResponse>(COMMENT_LIKE_URL, {
      commentId: props.reply.commentId,
      type: OperateTypeEnum.CANCEL_PRAISE,
    })
      .then((response) => {
        praiseCnt.value --
        commentPraised.value = false
      }).catch((error) => {
      console.error(error)
    })
      .finally(() => {
        btnLoading.value = false
      })
  }else {
    doGet<CommonResponse>(COMMENT_LIKE_URL, {
      commentId: props.reply.commentId,
      type: OperateTypeEnum.PRAISE,
    })
      .then((response) => {
        praiseCnt.value++
        commentPraised.value = true
      })
      .catch((error) => {
        console.error(error)
      })
      .finally(() => {
        btnLoading.value = false
      })
  }
}

// 更新文章的评论信息
const updateArticleComment = inject<(response: ArticleDetailResponse) => void>('updateArticleComment')
const isCommenting = ref(false)
const commentSubmit = () => {
  if (!global.isLogin) {
    if (showLoginDialog) {
      showLoginDialog()
    } else {
      console.error('showLoginDialog is not defined')
    }
    return
  }
  doPost<CommonResponse>(COMMENT_SUBMIT_URL, {
    articleId: props.article.articleId,
    commentContent: textarea.value,
    parentCommentId: Number(props.reply.commentId),
    topCommentId: Number(props.comment.commentId),
  }).then((response) => {
    messageTip('评论成功', 'success')
    textarea.value = ''
    replyEnabled.value = false
    if (updateArticleComment) {
      updateArticleComment(response.data.result)
    }else{
      console.error('updateArticle is not defined')
    }
  }).catch(() => {
    messageTip('评论失败', 'error')
  })
}

</script>

<style scoped>

</style>
