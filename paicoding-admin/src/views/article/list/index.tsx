/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import { connect } from "react-redux";
import { useNavigate } from "react-router";
import { DeleteOutlined, EditOutlined, HighlightOutlined } from "@ant-design/icons";
import { Avatar, Button, Form, Input, message, Modal, Select, Switch, Table, Tooltip } from "antd";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";

import { delArticleApi, getArticleListApi, operateArticleApi, updateArticleApi } from "@/api/modules/article";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import { baseDomain } from "@/utils/util";
import Search from "../components/search";

import "./index.scss";

interface DataType {
	articleId: number;
	author: number;
	authorName: string;
	authorAvatar: string;
	title: string;
	cover: string;
	status: number;
	officalStat: number;
	toppingStat: number;
	creamStat: number;
}

interface IProps {}

// 编辑表单接口，定义类型
interface IInitForm {
	articleId: number;
	title: string;
	shortTitle: string;
	status: number;
}

// 查询表单接口，定义类型
interface ISearchForm {
	userName: string;
	title: string;
	status: number;
	toppingStat: number;
	officalStat: number;
}

// 编辑表单默认值
const defaultInitForm = {
	articleId: -1,
	title: "",
	shortTitle: "",
	status: -1
};

// 查询表单默认值
const defaultSearchForm = {
	userName: "",
	title: "",
	status: -1,
	toppingStat : -1,
	officalStat : -1
};

const Article: FC<IProps> = props => {
	const [formRef] = Form.useForm();
	// 编辑表单
	const [form, setForm] = useState<IInitForm>(defaultInitForm);
	// 查询表单
	const [searchForm, setSearchForm] = useState<ISearchForm>(defaultSearchForm);
	// 弹窗
	const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
	// 列表数据
	const [tableData, setTableData] = useState<DataType[]>([]);
	// 刷新函数
	const [query, setQuery] = useState<number>(0);

	// 分页
	const [pagination, setPagination] = useState<IPagination>(initPagination);
	const { current, pageSize } = pagination;

	const paginationInfo = {
		showSizeChanger: true,
		showTotal: (total: number) => `共 ${total || 0} 条`,
		...pagination,
		onChange: (current: number, pageSize: number) => {
			setPagination({ current, pageSize });
		}
	};

	// 一些配置项
	//@ts-ignore
	const { PushStatusList, ToppingStatusList, OfficalStatusList} = props || {};

	const { articleId } = form;

	const navigate = useNavigate();

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);
	
	// 编辑表单值改变
	const handleChange = (item: MapItem) => {
		setForm({ ...form, ...item });
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
		// 查询的时候重置分页
		setPagination({ current: 1, pageSize });
		// 重新请求数据
		onSure();
	};

	// 删除
	const handleDel = (articleId: number) => {
		Modal.warning({
			title: "确认删除此文章吗",
			content: "删除此文章后无法恢复，请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delArticleApi(articleId);
				const { code, msg } = status || {};
				if (code === 0) {
					message.success("删除成功");
					onSure();
				} else {
					message.error(msg || "删除失败");
				}
			}
		});
	};

	// 置顶和官方
	const handleOperate = async (articleId: number, operateType: number) => {
		let operateDesc = "";
		if (operateType === 4) {
			operateDesc = "取消置顶";
		} else if (operateType === 3) {
			operateDesc = "置顶";
		} else if (operateType === 2) {
			operateDesc = "取消推荐";
		} else if (operateType === 1) {
			operateDesc = "推荐";
		}
		
		const { status } = await operateArticleApi({ articleId, operateType });
		const { code, msg } = status || {};
		if (code === 0) {
			message.success(operateDesc + "操作成功");
			onSure();
		} else {
			message.error(msg || operateDesc + "操作失败");
		}
	};

	// 改变文章状态的操作
	const handleStatusChange = async (articleId: number, status: number) => {
		// 将 articleId 和 status 作为参数传递给 updateArticleApi
		const newValues = { articleId, status };
		const { status: successStatus } = await updateArticleApi(newValues) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			message.success("状态操作成功");
			console.log("code", code);
			onSure();
		} else {
			message.error(msg || "状态操作失败");
		}
	};

	// 导航到文章编辑页面
	const handleEdit = (articleId: number) => {
		console.log("articleId", articleId);
		navigate("/article/edit/index", { state: { 
			articleId,
			status: UpdateEnum.Edit
		}});
	};

	const handleSubmit = async () => {
		const values = await formRef.validateFields();
		// 从 form 中取出 status
		const { status } = form;
		const newValues = { ...values, articleId, status };
		console.log("编辑 时提交的 newValues:", newValues);
		
		const { status: successStatus } = (await updateArticleApi(newValues)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			message.success("编辑成功");
			setIsModalOpen(false);
			onSure();
		} else {
			message.error(msg || "编辑失败");
		}
	};

	// 数据请求，这是一个钩子，query, current, pageSize, search 有变化的时候就会自动触发
	useEffect(() => {
		const getSortList = async () => {
			const { status, result } = await getArticleListApi({ 
				pageNumber: current, 
				pageSize,
				...searchForm
			});
			const { code } = status || {};
			//@ts-ignore
			const { list, pageNum, pageSize: resPageSize, total } = result || {};
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item?.articleId }));
				setTableData(newList);
			}
		};
		getSortList();
	}, [query, current, pageSize]);

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "标题",
			dataIndex: "title",
			key: "title",
			render(value, item) {
				return (
					<a 
						href={`${baseDomain}/article/detail/${item?.articleId}`}
						className="cell-text"
						target="_blank" rel="noreferrer">
						{value}
					</a>
				);
			}
		},
		{
			title: "修改时间",
			dataIndex: "updateTime",
			key: "updateTime",
			width: 120,
			render: (value: string) => {
				const time = dayjs(value);
				return <Tooltip title={time.format('YYYY-MM-DD HH:mm:ss')}><span>{time.format('MM-DD HH:mm')}</span></Tooltip>;
			}
		},	
		{
			title: "作者",
			dataIndex: "authorName",
			width: 110,
			key: "authorName",
			render(value) {
				return <>
					<Avatar style={{ backgroundColor: '#1890ff', color: '#fff' }} size="large">
						{value.slice(0, 3)}
					</Avatar>
				</>;
			}
		},
		{
			title: "置顶",
			dataIndex: "toppingStat",
			key: "toppingStat",
			render(_, item) {
				const { articleId, toppingStat } = item;
				// 返回的是 0 和 1
				const isTopped = toppingStat === 1;
		
				const topStatus = isTopped ? 4 : 3; // 3-置顶；4-取消置顶
				return <Switch 
					checked={isTopped} 
					onChange={() => handleOperate(articleId, topStatus)} 
					/>;
			}
		},
		{
			title: "推荐",
			dataIndex: "officalStat",
			key: "officalStat",
			render(_, item) {
				// 使用 switch 开关
				const { articleId, officalStat } = item;
				const isOffical = officalStat === 1;
				const officalStatus = isOffical ? 2 : 1; // 1-官方推荐；2-取消官方推荐
				return <Switch 
					checked={isOffical} 
					onChange={() => handleOperate(articleId, officalStatus)} 
					/>;
			}
		},
		{
			title: "状态",
			dataIndex: "status",
			key: "status",
			render(_, item) {
				const { articleId, status } = item;
				return <Select 
								// 如果 status 为 1 那么 status 为 warning
								status={status === 1 ? "" : "error"}
								value={status.toString()} 
								options={PushStatusList}
								onChange={(value) => handleStatusChange(articleId, Number(value))}
							>
							</Select>;
			}
		},
		{
			title: "操作",
			key: "key",
			width: 150,
			render: (_, item) => {
				// 从 item 中取出 articleId
				const { articleId } = item;
				return (
					<div className="operation-btn">
						<Tooltip title="调整标题">
							<Button
								type="primary"
								icon={<EditOutlined />}
								style={{ marginRight: "10px" }}
								onClick={() => {
									setIsModalOpen(true);
									handleChange({ ...item });
									formRef.setFieldsValue({
										...item
									});
								}}
							></Button>
						</Tooltip>
						<Tooltip title="调整内容">
							<Button
								type="primary"
								icon={<HighlightOutlined />}
								style={{ marginRight: "10px" }}
								onClick={() => {
									handleEdit(articleId);
								}}
							></Button>
						</Tooltip>
						<Tooltip title="删除">
							<Button 
								type="primary" 
								danger 
								icon={<DeleteOutlined />} 
								onClick={() => handleDel(articleId)}>
							</Button>
						</Tooltip>
					</div>
				);
			}
		}
	];

	// 编辑表单
	const reviseModalContent = (
		<Form name="basic" form={formRef} labelCol={{ span: 4 }} wrapperCol={{ span: 16 }} autoComplete="off">
			<Form.Item label="标题" name="title" rules={[{ required: false, message: "请输入标题!" }]}>
				<Input
					allowClear
					onChange={e => {
						handleChange({ tag: e.target.value });
					}}
				/>
			</Form.Item>
			<Form.Item 
				label="教程名" 
				name="shortTitle" 
				tooltip="教程的时候使用"
				rules={[{ required: false, message: "请输入短标题!" }]}
				>
				<Input
					allowClear
					onChange={e => {
						handleChange({ tag: e.target.value });
					}}
				/>
			</Form.Item>
		</Form>
	);

	return (
		<div className="article">
			<ContentWrap>
				{/* 搜索 */}
				<Search
					handleSearchChange={handleSearchChange}
					handleSearch={handleSearch}
					PushStatusList={PushStatusList}
					ToppingStatusList={ToppingStatusList}
					OfficalStatusList={OfficalStatusList}
				/>
				{/* 表格 */}
				<ContentInterWrap>
					<Table columns={columns} dataSource={tableData} pagination={paginationInfo} />
				</ContentInterWrap>
			</ContentWrap>
			{/* 弹窗 */}
			<Modal title="修改" visible={isModalOpen} onCancel={() => setIsModalOpen(false)} onOk={handleSubmit}>
				{reviseModalContent}
			</Modal>
		</div>
	);
};

const mapStateToProps = (state: any) => state.disc.disc;
const mapDispatchToProps = {};
export default connect(mapStateToProps, mapDispatchToProps)(Article);
