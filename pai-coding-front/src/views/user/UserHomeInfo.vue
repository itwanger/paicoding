<template>
  <div class="user-bg">
    <div class="user-head">
      <img :src="vo.userHome.photo" class="user-head-img" />
      <div class="user-head-title-wrap">
        <div class="user-head-title-name">
          {{vo.userHome.userName}}
        </div>
        <div class="user-head-title-classify hidden-when-screen-small">
          <div class="user-head-title-classify-item">
            <span class="text-base-pure">加入天数</span>
            <span>{{vo.userHome.joinDayCount}}</span>
          </div>
          <div class="user-head-cell"></div>
          <div class="user-head-title-classify-item">
            <span class="text-base-pure">关注数</span>
            <span >{{vo.userHome.followCount}}</span>
          </div>
          <div class="user-head-cell"></div>
          <div class="user-head-title-classify-item">
            <span class="text-base-pure">粉丝数</span>
            <span >{{vo.userHome.fansCount}}</span>
          </div>
        </div>
      </div>
      <div class="user-head-footer">
        <div class="tw-flex-1">
          <div class="d-flex tags">
            <div class="tag-item hidden-when-screen-small" v-if="vo.userHome.company">
              <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAAsklEQVR42mNgoBPI507vTbuU/g0OP6adSM+rZ8KpPO162n9MmL4Wh4b0XmzKwTAKqwbs5oPtWIJdA0Ryb4ZKmhwEZuql3QGLnsCivJ4J5uIajnomCCwUSrsEFn2cXpVelZGYoYJFA36Y0UqihrT/GYkkaki7hKEhfW/6DghMO4FNCzQi4Z7uRYmbvZgaajhQbbiTnpeeAYVV6R8JayAIR7KG9M3EakjfTJ4GiLMIQzKLCwBaavBCKUO/owAAAABJRU5ErkJggg=="
                   class="tw-h-3 tw-w-3 tw-mr-1">
              <span class="tw-text-sm" >{{vo.userHome.company}}</span>
            </div>
            <div class="tag-item hidden-when-screen-small" v-if="vo.userHome.position">
              <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAABIUlEQVR42r1TLW/DQAw1OxgWUhypNCykZDS0P6GyLzmVHxzobxgpLAjt2FBJf0DR4UiVCkMLQjpdmt1XsiZkc1Dk92w/3zPAvwWleKIWH8OPWlT8LYAXEV3HwIbUbBceAcUreEfZ+QQ1RcCbO9BqEq575FbwYQ4Bjz1cxP12VJlgNah7KJPnwNSK+Dn/pk8qySSjr7CqZEah6Aj83VbTC8aLgZ8kA7Bd+01R6i9PxFh3f5ciAsAPJ5v+iN45QwiAMqEGaz2x7R68BO2tEWgNwLMy8Z+T9oE58NOk7jzriqwdbx0H5pOMztY5xcqF01mLH0QR/WIQpcWPxnYx4tk68KkffEmNb2u+nDgkntE9lD91e7lZcD7zXHmGFVazqv9BfAN523/xHVmeOQAAAABJRU5ErkJggg=="
                   class="tw-h-3 tw-w-3 tw-mr-1">
              <span class="tw-text-sm">{{vo.userHome.position}}</span>
            </div>
            <div class="tag-item hidden-when-screen-small" v-if="vo.userHome.region">
              <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAABFUlEQVR42mNgoBRkSaRnpC1PP5R+KG15ekaWBH7FPOnd6T/S/iNg+o/07iweXMpl0y4iK4ZrupAli0V5GW/6VWzKwVqulvFiaEibi0s5GM5Fd45m+l98GtL/ZmmiaMhoRZL+mV6eJpkjlV6e9hMhmtGK6qAzSKaVw0TTy5GMOYOq4TWSlCRcVBJJ9DWqhj/YNORIIYn+QdXwiqCTUG1I34/u6TRJVE+nHUbVUIk3FkCh1ICioVAo7SteDb8QPsMWE5gRNwUjaeSyp13DqeExlrTEwJBhiN1Z6b8zHHAk8LTQtH9YNGThyUJpBRjKmwlk0YwyFA3tROTq9AxoUv+XVkxkQZDum/Yp7VNaAAllR6FQpiB2GQBLang0fXCP5wAAAABJRU5ErkJggg=="
                   class="tw-h-3 tw-w-3 tw-mr-1">
              <span class="tw-text-sm">IP属地：{{vo.userHome.region}}</span>
            </div>
          </div>
        </div>

        <div class="user-edit flex justify-between" v-if="global.isLogin && global.user.id == vo.userHome.userId">
          <div class="flex" @click="editInfoDialogVisible = true">
            <span style="color: black">
              {{vo.userHome.profile? vo.userHome.profile : '点击添加简介，让大家认识你吧'}}
            </span>
            <p class="center-content">
              <el-icon><edit></edit></el-icon>
            </p>
          </div>
          <div>
            <span>个人资料完善度：{{vo.userHome.infoPercent}} %</span>
            <span @click="editInfoDialogVisible = true" class="edit-btn" data-toggle="modal" data-target="#saveModel">去编辑 ></span>
          </div>

        </div>
      </div>
    </div>
    <div class="user-bg-mask"></div>
  </div>


  <el-dialog
    :model-value="editInfoDialogVisible"
    @close="editInfoDialogVisible = false"
  >
    <template #header>
      <span class="font-bold text-xl">编辑个人资料</span>
    </template>

    <template #default>
      <div class="flex">
        <el-form
          class="flex-grow"
          ref="userInfoFormRef"
          :model="userInfoForm"
          :size="userInfoFormSize"
          label-width="auto"
          status-icon
          :rules="userInfoFormRules"
        >
          <el-form-item prop="userName" label="用户名" style="margin-bottom: 15px" label-width="auto">
            <template #default>
              <el-input
                v-model="userInfoForm.userName"
                maxlength="40"
                placeholder="用户名"
              >
              </el-input>
            </template>
          </el-form-item>

          <el-form-item prop="company" label="公司" style="margin-bottom: 15px" label-width="auto">
            <template #default>
              <el-input
                v-model="userInfoForm.company"
                maxlength="40"
                placeholder="所属公司"
              >
              </el-input>
            </template>
          </el-form-item>

          <el-form-item prop="position" label="职位"  style="margin-bottom: 15px" label-width="auto">
            <template #default>
              <el-input
                v-model="userInfoForm.position"
                maxlength="40"
                placeholder="职位"
              >
              </el-input>
            </template>
          </el-form-item>

          <el-form-item prop="description" label="简介"  style="margin-bottom: 15px" label-width="auto">
            <template #default>
              <el-input
                v-model="userInfoForm.description"
                maxlength="40"
                placeholder="自我简介"
              >
              </el-input>
            </template>
          </el-form-item>
        </el-form>
        <div class="center-content ml-1">
          <el-avatar size="large" class="cursor-pointer" :src="cover" @click="uploadAvatar">
          </el-avatar>
          <span>我的头像</span>
          <span class="text-xs center-content">
            支持 jpg、png、jpeg <br>格式大小 2M 以内的图片
          </span>
        </div>
      </div>
      <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none;">
    </template>
    <template #footer>
      <div class="flex justify-center">
        <el-button type="info" @click="editInfoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserInfo" :disabled="isSaveDisabled" >保存</el-button>
      </div>
    </template>

  </el-dialog>

</template>

<script setup lang="ts">
import type { UserHomeInfoResponseType } from '@/http/ResponseTypes/UserHomeInfoResponseType'
import { useGlobalStore } from '@/stores/global'
import { Delete, Edit, Plus, ZoomIn } from '@element-plus/icons-vue'
import { reactive, ref, watch } from 'vue'
import type {
  ComponentSize,
  FormInstance,
  FormRules,
  UploadUserFile
} from 'element-plus'
import { doFilePost, doPost } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { FILE_UPLOAD_URL, USER_INFO_SAVE_URL } from '@/http/URL'
import { messageTip } from '@/util/utils'
import { useRoute, useRouter } from 'vue-router'
const globalStore = useGlobalStore()
const global = globalStore.global
const router = useRouter()
const route = useRoute()

const props = defineProps<{
  vo: UserHomeInfoResponseType
}>()

const userId = route.params.userId

// 修改用户信息
const editInfoDialogVisible = ref(false)

interface UserInfoForm {
  userName: string
  company: string
  position: string
  description: string
}
const userInfoFormSize = ref<ComponentSize>('default')
const userInfoFormRef = ref<FormInstance>()
const userInfoForm = reactive<UserInfoForm>({
  userName: '',
  company: '',
  position: '',
  description: ''
})

const userInfoFormRules = reactive<FormRules<UserInfoForm>>({
  userName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 3, max: 15, message: '昵称长度位于3-15之间', trigger: 'blur' },
  ],
  company: [
    { required: true, message: '请输入公司', trigger: 'blur' },
    { min: 2, max: 15, message: '公司长度位于2-15之间', trigger: 'blur' },

  ],
  position: [
    { required: true, message: '请输入自身职位', trigger: 'blur' },
    { min: 3, max: 10, message: '职位长度位于3-10之间', trigger: 'blur' },

  ],
  description: [
    { required: true, message: '请输入自我简介', trigger: 'blur' },
    { min: 2, max: 20, message: '自我介绍长度位于4-20之间', trigger: 'blur' },

  ]
})
// 上传头像
// cover 用来保存返回的图片地址
const cover = ref('')
const fileList = ref<UploadUserFile[]>([])

watch(() => props.vo.userHome, (newVal) => {
  if (newVal) {
    fileList.value.push({ url: newVal.photo, name: '头像' })
    if (newVal.photo != null) {
      cover.value = newVal.photo
    }
    userInfoForm.userName = newVal.userName || ''
    userInfoForm.company = newVal.company || ''
    userInfoForm.position = newVal.position || ''
    userInfoForm.description = newVal.profile || ''
    // upload.value?.fileList = fileList.value
  }
}, {
  immediate: true
})

// 获取隐藏的input元素
const fileInput = ref<HTMLInputElement | null>(null);

const onUploadFile = (file: File) => {
  const formData = new FormData();
  // @ts-ignore
  formData.append('image', file);

  doFilePost<CommonResponse>(FILE_UPLOAD_URL, formData)
    .then((response) => {
      console.log(response)
      messageTip('上传成功', 'success')
      cover.value = response.data.result.imagePath
    }).catch((error) => {
    console.error(error)
    messageTip('上传失败' , 'error')
  })
}

const uploadAvatar = () => {
  fileInput.value?.click()
}

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files ? target.files[0] : null;
  if (file) {
    console.log('File selected:', file.name);
    console.log('File selected:', file);
    onUploadFile(file)
    // 处理文件上传
  }
};

// 保存用户信息
// 是否禁用保存按钮
const isSaveDisabled = ref(false)
const saveUserInfo = async () => {
  isSaveDisabled.value = true
  if(!userInfoFormRef.value){
    return
  }
  await userInfoFormRef.value.validate((valid, fields) => {
    if(valid){
      doPost<CommonResponse>(USER_INFO_SAVE_URL, {
        userId: userId,
        userName: userInfoForm.userName,
        company: userInfoForm.company,
        position: userInfoForm.position,
        profile: userInfoForm.description,
        photo: cover.value
      }).then((response) => {
        console.log(response)
        messageTip('保存成功', 'success')
        editInfoDialogVisible.value = false
        window.location.reload()
      }).catch((error) => {
        console.error(error)
        messageTip('保存失败', 'error')
      }).finally(() => {
        isSaveDisabled.value = false
      })
    }else{
      isSaveDisabled.value = false
    }
  })
}

</script>



<style scoped>
.el-form-item__label{
  padding: 0;
  margin: 0;
}

.el-upload--picture-card {
  display: none;
}

@media (max-width: 768px) {
  .el-dialog{
    width: 480px;
  }
  .hidden-when-screen-small{
    display: none;
  }
}
</style>
