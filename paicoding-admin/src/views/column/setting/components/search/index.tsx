/* eslint-disable prettier/prettier */
import React, { FC } from "react";
import { PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input } from "antd";

import { ContentInterWrap } from "@/components/common-wrap";
import { UpdateEnum } from "@/enums/common";

import "./index.scss";

interface IProps {
	handleSearchChange: (e: object) => void;
	handleSearch: () => void;
	setStatus: (e: UpdateEnum) => void;
	setIsOpenDrawerShow: (e: boolean) => void;
}

const Search: FC<IProps> = ({ 
	handleSearchChange, 
	handleSearch,
	setStatus, 
	setIsOpenDrawerShow 
}) => {
	return (
		<div className="column-setting-search">
			<ContentInterWrap className="column-setting-search__wrap">
				<div className="column-setting-search__search">
					<div className="column-setting-search__search-item">
						<span className="column-setting-search-label">专栏名</span>
						<Input 
							allowClear 
							onChange={e => handleSearchChange({ column: e.target.value })} 
							style={{ width: 252 }} />
					</div>
				</div>
				<Button
					type="primary"
					icon={<SearchOutlined />}
					style={{ marginRight: "10px" }}
					onClick={e => {
						handleSearch();
					}}
				>
					搜索
				</Button>
				<Button
					type="primary"
					icon={<PlusOutlined />}
					style={{ marginRight: "20px" }}
					onClick={() => {
						setIsOpenDrawerShow(true);
						setStatus(UpdateEnum.Save);
					}}
				>
					添加
				</Button>
			</ContentInterWrap>
		</div>
	);
};
export default Search;
