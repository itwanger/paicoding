import produce from "immer";
import { AnyAction } from "redux";

import { BreadcrumbState } from "@/redux/interface";
import * as types from "@/redux/mutation-types";

const breadcrumbState: BreadcrumbState = {
	breadcrumbList: {}
};

// breadcrumb reducer
const breadcrumb = (state: BreadcrumbState = breadcrumbState, action: AnyAction) =>
	produce(state, draftState => {
		switch (action.type) {
			case types.SET_BREADCRUMB_LIST:
				draftState.breadcrumbList = action.breadcrumbList;
				break;
			default:
				return draftState;
		}
	});

export default breadcrumb;
