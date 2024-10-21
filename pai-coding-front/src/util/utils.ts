import {ElMessage, ElMessageBox} from "element-plus";
import { LOCALSTORAGE_AUTHORIZATION } from '@/constants/LocalStorageConstants'

/**
 * 消息提示
 * @param message
 * @param messageType
 */
export function messageTip(message: string, messageType: string) {
  ElMessage({
    // @ts-ignore
    center: true,   // 文字居中
    showClose: true,
    duration: 3000,   // 显示3秒
    message: message,
    // "success" | "warning" | "info" | "error"
    type: messageType,
  })
}

/**
 * 返回存储在local storage或者session storage中jwt token的名字
 * @returns {string}
 */
export function getTokenName(){
  return LOCALSTORAGE_AUTHORIZATION
}

/**
 * 清除storage中的token
 */
export function clearStorage(){
  window.localStorage.removeItem(getTokenName())
  window.sessionStorage.removeItem(getTokenName())
}

/**
 * 确认消息提示
 * @param msg
 */
export function messageConfirm(msg: string){
  return ElMessageBox.confirm(
    // 提示语
    msg,
    '系统提醒',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
}

export function removeToken() {
  window.sessionStorage.removeItem(getTokenName());
  window.localStorage.removeItem(getTokenName());
}


/**
 * 获取token
 *
 * @returns {string}
 */
export function getToken() {
  let token = window.sessionStorage.getItem(getTokenName());
  if (!token) { //前面加了一个！，表示token不存在，token是空的，token没有值，这个意思
    token = window.localStorage.getItem(getTokenName());
  }
  if (token) { //表示token存在，token不是空的，token有值，这个意思
    return token;
  } else {
    messageConfirm("请求token为空，是否重新去登录？").then(() => { //用户点击“确定”按钮就会触发then函数
      //既然后端验证token未通过，那么前端的token肯定是有问题的，那没必要存储在浏览器中，直接删除一下
      removeToken();
      //跳到登录页
      window.location.href = "/";
    }).catch(() => { //用户点击“取消”按钮就会触发catch函数
      messageTip("取消去登录", "warning");
    })
  }
}

// ============ 其他工具函数 ================
// 定义一个刷新页面的方法
export function refreshPage() {
  if (window.location.pathname === "/login") {
    // 登录成功，跳转首页
    window.location.href = "/";
  } else {
    // 刷新当前页面
    window.location.reload();
  }
}

// ================== 睡眠函数 ==================
function sleepFunc(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// 使用示例
export async function sleep(second: number) {
  // console.log('开始睡眠');
  await sleepFunc(second * 1000);
  // console.log('睡眠结束');
}

// ================== 获取cookie ==================
export function getCookie(name: string){
  const strcookie = document.cookie //获取cookie字符串
  const arrcookie = strcookie.split('; ')//分割
  //遍历匹配
  for (let i = 0; i < arrcookie.length; i++) {
    const arr = arrcookie[i].split('=')
    if (arr[0] == name){
      return arr[1];
    }
  }
  return "";
}

// ================== 设置LocalStorage ==================
export function setLocalStorageToken(key: string, value: string) {
  window.localStorage.setItem(key, value)
}

export function setAuthToken(token: string) {
  setLocalStorageToken(LOCALSTORAGE_AUTHORIZATION, token)
}

export function getAuthToken(): string {
  return window.localStorage.getItem(LOCALSTORAGE_AUTHORIZATION) || ""
}

// ================== 设置title ==================
export function setTitle(title: string) {
  document.title = title;
}

