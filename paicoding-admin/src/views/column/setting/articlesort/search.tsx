/* eslint-disable prettier/prettier */
import { FC } from "react";
import { ArrowLeftOutlined, PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input } from "antd";

import { ContentInterWrap } from "@/components/common-wrap";

import "./search.scss";

interface IProps {
	handleSearchChange: (e: object) => void;
	handleSearch: () => void;
	goBack: () => void;
	handleAdd: () => void;
}

const Search: FC<IProps> = ({ 
	handleSearchChange, 
	handleSearch,
	goBack,
	handleAdd
}) => {
	return (
		<div className="column-article-sort-search">
			<ContentInterWrap className="column-article-sort-search__wrap">
				<div className="column-article-sort-search__search">
					<div className="column-article-sort-search__search-item">
						<Button onClick={goBack}><ArrowLeftOutlined />返回专栏配置</Button>
					</div>
					<div className="column-article-sort-search__search-item">
						<span className="column-article-sort-search-label">教程标题</span>
						<Input 
							allowClear
							onChange={e => handleSearchChange({ articleTitle: e.target.value })} 
							style={{ width: 202 }} 
							/>
					</div>
				</div>
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
					style={{ marginRight: "16px" }}
					onClick={handleAdd}
				>
					添加
				</Button>
			</ContentInterWrap>
		</div>
	);
};
export default Search;
