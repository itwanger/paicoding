import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import Banner from "@/views/global";

const configRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/global/index",
				element: <Banner />,
				meta: {
					// requiresAuth: true,
					title: "全局",
					key: "global"
				}
			}
		]
	}
];

export default configRouter;
