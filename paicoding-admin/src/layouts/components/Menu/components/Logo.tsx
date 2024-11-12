import { connect } from "react-redux";

import logo from "@/assets/images/logo.svg";
import logoMd from "@/assets/images/logo_md.png";
const Logo = (props: any) => {
	const { isCollapse } = props;
	return (
		<div className="logo-box">
			<img src={!isCollapse ? logo : logoMd} alt="logo" className={!isCollapse ? "logo-img" : "logo-img-md"} />
		</div>
	);
};

const mapStateToProps = (state: any) => state.menu;
export default connect(mapStateToProps)(Logo);
