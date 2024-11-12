/* eslint-disable prettier/prettier */
import { FC } from "react";
import { PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input } from "antd";

import { ContentInterWrap } from "@/components/common-wrap";

import "./index.scss";

interface IProps {
	handleSearch: (e: object) => void;
	handleSearchChange: (e: object) => void;
	handleAdd: () => void;
}

const Search: FC<IProps> = ({ handleSearch, handleSearchChange, handleAdd }) => {
	return (
		<div className="global-config-search">
			<ContentInterWrap className="global-config-search__wrap">
				<div className="global-config-search__search">
					<div className="global-config-search__search-wrap">
						<div className="global-config-search__search-item">
							<Input
								allowClear
								style={{ width: 152 }}
								placeholder="请输入配置项名称"
								onChange={e => handleSearchChange({ keywords: e.target.value })}
							/>
						</div>
						<div className="global-config-search__search-item">
							<Input
								allowClear
								style={{ width: 252 }}
								placeholder="请输入配置项值"
								onChange={e => handleSearchChange({ value: e.target.value })}
							/>
						</div>
						<div className="global-config-search__search-item">
							<Input
								allowClear
								style={{ width: 152 }}
								placeholder="请输入备注"
								onChange={e => handleSearchChange({ comment: e.target.value })}
							/>
						</div>
					</div>
					<div className="tag-search__search-btn">
						<Button type="primary" icon={<SearchOutlined />} style={{ marginRight: "10px" }} onClick={handleSearch}>
							搜索
						</Button>
						<Button type="primary" icon={<PlusOutlined />} style={{ marginRight: "20px" }} onClick={handleAdd}>
							添加
						</Button>
					</div>
				</div>
			</ContentInterWrap>
		</div>
	);
};
export default Search;
