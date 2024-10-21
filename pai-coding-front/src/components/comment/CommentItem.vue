<template>
  <!-- 一级评论及其回复 -->
  <div class="comment-list-wrap">
    <div class="comment-item-wrap">
      <div class="comment-item-top">
        <a :href="'/user/' + comment.userId" target="_blank">
          <img :src="comment.userPhoto" class="comment-item-img" />
        </a>
        <div class="common-item-content">
          <div class="common-item-content-head">
          <span>
            <a
              :href="'/user/' + comment.userId"
              target="_blank"
              class="comment-name"
            >
              {{ comment.userName + (comment.userId == article.author ? '（作者）' : '')}}
            </a>
          </span>
            <span>{{format(new Date(Number(comment.commentTime)), 'yyyy年MM月dd日')}}</span>
          </div>
          <div class="common-item-content-value">
            {{ comment.commentContent}}
          </div>
          <div>
            <CommentAction :comment="comment" :article="article"></CommentAction>
          </div>
        </div>
      </div>
    </div>

    <!-- 二级评论 -->
    <div
      v-if="comment.childComments && comment.childComments.length > 0"
      class="comment-item-wrap-second"
    >
      <div class="comment-item-top" v-for="(reply,id) in comment.childComments" :key="id">
        <a :href="'/user/' + reply.userId" target="_blank">
          <img :src="reply.userPhoto" class="comment-item-img" />
        </a>
        <div class="common-item-content">
          <div class="common-item-content-head">
          <span>
            <a
              :href="'/user/' + reply.userId"
              target="_blank"
              class="comment-name"
            >
              {{ reply.userName + (reply.userId == article.author ? '（作者）' : '')}}
            </a>
          </span>
            <span>
            {{format(new Date(Number(reply.commentTime)), 'yyyy年MM月dd日')}}
          </span>
          </div>
          <div class="common-item-content-value">
            {{ reply.commentContent}}
          </div>
          <small
            v-if="reply.parentContent"
            style="
            display: flex;
            background: #f2f3f5;
            border: 1px solid #e4e6eb;
            box-sizing: border-box;
            border-radius: 4px;
            padding: 0 12px;
            line-height: 36px;
            height: 36px;
            font-size: 14px;
            color: #8a919f;
            margin-top: 8px;
          "
          >
            <span>{{reply.parentContent}}</span>
          </small>
          <div>
            <SubCommentAction :comment="comment" :reply="reply" :article="article"></SubCommentAction>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { format } from 'date-fns'
import CommentAction from '@/components/comment/CommentAction.vue'
import SubCommentAction from '@/components/comment/SubCommentAction.vue'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'

const props = defineProps<{
  comment: ArticleCommentType;
  article: ArticleType
}>()

</script>



<style scoped>

</style>
