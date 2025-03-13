<template>
  <HeaderBar></HeaderBar>
  <!-- 导航栏 -->
  <div class="custom-home">
    <div class="chat-wrap">
<!--      <div class="chat-sidebar">-->
        <!-- 侧边栏 -->
<!--        <ChatSideBar></ChatSideBar>-->
<!--      </div>-->
      <div class="chat-main">
        <div class="window-header">
          <div class="window-header-title">
            <div class="name">
              <div class="window-header-main-title home_chat-body-title__5S8w4"
                   v-if="!global.isLogin || !global.user">
                点击登录，体验编程汇智能对话
              </div>
              <div class="chat-annotation active-color" v-else>
                {{global.user.userName}}
<!--                <div th:switch="${global.user.starStatus.code}">-->
<!--                  <a th:case="-1"-->
<!--                     href="#"-->
<!--                     class="annotation"-->
<!--                     data-target="#registerModal"-->
<!--                     data-toggle="modal"-->
<!--                  >绑定编程星球，提升每天对话次数</a>-->
<!--                  <span th:case="0" class="annotation">审核中</span>-->
<!--                  <span th:case="1" class="annotation">试用中，添加管理员微信 xyf857998989 催审核</span>-->
<!--                  <div class="c-bubble-trigger com-verification" th:case="2">-->
<!--                    <i class="verified"></i>-->
<!--                  </div>-->
<!--                </div>-->
                    <span class="annotation">试用中</span>
                    <div class="c-bubble-trigger com-verification">
                      <i class="verified"></i>
                    </div>
              </div>

            </div>

            <div class="window-header-sub-title">与派聪明的 <span id="chatCnt">{{chatUsedCnt}}/{{chatMaxCnt}}</span> 条对话
              <span>(以天为单位，无限期重置）</span>
            </div>
          </div>

          <div class="chat-type">
            <!-- 加一个下拉框，选项是 OpenAI 讯飞星火 -->
            <el-select
              class="w-40"
              @change="chatTypeChange"
              v-model="chatType"
              placeholder="选择对话模型"
              default-first-option="XUN_FEI_AI"
            >
              <el-option
                :value="AiTypeEnum.XUN_FEI_AI"
                label="讯飞星火"
              />
<!--              <el-option-->
<!--                :value="AiTypeEnum.PAI_AI"-->
<!--                label="技术派"-->
<!--              />-->
<!--              <el-option-->
<!--                :value="AiTypeEnum.CHAT_GPT_3_5"-->
<!--                label="OPENAI"-->
<!--              />-->
            </el-select>
          </div>
        </div>
        <div class="overflow-auto flex-grow" ref="chatContent" id="chat-content">
          <div class="message-content overflow-auto" v-for="(msg, id) in msgRecords[chatType]" :key="id">
            <div v-if="msg.msgType == 'question'" class="flex justify-end">
              <p style="background: #FCEAE0" class="center-content p-2 rounded-lg m-1 text-sm">{{msg.question}}</p>
              <el-avatar :size="35" :src="global.user.photo" class="m-1"></el-avatar>
            </div>
            <div v-if="msg.msgType == 'answer'" class="flex justify-start">
              <el-avatar :size="35" class="m-1 min-w-8" src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/avatar/llm-avatar1.png"></el-avatar>
              <p style="background: #F2F2F2" class="center-content p-2 rounded-lg m-1 text-sm"><MdPreview style="font-size: small" :model-value="msg.answer"></MdPreview></p>
            </div>

            <el-divider v-if="msg.msgType == 'history'">我是可爱的历史记录分割线</el-divider>

          </div>
          <div v-if="aiLoading" class="flex justify-start">
            <el-avatar :size="35" class="m-1" src="https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/avatar/llm-avatar1.png"></el-avatar>
            <p style="background: #F2F2F2" class="center-content p-2 rounded-lg m-1 text-sm"> <el-icon :size="20" class="is-loading"><Loading /></el-icon></p>
          </div>
        </div>

        <div class="chat-input" id="chat-textarea">
          <textarea v-model="chatText" id="input-field" class="form-control" rows="3" :placeholder="!global.isLogin || !global.user.userId || chatTextAreaDisabled? '你好，快登录和我对线吧': '可按回车发送'" :disabled="!global.isLogin || !global.user.userId || chatTextAreaDisabled">
          </textarea>

          <button @click="sendMsg" id="send-btn" :disabled="!global.isLogin || !global.user.userId || chatBtnDisabled">
            <div class="button_icon-button-icon__qlUH3 no-dark">
              <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16" fill="none"><defs><path id="send-white_svg__a" d="M0 0h16v16H0z"></path></defs><g><mask id="send-white_svg__b" fill="#fff"><use xlink:href="#send-white_svg__a"></use></mask><g mask="url(#send-white_svg__b)"><path transform="translate(1.333 2)" d="M0 4.71 6.67 6l1.67 6.67L12.67 0 0 4.71Z" style="stroke: rgb(255, 255, 255); stroke-width: 1.33333; stroke-opacity: 1; stroke-dasharray: 0, 0;"></path><path transform="translate(8.003 6.117)" d="M0 1.89 1.89 0" style="stroke: rgb(255, 255, 255); stroke-width: 1.33333; stroke-opacity: 1; stroke-dasharray: 0, 0;"></path></g></g></svg>
            </div>
            <div v-if="!global.isLogin || !global.user.userId" class="button_icon-button-text__k3vob">等待登录</div>
            <div v-else class="button_icon-button-text__k3vob">发送</div>
          </button>
        </div>
      </div>
    </div>
    <!-- 底部信息 -->
    <Footer></Footer>
  </div>
  <LoginDialog :clicked="loginDialogClicked"></LoginDialog>
</template>

<script setup lang="ts">

import HeaderBar from '@/components/layout/HeaderBar.vue'
import Footer from '@/components/layout/Footer.vue'
import LoginDialog from '@/components/dialog/LoginDialog.vue'
import { useGlobalStore } from '@/stores/global'
import { nextTick, onMounted, provide, ref } from 'vue'
import ChatSideBar from '@/views/chat-home/ChatSideBar.vue'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { BASE_URL, GLOBAL_INFO_URL, WS_URL } from '@/http/URL'
import { getCookie, messageTip } from '@/util/utils'
//引入使用SockJS
import Stomp from "stompjs";
import { Loading, StarFilled } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import '@/assets/llm-answer.css'
import type { WebSocketRecordsType } from '@/http/ResponseTypes/ChatType/WebSocketRecordsType'
import type { WebSocketResponseType } from '@/http/ResponseTypes/WebSocketResponseType'
import { type AiTypeConstants, AiTypeEnum } from '@/constants/AiTypeEnumConstants'
const globalStore = useGlobalStore()
const global = globalStore.global

// 聊天次数
const chatUsedCnt = ref(0)
const chatMaxCnt = ref(0)
// 聊天框设置
const chatText = ref('')
const chatBtnDisabled = ref(true)
const chatBtnText = ref('等待登录')
const chatTextAreaDisabled = ref(true)
// 动态根据ai的回答变化html
const chatContent = ref<HTMLElement | null>(null);
const answers = ref('')
// 获取JWT token
const session = getCookie("f-session")
// 大模型的选择器
const chatType = ref<AiTypeConstants>('XUN_FEI_AI')
const chatTypeChange = (value: string ) => {
  if(global.isLogin){
    answers.value = ''
    disconnect()
    initWs()
  }
}
// stomp协议的客户端
let stompClient: Stomp.Client | null

const msgRecords = ref<Record<AiTypeConstants, WebSocketRecordsType[]>>({
  XUN_FEI_AI: [],
  CHAT_GPT_3_5: [],
  PAI_AI: []
})
const aiLoading = ref(false)


// 初始化ws
const initWs = () => {
  msgRecords.value[chatType.value] = []
  let aiType = chatType.value
  console.log("AITYPE = ", aiType);
  console.log("session = ", session)
  let socket = new WebSocket(`${WS_URL}/gpt/${session}/${aiType}`)
  stompClient = Stomp.over(socket)
  stompClient.connect({}, function(frame) {
    console.log('ws连接成功: ' + frame);
    // 开放按钮和输入框
    chatBtnDisabled.value = false
    chatTextAreaDisabled.value = false
    chatBtnText.value = '发送'
    // 清空输入框
    chatText.value = ''

    // @ts-ignore
    stompClient.subscribe('/user/chat/rsp', function(message: Stomp.Message){
      // 表示这个长连接，订阅了 "/chat/rsp" , 这样后端向这个路径转发消息时，我们就可以拿到对应的返回
      // 解析 JSON 字符串
      console.log("rsp:", message);
      let res = JSON.parse(message.body);
      console.log("res:", res);

      chatUsedCnt.value = res.usedCnt
      chatMaxCnt.value = res.maxCnt

      const data: WebSocketResponseType[] = res.records
      if (data.length > 1) {
        // 返回历史全部信息
        answers.value = ''
        for (let i = data.length - 1; i >= 0; i--) {
          if (data[i].question) {
            addClientMsg(data[i], false);
          }
          if (i == 0) {
            msgRecords.value[chatType.value].push({
              msgType: 'history'
            })
          }
          appendServerMessage(data[i]);
        }


        if (chatContent.value) {
          chatContent.value.scrollTop = chatContent.value.scrollHeight;
        }
      } else {
        appendServerMessage(data[0]);
      }

      // 添加完消息后，除了流式持续返回这种场景，其他的恢复按钮的状态
      if(data[data.length - 1].answerType != 'STREAM') {
        chatBtnDisabled.value = false
      }
    })

  })
  // 关闭链接
  socket.onclose = disconnect;

}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect(() => {});
  }
  console.log("ws中断");
  stompClient = null;
  // 提醒用户重新连接
  chatTextAreaDisabled.value = true
  chatBtnDisabled.value = false
  chatBtnText.value = '重连'
}

// 添加服务器端消息
function appendServerMessage(answer: WebSocketResponseType) {
  let content = answer.answer;
  let time = answer.answerTime;
  let answerType = answer.answerType;
  let chatId = answer.chatUid
  let appendLastChat = false;
  aiLoading.value = false
  // 如果 source 等于"CHAT_GPT_3_5"
  if("JSON" === answerType) {
    // 需要对 body 的 JSON 字符串进行解析
    const res = JSON.parse(content);
    console.log("CHAT_GPT_3_5 res:", res);
    if (res.length === 1) {
      content = res[0].message.content;
    }
  } else if ('STREAM' === answerType || 'STREAM_END' === answerType) {
    // const lastDiv = $(`#${chatId}`)
    const lastIndex = msgRecords.value[chatType.value].findLastIndex((msg) => msg.msgType === 'answer' && msg.chatUid === chatId)
    if(lastIndex == -1){
      // 上一次没有输出过，则格式化文本，重新输出
    }else{
      // 对于流式返回的结果，找上一次的返回，进行结果的追加，手动将分隔符给干掉
      msgRecords.value[chatType.value][lastIndex].answer = content
      appendLastChat = true
    }

  }

  if(!appendLastChat) {
    msgRecords.value[chatType.value].push({
      msgType: 'answer',
      answer: content,
      answerTime: time,
      chatUid: chatId
    })
  }
  scrollToBottom();


  // 添加完后滚动到底部
  // scrollToBottom();

  // copy();

}

// 添加用户端消息
function addClientMsg(data: WebSocketResponseType, showLoading: boolean) {
  msgRecords.value[chatType.value].push({
    msgType: 'question',
    question: data.question,
    questionTime: data.questionTime
  })
  // 添加完后滚动到底部
  scrollToBottom();
}


const scrollToBottom = () => {
  nextTick(() => {
    if (chatContent.value) {
      chatContent.value.scrollTop = chatContent.value.scrollHeight;
      chatContent.value.scroll({
        top: chatContent.value.scrollHeight + 100,
        behavior: 'smooth'
      });
    }
  });
}

// 复制功能
// function copy() {
//   // 从 chatContent 中获取最后一个 chat-message
//   const chatMessage = chatContent.value.children(".home_chat-message__rdH_g").last();
//   console.log("chatContent", chatMessage);
//   // 从 chatMessage 找出复制按钮
//   const copyBtn = chatMessage.find(".home_chat-message-top-action__wXKmA").get(0);
//
//   const clipboard = new ClipboardJS(copyBtn, {
//     text: function(trigger) {
//       let copyInput = chatMessage.find('.markdown-body').get(0);
//       return copyInput.innerText;
//     }
//   });
//
//   clipboard.on('success', function(e) {
//     // 复制成功
//     toastr.info("复制成功");
//     e.clearSelection();
//   });
//
//   clipboard.on('error', function(e) {
//     console.log('复制失败');
//   });
// }

// 发送问题
const doSend = () => {
  const qa = chatText.value
  if (qa.length > 512) {
    messageTip("提问长度请不要超过512字符哦~", 'info')
    return;
  }
  // 表示将消息转发到那个目标，类似于http请求中的path路径
  // @ts-ignore
  stompClient.send("/app/chat/" + session, {'s-uid': session}, qa);
  // 清空 textarea
  chatText.value = ''

  msgRecords.value[chatType.value].push({
    msgType: 'question',
    question: qa
  })
  aiLoading.value = true

  // 将 button 设为禁用，防止用户连续点击
  chatBtnDisabled.value = true
}

// 绑定按钮事件
const sendMsg = () => {
  if(stompClient == null){
    initWs()
  }else{
    // 如果消息内容为空的时候重新聚焦到输入框
    if (chatText.value == '') {
      messageTip("请输入内容", 'info')
    } else {
      // 发送消息
      doSend();
    }
  }
}

// 获取登录信息
onMounted(async () => {
  await doGet<CommonResponse>(GLOBAL_INFO_URL, {})
    .then((res) => {
      globalStore.setGlobal(res.data.global)
    })
  // 开始进行ws的初始化
  if(global.isLogin){
    initWs()

  }else{
    messageTip("请先登录", 'info')
  }

  console.log(global.isLogin, global.user.userId, chatTextAreaDisabled.value)
})

// 登录框
const changeClicked = () => {
  loginDialogClicked.value = !loginDialogClicked.value
  console.log("clicked: ", loginDialogClicked.value)
}

provide('loginDialogClicked', changeClicked)
const loginDialogClicked = ref(false)
</script>

<style scoped>

</style>
