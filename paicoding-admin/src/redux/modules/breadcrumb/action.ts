import * as types from "@/redux/mutation-types";

// * setBreadcrumbList
export const setBreadcrumbList = (breadcrumbList: { [propName: string]: any }) => ({
	type: types.SET_BREADCRUMB_LIST,
	breadcrumbList
});
