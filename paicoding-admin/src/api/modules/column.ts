import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";
import { IFormType } from "@/views/column/setting";
import { IArticleSortFormType } from "@/views/column/setting/articlesort";

/**
 * @name 教程模块
 */

// 获取列表
export const getColumnListApi = (data: { pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/column/list`, data);
};

// 添加返回类型
export const getColumnByNameListApi = (key: string) => {
	return http.get(`${PORT1}/column/query`, { key });
};

// 获取作者列表，参数为作者名称
export const getAuthorListApi = (key: string) => {
	return http.get(`${PORT1}/user/query`, { key });
};

// 保存操作
export const updateColumnApi = (form: IFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/column/saveColumn`, form);
};

// 删除专栏操作
export const delColumnApi = (columnId: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/column/deleteColumn`, { columnId });
};

// 获取列表
export const getColumnArticleListApi = (data: { columnId: number; pageNumber: number; pageSize: number }) => {
	return http.post(`${PORT1}/column/listColumnArticle`, data);
};

// 保存操作
export const updateColumnArticleApi = (form: IFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/column/saveColumnArticle`, form);
};

// 删除教程操作
export const delColumnArticleApi = (id: number) => {
	return http.get<Login.ResAuthButtons>(`${PORT1}/column/deleteColumnArticle`, { id });
};

// 调整两个教程的顺序
export const sortColumnArticleApi = (activeId: number, overId: number) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/column/sortColumnArticleApi`, { activeId, overId });
};

// 调整教程的顺序
export const sortColumnArticleByIDApi = (form: IArticleSortFormType) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/column/sortColumnArticleByIDApi`, form);
};
