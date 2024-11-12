import { LayoutIndex } from "@/routers/constant";
import { RouteObject } from "@/routers/interface";
import Label from "@/views/tag";

const labelRouter: Array<RouteObject> = [
	{
		element: <LayoutIndex />,
		children: [
			{
				path: "/tag/index",
				element: <Label />,
				meta: {
					// requiresAuth: true,
					title: "标签",
					key: "tag"
				}
			}
		]
	}
];

export default labelRouter;
