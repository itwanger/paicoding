import http from "@/api";
import { PORT1 } from "@/api/config/servicePort";
import { Login } from "@/api/interface/index";
/**
 * @name 分类模块
 */

// 获取字典值
export const getDiscListApi = () => {
	console.log("获取字典，getDiscListApi");
	return http.get(`${PORT1}/common/dict`);
};

// 上传图片
export const uploadImgApi = (data: FormData) => {
	return http.post<Login.ResAuthButtons>(`${PORT1}/image/upload`, data);
};
