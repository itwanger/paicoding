<template>
  <HeaderBar />

  <div class="chat-page-wrapper">
    <div class="chat-container">
      <div class="chat-view">
        <ChatSidebar />
        <ChatWindow />
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import ChatSidebar from '@/components/chatv2/ChatSidebar.vue'
import ChatWindow from '@/components/chatv2/ChatWindow.vue'
import { useChatStore } from '@/stores/chatStore'
import { getChatModels, getConversations, getDefaultModel } from '@/http/BackendRequests'

const chatStore = useChatStore()

onMounted(async () => {
  try {
    // 加载模型列表
    const modelsResponse = await getChatModels<any>()
    if (modelsResponse.data.status?.code === 0) {
      chatStore.setModels(modelsResponse.data.result)
    }

    // 加载默认模型
    const defaultModelResponse = await getDefaultModel<any>()
    if (defaultModelResponse.data.status?.code === 0) {
      chatStore.setSelectedModel(defaultModelResponse.data.result)
    }

    // 加载会话列表
    const conversationsResponse = await getConversations<any>()
    if (conversationsResponse.data.status?.code === 0) {
      chatStore.setConversations(conversationsResponse.data.result)
    }
  } catch (error) {
    console.error('Failed to initialize chat:', error)
  }
})
</script>

<style scoped>
.chat-page-wrapper {
  background: white;
  min-height: calc(100vh - 60px); /* 减去 Header 高度 */
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-view {
  display: flex;
  flex: 1;
  width: 100%;
  background: white;
  overflow: hidden;
  min-height: 0; /* 关键：允许 flex 子元素缩小 */
}
</style>
