#!/bin/bash
set -euo pipefail

# ==================== 配置区 ====================
CA_CN="kelinls CA"
DAYS_VALID=365        # 证书有效期（天）
PASSWORD="changeit"   # PKCS12 密钥库密码
# 获取脚本所在目录的绝对路径
OUTPUT_DIR="$(cd "$(dirname "$0")"; pwd)"

# 服务列表（每个服务需要服务端证书和客户端证书）
SERVICES=(
 "audit-server"
)



generate_server_cert() {
  local service=$1
  echo ">>> 生成 ${service} 服务端证书..."
  # 私钥
  openssl genrsa -out "${OUTPUT_DIR}/${service}.key" 2048
  # CSR
  openssl req -new -key "${OUTPUT_DIR}/${service}.key" \
    -out "${OUTPUT_DIR}/${service}.csr" \
    -subj "/C=CN/ST=Shanghai/L=Shanghai/O=kelinls/CN=${service}"
  # 签发
  openssl x509 -req -in "${OUTPUT_DIR}/${service}.csr" \
    -CA "${OUTPUT_DIR}/ca.crt" -CAkey "${OUTPUT_DIR}/ca.key" -CAcreateserial \
    -out "${OUTPUT_DIR}/${service}.crt" -days ${DAYS_VALID} -sha256
  # 打包 PKCS12（供 Spring Boot 服务端使用）
  openssl pkcs12 -export -in "${OUTPUT_DIR}/${service}.crt" \
    -inkey "${OUTPUT_DIR}/${service}.key" \
    -out "${OUTPUT_DIR}/${service}.p12" \
    -password pass:${PASSWORD} -name "${service}"
  echo "服务端证书已生成: ${OUTPUT_DIR}/${service}.p12"
}

generate_client_cert() {
  local service=$1
  local client_name="${service}-client"
  echo ">>> 生成 ${client_name} 客户端证书..."
  # 私钥
  openssl genrsa -out "${OUTPUT_DIR}/${client_name}.key" 2048
  # CSR
  openssl req -new -key "${OUTPUT_DIR}/${client_name}.key" \
    -out "${OUTPUT_DIR}/${client_name}.csr" \
    -subj "/C=CN/ST=Shanghai/L=Shanghai/O=kelinls/CN=${client_name}"
  # 签发
  openssl x509 -req -in "${OUTPUT_DIR}/${client_name}.csr" \
    -CA "${OUTPUT_DIR}/ca.crt" -CAkey "${OUTPUT_DIR}/ca.key" -CAcreateserial \
    -out "${OUTPUT_DIR}/${client_name}.crt" -days ${DAYS_VALID} -sha256
  # 打包 PKCS12（供 Spring Boot 客户端使用）
  openssl pkcs12 -export -in "${OUTPUT_DIR}/${client_name}.crt" \
    -inkey "${OUTPUT_DIR}/${client_name}.key" \
    -out "${OUTPUT_DIR}/${client_name}.p12" \
    -password pass:${PASSWORD} -name "${client_name}"
  echo "客户端证书已生成: ${OUTPUT_DIR}/${client_name}.p12"
}

generate_truststore() {
  echo ">>> 生成信任库（仅包含根 CA 证书）..."
  rm -f "${OUTPUT_DIR}/truststore.p12"
  keytool -import -trustcacerts -alias ca \
    -file "${OUTPUT_DIR}/ca.crt" \
    -keystore "${OUTPUT_DIR}/truststore.p12" \
    -storetype PKCS12 -storepass ${PASSWORD} -noprompt
  echo "信任库已生成: ${OUTPUT_DIR}/truststore.p12"
}

# ==================== 主流程 ====================
# 确保输出目录存在（其实就是脚本所在目录，一般已存在）
mkdir -p ${OUTPUT_DIR}
cd ${OUTPUT_DIR} || exit




# 2. 为每个服务生成服务端证书和客户端证书
for svc in "${SERVICES[@]}"; do
  generate_server_cert ${svc}
  generate_client_cert ${svc}
done



# 4. 清理中间文件（可选，默认保留）
# echo ">>> 清理中间文件（可选）..."
# rm -f *.csr *.key *.crt   # 取消注释以删除中间文件

echo "所有证书已生成在目录: ${OUTPUT_DIR}"
ls -lh ${OUTPUT_DIR}