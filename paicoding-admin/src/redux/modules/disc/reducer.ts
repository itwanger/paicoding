import produce from "immer";
import { AnyAction } from "redux";

import { DiscState } from "@/redux/interface";
import * as types from "@/redux/mutation-types";

const discState: DiscState = {
	disc: []
};

// disc reducer
const disc = (state: DiscState = discState, action: AnyAction) =>
	produce(state, draftState => {
		switch (action.type) {
			case types.UPDATE_DISC:
				draftState.disc = action.discList;
				break;
			default:
				return draftState;
		}
	});

export default disc;
