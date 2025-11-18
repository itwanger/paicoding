<template>
  <div class="chat-header">
    <div class="header-left">
      <div class="title-section">
        <span class="conversation-title" v-if="chatStore.currentConversation">
          {{ chatStore.currentConversation.title || '新对话' }}
        </span>
        <el-tag
          v-if="chatStore.currentConversation"
          size="small"
          type="info"
          class="model-tag"
        >
          {{ getModelDisplayName(chatStore.currentConversation.modelName) }}
        </el-tag>
      </div>
    </div>

    <div class="header-right" v-if="chatStore.currentModel">
      <el-select
        v-model="chatStore.selectedModelId"
        placeholder="选择模型"
        size="default"
        style="width: 200px"
        @change="onModelChange"
      >
        <el-option
          v-for="model in chatStore.models"
          :key="model.id"
          :label="model.name"
          :value="model.id"
        >
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>{{ model.name }}</span>
            <el-tag size="small" type="info">{{ model.provider }}</el-tag>
          </div>
        </el-option>
      </el-select>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chatStore'
import { ElMessage } from 'element-plus'

const chatStore = useChatStore()

function onModelChange(newModelId: string) {
  // 如果当前有对话，提示用户创建新对话
  if (chatStore.currentConversationId) {
    ElMessage.info('切换模型后需要创建新对话')
  }
}

// 获取模型显示名称
function getModelDisplayName(modelId: string): string {
  const model = chatStore.models.find(m => m.id === modelId)
  return model ? model.name : modelId
}
</script>

<style scoped>
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: white;
  min-height: 56px;
  flex-shrink: 0;
}

.header-left {
  flex: 1;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.conversation-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.model-tag {
  font-size: 12px;
  height: 22px;
  line-height: 22px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
