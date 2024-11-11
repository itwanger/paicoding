import React from "react";

import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import lazyLoad from "../utils/lazyLoad";

const columnRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/author",
				meta: {
					// requiresAuth: true,
					title: "作者",
					key: "/author"
				},
				children: [
					{
						path: "/author/whitelist/index",
						element: lazyLoad(React.lazy(() => import("@/views/author/whitelist/index"))),
						meta: {
							title: "作者白名单",
							key: "/author/whitelist/index"
						}
					},
					{
						path: "/author/zsxqlist/index",
						element: lazyLoad(React.lazy(() => import("@/views/author/zsxqlist/index"))),
						meta: {
							title: "作者白名单",
							key: "/author/zsxqlist/index"
						}
					}
				]
			}
		]
	}
];

export default columnRouter;
