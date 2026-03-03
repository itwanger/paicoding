<template>
  <HeaderBar />
  <div class="knowledge-page">
    <div class="knowledge-shell">
      <aside class="knowledge-sidebar">
        <div class="sidebar-title">知识分类</div>
        <el-scrollbar class="sidebar-scroll">
          <div
            v-for="l1 in categoryTree"
            :key="l1.categoryId"
            class="category-l1"
          >
            <div class="l1-title">{{ l1.categoryName }}</div>
            <div
              v-for="l2 in l1.children"
              :key="l2.categoryId"
              class="category-l2"
              :class="{ active: selectedCategoryId === l2.categoryId }"
              @click="selectCategory(l2.categoryId)"
            >
              <span>{{ l2.categoryName }}</span>
              <span class="doc-count">{{ l2.docCount || 0 }}</span>
            </div>
          </div>
        </el-scrollbar>
      </aside>

      <main class="knowledge-main">
        <div class="knowledge-toolbar">
          <el-input v-model="keyword" placeholder="搜索知识点，例如 MySQL redo log" clearable @keyup.enter="searchDocs" />
          <el-select v-model="selectedTagId" clearable placeholder="标签筛选" @change="loadDocs(1)">
            <el-option v-for="tag in tags" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId" />
          </el-select>
          <el-button type="primary" @click="searchDocs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>

        <div class="knowledge-content">
          <section class="doc-list">
            <div v-for="doc in docs" :key="doc.docId" class="doc-item" @click="openDoc(doc.docId)">
              <div class="doc-title">{{ doc.title }}</div>
              <div class="doc-desc">{{ doc.description }}</div>
              <div class="doc-tags">
                <el-tag v-for="tag in doc.tags || []" :key="tag.tagId" size="small" class="mr-1">{{ tag.tagName }}</el-tag>
              </div>
            </div>
            <el-empty v-if="docs.length === 0" description="暂无文档" />
            <el-pagination
              v-if="page.total > page.pageSize"
              class="doc-pagination"
              v-model:current-page="page.pageNum"
              v-model:page-size="page.pageSize"
              :total="page.total"
              layout="prev, pager, next"
              @current-change="loadDocs"
            />
          </section>

          <section class="doc-detail">
            <div v-if="activeDoc" class="detail-head">
              <h2>{{ activeDoc.title }}</h2>
              <p>{{ activeDoc.description }}</p>
            </div>
            <el-empty v-if="!activeDoc" description="请选择左侧文档" />
            <MdPreview v-else editor-id="knowledge-doc" :model-value="activeDoc.contentMd || ''" />
          </section>
        </div>
      </main>
    </div>

    <section class="knowledge-agent">
      <div class="agent-title">知识库 AI 助手</div>
      <el-input
        v-model="agentQuestion"
        type="textarea"
        :rows="3"
        placeholder="输入你的问题，AI 会基于知识库回答"
      />
      <div class="agent-actions">
        <el-button type="primary" :loading="agentLoading" @click="askAgent">提问</el-button>
      </div>
      <div v-if="agentAnswer" class="agent-answer">
        <MdPreview editor-id="knowledge-answer" :model-value="agentAnswer" />
        <div v-if="agentCitations.length > 0" class="agent-citations">
          引用:
          <span v-for="item in agentCitations" :key="item.docId" class="citation-item">#{{ item.docId }} {{ item.title }}</span>
        </div>
      </div>
    </section>

    <Footer />
    <LoginDialog :clicked="loginDialogClicked" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, provide, reactive, ref } from 'vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { MdPreview } from 'md-editor-v3'
import {
  askKnowledgeAgent,
  getKnowledgeDocDetail,
  getKnowledgeDocs,
  getKnowledgeTags,
  getKnowledgeTree,
  searchKnowledgeDocs
} from '@/http/BackendRequests'
import { useGlobalStore } from '@/stores/global'
import { messageTip } from '@/util/utils'

interface CategoryNode {
  categoryId: number
  categoryName: string
  docCount?: number
  children: CategoryNode[]
}

interface TagItem {
  tagId: number
  tagName: string
}

interface DocItem {
  docId: number
  title: string
  description: string
  contentMd?: string
  tags?: TagItem[]
}

interface CommonApi<T = any> {
  status: { code: number; msg: string }
  result: T
}

const categoryTree = ref<CategoryNode[]>([])
const tags = ref<TagItem[]>([])
const docs = ref<DocItem[]>([])
const activeDoc = ref<DocItem | null>(null)

const selectedCategoryId = ref<number | null>(null)
const selectedTagId = ref<number | null>(null)
const keyword = ref('')

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const agentQuestion = ref('')
const agentAnswer = ref('')
const agentCitations = ref<Array<{ docId: number; title: string }>>([])
const agentLoading = ref(false)

const globalStore = useGlobalStore()
const global = globalStore.global
const loginDialogClicked = ref(false)

const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
}
provide('loginDialogClicked', changeClicked)

onMounted(async () => {
  await Promise.all([loadTree(), loadTags()])
  await loadDocs(1)
})

const loadTree = async () => {
  const res = await getKnowledgeTree<CommonApi<CategoryNode[]>>()
  if (res.data.status.code === 0) {
    categoryTree.value = res.data.result || []
  }
}

const loadTags = async () => {
  const res = await getKnowledgeTags<CommonApi<TagItem[]>>()
  if (res.data.status.code === 0) {
    tags.value = res.data.result || []
  }
}

const selectCategory = (categoryId: number) => {
  selectedCategoryId.value = categoryId
  page.pageNum = 1
  loadDocs(1)
}

const loadDocs = async (pageNum = 1) => {
  page.pageNum = pageNum
  const res = await getKnowledgeDocs<CommonApi<any>>({
    categoryId: selectedCategoryId.value,
    tagId: selectedTagId.value,
    page: page.pageNum,
    size: page.pageSize
  })
  if (res.data.status.code === 0) {
    docs.value = res.data.result?.list || []
    page.total = Number(res.data.result?.total || 0)
    if (docs.value.length > 0 && (!activeDoc.value || !docs.value.find(it => it.docId === activeDoc.value?.docId))) {
      await openDoc(docs.value[0].docId)
    }
    if (docs.value.length === 0) {
      activeDoc.value = null
    }
  }
}

const searchDocs = async () => {
  if (!keyword.value) {
    await loadDocs(1)
    return
  }
  const res = await searchKnowledgeDocs<CommonApi<any>>({
    q: keyword.value,
    categoryId: selectedCategoryId.value,
    tagId: selectedTagId.value,
    page: page.pageNum,
    size: page.pageSize
  })
  if (res.data.status.code === 0) {
    docs.value = res.data.result?.list || []
    page.total = Number(res.data.result?.total || 0)
    if (docs.value.length > 0) {
      await openDoc(docs.value[0].docId)
    }
  }
}

const resetSearch = async () => {
  keyword.value = ''
  selectedTagId.value = null
  await loadDocs(1)
}

const openDoc = async (docId: number) => {
  const res = await getKnowledgeDocDetail<CommonApi<DocItem>>(docId)
  if (res.data.status.code === 0) {
    activeDoc.value = res.data.result
  }
}

const askAgent = async () => {
  if (!agentQuestion.value.trim()) {
    messageTip('请输入问题', 'warning')
    return
  }
  if (!global.isLogin) {
    messageTip('AI 调用需要先登录', 'warning')
    changeClicked()
    return
  }

  agentLoading.value = true
  try {
    const res = await askKnowledgeAgent<CommonApi<any>>({
      question: agentQuestion.value,
      contextDocIds: activeDoc.value ? [activeDoc.value.docId] : undefined,
      allowMutation: String(global.user.role || '').toUpperCase() === 'ADMIN'
    })
    if (res.data.status.code === 0) {
      agentAnswer.value = res.data.result?.answer || ''
      agentCitations.value = res.data.result?.citations || []
    }
  } finally {
    agentLoading.value = false
  }
}
</script>

<style scoped>
.knowledge-page {
  padding-top: 4px;
  background: linear-gradient(180deg, #f7fafc 0%, #ffffff 35%);
  min-height: 100vh;
}

.knowledge-shell {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 10px;
  width: calc(100% - 20px);
  margin: 0 10px;
  padding: 6px 0 12px;
}

.knowledge-sidebar,
.knowledge-main,
.knowledge-agent {
  background: #fff;
  border: 1px solid #e8eef5;
  border-radius: 14px;
}

.knowledge-sidebar {
  padding: 10px;
  height: calc(100vh - 110px);
  position: sticky;
  top: 54px;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 8px;
}

.sidebar-scroll {
  height: calc(100% - 28px);
}

.category-l1 {
  margin-bottom: 12px;
}

.l1-title {
  font-weight: 700;
  color: #1f2a37;
  margin-bottom: 6px;
}

.category-l2 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  color: #445;
  font-size: 13px;
}

.category-l2:hover,
.category-l2.active {
  background: #eef4ff;
  color: #165dff;
}

.doc-count {
  font-size: 12px;
  color: #8392a5;
}

.knowledge-main {
  padding: 14px;
}

.knowledge-toolbar {
  display: grid;
  grid-template-columns: 1fr 180px 90px 90px;
  gap: 10px;
  margin-bottom: 14px;
}

.knowledge-content {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 12px;
}

.doc-list {
  border-right: 1px solid #eef0f5;
  padding-right: 12px;
}

.doc-item {
  border: 1px solid #e8edf5;
  border-radius: 10px;
  padding: 10px;
  margin-bottom: 10px;
  cursor: pointer;
}

.doc-item:hover {
  border-color: #99b2ff;
}

.doc-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #122238;
}

.doc-desc {
  font-size: 13px;
  color: #5d6b7f;
  margin-bottom: 8px;
}

.doc-pagination {
  margin-top: 10px;
}

.doc-detail {
  min-height: 540px;
}

.detail-head h2 {
  margin: 0;
  font-size: 22px;
  color: #111827;
}

.detail-head p {
  margin: 8px 0 14px;
  color: #5b6574;
}

.knowledge-agent {
  width: calc(100% - 20px);
  margin: 10px 10px 16px;
  padding: 14px;
}

.agent-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 10px;
}

.agent-actions {
  margin-top: 10px;
}

.agent-answer {
  margin-top: 14px;
  border-top: 1px dashed #d8e0ef;
  padding-top: 12px;
}

.agent-citations {
  margin-top: 10px;
  font-size: 13px;
  color: #55657e;
}

.citation-item {
  margin-right: 10px;
}

@media (max-width: 900px) {
  .knowledge-shell {
    grid-template-columns: 1fr;
    margin: 0;
    padding: 12px;
  }

  .knowledge-sidebar {
    height: auto;
    position: static;
  }

  .knowledge-toolbar {
    grid-template-columns: 1fr 1fr;
  }

  .knowledge-content {
    grid-template-columns: 1fr;
  }

  .knowledge-agent {
    margin: 12px;
  }

  .doc-list {
    border-right: none;
    border-bottom: 1px solid #eef0f5;
    padding-right: 0;
    padding-bottom: 12px;
  }
}
</style>
