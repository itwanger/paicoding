import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import Statistics from "@/views/statistics";

const statisticsRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/statistics/index",
				element: <Statistics />,
				meta: {
					// requiresAuth: true,
					title: "数据统计",
					key: "statistics"
				}
			}
		]
	}
];

export default statisticsRouter;
