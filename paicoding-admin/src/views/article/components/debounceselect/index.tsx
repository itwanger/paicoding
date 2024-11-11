/* eslint-disable prettier/prettier */
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import React from "react";
import { message,Select, SelectProps, Spin } from "antd";
import { on } from "events";
import { debounce, set } from "lodash";

import { getTagListApi } from "@/api/modules/tag";
import { MapItem } from "@/typings/common";

// 导入 index.scss 文件
import "./index.scss";

export interface DebounceSelectProps<ValueType = any> extends Omit<SelectProps<ValueType | ValueType[]>, "options" | "children"> {
	debounceTimeout?: number;
}

export interface IPagination {
	current: number;
	pageSize: number;
	total?: number;
}

export const initPagination: IPagination = {
	current: 1,
	pageSize: 10,
	total: 0
};

export interface IFormType {
	// 标签名
	tag: string;
}

const defaultInitForm: IFormType = {
	tag: ""
}

// Usage of DebounceSelect
interface TagValue {
	key: string;
	label: string;
	value: string;
}

function DebounceSelect<ValueType extends { key?: string; label: React.ReactNode; value: string | number } = any>({
	debounceTimeout = 800,
	...props
}: DebounceSelectProps<ValueType>) {
	// 使用useState定义state变量，用于保存选项列表和加载状态
	const [fetching, setFetching] = useState(false);
	const [options, setOptions] = useState<ValueType[]>([]);
	// 使用useRef定义ref变量，用于记录请求的次数
	const fetchRef = useRef(0);

	// 刷新函数
	const [query, setQuery] = useState<number>(0);

	// 分页
	const [pagination, setPagination] = useState<IPagination>(initPagination);
	const { current, pageSize } = pagination;

	// 查询表单值
	const [searchForm, setSearchForm] = useState<IFormType>(defaultInitForm);

	// 检测滚动到底部的逻辑
	// @ts-ignore
	const handleScroll = event => {
		const { scrollTop, scrollHeight, clientHeight } = event.target;
		if (scrollTop + clientHeight === scrollHeight) {
			console.log("滚动到底部了");
			setPagination(prev => ({ ...prev, current: prev.current + 1 }));
			onSure();
		}
	};

	// 查询表单值改变
	const handleSearchChange = (item: MapItem) => {
		setSearchForm({ ...searchForm, ...item });
	};

	const onSure = useCallback(() => {
		setQuery(prev => prev + 1);
	}, []);

	const debounceFetcher = useMemo(() => {
		const loadOptions = debounce(async (value: string) => {
			console.log("loadOptions debounce", value);
			handleSearchChange({ tag: value });
			setOptions([]);
			setPagination(initPagination);
			// 一切准备好后，开始请求数据
			onSure();
			console.log("开始请求数据");
		}, debounceTimeout);
	
		return loadOptions;
	}, [debounceTimeout]);

	useEffect(() => {
		let isActive = true;
		const fetchId = ++fetchRef.current;
		setFetching(true);
	
		// 调用 API 并处理 Promise
		getTagListApi({
			...searchForm,
			pageNumber: current,
			pageSize
		}).then(({ status, result }) => {
			if (isActive && fetchId === fetchRef.current) {
				const { code } = status || {};
				//@ts-ignore
				const { list, pageNum, pageSize: resPageSize, pageTotal, total } = result || {};
				if (code === 0 && list.length > 0) {
					setPagination({ current: Number(pageNum), pageSize: resPageSize, total });

					const newList = list.map((item: MapItem) => ({
						key: item?.tagId,
						label: item?.tag,
						value: item?.tag
					}));
					setOptions(prevOptions => {
						console.log("当前选项 (prevOptions):", prevOptions); // 打印当前选项
						console.log("新加载的选项 (newOptions):", newList); // 打印新加载的选项
						return [...prevOptions, ...newList]; // 追加新数据
					});
				} else {
					console.error('该搜索的值没有标签');
				}
			}
			setFetching(false);
		}).catch(error => {
			console.error('加载数据失败:', error);
		});
	
		return () => {
			isActive = false; // 取消当前异步操作
		};
	}, [query]);	

	useEffect(() => {
		console.log("options 更新:", options);
	}, [options]);	

	// 清空
	const handleClear = () => {
		setOptions([]);
		setPagination(initPagination);
		setSearchForm(defaultInitForm);
	};

	return (
		<Select
			allowClear
			placeholder="请选择标签"
			// optionLabelProp：回填到选择框的 Option 的属性值，默认是 Option 的子元素。
			// 比如在子元素需要高亮效果时，此值可以设为 value
			optionLabelProp="value"
			// 是否在输入框聚焦时自动调用搜索方法
			showSearch={true}
			labelInValue
			mode="multiple"
			filterOption={false}
			// 绑定防抖函数到onSearch事件上，触发搜索操作时，会调用防抖函数
			onSearch={debounceFetcher}
			onDropdownVisibleChange={visible => {
				console.log("onDropdownVisibleChange", visible);
				// 下拉列表展开时，重新获取选项列表(如果之前没有值的话)
				if (visible) {
					console.log("debounceFetcher，继续初始化");
					handleClear();
					onSure();
				}
			}}
			onClear={handleClear}
			onPopupScroll={handleScroll}
			// 当加载状态为true时，显示旋转加载图标
			notFoundContent={fetching ? <Spin size="small" /> : null}
			{...props}
			// 将选项列表传递给Select组件进行展示
			options={options}
		/>
	);
}
export default DebounceSelect;
