import React from "react";

import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import Article from "@/views/article/list";
import lazyLoad from "../utils/lazyLoad";

const articleRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/article",
				meta: {
					// requiresAuth: true,
					title: "文章",
					key: "/article"
				},
				children: [
					{
						path: "/article/list/index",
						element: lazyLoad(React.lazy(() => import("@/views/article/list/index"))),
						meta: {
							title: "文章列表",
							key: "/article/list/index"
						}
					},
					{
						path: "/article/edit/index",
						element: lazyLoad(React.lazy(() => import("@/views/article/edit/index"))),
						meta: {
							title: "文章编辑",
							key: "/article/edit/index"
						}
					}
				]
			}
		]
	}
];

export default articleRouter;
