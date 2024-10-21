<template>
  <footer class="footer-wrap">
    <div class="foot">
      <div>
        <a class="text-reset" href="https://beian.miit.gov.cn" target="_blank">
          <span>{{global.siteInfo.websiteRecord}}</span>
        </a>
        <a class="text-reset"
           href="http://xuyifei.site"
           target="_blank">
          小灰灰
          {{'© 2024-' + format(new Date(), 'yyyy') + ' 编程汇 '}}
        </a>
      </div>
      <div class="stats-container">
        <div class="stats-row">
          本站总PV&nbsp;&nbsp;
        <span class="visit_cnt">
          {{global.siteStatisticInfo.pv}}
        </span>
          &nbsp;&nbsp;总UV&nbsp;&nbsp;
        <span class="visit_cnt">
          {{global.siteStatisticInfo.uv}}
        </span>
        </div>
        <div class="stats-row">
          &nbsp;&nbsp;今日PV&nbsp;&nbsp;
        <span class="visit_cnt">
          {{global.todaySiteStatisticInfo.pv}}
        </span>
          &nbsp;&nbsp;UV&nbsp;&nbsp;
        <span class="visit_cnt" >
          {{global.todaySiteStatisticInfo.uv}}
        </span>
          &nbsp;&nbsp;此时&nbsp;&nbsp;<span class="visit_cnt">{{global.onlineCnt}}</span>&nbsp;&nbsp;人在线
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">

import { format }  from 'date-fns'
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()

const global = globalStore.global
</script>


<style scoped>

.footer-wrap{
  width: 100%;
  height: var(--footer-height);
}

</style>
