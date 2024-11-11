/* eslint-disable prettier/prettier */
import { FC, useCallback, useEffect, useRef, useState } from "react";
import Highlighter from 'react-highlight-words';
import { connect } from "react-redux";
import { DeleteOutlined, PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Avatar, Button, Drawer, Form, Input, InputRef, message, Modal, Space, Table } from "antd";
import type { ColumnsType,ColumnType } from "antd/es/table";
import { FilterConfirmProps } from "antd/es/table/interface";

import { getAuthorWhiteListApi, resetAuthorWhiteApi, updateAuthorWhiteApi } from "@/api/modules/author";
import { ContentInterWrap, ContentWrap } from "@/components/common-wrap";
import { MapItem } from "@/typings/common";
import AuthorSelect from "@/views/column/setting/components/authorselect";

import "./index.scss";

interface DataType {
	key: string;
	userId: number;
	photo: string;
	userName: string;
	profile: string;
}

interface IProps {}

export interface IFormType {
	authorId: number;
	authorName: string;
}

const defaultInitForm: IFormType = {
	authorId: -1,
	authorName: "",
};

const AuthorWhiteList: FC<IProps> = props => {
	const [formRef] = Form.useForm();
	// form值
	const [form, setForm] = useState<IFormType>(defaultInitForm);
	// 抽屉
	const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
	// 列表数据
	const [tableData, setTableData] = useState<DataType[]>([]);
	// 刷新函数
	const [query, setQuery] = useState<number>(0);

	const { authorName, authorId } = form;

	const [searchText, setSearchText] = useState('');
  const [searchedColumn, setSearchedColumn] = useState('');
  const searchInput = useRef<InputRef>(null);

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	// 值改变
	const handleChange = (item: MapItem) => {
		setForm({ ...form, ...item });
		console.log("handleChange item setForm", item, form);
		formRef.setFieldsValue({ ...item });
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
		setIsDrawerOpen(true);
	};

	// 当点击查询按钮的时候触发

	type DataIndex = keyof DataType;

	const handleSearch = (
    selectedKeys: string[],
    confirm: (param?: FilterConfirmProps) => void,
    dataIndex: DataIndex,
  ) => {
    confirm();
    setSearchText(selectedKeys[0]);
    setSearchedColumn(dataIndex);
  };

	const handleReset = (clearFilters: () => void) => {
    clearFilters();
    setSearchText('');
  };

	// 删除
	const handleDel = (userId: number) => {
		Modal.warning({
			title: "确认要从白名单中删除该作者吗",
			content: "请谨慎操作！",
			maskClosable: true,
			closable: true,
			onOk: async () => {
				const { status } = await delAuthorWhiteApi(userId);
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
		console.log("handleSubmit newValues", values.author);

		const { status: successStatus } = (await updateAuthorWhiteApi(values.author)) || {};
		const { code, msg } = successStatus || {};
		if (code === 0) {
			setIsDrawerOpen(false);
			onSure();
		} else {
			message.error(msg);
		}
		
	};

	// 数据请求
	useEffect(() => {
		const getSortList = async () => {
			const { status, result } = await getAuthorWhiteListApi();
			const { code } = status || {};

			if (code === 0) {
				// @ts-ignore
				const newList = result.map((item: MapItem) => ({ ...item, key: item?.userId }));
				setTableData(newList);
			}
		};

		getSortList();
	}, [query]);

	const getColumnSearchProps = (dataIndex: DataIndex): ColumnType<DataType> => ({
    filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters, close }) => (
      <div style={{ padding: 8 }} onKeyDown={(e) => e.stopPropagation()}>
        <Input
          ref={searchInput}
          placeholder={`Search ${dataIndex}`}
          value={selectedKeys[0]}
          onChange={(e) => setSelectedKeys(e.target.value ? [e.target.value] : [])}
          onPressEnter={() => handleSearch(selectedKeys as string[], confirm, dataIndex)}
          style={{ marginBottom: 8, display: 'block' }}
        />
        <Space>
          <Button
            type="primary"
            onClick={() => handleSearch(selectedKeys as string[], confirm, dataIndex)}
            icon={<SearchOutlined />}
            size="small"
            style={{ width: 90 }}
          >
            查询
          </Button>
          <Button
            onClick={() => clearFilters && handleReset(clearFilters)}
            size="small"
            style={{ width: 90 }}
          >
            重置
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => {
              confirm({ closeDropdown: false });
              setSearchText((selectedKeys as string[])[0]);
              setSearchedColumn(dataIndex);
            }}
          >
            过滤
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => {
              close();
            }}
          >
            关闭
          </Button>
        </Space>
      </div>
    ),
    filterIcon: (filtered: boolean) => (
      <SearchOutlined style={{ color: filtered ? '#1677ff' : undefined }} />
    ),
    onFilter: (value, record) =>
      record[dataIndex]
        .toString()
        .toLowerCase()
        .includes((value as string).toLowerCase()),
    onFilterDropdownOpenChange: (visible) => {
      if (visible) {
        setTimeout(() => searchInput.current?.select(), 100);
      }
    },
    render: (text) =>
      searchedColumn === dataIndex ? (
        <Highlighter
          highlightStyle={{ backgroundColor: '#ffc069', padding: 0 }}
          searchWords={[searchText]}
          autoEscape
          textToHighlight={text ? text.toString() : ''}
        />
      ) : (
        text
      ),
  });

	// 表头设置
	const columns: ColumnsType<DataType> = [
		{
			title: "作者名称",
			dataIndex: "userName",
			key: "userName",
			...getColumnSearchProps('userName'),
		},
		{
			title: "作者头像",
			dataIndex: "photo",
			key: "photo",
			render(value) {
				return (
					<>
						<Avatar src={value} />
					</>
				);
			}
		},
		{
			title: "操作",
			key: "key",
			width: 210,
			render: (_, item) => {
				const { userId } = item;
				console.log("userId", userId);
				return (
					<div className="operation-btn">
						<Button type="primary" danger icon={<DeleteOutlined />} onClick={() => handleDel(userId)}>
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
			<Form.Item label="作者" name="author" rules={[{ required: true, message: "请选择作者!" }]}>
				<AuthorSelect authorName={authorName} handleChange={handleChange} />
			</Form.Item>
		</Form>
	);

	return (
		<div className="author-whitelist">
			<ContentWrap>
				{/* 新增 */}
				<div className="author-whitelist-search">
					<ContentInterWrap className="author-whitelist-search__wrap">
						<div className="author-whitelist-search__search">
							
						</div>
						<div className="author-whitelist-search__search-btn">
							<Button
							type="primary"
							icon={<PlusOutlined />}
							style={{ marginRight: "20px" }}
							onClick={handleAdd}
							>
								添加作者
							</Button>
						</div>
					</ContentInterWrap>
				</div>
				
				{/* 表格 */}
				<ContentInterWrap>
					<Table columns={columns} dataSource={tableData} />
				</ContentInterWrap>
			</ContentWrap>
			{/* 抽屉 */}
			<Drawer 
				title="添加" 
				open={isDrawerOpen} 
				onClose={handleClose}
				width={500}
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
export default connect(mapStateToProps, mapDispatchToProps)(AuthorWhiteList);
