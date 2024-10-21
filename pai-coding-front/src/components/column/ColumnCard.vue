<template>
  <!-- 专栏详情 -->
  <div>
    <a
      :href="'/column/' + column.columnId + '/1'"
      class="item"
    >
      <div class="poster">
        <img
          :src="column.cover"
          :alt="'「' + column.column + '」封面'"
          class="lazy poster-img"
          style=""
          loading="lazy"
        />
      </div>
      <div class="info">
        <div class="title">
          <span class="new-tag-wrap tag" v-if="column.type === 2" style="color: #f8f8f8">上新</span>
          <span class="text-highlight">
            {{column.column}}
          </span>
        </div>

        <div class="author">
            <a :href="'/user/' + column.author" class="flex">
              <div class="flex justify-around">
                <el-avatar size="small">
                  <img :src="column.authorAvatar"
                       class="lazy avatar hero"
                       loading="lazy"
                  />
                </el-avatar>
                <span class="name ml-2 center-content">
                  {{column.authorName}}
                </span>

                  <span class="self-description center-content">
                  |
                </span>

                  <span class="self-description center-content">
                    {{column.authorProfile != null && column.authorProfile ? column.authorProfile : '这人很懒，还没留下简介~'}}
                </span>
              </div>


            </a>
        </div>
        <div class="other">
          <div class="messages">
            <span class="message">
              <span>
                {{column.state == 2 ? '已完结 ' : '已更新 ' + column.count.articleCount + ' 小节 | '}}
              </span>
              <span>
                {{'共 ' + column.count.totalNums + ' 节 | '}}
              </span>
              <span>
                {{column.count.readCount + ' 人已阅读'}}
              </span>
            </span>
<!--            <span class="sale-tooltip" th:if="${column.type == 2 && new java.util.Date().getTime() < column.freeEndTime }" >-->
<!--              <span th:text="${column.type == 2} ? '限时免费': '免费'"></span>-->
<!--              <span class="count-down-text"-->
<!--                    th:data-free-endTime="${column.freeEndTime}"-->
<!--                    th:data-free-startTime="${column.freeStartTime}"-->
<!--                    th:text="''">-->
<!--                    剩余 1 天 12:50:43.92-->
<!--              </span>-->
<!--            </span>-->
          </div>
        </div>

        <span class="text-highlight desc">
          {{column.introduction}}
        </span>
      </div>
    </a>
  </div>
</template>

<script setup lang="ts">

import type { ColumnVoType } from '@/http/ResponseTypes/ColumnType/ColumnListVoType'

const props = defineProps<{
  column: ColumnVoType
}>()

</script>


<style scoped>

</style>
