import { useMemo, useRef, useState } from "react";
import { Select, SelectProps, Spin } from "antd";
import { debounce } from "lodash";

// 导入 index.scss 文件
import "./index.scss";

export interface DebounceSelectProps<ValueType = any> extends Omit<SelectProps<ValueType | ValueType[]>, "options" | "children"> {
	fetchOptions: (search: string) => Promise<ValueType[]>;
	debounceTimeout?: number;
}

function DebounceSelect<ValueType extends { key?: string; label: React.ReactNode; value: string | number } = any>({
	fetchOptions,
	debounceTimeout = 800,
	...props
}: DebounceSelectProps<ValueType>) {
	// 使用useState定义state变量，用于保存选项列表和加载状态
	const [fetching, setFetching] = useState(false);
	const [options, setOptions] = useState<ValueType[]>([]);
	// 使用useRef定义ref变量，用于记录请求的次数
	const fetchRef = useRef(0);

	// 使用useMemo定义防抖函数
	const debounceFetcher = useMemo(() => {
		// 定义异步加载选项的函数
		const loadOptions = (value: string) => {
			// 每次请求前递增fetchRef的值，用于区分请求的顺序
			fetchRef.current += 1;
			const fetchId = fetchRef.current;
			// 清空options和设置加载状态为true
			setOptions([]);
			setFetching(true);

			// 调用fetchOptions函数获取选项列表
			fetchOptions(value).then(newOptions => {
				// 判断当前请求的fetchId是否与最新的fetchRef值一致，保证按请求顺序更新选项
				if (fetchId !== fetchRef.current) {
					// 请求的回调顺序不一致，忽略该请求
					return;
				}

				// 设置获取到的选项列表，并设置加载状态为false
				setOptions(newOptions);
				setFetching(false);
			});
		};

		// 使用lodash的debounce函数创建防抖函数
		return debounce(loadOptions, debounceTimeout);
	}, [fetchOptions, debounceTimeout]);

	return (
		<Select
			labelInValue
			filterOption={false}
			// 绑定防抖函数到onSearch事件上，触发搜索操作时，会调用防抖函数
			onSearch={debounceFetcher}
			onDropdownVisibleChange={visible => {
				// 下拉列表展开时，重新获取选项列表
				if (visible && options.length === 0) {
					debounceFetcher("");
				}
			}}
			// 当加载状态为true时，显示旋转加载图标
			notFoundContent={fetching ? <Spin size="small" /> : null}
			{...props}
			// 将选项列表传递给Select组件进行展示
			options={options}
		/>
	);
}
export default DebounceSelect;
