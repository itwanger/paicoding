import { message } from "antd";
import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";

import { PaiRes, ResultData } from "@/api/interface";
import { LOGIN_URL } from "@/config/config";
import NProgress from "@/config/nprogress";
import { showFullScreenLoading, tryHideFullScreenLoading } from "@/config/serviceLoading";
import { ResultEnum } from "@/enums/httpEnum";
import { store } from "@/redux";
import { setToken } from "@/redux/modules/global/action";
import { AxiosCanceler } from "./helper/axiosCancel";
import { checkStatus } from "./helper/checkStatus";

const axiosCanceler = new AxiosCanceler();

const config = {
	// 默认地址请求地址，可在 .env 开头文件中修改，在 Axios 中使用
	// 实例化的使用用到
	baseURL: import.meta.env.VITE_API_URL as string,
	// 跨域时候允许携带凭证
	withCredentials: true
};

class RequestHttp {
	service: AxiosInstance;

	// 构造方法
	public constructor(config: AxiosRequestConfig) {
		// 实例化axios
		this.service = axios.create(config);

		/**
		 * @description 请求拦截器
		 * 客户端发送请求 -> [请求拦截器] -> 服务器
		 * token校验(JWT) : 接受服务器返回的token,存储到redux/本地储存当中
		 */
		this.service.interceptors.request.use(
			(config: AxiosRequestConfig) => {
				console.log("发起请求");
				// 进度条开始
				NProgress.start();
				// 将当前请求添加到 pending 中
				axiosCanceler.addPending(config);
				// 如果当前请求不需要显示 loading
				// 在api服务中通过指定的第三个参数: { headers: { noLoading: true } }来控制不显示loading，参见loginApi
				config.headers!.noLoading || showFullScreenLoading();
				// 从 Redux store 中获取 token 并将其添加到请求的 headers 中。这通常用于身份验证，确保后端可以验证用户的身份。
				const token: string = store.getState().global.token;
				console.log("token", token);

				return { ...config, headers: { ...config.headers, "x-access-token": token } };
			},
			(error: AxiosError) => {
				console.log("error", error);
				return Promise.reject(error);
			}
		);

		/**
		 * @description 响应拦截器
		 *  服务器换返回信息 -> [拦截统一处理] -> 客户端JS获取到信息
		 */
		this.service.interceptors.response.use(
			(response: AxiosResponse) => {
				console.log("response", response);

				const { data, config } = response;
				// 进度条结束
				NProgress.done();
				// 在请求结束后，移除本次请求(关闭loading)
				axiosCanceler.removePending(config);
				tryHideFullScreenLoading();
				// 服务器返回的状态码
				const dataStatus = data.status;
				// 登录失效（code == 599）
				if (dataStatus && dataStatus.code == ResultEnum.NOT_LOGIN) {
					// 重定向到登录页面
					store.dispatch(setToken(""));
					message.error(dataStatus.msg);
					window.location.hash = LOGIN_URL;
					return Promise.reject(data);
				}
				// 全局错误信息拦截（防止下载文件得时候返回数据流，没有code，直接报错）
				if (dataStatus.code && dataStatus.code !== ResultEnum.SUCCESS) {
					message.error(dataStatus.msg);
					return Promise.reject(data);
				}
				// 成功请求（在页面上除非特殊情况，否则不用处理失败逻辑）
				return data;
			},
			async (error: AxiosError) => {
				console.log("error", error);
				const { response } = error;
				NProgress.done();
				tryHideFullScreenLoading();
				// 请求超时单独判断，请求超时没有 response
				if (error.message.indexOf("timeout") !== -1) message.error("请求超时，请稍后再试");
				// 根据响应的错误状态码，做不同的处理
				if (response) checkStatus(response.status);
				// 服务器结果都没有返回(可能服务器错误可能客户端断网) 断网处理:可以跳转到断网页面
				if (!window.navigator.onLine) window.location.hash = "/500";
				return Promise.reject(error);
			}
		);
	}

	// * 常用请求方法封装
	get<T>(url: string, params?: object, _object = {}): Promise<ResultData<T>> {
		console.log("开始执行get 请求", url, params, _object);
		return this.service.get(url, { params, ..._object });
	}
	post<T>(url: string, params?: object, _object = {}): Promise<ResultData<T>> {
		return this.service.post(url, params, _object);
	}

	postForm<T>(url: string, params?: object, _object = {}): Promise<PaiRes<T>> {
		return this.service.post(url, params, _object);
	}

	put<T>(url: string, params?: object, _object = {}): Promise<ResultData<T>> {
		return this.service.put(url, params, _object);
	}
	delete<T>(url: string, params?: any, _object = {}): Promise<ResultData<T>> {
		return this.service.delete(url, { params, ..._object });
	}
}

export default new RequestHttp(config);
