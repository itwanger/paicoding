/* eslint-disable react/jsx-no-comment-textnodes */
/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import React from "react";
import { connect } from "react-redux";
import { useLocation, useNavigate } from "react-router-dom";
import { DeleteOutlined, EyeOutlined, SwapOutlined } from "@ant-design/icons";
import type { DragEndEvent } from '@dnd-kit/core';
import { DndContext, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { restrictToVerticalAxis } from '@dnd-kit/modifiers';
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Button, Descriptions, Drawer, Form, Input, InputNumber, message, Modal, Space, Table, Tooltip } from "antd";
import type { ColumnsType } from "antd/es/table";

import { delColumnArticleApi, getColumnArticleListApi, sortColumnArticleApi, sortColumnArticleByIDApi,updateColumnArticleApi } from "@/api/modules/column";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import { baseDomain } from "@/utils/util";
import TableSelect from "@/views/column/article/components/tableselect/TableSelect";
import Search from "./search";

import "./index.scss";

interface IProps {}

// 教程文章的数据类型
interface DataType {
	key: string;
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

export interface IArticleSortFormType {
	id: number; // 主键id
	articleId: number; // 文章ID
	sort: number; // 排序
}

const defaulArticleSorttInitForm: IArticleSortFormType = {
	id: -1,
	articleId: -1,
	sort: -1
};

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

const ColumnArticle: FC<IProps> = props => {

	const [formRef] = Form.useForm();
	// 调整文章书序的表单
	const [articleSortFormRef] = Form.useForm();
	// form值（详情和新增的时候会用到）
	const [form, setForm] = useState<IFormType>(defaultInitForm);
	// form值（调整文章顺序的表单值变化时保存）
	const [articleSortForm, setArticleSortForm] = useState<IArticleSortFormType>(defaulArticleSorttInitForm);
	// 查询表单
	const [searchForm, setSearchForm] = useState<ISearchForm>(defaultSearchForm);

	// 修改添加抽屉
	const [isOpenDrawerShow, setIsOpenDrawerShow] = useState<boolean>(false);
	// 详情抽屉
	const [isDetailDrawerShow, setIsDetailDrawerShow] = useState<boolean>(false);
	// 调整顺序抽屉
	const [isSortDrawerShow, setIsSortDrawerShow] = useState<boolean>(false);

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

	const location = useLocation();
	const navigate = useNavigate();
  const { columnId: columnIdParam } = location.state || {};

	// 拖拽相关 1
	interface RowProps extends React.HTMLAttributes<HTMLTableRowElement> {
		'data-row-key': string;
	}

	// 拖拽相关 2
	const Row = (props: RowProps) => {
		const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
			id: props['data-row-key'],
		});
	
		const style: React.CSSProperties = {
			...props.style,
			transform: CSS.Transform.toString(transform && { ...transform, scaleY: 1 }),
			transition,
			cursor: 'move',
			...(isDragging ? { position: 'relative', zIndex: 9999 } : {}),
		};
	
		return <tr {...props} ref={setNodeRef} style={style} {...attributes} {...listeners} />;
	};

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

	// 拖拽相关 3
	const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        // https://docs.dndkit.com/api-documentation/sensors/pointer#activation-constraints
        distance: 1,
      },
    }),
  );

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	// 值改变（新增教程文章时，老的做法，将 formRef 放到了这里，不太好）
	const handleChange = (item: MapItem) => {
		setForm({ ...form, ...item });
		formRef.setFieldsValue({ ...item });
	};

	// 值改变（调整顺序输入框发生变化时）
	const handleArticleSortChange = (item: MapItem) => {
		setArticleSortForm({ ...articleSortForm, ...item });
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
	};

	// 点击添加的时候触发
	const handleAdd = () => {
		setStatus(UpdateEnum.Save);
		formRef.resetFields();
		setIsOpenDrawerShow(true);
	};

	const goBack = () => {
    navigate(-1); // 返回上一个页面
  };

	// 关闭抽屉时触发
	const handleCloseDrawer = () => {
		// 关闭教程的下拉框
		setIsArticleSelectOpen(false);
		// 关闭抽屉
		setIsOpenDrawerShow(false);
		// 关闭调整顺序的抽屉
		setIsSortDrawerShow(false);
		// 关闭详情抽屉
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
			columnId: columnIdParam, 
		};
		console.log("提交的值:", newValues);
		
		const { status: successStatus } = (await updateColumnArticleApi(newValues)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			// 重置分页
			console.log("重置分页");
			setIsOpenDrawerShow(false);
			setPagination({ current: 1, pageSize });
			// 由于分页没有变化，所以只能是通过 query 来刷新
			onSure();
		} else {
			message.error(msg);
		}
	};

	// 调整顺序的 submit
	const handleSortByIDSubmit = async () => {
		const values = await articleSortFormRef.validateFields();
		const newValues = {
			...values,
			id: articleSortForm.id, 
		};
		console.log("提交的值:", newValues);
		
		const { status: successStatus } = (await sortColumnArticleByIDApi(newValues)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			setIsSortDrawerShow(false);
			onSure();
		} else {
			message.error(msg);
		}
	};

	const onDragEnd = async ({ active, over }: DragEndEvent) => {
		console.log("active over", active, over);
    if (over != null && active.id !== over.id) {
			// 此时，我需要把两个 ID 发送到服务器端等待更新后再在前端调整顺序
			const { status: successStatus } = (await sortColumnArticleApi(Number(active.id), Number(over.id))) || {};
			const { code, msg } = successStatus || {};
			if (code === 0) {
				// 重新查询一次
				onSure();
			} else {
				message.error(msg);
			}
    }
  };

	// 数据请求
	useEffect(() => {
		const getSortList = async () => {
			const newValues = {
				...searchForm,
				pageNumber: current, 
				pageSize,
				columnId: columnIdParam
			};
			console.log("查询教程列表之前的所有值:", newValues);
			
			const { status, result } = await getColumnArticleListApi(newValues);
			const { code } = status || {};
			// @ts-ignore
			const { list, pageNum, pageSize: resPageSize, pageTotal, total } = result || {};
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			console.log("设置分页后，current 和 pagesize 都没有变化，所以不会重新请求:", current, pageSize);
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item?.id }));
				console.log("教程列表", newList);
				setTableData(newList);
			}
		};
		getSortList();
	}, [query, current, pageSize]);

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "排序",
			dataIndex: "sort",
			key: "sort"
		},
		{
			title: "专栏名称",
			dataIndex: "column",
			key: "column",
		},
		{
			title: "教程ID",
			dataIndex: "articleId",
			key: "articleId"
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
			title: "操作",
			width:  150,
			render: (_, item) => {
				// 删除的时候用
				const { id, sort } = item;
				return (
					<div className="operation-btn">
						<Tooltip title="详情">
							<Button
								type="primary"
								icon={<EyeOutlined />}
								style={{ marginRight: "10px" }}
								onClick={() => {
									setIsDetailDrawerShow(true);
									// 把所有的值传给 form 表单
									handleChange({ ...item });
								}}
							></Button>
						</Tooltip>
						<Tooltip title="调整顺序">
							<Button
								type="primary"
								icon={<SwapOutlined className="rotated-icon"/>}
								style={{ marginRight: "10px" }}
								onClick={() => {
									setIsSortDrawerShow(true);
									// 把 id 和 sort 传给调整顺序的表单
									handleArticleSortChange({ id, sort });
									articleSortFormRef.setFieldsValue({ sort });
								}}
							></Button>
						</Tooltip>
						<Tooltip title="删除">
							<Button 
								type="primary" 
								danger 
								icon={<DeleteOutlined />} 
								onClick={() => handleDel(id)}>
							</Button>
						</Tooltip>
					</div>
				);
			}
		}
	];

	// 调整顺序的表单
	const articleSortContent = (
		<Form
			autoComplete="off"
			form={articleSortFormRef}>
      <Form.Item
        label="设置文章顺序为"
        name="sort"
        rules={[{ required: true, message: '请输入文章顺序' }]}
      >
        <InputNumber min={1} size="small"
					onChange={value => handleArticleSortChange({ sort: value })}
					/>
      </Form.Item>
    </Form>
	);

	// 编辑表单
	const reviseDrawerContent = (
		<Form 
			name="basic" 
			form={formRef} 
			labelCol={{ span: 4 }} 
			wrapperCol={{ span: 16 }} 
			autoComplete="off">
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
					goBack={goBack}
					handleSearch={handleSearch} 
					handleAdd={handleAdd}
					/>
				{/* 表格 */}
				<ContentInterWrap>
					<DndContext sensors={sensors} modifiers={[restrictToVerticalAxis]} onDragEnd={onDragEnd}>
						<SortableContext
							// rowKey array
							items={tableData.map((i) => i.key)}
							strategy={verticalListSortingStrategy}
						>
							<Table 
								components={{
									body: {
										row: Row,
									},
								}}
								rowKey="key"
								columns={columns} dataSource={tableData} pagination={paginationInfo} />
						</SortableContext>
					</DndContext>
				</ContentInterWrap>
			</ContentWrap>
			{/* 抽屉 */}
			<Drawer 
				title="详情" 
				placement="right" 
				onClose={handleCloseDrawer} 
				open={isDetailDrawerShow}>
				<Descriptions column={1} labelStyle={{ width: "100px" }}>
					{detailInfo.map(({ label, title }) => (
						<Descriptions.Item label={label} key={label}>
							{title !== 0 ? title || "-" : 0}
						</Descriptions.Item>
					))}
				</Descriptions>
			</Drawer>
			{/* 调整顺序的抽屉 */}
			<Modal 
				title="调整教程顺序"
				width={280}
				style={{ left: 200 }}
				onOk={handleSortByIDSubmit}
				onCancel={handleCloseDrawer} 
				open={isSortDrawerShow}>
				{articleSortContent}
			</Modal>
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

