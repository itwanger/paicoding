<template>
  <div class="home article-detail flex flex-col">
    <div class="col-body pg-2-article flex" id="article-detail-body-div">
      <div class="com-3-layout" >
        <section class="article-info-wrap com-2-panel col-2-article J-articlePanel">
          <h4 class="correlation-article-title">Excel工具</h4>
          <el-divider></el-divider>
<!--      用于内部处理表格的函数    -->
          <el-card class="mb-2">
            <template #header>信息处理表</template>
            <template #default>
              <el-form
                ref = "formRef"
                :model="formModel"
                :rules="excelInfoProcessFormRules"
              >
                <el-form-item prop="date">
                  <template #label>目标日期</template>
                  <el-date-picker
                    v-model="formModel.date"
                    type="date"
                    placeholder="选择日期"
                    size="default"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item prop="fileList">
                  <el-upload
                    ref="upload"
                    :limit="1"
                    :auto-upload="false"
                    :file-list="formModel.fileList"
                    :on-change="handleChange"
                    :on-exceed="handleExceed"
                  >
                    <template #trigger>
                      <el-button type="primary">选择文件</el-button>
                    </template>
                    <el-button class="ml-3" type="success" @click="submitUpload">
                      点击处理文件
                    </el-button>
                    <template #tip>
                      <div class="el-upload__tip text-red">
                        仅限上传一个文件，旧文件将被覆盖
                      </div>
                    </template>
                  </el-upload>
                </el-form-item>
              </el-form>
            </template>
          </el-card>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { reactive, ref } from 'vue'
import {
  type FormInstance,
  type FormRules,
  genFileId, type UploadFile, type UploadFiles,
  type UploadInstance,
  type UploadRawFile,
  type UploadUserFile
} from 'element-plus'
import { extraFilePostAndDownload } from '@/http/BackendRequests'
import { EXCEL_PROCESS_BASE_URL, EXCEL_PROCESS_URL } from '@/http/URL'



// excel信息处理表单实例
const formRef = ref<FormInstance>()
interface ExcelInfoProcessForm {
  date: string,
  fileList: UploadUserFile[]
}

const formModel = reactive<{
  date: string,
  fileList: UploadUserFile[]
}>({
  date: '',
  fileList: []
})

const excelInfoProcessFormRules = reactive<FormRules<ExcelInfoProcessForm>>({
  date: [
    { required: true, message: '请选择日期', trigger: 'blur' },
  ],
  fileList:[
    { required: true, message: '请选择文件', trigger: 'change' }
  ]
})
// excel信息处理文件上传框
const upload = ref<UploadInstance>()

const handleChange = (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  formModel.fileList = [uploadFile]
}

const handleExceed = (files: File[], uploadFiles: UploadUserFile[]) => {
  upload.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}

const submitUpload = async () => {
  if(!formRef.value){
    return
  }
  // 校验表单
  await formRef.value.validate( async (valid) => {
    if (!valid) {
      return
    }
    // 上传文件
    const formData = new FormData();


    formModel.fileList.forEach((file) => {
      // @ts-ignore
      formData.append('file', file.raw)
    })

    await extraFilePostAndDownload(EXCEL_PROCESS_BASE_URL, EXCEL_PROCESS_URL, formData, {
      // 注意这里要使用formModel.date.split('T')[0]，因为el-date-picker返回的是一个带有时间的字符串
      date: formModel.date.split('T')[0]
    })
      .then((response) => {
        console.log(response)
        console.log(response.data)
        const url = window.URL.createObjectURL(
          new Blob([response.data], { type: 'application/zip' })
        )
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'result.zip')

        document.body.appendChild(link)
        link.click()
        link.remove()
        formModel.fileList = []
      })
      .catch((error) => {
        console.error(error)
      })
  })

}

</script>



<style scoped>

.home{
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  display: flex;
  align-items: center;
  width: 100%;
}

</style>
