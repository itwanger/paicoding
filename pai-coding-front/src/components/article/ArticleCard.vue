<template>
  <!-- 文章卡片 -->
  <div
    class="cdc-article-panel user-article-item"
  >
    <a
      class="cdc-article-panel__link"
      rel="noreferrer"
      @click="clickArticle"
    ></a>
    <div class="cdc-article-panel__inner">
      <div class="user-article-item-content cdc-article-panel__main">
        <div class="user-article-item-title-wrap cdc-article-panel__title">
          <img
            class="article-card-top-img"
            v-if="article.toppingStat === 1 && router.currentRoute.value.path === '/'"
            src="https://cdn.cnbj1.fds.api.mi-img.com/aife/technology-platform-fe/preview/dist/static/commandImg.99adc700.svg"
          />
          <span class="user-article-item-title">{{article.title}}</span>
          <!--          TODO 看一下下面article.status的类型-->
          <span v-if="article.status != 1" >
            <span class="user-article-item-tag">
              {{article.status == 0 ? '(草稿)' : '(审核中)'}}
            </span>
          </span>
        </div>
        <div class="user-article-item-value-wrap cdc-article-panel__media">
          <div class="user-article-item-value-text cdc-article-panel__desc">
            {{article.summary}}
          </div>
        </div>
        <div class="user-article-item-footer cdc-article-panel__infos">
          <div class="user-article-item-footer-left cdc-article-panel__user">
            <span
              class="article-title-other-name cdc-avatar large cdc-article-panel__user-avatar circle"
            >
              <a
                class="cdc-avatar__inner"
                :style="{'background-image': 'url(' +article.authorAvatar + ')' }"
                :href="'/user/' + article.author"
              ></a>
            </span>
            <a class="article-title-wrap cdc-article-panel__user-name" :href="'/user/' + article.author">{{article.authorName}}</a>
          </div>
          <div class="cdc-article-panel__date">
            {{format(new Date(Number(article.createTime)), "yyyy年MM月dd日 HH:mm")}}
          </div>

          <div class="cdc-icon__list cdc-article-panel__operate d-flex" @click="router.push('/article/detail/'+article.articleId)">
            <div class="article-show-wrap">
              <!--  阅读计数  -->
                <img
                  class="read-comment-praise"
                  src="data:image/png;base64,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"
                />
                <span class="cdc-icon__number">{{article.count.readCount}}</span>
            </div>
            <div class="article-show-wrap">
              <!--  评论计数  -->
                <img
                  class="read-comment-praise"
                  src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAABYlAAAWJQFJUiTwAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAKRSURBVHgB7VZNbtpQEJ55BlR15RvUOUHTG5ATJD1BQ9NU6gq8JKiKoyrJ0u2qUmkFOUGaE5TegJwg7gnqLRh7MvMwwiDbYHCUDZ9kHph5M9+bN38AO+zwzEAoiEv3xz4v/KAFiK/0S6J/qPCBoui+Y38cFlC3HgHH7Zk1FTXZUIt/mivEPVY6GFFw4difPNiWwNW3X+dLhj1iA3JqProXq7EQ8TVoz4A1147OWfPkAjYhcO1+twirt7FSEexPiG4+26cDyMEXt1s3AI7Z+LsZ4TEFB1newBzjf2B6Gi8kaqwyvEpHFgmVtjmxcTgm401R44I2G0M2KjpEV5W9KbG0koC+8znrt47d8GFDCAk+gJCQwNyvwqS1LLNwBbHbHuQ7G99bJ4rXgY4LRPGqz4T2koda8EAERl2zIrory7hArlBnDmdSDSbHyf8WrwDVNHKV0YOyQdGNXhAPMwlgnHKjaHQPJUNBOEjamL9fhI7SMt0/Q3uu08wjoJGWLk+FZQKefLyEwIKSETcxwTCbANFfWQLAIygZCFifrjkEQq73WgixCaUziHWi+p1JIC65Hj/mpdt1oCQkq2u7+f4uk4BAGo8minguFQy2hFRXvlonqTuXgK5aRLqHc/m8vXK7LdgCE6hYskolTGtqqWnYsU+dmITJrnCvv/7sOXKSDVCBiScrJgeVBHInIokDuYqEsB5KQqgMl7uk1A4DwrpCkFLLExL0x1HAWfXCr2H4X2TOWh+wEAGB7pBQcRITzgwymvmxEilcVtp+cX1cfs20Drv2VKyDSVUPI4Ij3lRPEfFJclzXEvIQlXhioZ4QYaNjn/Q3IrAMiQmDA0wB+QGEflr/ENK6xXOXFS8QRQdFx/YdnhyP1D0hcwr1KvEAAAAASUVORK5CYII="
                />
                <span class="cdc-icon__number">{{article.count.commentCount}}</span>
            </div>
            <div class="article-show-wrap">
              <!--  点赞计数  -->
              <!--  fixme 后续优化为可以直接在列表页进行点赞/取消  -->
                <img
                  class="read-comment-praise"
                  src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAABYlAAAWJQFJUiTwAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAJ9SURBVHgB7VZNbtNQEP7GP7AkN8DcoJyA5gRNTwCR2kqsUm9YEKEaoZRl0hUSBLWcAHOCpjdIT1AfIewgTjzM+AccxwHXLRYS/STnvbyxZ743b34e8L+DcEMMhu+fERk7DLTAuAwRjjz3eVD1+xsROD75eARmr7AczDlsVyVhoCbeDt85mXHmqLtkbst0Ko9jk31aVU9tAgvYW7FxYNJ3D85eufuTOZtKYiZu3X4z/LBdRU9tAia4E0+YL7I1z+3OmPEpkWOrip7aBED0RAcb7K8KeBb/alD+LQIa+TI48kxfuAfTVSlVMlybgAafpN1RYgujopwIOzpGEhuogGuloTc8bd2j5TmS8/VfHu7t5uUaeCaRygORPaqi00JFJAVn2UuNa667xXfEeJZ+rePR+Kool7gIQg67+RpBJTvo5QMoZHPXxFLW8Tld2lhoUqMOfg/JlKjdT2Pnpwf0bDlx3worG1EnPlENF0m5OayO5+7NyjRLHXgMfCsNQhumrBviRfRAhnqyu0KAYXmpcX/BfKKekL+dvBIheOUddkuNK7QO6A43ycXDvolYr5OtGTnlD3VU41rVZLdfccvIipMYDdYINAHZZJyicgR+4wQ0hbVH6Px7RBeNE9BM0lGbVxorTRNImhcxf8mvNxcDafOywBM0TUCqqEa/I09QbF6NECDQdjKuN6hGCJSlX2MENqXfGgFKS6hlmA9wi7hvLJ/qWEy/DL96AfMlEXWkU/mD0XiCkjud7kRk56iOltwRYz3ShM7KXlhpx4PheBR3qxzS6zbSi0YtyOZe9919D38ioNC2vIDl6NyQC2bWtzWVomvf93gWwg7KXH+HfwY/AGsn+Lf3Dim6AAAAAElFTkSuQmCC"
                />
                <span class="cdc-icon__number">{{article.count.praiseCount}}</span>
            </div>
          </div>

          <div
            class="user-article-item-tag-wrap cdc-tag-links cdc-article-panel__tags  hidden-when-screen-small"
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              class="cdc-tag-links__icon m-0"
            >
              <mask
                id="mask0_1118_14146"
                style="mask-type: alpha"
                maskUnits="userSpaceOnUse"
                x="0"
                y="0"
                width="16"
                height="16"
              >
                <rect width="16" height="16" fill="#D9D9D9" />
              </mask>
              <g mask="url(#mask0_1118_14146)">
                <path
                  fill-rule="evenodd"
                  clip-rule="evenodd"
                  d="M13.7262 8.18068L12.5948 3.6552L8.06934 2.52383L2.45929 8.13388L8.11614 13.7907L13.7262 8.18068ZM13.4194 2.83058L14.8337 8.48743L8.11614 15.2049L1.04508 8.13388L7.76259 1.41636L13.4194 2.83058Z"
                  fill="#495770"
                />
                <circle
                  cx="10.5911"
                  cy="5.659"
                  r="1"
                  transform="rotate(45 10.5911 5.659)"
                  fill="#495770"
                />
              </g>
            </svg>

            <div class="cdc-tag-links__item" v-for="tag in article.tags" :key="tag.tagId">
              <a
                :href="'/article/tag/' + tag.tag"
                rel="article-tag"
                class="user-article-item-tag cdc-tag-links__item ml-2 mr-0"
              >{{tag.tag}}</a>
            </div>
          </div>
        </div>
      </div>
      <div class="user-article-img cdc-article-panel__object" v-if="article.cover">
        <span
          class="cdc-article-panel__object-thumbnail"
          :style="{'background-image': 'url(' + article.cover + ')'}"
        ></span>
      </div>
<!--      <script type="text/javascript" th:inline="javascript">-->
<!--        // 有图的时候加一点 margin-->
<!--        $(-->
<!--          ".cdc-article-panel__main:has(+ .cdc-article-panel__object) .cdc-article-panel__infos"-->
<!--        ).css("margin-top", "20px")-->
<!--      </script>-->
    </div>
  </div>
</template>

<script setup lang="ts">

import { format } from 'date-fns';
import { useRouter } from 'vue-router'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import {ArticleTypeNumberEnum} from "@/constants/ArticleTypeEnumConstants";
import {doGet} from "@/http/BackendRequests";
import type {CommonResponse} from "@/http/ResponseTypes/CommonResponseType";
import {ARTICLE_COLUMN_RELATION_URL} from "@/http/URL";
import {messageTip} from "@/util/utils";

const router = useRouter()

const props = defineProps<{
  article: ArticleType
}>()

const clickArticle = () =>{
  console.log(props.article.articleType)
  if(props.article.articleType === ArticleTypeNumberEnum.COLUMN){
    doGet<CommonResponse>(`${ARTICLE_COLUMN_RELATION_URL}/${props.article.articleId}`, {}).then(res=>{
      router.push(`/column/${res.data.result.columnId}/${res.data.result.section}`)
    }).catch(err=>{
      messageTip("获取专栏信息失败", "error")
      console.log(err)
    })
  }else{
    router.push('/article/detail/'+props.article.articleId)
  }
}

</script>

<style scoped>

</style>
