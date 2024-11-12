/* eslint-disable react/jsx-no-comment-textnodes */
/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import { connect } from "react-redux";
import { DeleteOutlined, EyeOutlined } from "@ant-design/icons";
import { Button, Descriptions, Drawer, Form, Image,Input, message, Modal, Space, Table } from "antd";
import type { ColumnsType } from "antd/es/table";

import { delColumnArticleApi, getColumnArticleListApi, getColumnByNameListApi, updateColumnArticleApi } from "@/api/modules/column";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import { baseDomain } from "@/utils/util";
import { getCompleteUrl } from "@/utils/util";
import DebounceSelect from "./components/debounceselect/DebounceSelect";
import Search from "./components/search";
import TableSelect from "./components/tableselect/TableSelect";

import "./index.scss";

interface IProps {}

// 教程文章的数据类型
interface DataType {
	id: number;
	articleId: string;
	title: string;
	shortTitle: string;
	columnId: number;
	column: string;
	sort: number;
}

// 查询表单接口，定义类型
interface ISearchForm {
	articleTitle: string;
	columnId: number;
}

export interface IFormType {
	id: number; // 主键id
	articleId: number; // 文章ID
	title: string; // 文章标题
	shortTitle: string; // 文章短标题
	columnId: number; // 教程ID
	column: string; // 教程名
	sort: number; // 排序
}

const defaultInitForm: IFormType = {
	id: -1,
	articleId: -1,
	title: "",
	shortTitle: "",
	columnId: -1,
	column: "",
	sort: -1
};

// 查询表单默认值
const defaultSearchForm = {
	articleTitle: "",
	columnId: -1,
};

// Usage of DebounceSelect
interface ColumnValue {
	key: string;
	label: string;
	value: string;
}

const ColumnArticle: FC<IProps> = props => {

	const [formRef] = Form.useForm();
	// form值（详情和新增的时候会用到）
	const [form, setForm] = useState<IFormType>(defaultInitForm);

	// 查询表单
	const [searchForm, setSearchForm] = useState<ISearchForm>(defaultSearchForm);

	// 修改添加抽屉
	const [isOpenDrawerShow, setIsOpenDrawerShow] = useState<boolean>(false);
	// 详情抽屉
	const [isDetailDrawerShow, setIsDetailDrawerShow] = useState<boolean>(false);
	// 文章选择下拉框是否打开
	const [isArticleSelectOpen, setIsArticleSelectOpen] = useState<boolean>(false);

	// 列表数据
	const [tableData, setTableData] = useState<DataType[]>([]);
	// 刷新函数
	const [query, setQuery] = useState<number>(0);

	//当前的状态
	const [status, setStatus] = useState<UpdateEnum>(UpdateEnum.Save);

	// 分页
	const [pagination, setPagination] = useState<IPagination>(initPagination);
	const { current, pageSize } = pagination;

	// 详情信息
	const { id, articleId, title, shortTitle, columnId, column, sort } = form;

	const detailInfo = [
		{ label: "专栏ID", title: columnId },
		{ label: "专栏名", title: column },
		{ label: "文章ID", title: articleId },
		{ label: "文章标题", title: title },
		{ label: "教程ID", title: id },
		{ label: "教程标题", title: shortTitle },
		{ label: "排序", title: sort }
	];

	const paginationInfo = {
		showSizeChanger: true,
		showTotal: (total: any) => `共 ${total || 0} 条`,
		...pagination,
		onChange: (current: number, pageSize: number) => {
			setPagination({ current, pageSize });
		}
	};

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	// 值改变（新增教程文章时）
	const handleChange = (item: MapItem) => {
		setForm({ ...form, ...item });
		formRef.setFieldsValue({ ...item });
	};

	// 查询表单值改变
	const handleSearchChange = (item: MapItem) => {
		// 当 status 的值为 -1 时，重新显示
		setSearchForm({ ...searchForm, ...item });
		console.log("查询条件变化了",searchForm);
	};

	// 当点击查询按钮的时候触发
	const handleSearch = () => {
		// 目前是根据文章标题搜索，后面需要加上其他条件
		console.log("查询条件", searchForm);
		setPagination({ current: 1, pageSize });
		// 直接触发刷新
		onSure();
	};

	// 点击添加的时候触发
	const handleAdd = () => {
		setStatus(UpdateEnum.Save);
		formRef.resetFields();
		setIsOpenDrawerShow(true);
	};

	// 关闭抽屉时触发
	const handleCloseDrawer = () => {
		// 关闭教程的下拉框
		setIsArticleSelectOpen(false);
		// 关闭抽屉
		setIsOpenDrawerShow(false);
	};

	// 关闭详情抽屉时触发
	const handleCloseDetailDrawer = () => {
		setIsDetailDrawerShow(false);
	};
		
	// 删除
	const handleDel = (id: number) => {
		Modal.warning({
			title: "确认删除此专栏的教程吗",
			content: "删除此专栏的教程后无法恢复，请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delColumnArticleApi(id);
				const { code, msg } = status || {};
				if (code === 0) {
					message.success("删除成功");
					onSure();
				} else {
					message.error(msg);
				}
			}
		});
	};

	// 添加教程文章，编辑取消了
	const handleSubmit = async () => {
		const values = await formRef.validateFields();
		const newValues = {
			...values,
			columnId: columnId, 
		};
		console.log("提交的值:", newValues);
		
		const { status: successStatus } = (await updateColumnArticleApi(newValues)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			setIsOpenDrawerShow(false);
			// 重置分页
			setPagination({ current: 1, pageSize });
			onSure();
		} else {
			message.error(msg);
		}
	};

	// 数据请求
	useEffect(() => {
		const getSortList = async () => {
			const newValues = {
				...searchForm,
				pageNumber: current, 
				pageSize,
			};
			console.log("查询教程列表之前的所有值:", newValues);
			
			const { status, result } = await getColumnArticleListApi(newValues);
			const { code } = status || {};
			// @ts-ignore
			const { list, pageNum, pageSize: resPageSize, pageTotal, total } = result || {};
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item?.categoryId }));
				setTableData(newList);
			}
		};
		getSortList();
	}, [query, current, pageSize]);

	// 教程下拉框，可根据教程查询
	async function fetchColumnList(key: string): Promise<ColumnValue[]> {
		console.log('根据教程名查询', key);
		const { status, result } = await getColumnByNameListApi(key);
		const { code } = status || {};
		//@ts-ignore
		const { items } = result || {};
		if (code === 0) {
			const newList = items.map((item: MapItem) => ({
				key: item?.columnId,
				// label 这里我想把教程封面也加上
				label: <div>
					<Image
						className="cover-select"
						src={getCompleteUrl(item?.cover)}
					/>
					<span>{item?.column}</span>
				</div>,
				value: item?.column
			}));
			console.log("教程列表", newList);
			return newList;
		}
		// 没查到数据时，返回空数组
		return [];
	};

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "专栏名称",
			dataIndex: "column",
			key: "column",
			render(value, item) {
				return (
					<a 
						href={`${baseDomain}/column/${item?.columnId}/1`}
						className="cell-text"
						target="_blank" rel="noreferrer">
						{value}
					</a>
				);
			}
		},
		{
			title: "教程标题",
			dataIndex: "shortTitle",
			key: "shortTitle",
			render(value, item) {
				return (
					<a 
						href={`${baseDomain}/column/${item?.columnId}/${item?.sort}`}
						className="cell-text"
						target="_blank" rel="noreferrer">
						{value}
					</a>
				);
			}
		},
		{
			title: "排序",
			dataIndex: "sort",
			key: "sort"
		},
		{
			title: "操作",
			key: "key",
			width: 210,
			render: (_, item) => {
				// 删除的时候用
				const { id } = item;
				return (
					<div className="operation-btn">
						<Button
							type="primary"
							icon={<EyeOutlined />}
							style={{ marginRight: "10px" }}
							onClick={() => {
								setIsDetailDrawerShow(true);
								// 把所有的值传给 form 表单
								handleChange({ ...item });
							}}
						>
							详情
						</Button>
						<Button 
							type="primary" 
							danger 
							icon={<DeleteOutlined />} 
							onClick={() => handleDel(id)}>
							删除
						</Button>
					</div>
				);
			}
		}
	];

	// 编辑表单
	const reviseDrawerContent = (
		<Form 
			name="basic" 
			form={formRef} 
			labelCol={{ span: 4 }} 
			wrapperCol={{ span: 16 }} 
			autoComplete="off">
			<Form.Item 
				label="专栏" 
				name="columnName" 
				rules={[{ required: true, message: "请选择专栏!" }]}>
				{/*用下拉框做一个教程的选择 */}
				<DebounceSelect
					allowClear
					filterOption={false}
					placeholder="选择专栏"
					// optionLabelProp：回填到选择框的 Option 的属性值，默认是 Option 的子元素。
					// 比如在子元素需要高亮效果时，此值可以设为 value
					optionLabelProp="value"
					// 是否在输入框聚焦时自动调用搜索方法
					showSearch={true}
					onChange={
						(value, option) => {
							console.log("添加教程文章时教程搜索的值改变", value, option)
						if(option)
							//@ts-ignore
							handleChange({ columnId: option.key, columnName: option.value})
						else
							handleChange({ columnId: -1 })
						}
					}
					fetchOptions={fetchColumnList}
				/>
			</Form.Item>

			<Form.Item 
				label="教程"
				name="articleId"
				rules={[{ required: true, message: "请选择教程!" }]}
				>
				<TableSelect 
					isArticleSelectOpen={isArticleSelectOpen}
					setIsArticleSelectOpen={setIsArticleSelectOpen}
					handleChange={handleChange}
					/>
			</Form.Item>

			<Form.Item 
				label="标题"
				name="shortTitle"
				rules={[{ required: true, message: "请输入标题!" }]}
				>
				<Input
					allowClear
					placeholder="请输入标题"
					value={shortTitle}
					onChange={e => handleChange({ shortTitle: e.target.value })}
				/>
			</Form.Item>
			
		</Form>
	);

	return (
		<div className="ColumnArticle">
			<ContentWrap>
				{/* 搜索 */}
				<Search
					handleSearchChange={handleSearchChange}
					fetchColumnList={fetchColumnList}
					handleSearch={handleSearch} 
					handleAdd={handleAdd}
					/>
				{/* 表格 */}
				<ContentInterWrap>
					<Table columns={columns} dataSource={tableData} pagination={paginationInfo} />
				</ContentInterWrap>
			</ContentWrap>
			{/* 抽屉 */}
			<Drawer 
				title="详情" 
				placement="right" 
				onClose={handleCloseDetailDrawer} 
				open={isDetailDrawerShow}>
				<Descriptions column={1} labelStyle={{ width: "100px" }}>
					{detailInfo.map(({ label, title }) => (
						<Descriptions.Item label={label} key={label}>
							{title !== 0 ? title || "-" : 0}
						</Descriptions.Item>
					))}
				</Descriptions>
			</Drawer>
			{/* 把弹窗修改为抽屉 */}
			<Drawer 
				title="添加" 
				size="large"
				placement="right"
				extra={
          <Space>
            <Button onClick={handleCloseDrawer}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              OK
            </Button>
          </Space>
        }
				onClose={handleCloseDrawer} 
				open={isOpenDrawerShow}>
				{reviseDrawerContent}
			</Drawer>
		</div>
	);
};

const mapStateToProps = (state: any) => state.disc.disc;
const mapDispatchToProps = {};
export default connect(mapStateToProps, mapDispatchToProps)(ColumnArticle);

