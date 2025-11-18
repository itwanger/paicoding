<template>
  <div class="empty-state">
    <div class="welcome-section">
      <div class="icon-wrapper">
        <el-icon :size="80" color="#409eff">
          <ChatDotRound />
        </el-icon>
      </div>
      <h1 class="app-title">开始对话</h1>
      <p class="subtitle">选择一个 AI 模型，然后在下方输入框发送消息即可开始对话</p>
    </div>

    <div class="model-selector-card">
      <el-form label-position="top">
        <el-form-item label="选择模型">
          <el-select
            v-model="chatStore.selectedModelId"
            placeholder="请选择一个模型"
            size="large"
            style="width: 100%"
          >
            <el-option
              v-for="model in chatStore.models"
              :key="model.id"
              :label="model.name"
              :value="model.id"
            >
              <div class="model-option">
                <div class="model-option-header">
                  <span class="model-option-name">{{ model.name }}</span>
                  <el-tag size="small" type="info">{{ model.provider }}</el-tag>
                </div>
                <div class="model-option-desc" v-if="model.description">
                  {{ model.description }}
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 选中模型后显示详细信息 -->
        <div v-if="chatStore.currentModel" class="selected-model-info">
          <el-divider />
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">提供商</span>
              <span class="info-value">{{ chatStore.currentModel.provider }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最大 Token</span>
              <span class="info-value">{{ chatStore.currentModel.maxTokens }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">温度</span>
              <span class="info-value">{{ chatStore.currentModel.temperature }}</span>
            </div>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chatStore'
import { ChatDotRound } from '@element-plus/icons-vue'

const chatStore = useChatStore()
</script>

<style scoped>
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: white;
  overflow-y: auto;
  min-height: 0;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.icon-wrapper {
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.app-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.subtitle {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
}

.model-selector-card {
  background: white;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  max-width: 500px;
  width: 90%;
}

.model-option {
  padding: 4px 0;
}

.model-option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.model-option-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.model-option-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.selected-model-info {
  margin-top: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .empty-state {
    padding: 40px 20px;
  }

  .app-title {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .model-selector-card {
    padding: 20px;
    width: 95%;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
