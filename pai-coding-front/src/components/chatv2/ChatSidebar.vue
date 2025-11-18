<template>
  <!-- 移动端菜单触发按钮 -->
  <el-button class="mobile-menu-trigger" @click="drawerVisible = true" :icon="Menu">
    历史对话
  </el-button>

  <!-- 移动端抽屉 -->
  <el-drawer
    v-model="drawerVisible"
    :size="280"
    direction="ltr"
    :with-header="false"
    class="sidebar-drawer"
  >
    <div class="sidebar-content">
      <!-- 新建对话按钮 -->
      <div class="new-chat-button" @click="createNewChatAndClose">
        <el-icon><Plus /></el-icon>
        <span>新建对话</span>
      </div>

      <!-- 会话列表 -->
      <div class="conversations-list">
        <div
          v-for="conv in chatStore.conversations"
          :key="conv.id"
          :class="['conversation-item', { active: conv.conversationId === chatStore.currentConversationId }]"
          @click="selectConversationAndClose(conv.id)"
        >
          <div class="conversation-content">
            <div class="conversation-title" v-if="!conv.isEditing" @dblclick="startEditing(conv, $event)">
              {{ conv.title }}
            </div>
            <input
              v-else
              v-model="editingTitle"
              class="title-input"
              @blur="saveTitle(conv)"
              @keyup.enter.prevent="saveTitle(conv)"
              @keyup.esc="cancelEditing(conv)"
              @click.stop
              ref="titleInput"
            />
            <div class="conversation-time">
              {{ formatTime(conv.updateTime) }}
            </div>
          </div>

          <div class="conversation-actions">
            <el-icon @click="startEditing(conv, $event)" class="action-icon">
              <Edit />
            </el-icon>
            <el-icon @click.stop="deleteConv(conv.id)" class="action-icon">
              <Delete />
            </el-icon>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>

  <!-- 桌面端侧边栏 -->
  <div class="chat-sidebar desktop-sidebar">
    <!-- 新建对话按钮 -->
    <div class="new-chat-button" @click="createNewChat">
      <el-icon><Plus /></el-icon>
      <span>新建对话</span>
    </div>

    <!-- 会话列表 -->
    <div class="conversations-list">
      <div
        v-for="conv in chatStore.conversations"
        :key="conv.id"
        :class="['conversation-item', { active: conv.conversationId === chatStore.currentConversationId }]"
        @click="selectConversation(conv.id)"
      >
        <div class="conversation-content">
          <div class="conversation-title" v-if="!conv.isEditing" @dblclick="startEditing(conv, $event)">
            {{ conv.title }}
          </div>
          <input
            v-else
            v-model="editingTitle"
            class="title-input"
            @blur="saveTitle(conv)"
            @keyup.enter.prevent="saveTitle(conv)"
            @keyup.esc="cancelEditing(conv)"
            @click.stop
            ref="titleInput"
          />
          <div class="conversation-meta">
            <span class="conversation-time">{{ formatTime(conv.updateTime) }}</span>
            <el-tag size="small" type="info" class="model-tag-small">
              {{ getModelDisplayName(conv.modelName) }}
            </el-tag>
          </div>
        </div>

        <div class="conversation-actions">
          <el-icon @click="startEditing(conv, $event)" class="action-icon">
            <Edit />
          </el-icon>
          <el-icon @click.stop="deleteConv(conv.id)" class="action-icon">
            <Delete />
          </el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import { getConversation, updateConversationTitle, deleteConversation } from '@/http/BackendRequests'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Plus, Menu } from '@element-plus/icons-vue'

const chatStore = useChatStore()
const editingTitle = ref('')
const titleInput = ref<HTMLInputElement>()
const drawerVisible = ref(false)

// 获取模型显示名称
function getModelDisplayName(modelId: string): string {
  const model = chatStore.models.find(m => m.id === modelId)
  return model ? model.name : modelId
}

// 创建新对话
async function createNewChat() {
  // 检查是否选择了模型
  const modelId = chatStore.selectedModelId || chatStore.models[0]?.id
  if (!modelId) {
    ElMessage.error('请先选择模型')
    return
  }

  // 清空当前会话和消息
  // 会话会在用户发送第一条消息时自动创建
  chatStore.setCurrentConversationId(null)
  chatStore.clearMessages()
  ElMessage.success('已创建新对话，请发送消息开始聊天')
}

// 创建新对话并关闭抽屉（移动端）
async function createNewChatAndClose() {
  await createNewChat()
  drawerVisible.value = false
}

// 选择会话
async function selectConversation(id: number) {
  try {
    // 先找到会话
    const conv = chatStore.conversations.find(c => c.id === id)
    if (!conv) {
      ElMessage.error('会话不存在')
      return
    }

    // 设置当前会话的 conversationId（UUID 字符串），而不是数字 ID
    chatStore.setCurrentConversationId(conv.conversationId)

    // 加载会话消息 - 使用 conversationId
    const response = await getConversation<any>(conv.conversationId)
    if (response.data.status?.code === 0) {
      const conversation = response.data.result
      chatStore.setMessages(conversation.messages || [])
    }
  } catch (error) {
    console.error('Failed to load conversation:', error)
    ElMessage.error('加载会话失败')
  }
}

// 选择会话并关闭抽屉（移动端）
async function selectConversationAndClose(id: number) {
  await selectConversation(id)
  drawerVisible.value = false
}

// 开始编辑标题
function startEditing(conv: any, event?: Event) {
  // 阻止事件冒泡，避免触发会话选择
  if (event) {
    event.stopPropagation()
  }

  conv.isEditing = true
  editingTitle.value = conv.title
  nextTick(() => {
    // 找到对应的输入框并聚焦
    const inputs = document.querySelectorAll('.title-input')
    inputs.forEach((input: any) => {
      if (input.value === conv.title) {
        input.focus()
      }
    })
  })
}

// 保存标题
async function saveTitle(conv: any) {
  if (!editingTitle.value.trim()) {
    cancelEditing(conv)
    return
  }

  try {
    // 使用 conversationId 而不是 id
    const response = await updateConversationTitle<any>(conv.conversationId, editingTitle.value)
    if (response.data.status?.code === 0) {
      chatStore.updateConversation(conv.id, { title: editingTitle.value })
      conv.isEditing = false
      editingTitle.value = ''

      // 主动失焦，移除输入框焦点
      nextTick(() => {
        const activeElement = document.activeElement as HTMLElement
        if (activeElement && activeElement.classList.contains('title-input')) {
          activeElement.blur()
        }
      })

      ElMessage.success('标题已更新')
    }
  } catch (error) {
    console.error('Failed to update title:', error)
    ElMessage.error('更新标题失败')
    cancelEditing(conv)
  }
}

// 取消编辑
function cancelEditing(conv: any) {
  conv.isEditing = false
  editingTitle.value = ''
}

// 删除会话
async function deleteConv(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 使用 conversationId 而不是 id
    const conv = chatStore.conversations.find(c => c.id === id)
    if (!conv) {
      ElMessage.error('会话不存在')
      return
    }

    const response = await deleteConversation<any>(conv.conversationId)
    if (response.data.status?.code === 0) {
      chatStore.deleteConversation(id)
      ElMessage.success('对话已删除')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete conversation:', error)
      ElMessage.error('删除对话失败')
    }
  }
}

// 格式化时间
function formatTime(date: Date): string {
  const now = new Date()
  const diff = now.getTime() - new Date(date).getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return new Date(date).toLocaleDateString()
}
</script>

<style scoped>
/* 移动端菜单触发按钮 - 默认隐藏 */
.mobile-menu-trigger {
  display: none;
  position: fixed;
  top: 70px;
  left: 10px;
  z-index: 999;
}

/* 桌面端侧边栏 */
.chat-sidebar {
  width: 260px;
  background: #fafbfc;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

/* 抽屉内容样式 */
.sidebar-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fafbfc;
}

.new-chat-button {
  margin: 16px;
  padding: 10px 16px;
  background: #409eff;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  border: none;
}

.new-chat-button:hover {
  background: #337ecc;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px 12px 12px;
}

.conversation-item {
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border: 1px solid transparent;
}

.conversation-item:hover {
  background: #f0f2f5;
  border-color: #e4e7ed;
}

.conversation-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.title-input {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid #409eff;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}

.conversation-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  margin-top: 4px;
}

.conversation-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.model-tag-small {
  font-size: 10px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
  transform: scale(0.9);
  transform-origin: right center;
}

.conversation-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.conversation-item:hover .conversation-actions {
  opacity: 1;
}

.action-icon {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
  font-size: 16px;
}

.action-icon:hover {
  color: #409eff;
}

.conversations-list::-webkit-scrollbar {
  width: 6px;
}

.conversations-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.conversations-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  /* 隐藏桌面端侧边栏 */
  .desktop-sidebar {
    display: none !important;
  }

  /* 显示移动端菜单按钮 */
  .mobile-menu-trigger {
    display: inline-flex !important;
  }
}

@media (min-width: 769px) {
  /* 桌面端隐藏移动端菜单按钮 */
  .mobile-menu-trigger {
    display: none !important;
  }
}
</style>
