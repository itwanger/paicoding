#!/bin/bash
# deploy-vod-cert.sh
# 将 acme.sh 签发的证书自动部署到阿里云 VOD 域名
# 用法：直接执行，或作为 acme.sh 的 --renew-hook 使用

DOMAIN="vod.paicoding.com"
CERT_NAME="vod-paicoding-$(date +%Y%m%d%H%M%S)"
REGION="cn-shanghai"

# 证书路径（paicoding.com 的 ECC 通配符证书）
CERT_DIR="$HOME/.acme.sh/paicoding.com_ecc"
CERT_FILE="$CERT_DIR/fullchain.cer"
KEY_FILE="$CERT_DIR/paicoding.com.key"

# 检查证书文件是否存在
if [ ! -f "$CERT_FILE" ] || [ ! -f "$KEY_FILE" ]; then
    echo "[ERROR] 证书文件不存在："
    echo "  CERT: $CERT_FILE"
    echo "  KEY:  $KEY_FILE"
    exit 1
fi

echo "[INFO] 正在部署证书到 VOD 域名: $DOMAIN"
echo "[INFO] 证书名称: $CERT_NAME"

# 读取证书和私钥内容
SSL_PUB=$(cat "$CERT_FILE")
SSL_PRI=$(cat "$KEY_FILE")

# 调用阿里云 VOD API 设置证书
aliyun vod SetVodDomainCertificate \
    --region "$REGION" \
    --DomainName "$DOMAIN" \
    --CertName "$CERT_NAME" \
    --SSLProtocol "on" \
    --SSLPub "$SSL_PUB" \
    --SSLPri "$SSL_PRI"

if [ $? -eq 0 ]; then
    echo "[SUCCESS] 证书部署成功！"
    echo ""
    echo "[INFO] 正在验证部署结果..."
    sleep 5
    # 查询部署后的证书信息
    aliyun vod DescribeVodDomainCertificateInfo \
        --DomainName "$DOMAIN" \
        --region "$REGION"
else
    echo "[ERROR] 证书部署失败！"
    exit 1
fi
