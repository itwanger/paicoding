// 路由权限控制

import { Navigate, useLocation } from "react-router-dom";

import { AxiosCanceler } from "@/api/helper/axiosCancel";
import { HOME_URL, LOGIN_URL } from "@/config/config";
import { store } from "@/redux/index";
import { rootRouter } from "@/routers/index";
import { searchRoute } from "@/utils/util";

const axiosCanceler = new AxiosCanceler();

/**
 * @description 路由守卫组件
 * */
const AuthRouter = (props: { children: JSX.Element }) => {
	const { pathname } = useLocation();
	const route = searchRoute(pathname, rootRouter);
	console.log({ route });

	// * 在跳转路由之前，清除所有的请求
	axiosCanceler.removeAllPending();
	console.log({ props });

	// * 判断当前路由是否需要访问权限(不需要权限直接放行)
	if (!route.meta?.requiresAuth) return props.children;

	// * 判断是否有Token
	const token = store.getState().global.token;
	if (!token) return <Navigate to={LOGIN_URL} replace />;

	// * Dynamic Router(动态路由，根据后端返回的菜单数据生成的一维数组)
	const dynamicRouter = store.getState().auth.authRouter;
	// * Static Router(静态路由，必须配置首页地址，否则不能进首页获取菜单、按钮权限等数据)，获取数据的时候会loading，所有配置首页地址也没问题
	const staticRouter = [HOME_URL, "/403"];
	const routerList = dynamicRouter.concat(staticRouter);
	// * 如果访问的地址没有在路由表中重定向到403页面
	if (routerList.indexOf(pathname) == -1) return <Navigate to="/403" />;

	// * 当前账号有权限返回 Router，正常访问页面
	return props.children;
};

export default AuthRouter;
