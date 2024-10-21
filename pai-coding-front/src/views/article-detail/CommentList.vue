
<template>

  <!-- 评论列表 -->
  <div
    class="comment-list-wrap bg-color-white"
    id="commentList">
    <div>
      <svg xmlns="http://www.w3.org/2000/svg" style="display:none;" class="">
        <symbol id="icon-comment" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd" clip-rule="evenodd"
                d="M4.62739 1.25C2.9347 1.25 1.5625 2.6222 1.5625 4.31489L1.56396 12.643C1.56403 14.3356 2.9362 15.7078 4.62885 15.7078H6.48326L6.93691 17.6869L6.93884 17.6948C7.16894 18.6441 8.28598 19.0599 9.08073 18.4921L12.7965 15.7078H15.5001C17.1928 15.7078 18.565 14.3355 18.565 12.6428L18.5635 4.31477C18.5635 2.62213 17.1913 1.25 15.4986 1.25H4.62739ZM5.98265 9.89255C6.68783 9.89255 7.2595 9.32089 7.2595 8.61571C7.2595 7.91053 6.68783 7.33887 5.98265 7.33887C5.27747 7.33887 4.70581 7.91053 4.70581 8.61571C4.70581 9.32089 5.27747 9.89255 5.98265 9.89255ZM9.95604 9.89255C10.6612 9.89255 11.2329 9.32089 11.2329 8.61571C11.2329 7.91053 10.6612 7.33887 9.95604 7.33887C9.25086 7.33887 8.6792 7.91053 8.6792 8.61571C8.6792 9.32089 9.25086 9.89255 9.95604 9.89255ZM15.2124 8.61571C15.2124 9.32089 14.6407 9.89255 13.9355 9.89255C13.2304 9.89255 12.6587 9.32089 12.6587 8.61571C12.6587 7.91053 13.2304 7.33887 13.9355 7.33887C14.6407 7.33887 15.2124 7.91053 15.2124 8.61571Z"
          ></path>
        </symbol>
        <symbol id="icon-zan" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd" clip-rule="evenodd"
                d="M13.0651 3.25923C12.6654 2.21523 12.1276 1.60359 11.4633 1.40559C10.8071 1.21 10.2539 1.48626 9.97848 1.67918C9.43962 2.05668 9.17297 2.64897 9.0009 3.12662C8.93522 3.30893 8.87504 3.50032 8.82077 3.67291L8.82077 3.67292C8.80276 3.73019 8.78541 3.78539 8.76872 3.8375C8.6974 4.06017 8.63455 4.23905 8.56561 4.38315C8.07104 5.41687 7.64014 6.034 7.2617 6.43277C6.89154 6.8228 6.5498 7.0275 6.18413 7.21038C5.8887 7.35813 5.69369 7.66144 5.69365 8.00211L5.69237 17.3908C5.6923 17.8783 6.08754 18.2736 6.57511 18.2736H14.8382C15.2621 18.2736 15.5829 18.1393 15.8149 17.9421C15.9234 17.8497 15.9985 17.7554 16.0484 17.6856C16.0695 17.6561 16.088 17.6282 16.0983 17.6126L16.1017 17.6075L16.1033 17.6051L16.1194 17.5857L16.1428 17.5478C16.913 16.3019 17.4472 15.3088 17.8659 14.1183C18.3431 12.7613 18.5849 11.5853 18.6874 10.6685C18.7871 9.77617 18.7612 9.07318 18.6558 8.68779C18.5062 8.14118 18.138 7.82653 17.7668 7.66617C17.4231 7.51771 17.0763 7.49836 16.8785 7.49807L13.1134 7.44551C13.662 5.19751 13.31 3.89889 13.0651 3.25923ZM1.251 8.0848C1.22726 7.5815 1.62891 7.16046 2.13277 7.16046H3.4408C3.92832 7.16046 4.32354 7.55568 4.32354 8.04321V17.4303C4.32354 17.9178 3.92832 18.313 3.4408 18.313H2.57554C2.10419 18.313 1.71599 17.9427 1.69378 17.4718L1.251 8.0848Z"
          ></path>
        </symbol>
      </svg>
    </div>
    <div class="comment-write-wrap">
      <img
        v-if="global.isLogin"
        :alt="global.user.userName"
        :src="global.user.photo"
        class="comment-write-img"
      />
      <!-- <div th:if="${!global.isLogin}"></div> -->
      <div class="common-write-content">
        <el-input
          @click="() => {if (!global.isLogin) {if(showLoginDialog) showLoginDialog()}}"
          v-model="textarea"
          :rows="2"
          resize="none"
          type="textarea"
          maxlength="512"
          :placeholder="global.isLogin ? '讨论应以学习和精进为目的。请勿发布不友善或者负能量的内容，与人为善，比聪明更重要！' : '请先登录后再评论'"
        />
        <p class="flex justify-end m-2">
          <el-button @click="commentSubmit" :disabled="textarea.length === 0 || isCommenting">
            评论<el-icon class="el-icon--right"><ChatSquare /></el-icon>
          </el-button>
        </p>
      </div>
    </div>

    <!-- 评论列表 -->
    <div :class="{'no-comment-box': comments && comments.length > 0}">
      <!-- TODO 热门评论中给评论点赞后，下方的全部评论的点赞数没有同时更新；反之亦然  -->
<!--      <div v-if="hotComment" class="comment-list-wrap hot-comment bg-color-white">-->
<!--        <h4 class="hot-comment-title flex">-->
<!--          <span>热门评论</span>-->
<!--          <el-icon size="20" color="#8a8a8a"><svg t="1719732315827" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5220" width="16" height="16"><path d="M410.882359 26.903266c-15.700967 50.217495-58.451969 140.455389-174.715923 260.132596-200.57132 206.715177-227.152033 539.72074 81.02211 703.471586 25.300743 13.439686 46.42025 5.162546 62.291879-11.4344 52.478775-54.782722 75.262244-135.164846 68.435737-241.061042-1.919955-29.183319 11.093074-36.777809 36.86314-16.810274 73.086295 56.57468 120.786515 143.740646 120.786515 237.43446 0 31.871256 12.757036 48.724196 42.495008 42.964331 196.518081-38.100444 436.000493-319.864536 177.915849-732.441577-15.444973-20.99151-38.271107-20.99151-37.887116 7.850484 0.426657 35.412507-3.626582 70.739683-12.117051 105.128213-5.845197 23.892776-28.330006 23.55145-33.620549 0-32.169916-143.313989-121.597163-265.12448-268.324405-365.644801-39.593743-27.092701-53.972074-18.772895-63.145194 10.453089z" fill="#F4420A" p-id="5221"></path></svg></el-icon>-->
<!--        </h4>-->

<!--        <CommentItem :comment="hotComment" :article="article"></CommentItem>-->
<!--        <hr />-->
<!--      </div>-->

      <!-- 全部评论列表 -->
      <div class="all-comment bg-color-white" v-if="comments && comments.length > 0">
        <h4 class="all-comment-title">
          全部
          <em >{{comments.length.toString()}}</em>
          条评论
        </h4>
        <div class="all-comment-item" v-for="(comment,id) in comments" :key="id">
          <CommentItem :comment="comment" :article="article"></CommentItem>
        </div>
      </div>
    </div>

    <!-- 二级评论 Modal -->
    <div
      class="modal fade"
      id="commentModal"
      data-backdrop="static"
      data-keyboard="false"
      tabindex="-1"
      role="dialog"
      aria-labelledby="commentModalDropLabel"
      aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="commentModalDropLabel">回复</h5>
            <button
              type="button"
              class="close"
              data-dismiss="modal"
              aria-label="Close"
            >
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <p id="repliedContent" class="comment-content-box-content"></p>
            <div class="input-group">
              <textarea
                id="replyContent"
                placeholder="请输入回复内容"
                class="posts-comment-input-box-textarea"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button
              id="replyBtn"
              data-reply-id=""
              type="button"
              class="btn btn-primary"
            >
              回复
            </button>
            <button
              type="button"
              class="btn btn-secondary"
              data-dismiss="modal"
            >
              取消
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import type { ArticleDetailResponse } from '@/http/ResponseTypes/ArticleDetailResponseType'
import CommentItem from '@/components/comment/CommentItem.vue'
import { useGlobalStore } from '@/stores/global'
import { inject, onMounted, ref } from 'vue'
import { ChatSquare, StarFilled } from '@element-plus/icons-vue'
import { doPost } from '@/http/BackendRequests'
import { COMMENT_SUBMIT_URL } from '@/http/URL'
import { messageTip } from '@/util/utils'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
// 全局的用户信息
const globalStore = useGlobalStore()
const global = globalStore.global
// 登录框的激活
const showLoginDialog = inject<() => void>('loginDialogClicked')

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
  }).then((response) => {
    messageTip('评论成功', 'success')
    textarea.value = ''
    if (updateArticleComment) {
      updateArticleComment(response.data.result)
    }else{
      console.error('updateArticle is not defined')
    }
  }).catch(() => {
    messageTip('评论失败', 'error')
  })
}

const props = defineProps<{
  hotComment: ArticleCommentType,
  comments: ArticleCommentType[],
  article: ArticleType,
}>()

// 评论区的内容
const textarea = ref('')


</script>

<style scoped>

</style>
