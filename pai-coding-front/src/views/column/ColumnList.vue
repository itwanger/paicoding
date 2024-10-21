
<template>
    <ColumnCard v-for="(column, id) in filteredColumns" :key="id" :column="column"></ColumnCard>
<!--    <div th:replace="components/column/column-card :: column_card(${column})">-->
<!--      专栏详情-->
<!--    </div>-->
</template>

<script setup lang="ts">

import type { ColumnVoType } from '@/http/ResponseTypes/ColumnType/ColumnListVoType'
import ColumnCard from '@/components/column/ColumnCard.vue'
import { computed } from 'vue'

const props = defineProps<{
  columns: ColumnVoType[];
}>()

// 计算属性过滤掉 articleCount 为 0 的专栏
const filteredColumns = computed(() => {
  return props.columns.filter(column => column.count.articleCount !== 0);
});

</script>


<style scoped>

</style>
