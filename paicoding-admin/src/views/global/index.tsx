/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import { connect } from "react-redux";
import { DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { Button, Drawer, Form, Input, message, Modal, Space, Switch, Table } from "antd";
import type { ColumnsType } from "antd/es/table";

import { delGlobalConfigApi, getGlobalConfigListApi, updateGlobalConfigApi } from "@/api/modules/global";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import Search from "./components/search";

import "./index.scss";

interface DataType {
	id: number;
	keywords: string;
	value: string;
	comment: string;
}

interface IProps {}

export interface IFormType {
	id: number; // 为0时，是保存，非0是更新
	keywords: string; // 键名
	value: string; // 键值
	comment: string; // 备注
}

const defaultInitForm: IFormType = {
	id: -1,
	keywords: "",
	value: "",
	comment: ""
};

const GlobalConfig: FC<IProps> = props => {
	const [formRef] = Form.useForm();
	// form值
	const [form, setForm] = useState<IFormType>(defaultInitForm);
	// 查询表单值
	const [searchForm, setSearchForm] = useState<IFormType>(defaultInitForm);
	// 抽屉
	const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
	// 列表数据
	const [tableData, setTableData] = useState<DataType[]>([]);
	// 刷新函数
	const [query, setQuery] = useState<number>(0);

	//当前的状态
	const [status, setStatus] = useState<UpdateEnum>(UpdateEnum.Save);

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

	const { id } = form;

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	// 值改变
	const handleChange = (item: MapItem) => {
		setForm({ ...form, ...item });
		console.log(form, item);
	};
	// 查询表单值改变
	const handleSearchChange = (item: MapItem) => {
		setSearchForm({ ...searchForm, ...item });
	};
	// 点击搜索按钮时触发搜索
	const handleSearch = () => {
		setPagination({ current: 1, pageSize });
		onSure();
	};
	// 抽屉关闭
	const handleClose = () => {
		setIsDrawerOpen(false);
	};
	// 重置表单
	const resetForm = () => {
		setForm(defaultInitForm);
	};
	// 新增触发
	const handleAdd = () => {
		resetForm();
		setStatus(UpdateEnum.Save);
		setIsDrawerOpen(true);
	};

	// 删除
	const handleDel = (id: number) => {
		Modal.warning({
			title: "确认删除此全局配置项吗",
			content: "删除此全局配置项后无法恢复，请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delGlobalConfigApi(id);
				const { code, msg } = status || {};
				console.log();
				if (code === 0) {
					message.success("删除成功");
					onSure();
				} else {
					message.error(msg);
				}
			}
		});
	};

	const handleSubmit = async () => {
		const values = await formRef.validateFields();
		const newValues = {
			...values,
			id: status === UpdateEnum.Save ? UpdateEnum.Save : id
		};
		console.log("提交的时候", newValues);
		const { status: successStatus } = (await updateGlobalConfigApi(newValues)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			setIsDrawerOpen(false);
			setPagination({ current: 1, pageSize });
			onSure();
		} else {
			message.error(msg);
		}
	};

	// 数据请求
	useEffect(() => {
		const getSortList = async () => {
			console.log("searchForm", searchForm);
			const { status, result } = await getGlobalConfigListApi({
				...searchForm,
				pageNumber: current,
				pageSize
			});
			const { code } = status || {};
			//@ts-ignore
			const { list, pageNum, pageSize: resPageSize, pageTotal, total } = result || {};
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item?.id }));
				setTableData(newList);
			}
		};
		getSortList();
	}, [query, current, pageSize]);

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "配置项名",
			dataIndex: "keywords",
			key: "keywords"
		},
		{
			title: "配置项值",
			dataIndex: "value",
			width: 400,
			key: "value",
			render: (text) => (
				<div style={{
					wordWrap: 'break-word',
					wordBreak: 'break-all',
					maxHeight: '4rem', // 3行的高度，可以根据字体大小调整
					overflow: 'hidden',
					textOverflow: 'ellipsis',
					display: '-webkit-box',
					WebkitLineClamp: 3, // 限制显示3行
					WebkitBoxOrient: 'vertical',
				}}>
					{text}
				</div>
			)
		},		
		{
			title: "备注",
			dataIndex: "comment",
			key: "comment"
		},
		{
			title: "操作",
			key: "key",
			width: 210,
			render: (_, item) => {
				return (
					<div className="operation-btn">
						<Button
							type="primary"
							icon={<EditOutlined />}
							style={{ marginRight: "10px" }}
							onClick={() => {
								setIsDrawerOpen(true);
								setStatus(UpdateEnum.Edit);
								handleChange({ ...item });
								formRef.setFieldsValue({ ...item });
							}}
						>
							编辑
						</Button>

						<Button type="primary" danger icon={<DeleteOutlined />} onClick={() => handleDel(item.id)}>
							删除
						</Button>
					</div>
				);
			}
		}
	];

	// 编辑表单
	const reviseDrawerContent = (
		<Form name="basic" form={formRef} labelCol={{ span: 4 }} wrapperCol={{ span: 16 }} autoComplete="off">
			<Form.Item label="配置项名" name="keywords" rules={[{ required: true, message: "请输入配置项名称!" }]}>
				<Input
					allowClear
					onChange={e => {
						handleChange({ keywords: e.target.value });
					}}
				/>
			</Form.Item>
			<Form.Item label="配置项值" name="value" rules={[{ required: true, message: "请输入配置项值!" }]}>
				<Input.TextArea
					allowClear
					onChange={e => {
						handleChange({ value: e.target.value });
					}}
				/>
			</Form.Item>
			<Form.Item label="备注" name="comment" rules={[{ required: true, message: "请输入备注!" }]}>
				<Input
					allowClear
					onChange={e => {
						handleChange({ comment: e.target.value });
					}}
				/>
			</Form.Item>
		</Form>
	);

	return (
		<div className="banner">
			<ContentWrap>
				{/* 搜索 */}
				<Search handleSearchChange={handleSearchChange} handleSearch={handleSearch} handleAdd={handleAdd} />
				{/* 表格 */}
				<ContentInterWrap>
					<Table columns={columns} dataSource={tableData} pagination={paginationInfo} />
				</ContentInterWrap>
			</ContentWrap>
			{/* 抽屉 */}
			<Drawer
				title="添加/修改"
				open={isDrawerOpen}
				size="large"
				onClose={handleClose}
				extra={
					<Space>
						<Button onClick={handleClose}>取消</Button>
						<Button type="primary" onClick={handleSubmit}>
							确定
						</Button>
					</Space>
				}
			>
				{reviseDrawerContent}
			</Drawer>
		</div>
	);
};

const mapStateToProps = (state: any) => state.disc.disc;
const mapDispatchToProps = {};
export default connect(mapStateToProps, mapDispatchToProps)(GlobalConfig);
