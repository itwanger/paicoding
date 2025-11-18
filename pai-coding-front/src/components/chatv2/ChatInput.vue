<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <div class="input-row">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          :autosize="{ minRows: 2, maxRows: 6 }"
          placeholder="输入消息...（Shift + Enter 换行，Enter 发送）"
          :disabled="chatStore.isStreaming"
          @keydown.enter.exact.prevent="sendMessage"
          @keydown.enter.shift.exact="handleShiftEnter"
          class="message-input"
        />

        <el-button
          type="primary"
          :disabled="!inputMessage.trim() || chatStore.isStreaming"
          @click="sendMessage"
          class="send-button"
        >
          <el-icon class="el-icon--left">
            <Promotion />
          </el-icon>
          发送
        </el-button>
      </div>

      <div class="input-info">
        <span v-if="chatStore.isStreaming" class="streaming-indicator">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
          AI 正在回复...
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import { sendChatMessage, getConversations, generateConversationId } from '@/http/BackendRequests'
import { ElMessage } from 'element-plus'
import { Promotion, Loading } from '@element-plus/icons-vue'
import type { Message } from '@/stores/chatStore'

const chatStore = useChatStore()
const inputMessage = ref('')

// Shift + Enter 换行
function handleShiftEnter(event: KeyboardEvent) {
  // 允许默认行为（换行）
}

// 发送消息
async function sendMessage() {
  const message = inputMessage.value.trim()
  if (!message || chatStore.isStreaming) return

  // 检查是否选择了模型
  if (!chatStore.selectedModelId) {
    ElMessage.warning('请先选择一个模型')
    return
  }

  // 如果没有当前会话，先从后端获取 conversationId
  let conversationId = chatStore.currentConversationId
  const isNewConversation = !conversationId
  if (!conversationId) {
    try {
      const response = await generateConversationId<any>()
      if (response.data.status?.code === 0) {
        conversationId = response.data.result
        chatStore.setCurrentConversationId(conversationId)
      } else {
        ElMessage.error('生成会话ID失败')
        return
      }
    } catch (error) {
      console.error('Failed to generate conversationId:', error)
      ElMessage.error('生成会话ID失败')
      return
    }
  }

  // 添加用户消息到界面
  const userMessage: Message = {
    role: 'user',
    content: message,
    timestamp: new Date()
  }
  chatStore.addMessage(userMessage)

  // 清空输入框
  inputMessage.value = ''

  // 添加一个空的 AI 消息用于流式更新
  const aiMessage: Message = {
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    isStreaming: true
  }
  chatStore.addMessage(aiMessage)
  chatStore.setStreaming(true)

  try {
    await sendChatMessage(
      message,
      conversationId,
      chatStore.selectedModelId,
      // onChunk: 接收流式数据（完整文本，参考 deepextract 实现）
      (responseText: string) => {
        // 参考 deepextract: 直接替换最后一条消息的内容
        // responseText 是累积的完整文本（包含 [HEARTBEAT]、[TOOL_EXECUTING] 等）
        if (chatStore.messages.length > 0) {
          const lastMsg = chatStore.messages[chatStore.messages.length - 1]
          // 移除特殊标记后显示
          lastMsg.content = responseText
            .replaceAll('[TOOL_EXECUTING]', '')
            .replaceAll('[HEARTBEAT]', '')
        }
      },
      // onComplete: 完成
      async () => {
        const lastMessage = chatStore.messages[chatStore.messages.length - 1]
        if (lastMessage) {
          lastMessage.isStreaming = false
          // 最后再清理一次，确保没有特殊标记
          lastMessage.content = lastMessage.content
            .replace('[DONE]', '')
            .replaceAll('[TOOL_EXECUTING]', '')
            .replaceAll('[HEARTBEAT]', '')
            .trim()
        }
        chatStore.setStreaming(false)

        // 如果是新对话，刷新对话列表以显示新创建的对话
        if (isNewConversation) {
          try {
            const conversationsResponse = await getConversations<any>()
            if (conversationsResponse.data.status?.code === 0) {
              chatStore.setConversations(conversationsResponse.data.result)

              // 找到新创建的对话（通过 conversationId 匹配）
              const newConv = conversationsResponse.data.result.find((c: any) => c.conversationId === conversationId)
              if (newConv) {
                // 注意：保持使用 UUID conversationId，不要切换到数字 ID
                // currentConversationId 应该始终是 UUID 字符串，用于后续消息发送
                // （不需要调用 setCurrentConversation，因为 conversationId 已经正确设置）
              }
            }
          } catch (error) {
            console.error('Failed to refresh conversations:', error)
          }
        }
      },
      // onError: 错误处理
      (error: Error) => {
        console.error('Chat error:', error)
        ElMessage.error('发送失败: ' + error.message)

        // 移除失败的 AI 消息
        chatStore.messages.pop()
        chatStore.setStreaming(false)
      }
    )
  } catch (error) {
    console.error('Failed to send message:', error)
    ElMessage.error('发送消息失败')
    chatStore.setStreaming(false)
  }
}
</script>

<style scoped>
.chat-input-container {
  border-top: 1px solid #e4e7ed;
  background: white;
  padding: 16px 20px;
  flex-shrink: 0;
}

.input-wrapper {
  max-width: 100%;
  width: 100%;
  box-sizing: border-box;
}

@media (min-width: 1200px) {
  .input-wrapper {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
}

.input-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  font-size: 14px;
  line-height: 1.6;
  transition: all 0.2s;
  resize: none;
}

.message-input :deep(.el-textarea__inner):focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-info {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  font-size: 13px;
}

.send-button {
  padding: 12px 24px;
  font-size: 14px;
  height: auto;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 1199px) {
  .input-wrapper {
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .chat-input-container {
    padding: 12px;
  }

  .input-wrapper {
    padding: 0;
  }

  .input-row {
    gap: 8px;
  }

  .send-button {
    padding: 10px 16px;
    font-size: 13px;
  }

  .message-input :deep(.el-textarea__inner) {
    font-size: 13px;
  }
}
</style>
