import loginLeft from "@/assets/images/login_left.png";
import logo from "@/assets/images/logo.svg";
import SwitchDark from "@/components/SwitchDark";
import LoginForm from "./components/LoginForm";

import "./index.less";

const Login = () => {
	return (
		<div className="login-container">
			<SwitchDark />
			<div className="login-box">
				<div className="login-left">
					<img src={loginLeft} alt="login" />
				</div>
				<div className="login-form">
					<div className="login-logo">
						<img className="login-icon" src={logo} alt="logo" />
					</div>
					<LoginForm />
				</div>
			</div>
		</div>
	);
};

export default Login;
