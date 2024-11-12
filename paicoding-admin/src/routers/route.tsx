/* eslint-disable prettier/prettier */
import {
	BarsOutlined,
	CalendarOutlined,
	FileAddOutlined,
	FilePptOutlined,
	FileTextOutlined,
	LineChartOutlined,
	ReadOutlined,
	SettingOutlined,
	SmileOutlined,
	TagsOutlined,
	UserOutlined
} from "@ant-design/icons";

export const currentMenuList = [
	{ key: "/statistics/index", icon: <LineChartOutlined />, children: undefined, label: "数据统计" },
	{ key: "/config/index", icon: <CalendarOutlined />, children: undefined, label: "运营配置" },
	{ key: "/global/index", icon: <SettingOutlined />, children: undefined, label: "全局配置" },
	{ key: "/category/index", icon: <BarsOutlined />, children: undefined, label: "分类管理" },
	{ key: "/tag/index", icon: <TagsOutlined />, children: undefined, label: "标签管理" },
	{
		key: "/article",
		icon: <ReadOutlined />,
		children: [
			{ key: "/article/list/index", icon: <FilePptOutlined />, children: undefined, label: "文章列表" },
			{ key: "/article/edit/index", icon: <FileAddOutlined />, children: undefined, label: "文章编辑" }
		],
		label: "文章管理"
	},
	{
		key: "/author",
		icon: <UserOutlined />,
		children: [
			{ key: "/author/whitelist/index", icon: <SmileOutlined />, children: undefined, label: "作者白名单" },
			{ key: "/author/zsxqlist/index", icon: <SmileOutlined />, children: undefined, label: "星球白名单" }
		],
		label: "用户管理"
	},
	{
		key: "/column",
		icon: <ReadOutlined />,
		children: [
			{ key: "/column/setting/index", icon: <FilePptOutlined />, children: undefined, label: "专栏配置" },
			{ key: "/column/article/index", icon: <FileAddOutlined />, children: undefined, label: "教程配置" }
		],
		label: "教程管理"
	}
];
