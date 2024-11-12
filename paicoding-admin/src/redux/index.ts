/* eslint-disable simple-import-sort/imports */
/* eslint-disable prettier/prettier */
import { Action, combineReducers, compose, legacy_createStore as createStore, Store } from "redux";
import { ThunkAction, ThunkDispatch } from 'redux-thunk';
import { applyMiddleware } from "redux";
import { persistReducer, persistStore } from "redux-persist";
import storage from "redux-persist/lib/storage";
import reduxPromise from "redux-promise";
import reduxThunk from "redux-thunk";

import auth from "./modules/auth/reducer";
import breadcrumb from "./modules/breadcrumb/reducer";
import disc from "./modules/disc/reducer";
import global from "./modules/global/reducer";
import menu from "./modules/menu/reducer";
import tabs from "./modules/tabs/reducer";

// 创建reducer(拆分reducer)
const reducer = combineReducers({
	global,
	menu,
	tabs,
	auth,
	breadcrumb,
	disc
});

export type RootState = ReturnType<typeof reducer>;
// 定义自定义的 thunk action 类型
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;

// 定义自定义的 dispatch 类型
export type AppDispatch = ThunkDispatch<RootState, unknown, Action<string>>;

// redux 持久化配置
const persistConfig = {
	key: "redux-state",
	storage: storage
};
const persistReducerConfig = persistReducer(persistConfig, reducer);

// 开启 redux-devtools
const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

// 使用 redux 中间件
const middleWares = applyMiddleware(reduxThunk, reduxPromise);

// 创建 store
const store: Store = createStore(persistReducerConfig, composeEnhancers(middleWares));

// 创建持久化 store
const persistor = persistStore(store);

export { persistor, store };
