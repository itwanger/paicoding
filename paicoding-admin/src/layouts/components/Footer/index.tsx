import { connect } from "react-redux";

import { baseDomain } from "@/utils/util";

import "./index.less";

const LayoutFooter = (props: any) => {
	const { themeConfig } = props;

	// 定义一个自动获取年份的方法
	const getYear = () => {
		return new Date().getFullYear();
	};

	return (
		<>
			{!themeConfig.footer && (
				<div className="footer">
					<a href={baseDomain} target="_blank" rel="noreferrer">
						{getYear()} © paicoding-admin By 技术派团队.
					</a>
				</div>
			)}
		</>
	);
};

const mapStateToProps = (state: any) => state.global;
export default connect(mapStateToProps)(LayoutFooter);
