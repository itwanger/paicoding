import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";
import { IFormType } from "@/views/global";

/**
 * @name 标签模块
 */

// 获取列表
export const getGlobalConfigListApi = (data: { pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/global/config/list`, data);
};

// 删除操作
export const delGlobalConfigApi = (id: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/global/config/delete`, { id });
};

// 保存操作
export const updateGlobalConfigApi = (form: IFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/global/config/save`, form);
};
