/* eslint-disable prettier/prettier */
import React, { FC } from "react";
import { SearchOutlined } from "@ant-design/icons";
import { Button, Input, Radio, RadioChangeEvent, Select } from "antd";

import { ContentInterWrap } from "@/components/common-wrap";

import "./index.scss";

interface IProps {
	handleSearchChange: (e: object) => void;
	handleSearch: () => void;
	radioValue: number;
	handleBatchStatusChange: (e: RadioChangeEvent) => void;
	UserAIStatList: Array<{ label: string; value: number }>;
}

const optionsRadioGroup = [
	{ label: "通过", value: 2 },
	{ label: "拒绝", value: 3 }
];

const Search: FC<IProps> = ({ handleSearchChange, handleSearch, radioValue, handleBatchStatusChange, UserAIStatList }) => {
	return (
		<div className="zsxq-white-list-search">
			{/* 搜索 */}
			<ContentInterWrap className="zsxq-white-list-search__wrap">
				<div className="zsxq-white-list-search__search">
					<div className="zsxq-white-list-search__search-item">
						{/* 增加一个作者的查询条件 */}
						<Input
							allowClear
							placeholder="请输入星球编号"
							style={{ width: 142 }}
							onChange={e => {
								handleSearchChange({ starNumber: e.target.value });
							}}
						/>
					</div>
					<div className="zsxq-white-list-search__search-item">
						<Input
							allowClear
							placeholder="请输入登录用户名"
							style={{ width: 152 }}
							onChange={e => handleSearchChange({ userCode: e.target.value })}
						/>
					</div>
					<div className="zsxq-white-list-search__search-item">
						<Input
							allowClear
							placeholder="请输入用户昵称"
							style={{ width: 152 }}
							onChange={e => handleSearchChange({ name: e.target.value })}
						/>
					</div>
					<div className="zsxq-white-list-search__search-item">
						<Select
							// 可以清空
							allowClear
							// 默认值
							placeholder="选择状态"
							options={UserAIStatList}
							style={{ width: 100 }}
							// 触发搜索
							onChange={value => handleSearchChange({ state: Number(value || -1) })}
						></Select>
					</div>
					<div className="zsxq-white-list-search__search-btn">
						<Button type="primary" icon={<SearchOutlined />} style={{ marginRight: "25px" }} onClick={handleSearch}>
							搜索
						</Button>
						<Radio.Group
							value={radioValue}
							onChange={handleBatchStatusChange}
							options={optionsRadioGroup}
							optionType="button"
							buttonStyle="solid"
						/>
					</div>
				</div>
			</ContentInterWrap>
		</div>
	);
};
export default Search;
