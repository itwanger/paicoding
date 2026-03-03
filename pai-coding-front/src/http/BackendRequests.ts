import axios, { AxiosError, type AxiosResponse } from "axios";
import {clearStorage, getTokenName, messageConfirm, messageTip} from "@/util/utils";
import {
  BASE_URL,
  KNOWLEDGE_AGENT_ASK_URL,
  KNOWLEDGE_DOC_DETAIL_URL,
  KNOWLEDGE_DOC_LIST_URL,
  KNOWLEDGE_SEARCH_URL,
  KNOWLEDGE_TAGS_URL,
  KNOWLEDGE_TREE_URL,
  MOCK_LOGIN_URL
} from '@/http/URL'
import { LOCALSTORAGE_AUTHORIZATION } from '@/constants/LocalStorageConstants'

axios.defaults.baseURL = BASE_URL;
axios.defaults.withCredentials = true

interface Params {
  [key: string]: any;
}

interface Data {
  [key: string]: any;
}


export function doGet<CommonResponse>(url: string, params: Params, type?: 'json'|'text'): Promise<AxiosResponse<CommonResponse>> {
  const responseType = type? type : 'json';
  return axios({
    method: 'get',
    url: url,
    params: params,
    responseType: responseType,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}



export function doPost<CommonResponse>(url: string, data: Data): Promise<AxiosResponse<CommonResponse>> {
  return axios({
    method: 'post',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

export function doFilePost<T>(url: string, data: FormData): Promise<AxiosResponse<T>> {
  return axios({
    method: 'post',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

export function doLoginPost<T>(url: string, data: Data): Promise<AxiosResponse<T>> {
  return axios({
    method: 'post',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

export function doPut<T>(url: string, data: Data): Promise<AxiosResponse<T>> {
  return axios({
    method: 'put',
    url: url,
    data: data,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

export function doDelete<T>(url: string, params: Params): Promise<AxiosResponse<T>> {
  return axios({
    method: 'delete',
    url: url,
    params: params,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

// 用于发送给除了springboot后端以外的其他后端
// 用于发送文件并接收文件下载
export function extraFilePostAndDownload(baseUrl: string, url: string, data: FormData, params?: Data): Promise<AxiosResponse<any>> {
  return axios({
    method: 'post',
    baseURL: baseUrl,
    url: url,
    data: data,
    params: params,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    withCredentials: false,
    responseType: 'blob', // 设置 responseType 为 'blob'
  });
}

axios.interceptors.request.use((config) => {
  let token = window.sessionStorage.getItem(getTokenName())
  if(!token){
    token = window.localStorage.getItem(getTokenName())
    // if(token){
      // config.headers['rememberMe'] = true
    // }
  }
  if(token){
    config.headers[LOCALSTORAGE_AUTHORIZATION] = token
  }
  return config;
}, (error) => {
  return Promise.reject(error);
})

axios.interceptors.response.use(
  // @ts-ignore
  function (response: AxiosResponse): AxiosResponse | undefined {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    // 拦截token验证结果，进行对应提示和页面调整
    if (response.data.code > 900) { // code 大于900说明验证未通过
      // 给前端用户提示，并且跳转页面
      messageConfirm(response.data.msg + ". 是否重新去登录？")
        .then(() => { // 用户点击确定按钮的回调
          console.log("用户点击确定");
          clearStorage();
          window.location.href = "/";
          console.log("用户点击确定2");
        })
        .catch(() => { // 用户点击取消的回调
          messageTip("取消去登录", "warning");
        });
      return; // 返回 undefined 以确保 Axios 不继续处理响应
    }
    // else if(response.data.code !== 200){
    //   messageTip(response.data.msg, "error")
    //   return;
    // }
    return response;
  },
  function (error: AxiosError): Promise<AxiosError> {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    return Promise.reject(error);
  }
);


// ============== 模拟登录的两个请求 ==============
export function mockLoginXML<CommonResponse>(code: string): Promise<AxiosResponse<CommonResponse>> {
  return axios({
    method: 'post',
    url: MOCK_LOGIN_URL,
    data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[demoUser1234]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + code + "]]></Content><MsgId>11111111</MsgId></xml>",
    headers: {
      'Content-Type': 'application/xml',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

export function mockLogin2XML<CommonResponse>(code: string): Promise<AxiosResponse<CommonResponse>> {
  const randUid = 'demoUser_' + Math.round(Math.random() * 100);
  return axios({
    method: 'post',
    url: MOCK_LOGIN_URL,
    data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[" + randUid + "]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + code + "]]></Content><MsgId>11111111</MsgId></xml>",
    headers: {
      'Content-Type': 'application/xml',
    },
    withCredentials: true, // 确保发送请求时包含凭证
  });
}

// ============== Chat V2 API ==============

/**
 * 获取可用模型列表
 */
export function getChatModels<T>(): Promise<AxiosResponse<T>> {
  return doGet('/chatv2/api/models', {});
}

/**
 * 获取默认模型
 */
export function getDefaultModel<T>(): Promise<AxiosResponse<T>> {
  return doGet('/chatv2/api/models/default', {});
}

/**
 * 获取会话列表
 */
export function getConversations<T>(): Promise<AxiosResponse<T>> {
  return doGet('/chatv2/api/conversations', {});
}

/**
 * 生成新的 conversationId
 */
export function generateConversationId<T>(): Promise<AxiosResponse<T>> {
  return doPost('/chatv2/api/conversation/generate-id', {});
}

/**
 * 获取会话详情
 * 使用路径参数传递 conversationId
 */
export function getConversation<T>(conversationId: string): Promise<AxiosResponse<T>> {
  return doGet(`/chatv2/api/conversation/${conversationId}`, {});
}

/**
 * 更新会话标题
 * 使用路径参数传递 conversationId
 */
export function updateConversationTitle<T>(conversationId: string, title: string): Promise<AxiosResponse<T>> {
  return doPut(`/chatv2/api/conversation/${conversationId}/title`, { title });
}

/**
 * 删除会话
 * 使用路径参数传递 conversationId
 */
export function deleteConversation<T>(conversationId: string): Promise<AxiosResponse<T>> {
  return doDelete(`/chatv2/api/conversation/${conversationId}`, {});
}

/**
 * 发送消息（流式响应）
 * 使用 axios onDownloadProgress 处理流式响应，参考 deepextract 实现
 * 后端直接返回纯文本流，不带 SSE "data:" 前缀
 */
export async function sendChatMessage(
  message: string,
  conversationId: string | number | null,
  modelId: string | null,
  onChunk: (chunk: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): Promise<void> {
  let lastLength = 0;
  let requestCompleted = false;
  const cancelTokenSource = axios.CancelToken.source();

  const requestConfig = {
    method: 'post',
    url: '/chatv2/api/send',
    data: {
      conversationId: String(conversationId),
      modelId,
      message
    },
    headers: {
      'Content-Type': 'application/json'
    },
    responseType: 'text' as const, // 明确指定为文本类型，确保流式响应
    withCredentials: true,
    onDownloadProgress: (progressEvent: any) => {
      if (requestCompleted) return;

      // 获取累积的响应文本（纯文本，不带 SSE 格式）
      const responseText = progressEvent.event?.target?.responseText || '';

      console.log('📥 Progress event:', {
        loaded: progressEvent.loaded,
        total: progressEvent.total,
        responseTextLength: responseText.length,
        lastLength: lastLength,
        newContent: responseText.substring(lastLength, lastLength + 50)
      });

      // 参考 deepextract: 检查是否完成
      if (responseText.includes('[DONE]')) {
        requestCompleted = true;
        console.log('✅ Stream completed, responseText length:', responseText.length);
        // 传递原始 responseText，让调用方自己处理清理
        onChunk(responseText);
        onComplete();
        cancelTokenSource.cancel('Stream completed');
        return;
      }

      // 检查是否有错误
      if (responseText.includes('[ERROR]')) {
        requestCompleted = true;
        const errorMatch = responseText.match(/\[ERROR\] (.+)/);
        const errorMsg = errorMatch ? errorMatch[1] : 'Unknown error';
        console.error('❌ Stream error:', errorMsg);
        onError(new Error(errorMsg));
        cancelTokenSource.cancel('Stream error');
        return;
      }

      // 参考 deepextract: 直接传递原始 responseText，实现流式更新
      if (responseText.length > lastLength) {
        console.log('📝 Updating, length:', responseText.length, 'preview:', responseText.substring(0, 50));
        onChunk(responseText);
        lastLength = responseText.length;
      }
    },
    cancelToken: cancelTokenSource.token
  };

  try {
    await axios(requestConfig);

    // 请求正常完成但没有触发 [DONE]
    if (!requestCompleted) {
      onComplete();
    }
  } catch (error: any) {
    if (axios.isCancel(error)) {
      // 流式请求被正常取消（已完成）
      console.log('Stream request cancelled normally');
      return;
    }

    if (!requestCompleted) {
      console.error('Stream error:', error);
      onError(error);
    }
  }
}

// ============== Knowledge Base API ==============

export function getKnowledgeTree<T>(): Promise<AxiosResponse<T>> {
  return doGet(KNOWLEDGE_TREE_URL, {})
}

export function getKnowledgeTags<T>(): Promise<AxiosResponse<T>> {
  return doGet(KNOWLEDGE_TAGS_URL, {})
}

export function getKnowledgeDocs<T>(params: { categoryId?: number | null; tagId?: number | null; page?: number; size?: number }): Promise<AxiosResponse<T>> {
  return doGet(KNOWLEDGE_DOC_LIST_URL, params)
}

export function searchKnowledgeDocs<T>(params: { q: string; categoryId?: number | null; tagId?: number | null; page?: number; size?: number }): Promise<AxiosResponse<T>> {
  return doGet(KNOWLEDGE_SEARCH_URL, params)
}

export function getKnowledgeDocDetail<T>(docId: number | string): Promise<AxiosResponse<T>> {
  return doGet(`${KNOWLEDGE_DOC_DETAIL_URL}/${docId}`, {})
}

export function askKnowledgeAgent<T>(payload: { question: string; contextDocIds?: number[]; allowMutation?: boolean }): Promise<AxiosResponse<T>> {
  return doPost(KNOWLEDGE_AGENT_ASK_URL, payload)
}
