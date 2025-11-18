<template>
  <!-- Token 配额展示板块 -->
  <div class="card">
    <div class="card-body quota-wrap">
      <div class="quota-title">
        <span class="font-bold">LLM Token 配额</span>
      </div>

      <!-- Loading 状态 -->
      <div v-if="loading" class="loading-state">
        <span>加载中...</span>
      </div>

      <!-- 配额列表 -->
      <div v-else-if="quotas.length > 0" class="quota-list">
        <div v-for="quota in quotas" :key="quota.modelId" class="quota-item">
          <div class="quota-header">
            <span class="model-name">{{ quota.modelName }}</span>
            <span class="quota-percentage">{{ quota.usageRate }}%</span>
          </div>

          <!-- 进度条 -->
          <div class="progress-bar-container">
            <div
              class="progress-bar"
              :style="{
                width: quota.usageRate + '%',
                backgroundColor: getProgressColor(quota.usageRate)
              }"
            ></div>
          </div>

          <!-- 配额详情 -->
          <div class="quota-details">
            <span class="used">已用: {{ formatTokens(quota.usedQuota) }}</span>
            <span class="total">总计: {{ formatTokens(quota.totalQuota) }}</span>
          </div>
        </div>
      </div>

      <!-- 无配额状态 -->
      <div v-else class="empty-state">
        <span>暂无配额信息</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'

interface UserModelQuota {
  id: number
  userId: number
  modelId: string
  modelName: string
  totalQuota: number
  usedQuota: number
  remainingQuota: number
  totalUsed: number
  usageRate: number
  lastUsedTime: string | null
}

const props = defineProps<{
  userId: number
}>()

const quotas = ref<UserModelQuota[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const response = await doGet<CommonResponse>('/chatv2/api/quota/my-quotas')
    if (response.data.status.code === 0) {
      quotas.value = response.data.result || []
    }
  } catch (error) {
    console.error('Failed to load token quotas:', error)
  } finally {
    loading.value = false
  }
})

// 格式化 token 数量
const formatTokens = (tokens: number): string => {
  if (tokens >= 1000000) {
    return (tokens / 1000000).toFixed(1) + 'M'
  } else if (tokens >= 1000) {
    return (tokens / 1000).toFixed(1) + 'K'
  }
  return tokens.toString()
}

// 根据使用率返回进度条颜色
const getProgressColor = (usageRate: number): string => {
  if (usageRate >= 90) {
    return '#f56c6c' // 红色 - 配额即将用完
  } else if (usageRate >= 70) {
    return '#e6a23c' // 橙色 - 配额较少
  } else {
    return '#67c23a' // 绿色 - 配额充足
  }
}
</script>

<style scoped>
.quota-wrap {
  padding: 20px;
}

.quota-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.quota-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quota-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quota-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.model-name {
  font-weight: 500;
  color: #606266;
}

.quota-percentage {
  font-weight: 600;
  color: #303133;
}

.progress-bar-container {
  width: 100%;
  height: 8px;
  background-color: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease, background-color 0.3s ease;
}

.quota-details {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.used {
  color: #606266;
}

.total {
  color: #909399;
}
</style>
