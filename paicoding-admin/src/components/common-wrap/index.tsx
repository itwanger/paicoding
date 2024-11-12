import React, { ReactNode } from "react";

import "./index.scss";

interface IProps {
	children: ReactNode;
	className?: string;
	style?: any;
}

export const ContentWrap = ({ children, className, style }: IProps) => (
	<div className={`content-wrap ${className}`} style={style}>
		{children}
	</div>
);

export const ContentInterWrap = ({ children, className, style }: IProps) => (
	<div className={`content-inter-wrap ${className}`} style={style}>
		{children}
	</div>
);
