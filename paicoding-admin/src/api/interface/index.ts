// * 请求响应参数(不包含data)
export interface Result {
	code: string;
	msg: string;
}

// * 请求响应参数(包含data)
export interface ResultData<T = any> extends Result {
	data?: T;
	status?: Status;
	result?: T;
}

export interface Status {
	code: number;
	msg: string;
}
export interface PaiRes<T = any> {
	status: Status;
	result?: T;
}

// * 分页响应参数
export interface ResPage<T> {
	datalist: T[];
	pageNum: number;
	pageSize: number;
	total: number;
}

// * 分页请求参数
export interface ReqPage {
	pageNum: number;
	pageSize: number;
}

// * 登录
export namespace Login {
	export interface ReqLoginForm {
		username: string;
		password: string;
	}
	export interface ResLogin {
		access_token: string;
		userId: number;
		// 登录用户名
		userName: string;
		// 用户头像
		photo: string;
	}
	export interface ResAuthButtons {
		[propName: string]: any;
	}
}
