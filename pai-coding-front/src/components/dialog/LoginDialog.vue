<template>
  <!-- 登录 Modal -->
  <el-dialog v-model="loginModal" title="登录编程汇畅享更多权益" width="600" border="border">
    <el-divider style="margin: 10px 0"></el-divider>
    <el-container>
      <el-main>
        <el-container>
          <el-container>
            <el-main>
              <el-form
                ref="formRef"
                style="max-width: 325px"
                :model="dynamicValidateForm"
                label-width="auto"
                class="demo-dynamic"
              >
                <el-form-item style="margin: 0">
                  <span class="bold-span mb-4">用户密码登录</span>
                </el-form-item>
                <el-form-item
                  prop="username"
                  label="用户名"
                  :rules="[
                      {
                        required: true,
                        message: '用户名不能为空',
                        trigger: 'blur',
                      },
                    ]"
                >
                  <el-input v-model="dynamicValidateForm.username" placeholder="请输入用户名"/>
                </el-form-item>
                <el-form-item
                  prop="password"
                  label="密码"
                  :rules="[
                        {
                          required: true,
                          message: '密码不能为空',
                          trigger: 'blur',
                        },
                      ]"
                >
                  <el-input v-model="dynamicValidateForm.password" type="password" placeholder="请输入密码"/>
                </el-form-item>
                <el-form-item class="center-content">
                  <el-button type="primary" @click="submitForm(formRef)">提交</el-button>
                  <el-button @click="resetForm(formRef)">清空</el-button>
                </el-form-item>
              </el-form>
            </el-main>
            <el-footer>
              <div class="other-login-box">
                <div class="oauth-box">
                  <span>其他登录——敬请期待</span>
                </div>
              </div>
            </el-footer>
          </el-container>
          <el-container>
            <el-main>
              <div class="tabpane-container" style="display: flex; flex-direction: column; justify-content: space-between; align-content: center">
                <span class="wx-login-span-info center-content">微信扫码/长按识别登录</span>
                <div class="first center-content">
                  <img class="signin-qrcode" width="150px" src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/%E5%85%AC%E4%BC%97%E5%8F%B7qrcode.jpg" />
                </div>

                <div class="explain center-content">
                  <span ><bold>输入验证码</bold> <span class="link-color">{{code}}</span></span>
                  <div><span id="state">有效期五分钟 👉</span> <a class="bold-span underline cursor-pointer link-color" @click="refreshCode">手动刷新</a></div>
                </div>
              </div>
            </el-main>
          </el-container>

        </el-container>

      </el-main>
      <el-footer>
        <div class="modal-footer">
          <div class="agreement-box">
            <div class="mdnice-user-dialog-footer center-content" >
              <p id="login-agreement-message">登录即同意
                <span class="font-bold"> 用户协议</span>（现在没什么协议）
                和
                <span class="font-bold"> 隐私政策</span>（现在没什么隐私）
              </p>
            </div>
          </div>
<!--          <div class="mock-login flex flex-grow" v-if="global.env !== 'prod'">-->
<!--            &lt;!&ndash; 非生产环境，使用模拟登陆  &ndash;&gt;-->
<!--            <el-button @click="mockLogin2">随机新用户</el-button>-->
<!--            <el-button @click="mockLogin2">一键登录</el-button>-->
<!--          </div>-->
        </div>
      </el-footer>
    </el-container>

  </el-dialog>
</template>
<script setup lang="ts">

import { reactive, ref, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { doGet, doPost, mockLogin2XML, mockLoginXML } from '@/http/BackendRequests'
import type { CommonResponse, GlobalResponse } from '@/http/ResponseTypes/CommonResponseType'
import { BASE_URL, LOGIN_USER_NAME_URL } from '@/http/URL'
import { getCookie, messageTip, refreshPage, setAuthToken } from '@/util/utils'
import { MESSAGE_TYPE } from '@/constants/MessageTipEnumConstant'
import { COOKIE_DEVICE_ID } from '@/constants/CookieConstants'
import { useGlobalStore } from '@/stores/global'
const globalStore = useGlobalStore()
const global = globalStore.global

const props = defineProps<{
  clicked: boolean,
}>()

const loginModal = ref(false)
let init = false


watch(() => props.clicked, () => {
  loginModal.value = true
  // 如果是第一次打开，需要建立长连接
  if(!init){
    buildConnect()
    init = true
  }
})

const formRef = ref<FormInstance>()

const dynamicValidateForm = reactive<{
  username: string,
  password: string
}>({
  username: '',
  password: '',
})


const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {
      console.log(dynamicValidateForm)
      doPost<CommonResponse>(LOGIN_USER_NAME_URL, {
        username: dynamicValidateForm.username,
        password: dynamicValidateForm.password
      })
        .then((response) => {
          if(response.data.status.code === 0){
            messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
            setAuthToken(response.data.result.token)
            console.log(response.data)
            refreshPage()
          }})
        .catch((error) => {
          console.error(error)
        })
    } else {
      messageTip("请按要求填写用户名密码", MESSAGE_TYPE.ERROR)
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}


// ==========模拟登录==========
const mockLogin = () => {
  mockLoginXML<CommonResponse>(code.value)
    .then((response) => {
      console.log(response)
      messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
      refreshPage()
    })
    .catch((error) => {
      console.log(code)
      console.error(error)
    })
}

const mockLogin2 = () => {
  mockLogin2XML<CommonResponse>(code.value)
    .then((response) => {
      messageTip("登录成功", MESSAGE_TYPE.SUCCESS)
      refreshPage()
    })
    .catch((error) => {
      console.log(code)
      console.error(error)
    })
}

// ==========长连接==========
// /**
//  * 记录长连接
//  * @type {null}
//  */
let sseSource:any = null;
let intHook:any = null;
let deviceId:any = null;
const code = ref('')
const state = ref('有效期五分钟 👉')
let fetchCodeCnt = 0

/**
 * 建立半长连接，用于实现自动登录
 */
function buildConnect() {
  if (sseSource != null) {
    try {
      sseSource.close();
    } catch (e) {
      console.log("关闭上次的连接", e);
    }
    try {
      window.clearInterval(intHook);
    } catch (e) { /* empty */ }
  }

  if(!deviceId) {
    deviceId = getCookie(COOKIE_DEVICE_ID);
    console.log("获取设备id: ", deviceId)
  }
  const subscribeUrl = BASE_URL + "/subscribe?deviceId=" + deviceId;
  const source = new EventSource(subscribeUrl);
  sseSource = source;

  source.onmessage = function (event) {
    let text = event.data.replaceAll("\"", "").trim();
    console.log("receive: " + text);

    let newCode;
    if (text.startsWith('refresh#')) {
      // 刷新验证码
      newCode = text.substring(8).trim();
      code.value = newCode
      state.value = '已刷新 '
    } else if (text === 'scan') {
      // 二维码扫描
      state.value = '已扫描 '
      // stateTag.text("已扫描 ");
    } else if (text.startsWith('login#')) {
      // 登录格式为 login#cookie
      console.log("登录成功,保存cookie", text)
      document.cookie = text.substring(6);
      source.close();
      refreshPage();
    } else if (text.startsWith("init#")) {
      newCode = text.substring(5).trim();
      code.value = newCode
      console.log("初始化验证码: ", newCode);
    }

    if (newCode != null) {
      try {
        window.clearInterval(intHook);
      } catch (e) { /* empty */ }
    }
  };

  source.onopen = function (evt) {
    deviceId = getCookie("f-device");
    console.log("开始订阅, 设备id=", deviceId, evt);
  }

  source.onerror = function (e: Event) {
    console.log("连接错误，重新开始", e);
    state.value = '连接中断,请刷新重连'
    buildConnect();
  };

  fetchCodeCnt = 0;
  console.log("#############################")
  intHook = setInterval(() => fetchCode(), 1000);
}

function fetchCode() {
  if (deviceId) {
    if (++fetchCodeCnt > 5) {
      // 为了避免不停的向后端发起请求，做一个最大的重试计数限制
      try {
        window.clearInterval(intHook);
      } catch (e) { /* empty */ }
      return;
    }

    doGet('/login/fetch?deviceId=' + deviceId, {}, 'text')
      .then((response) => {
        console.log(response)
        if(response.data){
          if (response.data !== 'fail') {
            // @ts-ignore
            code.value = response.data
            try {
              window.clearInterval(intHook);
            } catch (e) { /* empty */ }
          }
        }
      })
      .catch((error) => {
        console.error(error)
      })
  } else {
    console.log("deviceId未获取，稍后再试!");
  }
}

function refreshCode() {
  doGet('/login/refresh?deviceId=' + deviceId, {}, 'json')
    .then((response) => {
      // @ts-ignore
      const validationCode = response.data['result']['code']
      // @ts-ignore
      const reconnect = response.data['result']['reconnect']
      console.log("验证码刷新完成: ", response)

      if (reconnect) {
        // 重新建立连接
        buildConnect()
        state.value = '已刷新'
      } else if(validationCode) {
        if (code.value !== validationCode) {
          console.log("主动刷新验证码!")
          code.value = validationCode
          state.value = '已刷新'
        } else {
          console.log("验证码已刷新了!")
        }
      }
    })
    .catch((error) => {
      console.error(error)
    })
}




</script>



<style scoped>

</style>
