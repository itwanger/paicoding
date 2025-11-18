<template>
  <div class="chat-window">
    <!-- 空白状态或聊天内容 -->
    <div class="chat-content">
      <MessageList v-if="chatStore.messages.length > 0" />
      <EmptyState v-else />
      <ChatInput />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chatStore'
import EmptyState from './EmptyState.vue'
import MessageList from './MessageList.vue'
import ChatInput from './ChatInput.vue'

const chatStore = useChatStore()
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
  min-height: 0;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}
</style>
