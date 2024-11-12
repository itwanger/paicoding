import { useEffect } from "react";
import { connect } from "react-redux";
import { Layout } from "antd";

import { loginUserInfo } from "@/api/modules/login";
import { getDiscListAction } from "@/redux/modules/disc/action";
import { setToken, setUserInfo } from "@/redux/modules/global/action";
import { setTabsList } from "@/redux/modules/tabs/action";
import AssemblySize from "./components/AssemblySize";
import AvatarIcon from "./components/AvatarIcon";
import BreadcrumbNav from "./components/BreadcrumbNav";
import CollapseIcon from "./components/CollapseIcon";
import Fullscreen from "./components/Fullscreen";
import Theme from "./components/Theme";

import "./index.less";

const LayoutHeader = (props: any) => {
	const { setToken, setUserInfo, setTabsList, getDiscListAction } = props;

	// 尝试从 props 中获取用户信息，如果没有登录行为，则从后端直接获取
	let { userInfo } = props || {};
	console.log("LayoutHeader userInfo", userInfo);

	const { Header } = Layout;

	useEffect(() => {
		let toCheck = !userInfo || JSON.stringify(userInfo) === "{}";
		console.log("toCheck", toCheck);

		if (toCheck) {
			const fetchUsrInfo = async () => {
				try {
					const { status, result } = await loginUserInfo();
					console.log("请求用户信息: ", result);

					if (status && status.code == 0 && result && result.userId > 0) {
						// 保存 token 到 Redux 的状态中
						setToken(String(result.userId));
						// 保存用户登录信息
						setUserInfo(result);
						setTabsList([]);
						// 获取字典数据
						getDiscListAction();
					}
				} catch (e) {
					console.log("初始化用户身份异常!", e);
				}
			};
			// 未拿到用户信息时，主动去拿一下
			fetchUsrInfo();
		}
	}, []);

	return (
		<Header>
			<div className="header-lf">
				<CollapseIcon />
				<BreadcrumbNav />
			</div>
			<div className="header-ri">
				<AssemblySize />
				<Theme />
				<Fullscreen />
				<span className="username">{userInfo.userName || "技术派"}</span>
				<AvatarIcon userInfo={userInfo} />
			</div>
		</Header>
	);
};

const mapStateToProps = (state: any) => state.global;
const mapDispatchToProps = { setToken, setUserInfo, getDiscListAction, setTabsList };
export default connect(mapStateToProps, mapDispatchToProps)(LayoutHeader);
