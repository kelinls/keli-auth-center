#!/bin/bash

# ==================== 配置区 ====================
CA_CN="kelinls CA"
DAYS_VALID=365        # 证书有效期（天）
PASSWORD="changeit"   # PKCS12 密钥库密码
# 获取脚本所在目录的绝对路径
OUTPUT_DIR="$(cd "$(dirname "$0")"; pwd)"

# 服务列表（每个服务需要服务端证书和客户端证书）
SERVICES=(
  "login-server"
  "auth-server"
  "token-server"
  "userinfo-server"
  "session-server"
  "client-server"
)

# ==================== 函数定义 ====================
generate_ca() {
  echo ">>> 生成根 CA 证书..."
  openssl genrsa -out "${OUTPUT_DIR}/ca.key" 2048
  openssl req -x509 -new -nodes -key "${OUTPUT_DIR}/ca.key" -sha256 -days 3650 \
    -out "${OUTPUT_DIR}/ca.crt" \
    -subj "/C=CN/ST=Shanghai/L=Shanghai/O=kelinls/CN=${CA_CN}"
  echo "根 CA 证书已生成: ${OUTPUT_DIR}/ca.crt"
}

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

# 1. 生成根 CA
generate_ca

# 2. 为每个服务生成服务端证书和客户端证书
for svc in "${SERVICES[@]}"; do
  generate_server_cert ${svc}
  generate_client_cert ${svc}
done

# 3. 生成统一的信任库（所有服务共用）
generate_truststore

# 4. 清理中间文件（可选，默认保留）
# echo ">>> 清理中间文件（可选）..."
# rm -f *.csr *.key *.crt   # 取消注释以删除中间文件

echo "所有证书已生成在目录: ${OUTPUT_DIR}"
ls -lh ${OUTPUT_DIR}