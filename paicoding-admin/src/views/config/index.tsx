/* eslint-disable react/jsx-no-undef */
/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useState } from "react";
import { connect } from "react-redux";
import { DeleteOutlined, EditOutlined, EyeOutlined } from "@ant-design/icons";
import {
	Button,
	Descriptions,
	Drawer,
	Form,
	Image,
	Input,
	InputNumber,
	message,
	Modal,
	Select,
	Space,
	Switch,
	Table,
	Tag,
	UploadFile
} from "antd";
import type { ColumnsType } from "antd/es/table";

import { delConfigApi, getConfigListApi, operateConfigApi, updateConfigApi } from "@/api/modules/config";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { initPagination, IPagination, UpdateEnum } from "@/enums/common";
import { MapItem } from "@/typings/common";
import { getCompleteUrl } from "@/utils/util";
import ImgUpload from "./components/imgupload";
import Search from "./components/search";

import "./index.scss";

import "antd/es/modal/style";
import "antd/es/slider/style";

interface DataType {
	bannerUrl: string;
	jumpUrl: string;
	id: number;
	key: string;
	name: string;
	tags: string;
	type: number;
	status: number;
}

interface IProps {}

// 编辑新增表单的值类型
export interface IFormType {
	configId: number; // ID
	type: number; // 类型
	name: string; // 名称
	content: string; // 详细描述
	bannerUrl: string; // 图片
	jumpUrl: string; // 跳转URL
	rank: number; // 排序
	tags: number; // 标签
}

// 查询表单的值类型
interface ISearchFormType {
	type: number; // 类型
	name: string; // 名称
}

// 编辑新增表单默认值
const defaultInitForm: IFormType = {
	configId: -1,
	type: -1,
	name: "",
	rank: -1,
	tags: -1,
	content: "",
	bannerUrl: "",
	jumpUrl: ""
};

// 查询表单默认值
const defaultSearchForm: ISearchFormType = {
	type: -1,
	name: ""
};

const Banner: FC<IProps> = props => {
	const [formRef] = Form.useForm();
	// form值
	const [form, setForm] = useState<IFormType>(defaultInitForm);
	// 查询表单值
	const [searchForm, setSearchForm] = useState<ISearchFormType>(defaultSearchForm);
	// 改成抽屉
	const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
	// 弹窗
	const [isOpenDrawerShow, setIsOpenDrawerShow] = useState<boolean>(false);
	// 列表数据
	const [tableData, setTableData] = useState<DataType[]>([]);
	// 刷新函数
	const [query, setQuery] = useState<number>(0);

	//当前的状态
	const [status, setStatus] = useState<UpdateEnum>(UpdateEnum.Save);

	// 图片
	const [coverList, setCoverList] = useState<UploadFile[]>([]);

	// 分页
	const [pagination, setPagination] = useState<IPagination>(initPagination);
	const { current, pageSize } = pagination;

	//@ts-ignore
	const { ConfigType, ConfigTypeList, ArticleTag, ArticleTagList } = props || {};

	const { configId, type, name, content, bannerUrl, jumpUrl, rank, tags } = form;

	const paginationInfo = {
		showSizeChanger: true,
		showTotal: (total: number) => `共 ${total || 0} 条`,
		...pagination,
		onChange: (current: number, pageSize: number) => {
			setPagination({ current, pageSize });
		}
	};

	const detailInfo = [
		{ label: "类型", title: ConfigType[type] },
		{ label: "名称", title: name },
		{ label: "内容", title: content },
		{ label: "排序", title: rank }
	];

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	// 编辑新增表单值改变
	const handleChange = (item: MapItem) => {
		console.log("ConfigTypeList", ConfigTypeList);
		setForm({ ...form, ...item });
	};
	// 查询表单值改变
	const handleSearchChange = (item: MapItem) => {
		setSearchForm({ ...searchForm, ...item });
	};

	// 点击搜索按钮时触发搜索
	const handleSearch = () => {
		// 重置分页
		setPagination({ current: 1, pageSize });
		onSure();
	};

	// 点击取消关闭按钮时触发，关闭抽屉
	const handleClose = () => {
		setIsOpenDrawerShow(false);
		setIsDrawerOpen(false);
	};

	// 重置表单
	const resetForm = () => {
		setForm(defaultInitForm);
		formRef.resetFields();
	};

	// 新增触发
	const handleAdd = () => {
		resetForm();
		setStatus(UpdateEnum.Save);
		setIsDrawerOpen(true);
		// 图片也清空
		setCoverList([]);
	};

	// 删除
	const handleDel = (configId: number) => {
		Modal.warning({
			title: "确认删除此配置吗",
			content: "删除此配置后无法恢复，请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delConfigApi(configId);
				const { code, msg } = status || {};
				if (code === 0) {
					message.success("删除成功");
					// 触发刷新
					onSure();
				} else {
					message.error(msg);
				}
			}
		});
	};

	const handleSubmit = async () => {
		const values = await formRef.validateFields();
		console.log("values", values);
		// 从 value 中取出 type 定义为变量 value 并转为 number 类型
		const { type } = values;
		// 转成 number
		const typeNumber = Number(type);

		// 如果类型为教程和公告时，详细描述必填
		if (typeNumber === 4 || typeNumber === 5) {
			if (!values.content) {
				message.error("类型为" + ConfigType[type] + "时详细描述不能为空");
				return;
			}
		}

		console.log("bannerUrl", bannerUrl);

		// 如果类型为教程和 PDF 时，图片不能为空
		if (typeNumber === 5 || typeNumber === 6) {
			if (!bannerUrl) {
				message.error("类型为" + ConfigType[type] + "时图片不能为空");
				return;
			}
		}

		// 重写新的值
		const newValues = {
			...values,
			bannerUrl: bannerUrl, // 图片要重新赋值，因为 values 中没有
			configId: status === UpdateEnum.Save ? UpdateEnum.Save : configId
		};

		const { status: successStatus } = (await updateConfigApi(newValues)) || {};
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
	const handleOperate = async (configId: number, pushStatus: number) => {
		const { status } = await operateConfigApi({ configId, pushStatus });
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
		const getConfigList = async () => {
			console.log("searchForm", searchForm);
			const { status, result } = await getConfigListApi({
				...searchForm,
				pageNumber: current,
				pageSize
			});
			const { code } = status || {};
			//@ts-ignore
			const { list, pageNum, pageSize: resPageSize, total } = result || {};
			setPagination({ current: Number(pageNum), pageSize: resPageSize, total });
			if (code === 0) {
				const newList = list.map((item: MapItem) => ({ ...item, key: item.id }));
				setTableData(newList);
			}
		};
		getConfigList();
	}, [query, current, pageSize]);

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "配置名称",
			dataIndex: "name",
			key: "name",
			width: 400,
			render(name, item) {
				return (
					<span>
						<Tag color="orange">{ArticleTag[item.tags]}</Tag>
						{name}
					</span>
				);
			}
		},
		{
			title: "类型",
			dataIndex: "type",
			key: "type",
			render(type) {
				return ConfigType[type];
			}
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
							handleOperate(item.id, pushStatus);
						}}
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
			width: 300,
			render: (_, item) => {
				const { id, type, status } = item;
				return (
					<div className="operation-btn">
						<Button
							type="primary"
							icon={<EyeOutlined />}
							style={{ marginRight: "10px" }}
							onClick={() => {
								setIsOpenDrawerShow(true);
								handleChange({ configId: id, ...item });
							}}
						>
							详情
						</Button>
						<Button
							type="primary"
							icon={<EditOutlined />}
							style={{ marginRight: "10px" }}
							onClick={() => {
								setIsDrawerOpen(true);
								setStatus(UpdateEnum.Edit);
								const { bannerUrl } = item;
								console.log("点击编辑 bannerUrl", bannerUrl);
								handleChange({ configId: id, bannerUrl: bannerUrl });
								// 需要设置 image 的值
								const coverUrl = getCompleteUrl(bannerUrl);
								// 更新 coverList
								setCoverList([
									{
										uid: "-1",
										name: "封面图(建议70px*100px)",
										status: "done",
										thumbUrl: coverUrl,
										url: coverUrl
									}
								]);
								formRef.setFieldsValue({ ...item, type: String(type), status: String(status) });
							}}
						>
							编辑
						</Button>
						<Button type="primary" danger icon={<DeleteOutlined />} onClick={() => handleDel(id)}>
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
			<Form.Item label="类型" name="type" rules={[{ required: true, message: "请选择类型!" }]}>
				<Select
					allowClear
					onChange={value => {
						handleChange({ type: value });
					}}
					options={ConfigTypeList}
				/>
			</Form.Item>
			<Form.Item label="名称" name="name" rules={[{ required: true, message: "请输入名称!" }]}>
				<Input
					allowClear
					onChange={e => {
						handleChange({ name: e.target.value });
					}}
				/>
			</Form.Item>
			<Form.Item label="详细描述" name="content" tooltip="对公告和教程类型的配置进行一些简短介绍，不要太多字哦">
				<Input.TextArea
					allowClear
					maxLength={120}
					onChange={e => {
						handleChange({ content: e.target.value });
					}}
				/>
			</Form.Item>
			<Form.Item label="图片" name="bannerUrl" tooltip="类型为教程和PDF需要，建议尺寸：70*100">
				<ImgUpload coverList={coverList} setCoverList={setCoverList} handleChange={handleChange} />
			</Form.Item>
			<Form.Item
				tooltip="可以是内部或者外部链接"
				label="跳转URL"
				name="jumpUrl"
				rules={[{ required: true, message: "请输入跳转URL!" }]}
			>
				<Input.TextArea
					allowClear
					onChange={e => {
						handleChange({ jumpUrl: e.target.value });
					}}
				/>
			</Form.Item>

			<Form.Item
				tooltip="在用户端显示的时候会用到"
				label="标签"
				name="tags"
				rules={[{ required: false, message: "请选择标签!" }]}
			>
				<Select
					allowClear
					onChange={value => {
						handleChange({ tags: value });
					}}
					options={ArticleTagList}
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
		<div>
			<ContentWrap className="container">
				{/* 搜索 */}
				<Search
					ConfigTypeList={ConfigTypeList}
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
			<Drawer title="详情" placement="right" width={500} onClose={handleClose} open={isOpenDrawerShow}>
				<Descriptions column={1} labelStyle={{ width: "100px" }}>
					{detailInfo.map(({ label, title }) => (
						<Descriptions.Item label={label} key={label}>
							{title || "-"}
						</Descriptions.Item>
					))}
					{/* 标签显示 */}
					<Descriptions.Item label="标签" key="tags">
						<Tag color="orange">{ArticleTag[tags]}</Tag>
					</Descriptions.Item>
					{/* 链接显示 */}
					<Descriptions.Item label="跳转URL" key="jumpUrl">
						<Button type="link" style={{ padding: 0, margin: 0 }} target="_blank" href={jumpUrl}>
							{jumpUrl}
						</Button>
					</Descriptions.Item>
					{/* 图片组件显示 */}
					<Descriptions.Item label="图片" key="bannerUrl">
						<Image width={100} src={getCompleteUrl(bannerUrl)} />
					</Descriptions.Item>
				</Descriptions>
			</Drawer>
			{/* 弹窗 */}
			<Drawer
				title="添加/修改"
				open={isDrawerOpen}
				width={620}
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
export default connect(mapStateToProps, mapDispatchToProps)(Banner);
