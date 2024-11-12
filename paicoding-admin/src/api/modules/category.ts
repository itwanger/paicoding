import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";
import { IFormType } from "@/views/config";

/**
 * @name 分类模块
 */

// 获取列表
export const getCategoryListApi = (data: { pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/category/list`, data);
};

// 删除操作
export const delCategoryApi = (categoryId: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/category/delete`, { categoryId });
};

// 保存操作
export const updateCategoryApi = (form: IFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/category/save`, form);
};

// 上线/下线操作
export const operateCategoryApi = (params: object | undefined) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/category/operate`, params);
};
