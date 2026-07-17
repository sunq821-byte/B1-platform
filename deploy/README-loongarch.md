# B1 平台龙芯（LoongArch64）Docker 部署指南

## 前提

- 目标机：龙芯虚拟机，已装原生 `linux/loong64` Docker
- 镜像仓库 `lcr.loongnix.cn` 可达
- **Docker Compose V2 已安装**（`docker compose` 或 `docker-compose`）

## 步骤

### 1. Clone 仓库

```bash
git clone <repo-url> B1
cd B1
```

### 2. 配置密钥（可选）

```bash
cp .env.example .env
vi .env          # 按需填 DEEPSEEK_API_KEY / QWEN_API_KEY（留空则 AI 不可用）
```

### 3. 构建镜像

首先需要在**开发机（Windows/x86）**上预构建产物并传到 VM：

```powershell
# Windows 开发机
cd server && mvn package -DskipTests         # → target/b1-platform-1.0.0-SNAPSHOT.jar
cd B1_Platform && npm install && npm run build  # → dist/
```

将 jar 和 dist 传到 VM 对应位置后：

```bash
cd B1
docker-compose -f docker-compose.prod.yml build
```

### 4. 启动

```bash
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.prod.yml ps
```

### 5. 验证

- 浏览器访问 `http://<龙芯机IP>/`
- 登录：教师 `teacher1/123456`，学生 `student1/123456`

## 常见问题

- **镜像拉取失败**：确认 `lcr.loongnix.cn` 可达（`ping lcr.loongnix.cn`），可在 `.env` 中覆盖 `JDK_IMAGE` / `NGINX_IMAGE`
- **backend 起不来**：`docker-compose logs backend`，多为 mysql 未就绪，healthcheck 自动等待
- **上传文件失败**：`docker logs b1-minio-init` 确认已建桶
- **首次启动慢**：Flyway 建表 1-2 分钟

## 停止 / 清理

```bash
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml down -v    # 连数据卷一起删
```
