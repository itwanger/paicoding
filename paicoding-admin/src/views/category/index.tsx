/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import { connect } from "react-redux";
import { DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { Button, Drawer, Form, Input, InputNumber,message, Modal, Space, Switch,Table } from "antd";
import type { ColumnsType } from "antd/es/table";

import { delCategoryApi, getCategoryListApi, operateCategoryApi, updateCategoryApi } from "@/api/modules/category";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import Search from "./components/search";

import "./index.scss";

interface DataType {
	categoryId: number;
	key: string;
	name: string;
}

interface IProps {}

export interface IFormType {
	categoryId: number; // 为0时，是保存，非0是更新
	category: string; // 分类名
	rank: number; // 排名
}

const defaultInitForm: IFormType = {
	categoryId: -1,
	category: "",
	rank: -1
};

const Category: FC<IProps> = props => {
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
		...pagination,
		showSizeChanger: true,
		showTotal: (total: number) => `共 ${total || 0} 条`,
		onChange: (current: number, pageSize: number) => {
			setPagination({ current, pageSize });
		}
	};

	const { categoryId } = form;

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
		setPagination({ current: 1, pageSize });
		onSure();
	};
	// 抽屉关闭
	const handleClose = () => {
		setIsDrawerOpen(false);
	};

	// 重置表单
	const resetFrom = () => {
		setForm(defaultInitForm);
	};
	// 新增触发
	const handleAdd = () => {
		resetFrom();
		setStatus(UpdateEnum.Save);
		setIsDrawerOpen(true);
	};

	// 删除
	const handleDel = (categoryId: number) => {
		Modal.warning({
			title: "确认删除此分类吗",
			content: "删除此分类后无法恢复，请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delCategoryApi(categoryId);
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

	const handleSubmit = async () => {
		const values = await formRef.validateFields();
		const newValues = { 
			...values, 
			categoryId: status === UpdateEnum.Save ? UpdateEnum.Save : categoryId 
		};

		const { status: successStatus } = (await updateCategoryApi(newValues)) || {};
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
	const handleOperate = async (categoryId: number, pushStatus: number) => {
		const { status } = await operateCategoryApi({ categoryId, pushStatus });
		const { code, msg } = status || {};
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
			const { status, result } = await getCategoryListApi({ 
				...searchForm,
				pageNumber: current, 
				pageSize 
			});
			const { code } = status || {};
			//@ts-ignore
			const { list, pageNum, pageSize: resPageSize, total } = result || {};
			console.log("result", result);
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item?.categoryId }));
				setTableData(newList);
			}
		};
		getSortList();
	}, [query, current, pageSize]);

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "分类名称",
			dataIndex: "category",
			key: "category"
		},
		{
			title: "上下线",
			dataIndex: "status",
			key: "status",
			render(status, item) {
				// switch 组件
				return (
					<Switch
						checked={status === 1}
						onChange={() => {
							const pushStatus = status === 0 ? 1 : 0;
							handleOperate(item.categoryId, pushStatus);
						}
					}
				/>
				);
			}
		},
		{
			title: "排序",
			dataIndex: "rank",
			key: "rank"
		},
		{
			title: "操作",
			key: "key",
			width: 210,
			render: (_, item) => {
				const { categoryId } = item;
				return (
					<div className="operation-btn">
						<Button
							type="primary"
							icon={<EditOutlined />}
							style={{ marginRight: "10px" }}
							onClick={() => {
								setIsDrawerOpen(true);
								setStatus(UpdateEnum.Edit);
								handleChange({ categoryId: categoryId });
								formRef.setFieldsValue({ ...item });
							}}
						>
							编辑
						</Button>
						
						<Button type="primary" danger icon={<DeleteOutlined />} onClick={() => handleDel(categoryId)}>
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
			<Form.Item label="分类" name="category" rules={[{ required: true, message: "请输入分类!" }]}>
				<Input
					allowClear
					onChange={e => {
						handleChange({ category: e.target.value });
					}}
				/>
			</Form.Item>
			<Form.Item label="排序" name="rank" rules={[{ required: true, message: "请输入排序!" }]}>
				<InputNumber
					min={0}
					onChange={value => {
						handleChange({ rank: value });
					}}
				/>
			</Form.Item>
		</Form>
	);

	return (
		<div className="category">
			<ContentWrap>
				{/* 搜索 */}
				<Search 
					handleSearchChange={handleSearchChange} 
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
export default connect(mapStateToProps, mapDispatchToProps)(Category);
