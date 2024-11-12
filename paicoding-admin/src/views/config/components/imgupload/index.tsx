/* eslint-disable react/prop-types */
// 这是一个上传图片的组件，使用的是antd的Upload组件

import { FC } from "react";
import { UploadOutlined } from "@ant-design/icons";
import { Button, message, Upload } from "antd";

import { uploadImgApi } from "@/api/modules/common";
import { getCompleteUrl } from "@/utils/util";

interface IProps {
	coverList: any[];
	setCoverList: (e: any[]) => void;
	handleChange: (e: any) => void;
}

const ImgUpload: FC<IProps> = ({ coverList, setCoverList, handleChange }) => {
	const customCoverUpload = async (options: any) => {
		const { onSuccess, onProgress, onError, file } = options;
		console.log("上传图片", options);
		// 限制图片大小，不超过 5M
		if (file.size > 5 * 1024 * 1024) {
			onError("图片大小不能超过 5M");
			return;
		}

		const formData = new FormData();
		formData.append("image", file);

		const { status, result } = await uploadImgApi(formData);
		const { code, msg } = status || {};
		const { imagePath } = result || {};
		console.log("上传图片", status, result, code, msg, imagePath);

		if (code === 0) {
			console.log("上传图片成功，回调 onsuccess", imagePath);
			onSuccess(imagePath);
			// 把 data 的值赋给 form 的 bannerUrl，传递给后端
			handleChange({ bannerUrl: imagePath });
			const coverUrl = getCompleteUrl(imagePath);
			console.log("上传封面 onchange done", coverUrl);
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
			console.log("上传封面 onchange done", coverList);
		} else {
			onError("上传失败");
		}
	};

	return (
		<Upload
			customRequest={customCoverUpload}
			multiple={false}
			listType="picture"
			maxCount={1}
			fileList={[...coverList]}
			accept="image/*"
			onRemove={() => {
				console.log("删除封面");
				// 删除封面的时候，清空 cover
				handleChange({ bannerUrl: "" });
				// 清空 coverList
				setCoverList([]);
			}}
			onChange={info => {
				// clear 的时候记得清空 cover
				// submit 的时候要判断 cover 是否为空，空的话提示用户上传
				const { status, name, response } = info.file;
				console.log("上传封面 onchange info", status, name, response);

				if (status !== "uploading") {
					console.log("上传封面 onchange !uploading");
				}
				if (status === "done") {
					message.success(`${name} 封面上传成功.`);
				} else if (status === "error") {
					message.error(`封面上传失败，原因：${info.file.error}`);
				}
			}}
		>
			<Button icon={<UploadOutlined />}>Upload</Button>
		</Upload>
	);
};
export default ImgUpload;
