import { Ref, useImperativeHandle, useState } from "react";
import { Avatar, message, Modal } from "antd";

interface Props {
	innerRef: Ref<{ showModal: (params: any) => void } | undefined>;
}

const InfoModal = (props: Props) => {
	const [modalVisible, setModalVisible] = useState(false);
	const [userInfo, setUserInfo] = useState<Record<string, any>>({}); // 新增状态来存储用户信息

	useImperativeHandle(props.innerRef, () => ({
		showModal
	}));

	const showModal = (params: Record<string, any>) => {
		console.log(params);
		// 把params 显示到 model 中
		setUserInfo(params);
		setModalVisible(true);
	};

	const handleCancel = () => {
		setModalVisible(false);
	};
	return (
		<Modal title="个人信息" footer={null} open={modalVisible} onCancel={handleCancel} destroyOnClose={true}>
			<div className="info-modal">
				<div className="info-modal-item">
					<span className="info-modal-item-label">头像：</span>
					<span className="info-modal-item-value">
						<Avatar src={userInfo.photo} />
					</span>
				</div>
				<div className="info-modal-item">
					<span className="info-modal-item-label">用户名：</span>
					<span className="info-modal-item-value">{userInfo.userName}</span>
				</div>
				<div className="info-modal-item">
					<span className="info-modal-item-label">角色：</span>
					<span className="info-modal-item-value">{userInfo.role}</span>
				</div>
				<div className="info-modal-item">
					<span className="info-modal-item-label">个人简介：</span>
					<span className="info-modal-item-value">{userInfo.profile}</span>
				</div>
			</div>
		</Modal>
	);
};
export default InfoModal;
