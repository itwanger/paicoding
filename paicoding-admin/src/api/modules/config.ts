import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";
import { IFormType } from "@/views/config";

/**
 * @name 分类模块
 */

// 获取列表
export const getConfigListApi = (data: { pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/config/list`, data);
};

// 删除操作
export const delConfigApi = (configId: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/config/delete`, { configId });
};

// 保存操作
export const updateConfigApi = (form: IFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/config/save`, form);
};

// 上线/下线操作
export const operateConfigApi = (params: object | undefined) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/config/operate`, params);
};
