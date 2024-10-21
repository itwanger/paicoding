<script setup lang="ts">
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'

const props = defineProps<{
  side: SideBarItem
}>()
</script>

<template>
  <div class="home-right-item-wrap p-0">
    <div class="cdc-card mod-subscribe">
      <div class="cdc-card__inner">
        <div class="cdc-card__hd">
          <div class="cdc-card__title">订阅</div>
        </div>
        <div class="cdc-card__bd">
          <div class="cdc-card__bd">
            <div class="mod-subscribe__title">微信搜<em>{{side.subTitle}}</em>回复关键字<em>110</em></div>
            <div class="mod-subscribe__content">
              <div class="mod-subscribe__content-tag">将获得</div>
              <div class="mod-subscribe__content-inner">
                <div class="mod-subscribe__content-detail">
                  <div class="mod-subscribe__content-prize">
                    {{side.content}}
                  </div>
                  <div class="mod-subscribe__content-desc">编程汇良心推荐</div>
                </div>
                <div class="mod-subscribe__content-qr">
                  <img :src="side.img" :alt="side.subTitle"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
