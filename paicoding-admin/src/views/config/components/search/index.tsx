/* eslint-disable prettier/prettier */
import { FC } from "react";
import { PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input, Select } from "antd";

import { ContentInterWrap } from "@/components/common-wrap";

import "./index.scss";

interface IProps {
	ConfigTypeList: any;
	handleSearch: (e: object) => void;
	handleSearchChange: (e: object) => void;
	handleAdd: () => void;
}

const Search: FC<IProps> = ({ 
	ConfigTypeList,
	handleSearch,
	handleSearchChange,
	handleAdd,
}) => {
	return (
		<div className="config-search">
			<ContentInterWrap className="config-search__wrap">
				<div className="config-search__search ">
					<div className="config-search__search-wrap">
						<div className="config-search__search-item">
							<label className="config-search-label">类型</label>
							<Select
								allowClear
								style={{ width: 152 }}
								onChange={value => {
									console.log("查询类型",value);
									handleSearchChange({ type: Number(value) });
								}}
								placeholder="请选择类型"
								options={ConfigTypeList}
							/>
						</div>
						<div className="config-search__search-item">
							<label className="config-search-label">名称</label>
							<Input 
								allowClear
								style={{ width: 252 }}
								placeholder="请输入配置名称"
								onChange={e => handleSearchChange({ name: e.target.value })} 
								/>
						</div>
					</div>
					<div className="config-search__search-btn">
						<Button 
							type="primary" 
							icon={<SearchOutlined />}
							style={{ marginRight: "10px" }}
							onClick={handleSearch}
							>
							搜索
						</Button>

						<Button
							type="primary"
							icon={<PlusOutlined />}
							style={{ marginRight: "20px" }}
							onClick={handleAdd}
						>
							添加
						</Button>
					</div>
				</div>
			</ContentInterWrap>
		</div>
	);
};
export default Search;
