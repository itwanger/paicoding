/* eslint-disable prettier/prettier */
import { FC } from "react";
import { PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input } from "antd";

import { ContentInterWrap } from "@/components/common-wrap";
import { UpdateEnum } from "@/enums/common";
import DebounceSelect from "../debounceselect/DebounceSelect";

import "./index.scss";

interface IProps {
	handleSearchChange: (e: object) => void;
	handleSearch: () => void;
	fetchColumnList: (search: string) => Promise<any[]>;
	handleAdd: () => void;
}

const Search: FC<IProps> = ({ 
	handleSearchChange, 
	handleSearch,
	fetchColumnList,
	handleAdd
}) => {
	return (
		<div className="column-article-search">
			<ContentInterWrap className="column-article-search__wrap">
				<div className="column-article-search__search">
					<div className="column-article-search__search-item">
						<span className="column-article-search-label">专栏</span>
						{/*用下拉框做一个教程的选择 */}
						<DebounceSelect
							allowClear
							style={{ width: 262 }}
							filterOption={false}
							placeholder="选择专栏"
							// 回填到选择框的 Option 的属性值，默认是 Option 的子元素。
							// 比如在子元素需要高亮效果时，此值可以设为 value
							optionLabelProp="value"
							// 是否在输入框聚焦时自动调用搜索方法
							showSearch={true}
							onChange={(value, option) => {
								console.log("教程搜索的值改变", value, option);
								if (option) 
									//@ts-ignore
									handleSearchChange({ columnId: option.key });
								else 
									handleSearchChange({ columnId: -1 });
							}}
							fetchOptions={fetchColumnList}
						/>
					</div>
					<div className="column-article-search__search-item">
						<span className="column-article-search-label">教程标题</span>
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
