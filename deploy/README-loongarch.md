# B1 平台龙芯（LoongArch64）Docker 部署指南

## 前提

- 目标机：龙芯虚拟机，已装原生 `linux/loong64` Docker（`docker version` 显示 OS/Arch: linux/loong64）
- 镜像源已配置 `lcr.loongnix.cn`（`docker info` 可见 Registry Mirrors）

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

若 Docker Hub 不可达，在 `.env` 中取消注释龙芯仓库镜像变量。

### 3. 构建镜像（首次较慢，需下载依赖并编译）

```bash
docker compose -f docker-compose.prod.yml build
```

### 4. 启动

```bash
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps    # 全部 Up；b1-minio-init 退出(Exited 0)属正常
```

### 5. 验证

- 浏览器访问 `http://<龙芯机IP>/`
- 登录：教师 `teacher1/123456`，学生 `student1/123456`
- MinIO 控制台（可选排查）：`http://<龙芯机IP>:9001`

## 常见问题

- **backend 起不来**：`docker compose -f docker-compose.prod.yml logs backend`。多为 mysql 未就绪，healthcheck 会自动等待，稍候重试。
- **构建失败**：检查 Docker Hub 连通性。若不可达，在 `.env` 设置 `JDK_IMAGE`/`NODE_IMAGE`/`NGINX_IMAGE` 指向 `cr.loongnix.cn/library/`。
- **上传文件失败**：确认 `b1-minio-init` 已成功建桶（`docker logs b1-minio-init` 显示 `buckets ready`）。
- **首次启动慢**：Flyway 建表 + MySQL 初始化，首次约 1-2 分钟。
- **镜像拉不到**：确认在龙芯机执行（非 x86），且镜像源可达。

## 停止 / 清理

```bash
docker compose -f docker-compose.prod.yml down          # 停止容器，保留数据卷
docker compose -f docker-compose.prod.yml down -v       # 连数据卷一起删（慎用）
```
