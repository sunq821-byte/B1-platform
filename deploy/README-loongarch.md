# B1 平台龙芯（LoongArch64）Docker 部署指南

## 前提

- 目标机：龙芯虚拟机，已装原生 `linux/loong64` Docker
- **网络受限环境**：Docker Registry 不可达，需离线导入基础镜像

## 步骤

### 1. 获取基础镜像（在可联网的 loong64 机器上）

```bash
docker pull loongarch64/openjdk:21
docker pull loongarch64/nginx
docker save -o openjdk21-loong64.tar loongarch64/openjdk:21
docker save -o nginx-loong64.tar loongarch64/nginx
```

> 注意：基础镜像必须是 loong64 架构，x86 机器导出的镜像无法在龙芯上运行。

### 2. 传输镜像到目标机并导入

```bash
docker load -i openjdk21-loong64.tar
docker load -i nginx-loong64.tar
docker images | grep loongarch64    # 确认存在
```

### 3. Clone 仓库并配置

```bash
git clone <repo-url> B1
cd B1
cp .env.example .env
vi .env          # 按需填 DEEPSEEK_API_KEY / QWEN_API_KEY
```

### 4. 安装构建工具（JDK 21 + Maven + Node 20）

```bash
# JDK 21
sudo yum install java-21-openjdk-devel

# Maven（yum 版本可能较旧，也可手动下载）
sudo yum install maven

# Node 20（推荐 nvm 或直接装）
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo yum install nodejs

# 验证
java -version && mvn -version && node -v && npm -v
```

### 5. 构建产物

```bash
# 后端 fat jar
cd server
mvn package -DskipTests

# 前端静态资源
cd ../B1_Platform
npm install
npm run build
```

### 6. 构建 Docker 镜像并启动

```bash
cd ..
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.prod.yml ps
```

### 7. 验证

- 浏览器访问 `http://<龙芯机IP>/`
- 登录：教师 `teacher1/123456`，学生 `student1/123456`
- MinIO 控制台：`http://<龙芯机IP>:9001`

## 常见问题

- **"failed to do request" / timeout**：Docker 镜像源不可达，确认已离线导入基础镜像
- **"target/*.jar not found"**：未执行 `mvn package`，先在 server 目录构建
- **"dist not found"**：未执行 `npm run build`，先在 B1_Platform 目录构建
- **backend 起不来**：`docker-compose -f docker-compose.prod.yml logs backend`，多为 mysql 未就绪
- **上传文件失败**：确认 `b1-minio-init` 已成功建桶

## 停止 / 清理

```bash
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml down -v   # 连数据卷一起删
```
