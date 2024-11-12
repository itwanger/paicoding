import { ThemeConfigProp } from "@/redux/interface/index";
import * as types from "@/redux/mutation-types";
import { MapItem } from "@/typings/common";

// * setToken
export const setToken = (token: string) => ({
	type: types.SET_TOKEN,
	token
});

export const setUserInfo = (userInfo: MapItem) => {
	return {
		type: types.USER_INFO,
		userInfo
	};
};

// * setAssemblySize
export const setAssemblySize = (assemblySize: string) => ({
	type: types.SET_ASSEMBLY_SIZE,
	assemblySize
});

// * setThemeConfig
export const setThemeConfig = (themeConfig: ThemeConfigProp) => ({
	type: types.SET_THEME_CONFIG,
	themeConfig
});
