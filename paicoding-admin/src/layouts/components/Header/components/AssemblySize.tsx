import { connect } from "react-redux";
import { Dropdown, Menu } from "antd";

import { setAssemblySize } from "@/redux/modules/global/action";

const AssemblySize = (props: any) => {
	const { assemblySize, setAssemblySize } = props;

	// 切换组件大小
	const onClick = (e: MenuInfo) => {
		setAssemblySize(e.key);
	};

	const menu = (
		<Menu
			items={[
				{
					key: "middle",
					disabled: assemblySize == "middle",
					label: <span>默认</span>,
					onClick
				},
				{
					disabled: assemblySize == "large",
					key: "large",
					label: <span>大型</span>,
					onClick
				},
				{
					disabled: assemblySize == "small",
					key: "small",
					label: <span>小型</span>,
					onClick
				}
			]}
		/>
	);
	return (
		<Dropdown overlay={menu} placement="bottom" trigger={["click"]} arrow={true}>
			<i className="icon-style iconfont icon-contentright"></i>
		</Dropdown>
	);
};

const mapStateToProps = (state: any) => state.global;
const mapDispatchToProps = { setAssemblySize };
export default connect(mapStateToProps, mapDispatchToProps)(AssemblySize);
