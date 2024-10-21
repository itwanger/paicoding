<template>
  <HeaderBar />
  <div class="flex mt-3">
    <el-text style="margin-left: 10px; height: 40px; width: 60px; font-size: medium"> 标题</el-text>
    <el-form
      ref="titleFormRef"
      style="width: 100%; height: 60px"
      :model="titleForm"
      :rules="titleFormRules"
      label-width="auto"
      :size="titleFormSize"
      status-icon
    >

      <el-form-item size="large" prop="title">
        <template #default>
          <el-input  v-model="titleForm.title" placeholder="请输入标题"></el-input>
        </template>
      </el-form-item>
    </el-form>
    <el-button @click="submitDialogVisible = true" :disabled="titleForm.title.length < 6 || titleForm.title.length > 40" type="success" style="height: 40px; width: 100px"> 保存</el-button>
  </div>

  <!--  正文内容-->

    <MdEditor
      style="height: calc(100vh - 72px - var(--footer-height) - var(--header-height))"
      :editor-id="'id'"
      v-model="text"
      @onUploadImg="onUploadImg"
      :toolbars-exclude="['prettier', 'github']"
    ></MdEditor>

    <Footer :global="global"></Footer>

  <el-dialog v-model="submitDialogVisible" style="min-width: 300px">
<!--    标题-->
    <template #header>
      <span class="font-medium text-xl">发布文章</span>
    </template>

    <template #default>
        <el-form
          ref="articleInfoFormRef"
          style="width: 100%; height: 450px"
          :model="articleInfoForm"
          :rules="articleInfoFormRules"
          label-width="auto"
          :size="articleInfoFormSize"
          status-icon
        >

          <el-form-item prop="category" label="分类">
            <template #default>
              <el-select
                v-model="articleInfoForm.category"
                clearable
                placeholder="请选择文章的分类"
                :loading="categoryLoading"
              >

                <el-option v-for="(item, id) in categoryOptions" :key="id" :label="item.category" :value="item.categoryId"></el-option>

                <template #loading>
                  <el-icon class="is-loading">
                    <Loading/>
                  </el-icon>
                </template>
              </el-select>
            </template>
          </el-form-item>

<!--       tags选项   -->
          <el-form-item prop="tags" label="添加标签">
            <template #default>
              <el-select
                v-model="articleInfoForm.tags"
                clearable
                multiple
                multiple-limit="6"
                placeholder="请选择文章的标签"
                filterable
                :loading="tagsLoading"
              >

                <el-option v-for="(item, id) in tagsOptions" :key="id" :label="item.tag" :value="item.tagId"></el-option>

                <template #loading>
                  <el-icon class="is-loading">
                    <Loading/>
                  </el-icon>
                </template>
              </el-select>
            </template>
          </el-form-item>

<!--      封面上传    -->
          <el-form-item prop="avatar" label="文章封面">
            <template #default>
              <el-upload
                :class = "{disabled: isMax}"
                ref="upload"
                :on-change="onUploadFile"
                list-type="picture-card"
                action="#"
                :auto-upload="false"
                :multiple="true"
                :limit="1"
                v-model:file-list="fileList"
              >
                <el-icon><plus /></el-icon>

                <template #file="{ file }">
                  <div>
                    <img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
                    <span class="el-upload-list__item-actions">
                      <span
                        class="el-upload-list__item-preview"
                        @click="handlePictureCardPreview(file)"
                      >
                        <el-icon><zoom-in /></el-icon>
                      </span>
                      <span
                        v-if="!disabled"
                        class="el-upload-list__item-delete"
                        @click="handleRemove(file)"
                      >
                        <el-icon><Delete /></el-icon>
                      </span>
                    </span>
                  </div>
                </template>

                <template #tip>
                  <div class="el-upload__tip">
                    jpg/png，小于500kb
                  </div>
                </template>
              </el-upload>
            </template>
          </el-form-item>

<!--      摘要填写    -->
          <el-form-item prop="abstract" label="文章摘要" style="margin-bottom: 0">
            <template #default>
              <el-input
                v-model="articleInfoForm.abstract"
                maxlength="256"
                :autosize="{minRows: 3, maxRows: 5}"
                rows="4"
                type="textarea"
                resize="none"
                show-word-limit
                placeholder="摘要（必填）：支持一键将正文前256字符键入摘要文本框"
              >
              </el-input>
            </template>
          </el-form-item>
          <div class="flex justify-end">
            <el-button size="small" round @click="extractAbstract">一键提取</el-button>
          </div>
        </el-form>
      <el-divider></el-divider>
      <div class="flex justify-center">
        <el-button :loading="saveBtnLoading" type="success" @click="postArticle('post')" style="width: 80px">发布</el-button>
        <el-button type="info" @click="postArticle('save')">存草稿</el-button>
      </div>

    </template>

  </el-dialog>
  <LoginDialog :global="global" :clicked="clicked"></LoginDialog>
  <el-dialog v-model="dialogVisible">
    <img w-full :src="dialogImageUrl" alt="Preview Image" />
  </el-dialog>

</template>

<script setup lang="ts">

import { MdEditor } from 'md-editor-v3'
import '@/assets/style.css'
import { onMounted, provide, reactive, ref } from 'vue'
import HeaderBar from '@/components/layout/HeaderBar.vue'
const text = ref('')
import { useGlobalStore } from '@/stores/global'
import Footer from '@/components/layout/Footer.vue'
import {
  type ComponentSize,
  type FormInstance,
  type FormRules,
  type UploadFile,
  type UploadFiles,
  type UploadInstance, type UploadUserFile
} from 'element-plus'
import { Delete, Loading, Plus, ZoomIn } from '@element-plus/icons-vue'
import { doFilePost, doGet, doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { ARTICLE_EDIT_URL, ARTICLE_UPLOAD_URL, CATEGORY_LIST_URL, FILE_UPLOAD_URL, TAG_LIST_URL } from '@/http/URL'
import type { ArticleCategoryType } from '@/http/ResponseTypes/CategoryType/ArticleCategoryType'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import type { ArticleTagType } from '@/http/ResponseTypes/TagType/ArticleTagType'
import { messageTip } from '@/util/utils'
import { ArticleTypeEnum } from '@/constants/ArticleTypeEnumConstants'
import { DocumentSourceTypeEnum } from '@/constants/DocumentSourceTypeEnumConstants'
import { useRoute, useRouter } from 'vue-router'
import type { ArticleEditResponseType } from '@/http/ResponseTypes/ArticleEditResponseType'
const globalStore = useGlobalStore()

const global = globalStore.global
const router = useRouter()
const route = useRoute()

const clicked = ref(false)

const changeClicked = () => {
  clicked.value = !clicked.value
  console.log("clicked: ", clicked.value)
}

provide('loginDialogClicked', changeClicked)


// ========  标题输入框 =========
interface TitleForm {
  title: string
}
const titleFormSize = ref<ComponentSize>('default')
const titleFormRef = ref<FormInstance>()
const titleForm = reactive<TitleForm>({
  title: '',
})
const titleFormRules = reactive<FormRules<TitleForm>>({
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 6, max: 40, message: '长度在 6 到 40 个字符', trigger: 'blur' },
  ],
})

onMounted(() => {
  // 如果带了articleId参数，说明是编辑文章，需要获取文章信息
  if(route.params.articleId){
    doGet<CommonResponse<ArticleEditResponseType>>(ARTICLE_EDIT_URL + '/' + route.params.articleId, {})
      .then((response) => {
        if (response.data) {
          // 获取文章详情然后给表单赋值
          console.log(response)
          titleForm.title = response.data.result.article.title
          text.value = response.data.result.article.content
          articleInfoForm.abstract = response.data.result.article.summary
          articleInfoForm.tags = response.data.result.tags.map(tagItem => String(tagItem.tagId))
          articleInfoForm.category = String(response.data.result.categories.filter(categoryItem => categoryItem.categoryId === response.data.result.article.category.categoryId)[0].categoryId)
          articleId.value = Number(response.data.result.article.articleId)
          upload.value?.clearFiles()
          if(response.data.result.article.cover){
            fileList.value.push({
              name: response.data.result.article.cover,
              url: response.data.result.article.cover
            })
            upload.value?.fileList.push({
              name: response.data.result.article.cover,
              url: response.data.result.article.cover
            })
          }
        }
      })
  }
  // 获取分类标签
  categoryLoading.value = true
  doGet<CommonResponse>(CATEGORY_LIST_URL, {})
    .then((response) => {
      if (response.data) {
        globalStore.setGlobal(response.data.global)
        categoryOptions.value = response.data.result
      }
    })
    .catch((error) => {
      console.error(error)
    })
    .finally(() => {
      categoryLoading.value = false
    })

  // 获取tags标签
  doGet<CommonResponse>(TAG_LIST_URL, {})
    .then((response) => {
      if (response.data) {
        tagsOptions.value = response.data.result
      }
    })
    .catch((error) => {
      console.error(error)
    })
    .finally(() => {
      tagsLoading.value = false
    })

})


// ============ 简介与其他信息的输入框 ==============
const submitDialogVisible = ref(false)

interface ArticleInfoForm {
  category: string
  tags: string[]
  abstract: string
}
const articleInfoFormSize = ref<ComponentSize>('default')
const articleInfoFormRef = ref<FormInstance>()
const articleInfoForm = reactive<ArticleInfoForm>({
  category: '',
  tags: [],
  abstract: ''
})
const articleInfoFormRules = reactive<FormRules<ArticleInfoForm>>({
  category: [
    { required: true, message: '请选择文章标题', trigger: 'change' },
  ],
  tags: [
    { required: true, message: '请选择标签', trigger: 'change' },
  ],
  abstract: [
    { required: true, message: '请输入文章简介', trigger: 'blur' },
  ]
})

// 分类下拉框
const categoryLoading = ref(false)
const categoryOptions = ref<ArticleCategoryType[]>([])


// tags多选框，tag选择框要根据category来加载
const tagsLoading = ref(false)
const tagsOptions = ref<ArticleTagType[]>([])


// 文件上传
// isMax用来控制样式，当上传一张图片后隐藏后面的上传框
// cover 用来保存返回的图片地址
const cover = ref('')
const isMax = ref(false)
const upload = ref<UploadInstance>()
const fileList = ref<UploadUserFile[]>([])

const onUploadFile = (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  console.log(uploadFiles.length)
  const formData = new FormData();
  // @ts-ignore
  formData.append('image', uploadFile.raw);
  fileList.value?.push(uploadFile.raw as UploadUserFile)
  isMax.value = fileList.value.length >= 1

  doFilePost<CommonResponse>(FILE_UPLOAD_URL, formData)
    .then((response) => {
      console.log(response)
      messageTip('上传成功', 'success')
      cover.value = response.data.result.imagePath
    }).catch((error) => {
      console.error(error)
      messageTip('上传失败' , 'error')
      isMax.value = fileList.value.length >= 1
    })

}

const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const disabled = ref(false)

const handleRemove = (file: UploadFile) => {
  console.log(upload.value)
  fileList.value.splice(fileList.value.indexOf(file.raw as UploadUserFile), 1)
  if(upload.value){
    upload.value.clearFiles()
    isMax.value = fileList.value.length >= 1
  }
}

const handlePictureCardPreview = (file: UploadFile) => {
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

// 一键提取摘要
const extractAbstract = () => {
  if(text.value.length > 255)
    articleInfoForm.abstract = text.value.slice(0, 256)
  else
    messageTip('正文内容不足256字', 'warning')
}

// ================= 发布文章 =================
// 点击按钮后无法再次点击，防止重复提交
const saveBtnLoading = ref(false)
const articleId = ref(0)
const postArticle = (actionType: 'save' | 'post') => {
  saveBtnLoading.value = true
  if(text.value.length < 50){
    messageTip('正文内容不得少于50字', 'warning')
    return
  }
  doPost<CommonResponse>(ARTICLE_UPLOAD_URL, {
    articleId: articleId.value,
    title: titleForm.title,
    categoryId: articleInfoForm.category,
    tagIds: articleInfoForm.tags,
    summary: articleInfoForm.abstract,
    content: text.value,
    cover: cover.value? cover.value : undefined,
    articleType: ArticleTypeEnum.BLOG,
    source: DocumentSourceTypeEnum.ORIGINAL,
    actionType: actionType,
  })
    .then((response) => {
      console.log(response)
      messageTip('发布成功', 'success')
      setTimeout(() => {
        router.push(`/`)
      }, 1000)
    })
    .catch((error) => {
      console.error(error)
    })
    .finally(() => {
      saveBtnLoading.value = true
      submitDialogVisible.value = false
    })
}


// ============= 正文操作 ===============
// 上传图片
const onUploadImg = async (files: Array<File>, callback:(urls: string[] | { url: string; alt: string; title: string }[]) => void) => {
  const res = await Promise.all(
    files.map((file) => {
      return new Promise((rev, rej) => {
        const form = new FormData();
        form.append('image', file);
        doFilePost<CommonResponse>(FILE_UPLOAD_URL, form)
          .then((response) => {
            rev(response)
          })
          .catch((error) => rej(error));
      });
    })
  );
  // @ts-ignore
  callback(res.map((r) => r.data.result.imagePath));
}

/**
 * 暂时用不到，后续可能会用到，这是在点击分类下拉框时，获取分类列表，但是现在已经在onMounted中获取了，并且同时获取了global信息
 * @param visible
 */
const getCategories = (visible: boolean) => {
  if(!visible)
    return
  categoryLoading.value = true
  doGet<CommonResponse>(CATEGORY_LIST_URL, {})
    .then((response) => {
      if (response.data) {
        categoryOptions.value = response.data.result
      }
    })
    .catch((error) => {
      console.error(error)
    })
    .finally(() => {
      categoryLoading.value = false
    })
}


</script>



<style scoped>

#edit-body{
  max-height: calc(100vh - 84px - var(--header-height));
}


.el-upload--picture-card {
  display: none;
}

</style>
