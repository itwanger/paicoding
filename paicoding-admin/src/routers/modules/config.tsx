import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import Banner from "@/views/config";

const configRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/config/index",
				element: <Banner />,
				meta: {
					// requiresAuth: true,
					title: "配置",
					key: "config"
				}
			}
		]
	}
];

export default configRouter;
