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

const Search: FC<IProps> = ({ 
	handleSearch, 
	handleSearchChange, 
	handleAdd 
}) => {
	return (
		<div className="tag-search">
			<ContentInterWrap className="tag-search__wrap">
				<div className="tag-search__search">
					<div className="tag-search__search-wrap">
						<div className="tag-search__search-item">
							<label className="tag-search-label">名称</label>
							<Input
								allowClear
								style={{ width: 252 }}
								placeholder="请输入标签名称"
								onChange={e => handleSearchChange({ tag: e.target.value })}
							/>
						</div>
					</div>
					<div className="tag-search__search-btn">
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
