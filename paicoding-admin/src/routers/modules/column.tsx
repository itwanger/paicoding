import React from "react";

import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import lazyLoad from "../utils/lazyLoad";

const columnRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/column",
				meta: {
					// requiresAuth: true,
					title: "专栏",
					key: "/column"
				},
				children: [
					{
						path: "/column/setting/index",
						element: lazyLoad(React.lazy(() => import("@/views/column/setting/index"))),
						meta: {
							title: "专栏设置",
							key: "/column/setting/index"
						}
					},
					{
						path: "/column/setting/index/articlesort",
						element: lazyLoad(React.lazy(() => import("@/views/column/setting/articlesort/index"))),
						meta: {
							title: "教程排序",
							key: "/column/setting/index/articlesort"
						}
					},
					{
						path: "/column/article/index",
						element: lazyLoad(React.lazy(() => import("@/views/column/article/index"))),
						meta: {
							title: "添加教程",
							key: "/column/article/index"
						}
					}
				]
			}
		]
	}
];

export default columnRouter;
