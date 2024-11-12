import { toPairs } from "lodash";
import { Dispatch } from "redux";

import { getDiscListApi } from "@/api/modules/common";
import * as types from "@/redux/mutation-types";

// * setDiscList
// export const setDiscList = discList => ({
// 	type: types.UPDATE_DISC,
// 	discList
// });
// * redux-promise《async/await》
const dictTransform = (dict = {}, keys = ["id", "title"]) => {
	console.log("字典 d", dict);

	return toPairs(dict).map(item => {
		return {
			[keys[0]]: item[0],
			[keys[1]]: item[1]
		};
	});
};

// * redux-thunk
// 获取字典数据
// 异步 action creator
export const getDiscListAction = () => {
	return async (dispatch: Dispatch) => {
		const { result } = (await getDiscListApi()) || {};
		console.log("获取字典，getDiscListAction");

		let dictionaryMap = {};
		for (const key in result as object) {
			if (Object.getOwnPropertyDescriptor(result, key)) {
				// @ts-ignore
				dictionaryMap[key] = result[key];
				// @ts-ignore
				dictionaryMap[`${key}List`] = dictTransform(result[key], ["value", "label"]);
			}
		}

		console.log("字典", dictionaryMap);

		// 分发 action 更新 state
		dispatch({
			type: types.UPDATE_DISC,
			discList: dictionaryMap
		});
	};
};
