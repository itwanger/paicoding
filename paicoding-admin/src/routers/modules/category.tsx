import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import Sort from "@/views/category";

const sortRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/category/index",
				element: <Sort />,
				meta: {
					// requiresAuth: true,
					title: "分类",
					key: "sort"
				}
			}
		]
	}
];

export default sortRouter;
