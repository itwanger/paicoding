/* eslint-disable prettier/prettier */
import React, { useState } from "react";
import { Upload } from "antd";
import type { RcFile, UploadFile, UploadProps } from "antd/es/upload/interface";
import ImgCrop from "antd-img-crop";

interface ImgCropUploadProps {
	action: string;
	initialFileList?: UploadFile[];
	maxFiles?: number;
	onChange?: (fileList: UploadFile[]) => void;
	onPreview?: (file: UploadFile) => void;
}

const ImgCropUpload: React.FC<ImgCropUploadProps> = ({ 
	action, 
	initialFileList = [], 
	maxFiles = 5, 
	onChange, onPreview 
}) => {

	const [fileList, setFileList] = useState<UploadFile[]>(initialFileList);

	const handleChange: UploadProps["onChange"] = ({ fileList: newFileList }) => {
		setFileList(newFileList);
		onChange && onChange(newFileList);
	};

	const handlePreview = async (file: UploadFile) => {
		if (onPreview) {
			onPreview(file);
		} else {
			let src = file.url as string;
			if (!src) {
				src = await new Promise(resolve => {
					const reader = new FileReader();
					reader.readAsDataURL(file.originFileObj as RcFile);
					reader.onload = () => resolve(reader.result as string);
				});
			}
			const image = new Image();
			image.src = src;
			const imgWindow = window.open(src);
			imgWindow?.document.write(image.outerHTML);
		}
	};

	return (
		<ImgCrop>
			<Upload 
				action={action} 
				listType="picture-card" 
				fileList={fileList} 
				onChange={handleChange} 
				onPreview={handlePreview}>
				{fileList.length < maxFiles && "+ Upload"}
			</Upload>
		</ImgCrop>
	);
};

export default ImgCropUpload;
