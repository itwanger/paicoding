import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";

// 添加返回类型
export const getAuthorWhiteListApi = () => {
	return http.get(`${PORT1}/author/whitelist/get`);
};

// 获取知识星球用户白名单
export const getZsxqWhiteListApi = (data: { pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/zsxq/whitelist`, data);
};

// 更新知识星球用户白名单
export const updateZsxqWhiteApi = (params: object | undefined) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/zsxq/whitelist/save`, params);
};

// 审核知识星球用户白名单
export const operateZsxqWhiteApi = (params: object | undefined) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/zsxq/whitelist/operate`, params);
};

// 审核知识星球用户白名单，批量
export const operateBatchZsxqWhiteApi = (params: object | undefined) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/zsxq/whitelist/batchOperate`, params);
};

// 重置操作
export const resetAuthorWhiteApi = (authorId: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/zsxq/whitelist/reset`, { authorId });
};

// 保存操作
export const updateAuthorWhiteApi = (authorId: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/author/whitelist/add`, { authorId });
};
