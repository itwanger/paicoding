/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import { connect } from "react-redux";
import { DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { Button, Drawer, Form, Input, message, Modal, Space, Switch, Table } from "antd";
import type { ColumnsType } from "antd/es/table";

import { delTagApi, getTagListApi, operateTagApi, updateTagApi } from "@/api/modules/tag";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import Search from "./components/search";

import "./index.scss";

interface DataType {
	tagId: number;
	tag: string;
	status: number;
}

interface IProps {}

export interface IFormType {
	tagId: number; // 为0时，是保存，非0是更新
	tag: string; // 标签名
}

const defaultInitForm: IFormType = {
	tagId: -1,
	tag: ""
};

const Tag: FC<IProps> = props => {
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

	const { tagId } = form;

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	// 值改变
	const handleChange = (item: MapItem) => {
		setForm({ ...form, ...item });
	};
	// 查询表单值改变
	const handleSearchChange = (item: MapItem) => {
		setSearchForm({ ...searchForm, ...item });
	};
	// 点击搜索按钮时触发搜索
	const handleSearch = () => {
		setPagination(initPagination);
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
	const handleDel = (tagId: number) => {
		Modal.warning({
			title: "确认删除此标签吗",
			content: "删除此标签后无法恢复，请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delTagApi(tagId);
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
			tagId: status === UpdateEnum.Save ? UpdateEnum.Save : tagId
		};
		const { status: successStatus } = (await updateTagApi(newValues)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			setIsDrawerOpen(false);
			setPagination({ current: 1, pageSize });
			onSure();
		} else {
			message.error(msg);
		}
	};

	// 上线/下线
	const handleOperate = async (tagId: number, pushStatus: number) => {
		const { status } = await operateTagApi({ tagId, pushStatus });
		const { code, msg } = status || {};
		console.log();
		if (code === 0) {
			message.success("操作成功");
			onSure();
		} else {
			message.error(msg);
		}
	};

	// 数据请求
	useEffect(() => {
		const getSortList = async () => {
			const { status, result } = await getTagListApi({
				...searchForm,
				pageNumber: current,
				pageSize
			});
			const { code } = status || {};
			//@ts-ignore
			const { list, pageNum, pageSize: resPageSize, pageTotal, total } = result || {};
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item?.tagId }));
				setTableData(newList);
			}
		};
		getSortList();
	}, [query, current, pageSize]);

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "标签",
			dataIndex: "tag",
			key: "tag"
		},
		{
			title: "上下线",
			dataIndex: "status",
			key: "status",
			render(status, item) {
				return (
					<Switch
						checked={status === 1}
						onChange={() => {
							const pushStatus = status === 0 ? 1 : 0;
							handleOperate(item.tagId, pushStatus);
						}}
					/>
				);
			}
		},
		{
			title: "操作",
			key: "key",
			width: 210,
			render: (_, item) => {
				const { tagId } = item;
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

						<Button type="primary" danger icon={<DeleteOutlined />} onClick={() => handleDel(tagId)}>
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
			<Form.Item label="标签" name="tag" rules={[{ required: true, message: "请输入名称!" }]}>
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
export default connect(mapStateToProps, mapDispatchToProps)(Tag);
