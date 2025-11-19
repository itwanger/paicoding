import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Message {
  id?: number
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp?: Date
  isStreaming?: boolean
}

export interface Conversation {
  id: number
  conversationId: string
  modelName: string
  title: string
  updateTime: Date
  createTime: Date
  isEditing?: boolean
}

export interface ModelInfo {
  id: string
  name: string
  provider: string
  description: string
  enabled: boolean
  maxTokens: number
  temperature: number
}

export const useChatStore = defineStore('chat', () => {
  // State
  const conversations = ref<Conversation[]>([])
  const currentConversationId = ref<string | number | null>(null) // 支持 UUID 字符串或数字 ID
  const messages = ref<Message[]>([])
  const models = ref<ModelInfo[]>([])
  const selectedModelId = ref<string | null>(null)
  const isStreaming = ref(false)
  const isLoading = ref(false)

  // Computed
  const currentConversation = computed(() => {
    // currentConversationId 现在是 UUID 字符串，需要通过 conversationId 匹配
    if (typeof currentConversationId.value === 'string') {
      return conversations.value.find(c => c.conversationId === currentConversationId.value)
    }
    // 兼容旧的数字 ID（但不应该再使用）
    return conversations.value.find(c => c.id === currentConversationId.value)
  })

  const currentModel = computed(() =>
    models.value.find(m => m.id === selectedModelId.value)
  )

  const hasMessages = computed(() => messages.value.length > 0)

  // Actions
  function setConversations(convs: Conversation[]) {
    conversations.value = convs
  }

  function addConversation(conv: Conversation) {
    conversations.value.unshift(conv)
  }

  function updateConversation(id: number, updates: Partial<Conversation>) {
    const index = conversations.value.findIndex(c => c.id === id)
    if (index !== -1) {
      conversations.value[index] = { ...conversations.value[index], ...updates }
    }
  }

  function deleteConversation(id: number) {
    const deletedConv = conversations.value.find(c => c.id === id)
    conversations.value = conversations.value.filter(c => c.id !== id)

    // 如果删除的是当前会话，清空当前会话
    if (deletedConv && currentConversationId.value === deletedConv.conversationId) {
      currentConversationId.value = null
      messages.value = []
    }
  }

  function setCurrentConversation(id: number | null) {
    currentConversationId.value = id
  }

  function setCurrentConversationId(id: string | number | null) {
    currentConversationId.value = id
  }

  function setMessages(msgs: Message[]) {
    messages.value = msgs
  }

  function addMessage(message: Message) {
    messages.value.push(message)
  }

  function updateLastMessage(content: string) {
    if (messages.value.length > 0) {
      const lastMsg = messages.value[messages.value.length - 1]
      lastMsg.content += content
    }
  }

  function setModels(modelList: ModelInfo[]) {
    models.value = modelList
  }

  function setSelectedModel(modelId: string) {
    selectedModelId.value = modelId
  }

  function setStreaming(streaming: boolean) {
    isStreaming.value = streaming
  }

  function setLoading(loading: boolean) {
    isLoading.value = loading
  }

  function clearMessages() {
    messages.value = []
  }

  function reset() {
    conversations.value = []
    currentConversationId.value = null
    messages.value = []
    selectedModelId.value = null
    isStreaming.value = false
    isLoading.value = false
  }

  return {
    // State
    conversations,
    currentConversationId,
    messages,
    models,
    selectedModelId,
    isStreaming,
    isLoading,

    // Computed
    currentConversation,
    currentModel,
    hasMessages,

    // Actions
    setConversations,
    addConversation,
    updateConversation,
    deleteConversation,
    setCurrentConversation,
    setCurrentConversationId,
    setMessages,
    addMessage,
    updateLastMessage,
    setModels,
    setSelectedModel,
    setStreaming,
    setLoading,
    clearMessages,
    reset
  }
})
