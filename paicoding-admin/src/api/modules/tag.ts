import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";
import { IFormType } from "@/views/tag";

/**
 * @name 标签模块
 */

// 获取列表
export const getTagListApi = (data: { pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/tag/list`, data);
};

// 删除操作
export const delTagApi = (tagId: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/tag/delete`, { tagId });
};

// 保存操作
export const updateTagApi = (form: IFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/tag/save`, form);
};

// 上线/下线操作
export const operateTagApi = (params: object | undefined) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/tag/operate`, params);
};
