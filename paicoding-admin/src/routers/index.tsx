import { Navigate, useRoutes } from "react-router-dom";

import { RouteObject } from "@/routers/interface";
import Login from "@/views/login/index";

// * 导入所有router
const metaRouters = import.meta.globEager("./modules/*.tsx");
console.log("metaRouters", metaRouters);

// * 处理路由
export const routerArray: RouteObject[] = [];

Object.keys(metaRouters).forEach(item => {
	console.log("item", item);
	const router = metaRouters[item];

	Object.keys(router).forEach((key: any) => {
		console.log("key", key);

		routerArray.push(...router[key]);
	});
});

export const rootRouter: RouteObject[] = [
	{
		path: "/",
		element: <Navigate to="/login" />
	},
	{
		path: "/login",
		element: <Login />,
		meta: {
			title: "登录页",
			key: "login"
		}
	},
	...routerArray,
	{
		path: "*",
		element: <Navigate to="/404" />
	}
];

const Router = () => {
	const routes = useRoutes(rootRouter);
	console.log("routes", { routes });

	return routes;
};

export default Router;
