/* eslint-disable prettier/prettier */
/* eslint-disable react/prop-types */
// 这是一个上传图片的组件，使用的是antd的Upload组件

import { FC, useEffect, useState } from "react";
import React from "react";
import { Avatar, Button, Divider, Input, Select } from "antd";

import { getAuthorListApi } from "@/api/modules/column";
import { MapItem } from "@/typings/common";

import "./index.scss";

interface IProps {
	handleChange: (e: any) => void;
	handleFormRefChange: (e: any) => void;
	authorName:string;
}

const AuthorSelect: FC<IProps> = ({ 
	handleChange,
	handleFormRefChange,
	authorName
}) => {
	// 作者列表的查询条件
	const [authorSearchKey, setAuthorSearchKey] = useState<string>("");
	// 触发作者列表的搜索，查询条件为作者名
	const [authorSearch, setAuthorSearch] = useState<string>("");
	// authorList，从后台返回作者列表
	const [authorList, setAuthorList] = useState<MapItem[]>([]);

	// 作者列表查询条件变化
	const handleAuthorSearchChange = (value: string) => {
		console.log("作者列表查询条件变化", value);
		setAuthorSearchKey(value);
	};
	
	// 获取作者列表
	useEffect(() => {
		const getAuthorList = async () => {
			const { status, result } = await getAuthorListApi(authorSearchKey);
			const { code } = status || {};
			//@ts-ignore
			const { items } = result || {};
			if (code === 0) {
				const newList = items.map((item: MapItem) => ({
					value: item.name,
					label: (
						<React.Fragment>
							<Avatar src={item.avatar} alt="avatar" className="avatar" />
							{item.name}
						</React.Fragment>
					),
					key: item.userId,
				}));
							
				setAuthorList(newList);
			}
		};
		getAuthorList();
	}, [authorSearch]);

	return (
		<Select
			// 使用 select 选择器实现一个可以查找的作者列表供选择
			// value 会从 item 中的 name 中取出来，所以这里不需要设置
			placeholder="请选择作者"
			size="large"
			value={authorName}
			options={authorList}
			onChange={(value, option) => {
				// 需要把作者 ID 传递给后端
				console.log("选择的作者", option);
				console.log("选择的作者 value", value);
				// 取出 key
				//@ts-ignore
				const { key } = option || {};
				handleChange({ author: key, authorName: value });
				handleFormRefChange({author: key});
			}}
			// render
			dropdownRender={menu => {
				return (
					<div>
						<div
							style={{
								display: "flex",
								flexWrap: "nowrap",
								padding: 8
							}}
						>
							<Input
								allowClear
								placeholder="请输入你想要查找的作者名"
								style={{ flex: "auto" }}
								onChange={e => {
									handleAuthorSearchChange(e.target.value);
								}}
							/>
							<Button
								type="primary"
								style={{ marginLeft: 8 }}
								// 触发搜索authorSearch
								onClick={() => {
									setAuthorSearch(authorSearchKey);
								}}
							>
								筛选
							</Button>
						</div>
						{/* 添加一个分割线 */}
						<Divider style={{ margin: "4px 0" }} />
						{menu}
					</div>
				);
			}}
			// 下拉框展开时触发
			onDropdownVisibleChange={() => {
				console.log("下拉框展开");
			}}
		>
		</Select>
	);
};
export default AuthorSelect;
