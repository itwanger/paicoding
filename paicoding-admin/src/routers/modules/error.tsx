import React from "react";

import { RouteObject } from "@/routers/interface";
import lazyLoad from "@/routers/utils/lazyLoad";

// 错误页面模块
const errorRouter: Array<RouteObject> = [
	{
		path: "/403",
		element: lazyLoad(React.lazy(() => import("@/components/ErrorMessage/403"))),
		meta: {
			// requiresAuth: true,
			title: "403页面",
			key: "403"
		}
	},
	{
		path: "/404",
		element: lazyLoad(React.lazy(() => import("@/components/ErrorMessage/404"))),
		meta: {
			// requiresAuth: false,
			title: "404页面",
			key: "404"
		}
	},
	{
		path: "/500",
		element: lazyLoad(React.lazy(() => import("@/components/ErrorMessage/500"))),
		meta: {
			// requiresAuth: false,
			title: "500页面",
			key: "500"
		}
	}
];

export default errorRouter;
