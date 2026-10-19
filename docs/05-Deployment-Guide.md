# 部署指南

## 衡迹——高校实践教学成果评价与管理平台

| 文档信息 | |
|---|---|
| 文档版本 | v2.0 |
| 创建日期 | 2026-07-18 |
| 最后更新 | 2026-08-23 |
| 文档状态 | 正式发布 |
| 适用版本 | 衡迹 V1.0 |
| 前置文档 | `docs/01-PRD-产品需求规格说明书.md` |

---

## 1 系统概述

衡迹采用前后端分离架构，支持 **本地开发** 与 **Docker 容器化生产部署**（Linux x86_64 / amd64）。

| 组件 | 技术 | 默认端口 |
|---|---|---|
| 前端（SPA） | Vue 3 + TypeScript + Vite + Nginx | 80 |
| 后端 API | Spring Boot 3 + JDK 21 | 8080 |
| 数据库 | MySQL 8.0 | 3306 |
| 缓存 | Redis 7 | 6379 |
| 对象存储 | MinIO | 9000（API）、9001（Console） |

---

## 2 部署模式

| 场景 | 目标环境 | 方式 |
|---|---|---|
| 本地开发 | Windows / macOS / Linux | Docker 基础设施 + 本地源码运行 |
| 生产部署 | Linux（x86_64） | Docker Compose 全容器化 |

---

## 3 源码与产物

### 3.1 获取源码

```bash
git clone <repository-url>
cd B1
```

### 3.2 预构建产物（可选）

若目标机无法安装 JDK/Maven/Node，可在开发机预构建后传输：

| 产物 | 构建命令 | 产出路径 |
|---|---|---|
| 后端 Fat Jar | `cd server && mvn package -DskipTests` | `server/target/b1-platform-1.0.0.jar` |
| 前端静态资源 | `cd B1_Platform && npm install && npm run build` | `B1_Platform/dist/` |

---

## 4 本地开发部署

### 4.1 环境要求

| 依赖 | 版本要求 |
|---|---|
| JDK | 21+ |
| Maven | 3.6+ |
| Node.js | 20+ |
| Docker Desktop / Docker Engine | 24+ |
| Git | 2.x |

### 4.2 启动基础设施

```bash
docker compose up -d
docker compose ps    # 确认 mysql / redis / minio 均为 Up
```

### 4.3 启动后端

```bash
cd server
mvn spring-boot:run
```

首次启动 Flyway 自动建表并灌入种子数据。

- API：`http://localhost:8080`
- Swagger：`http://localhost:8080/doc.html`

### 4.4 启动前端

```bash
cd B1_Platform
npm install       # 首次运行
npm run dev
```

- 开发地址：`http://127.0.0.1:5173`
- Vite Proxy 将 `/api` 反代到后端

### 4.5 测试账号

> 详见 PRD 第 12.2 节。密码均为 `123456`（`admin` 账号以实际种子数据为准）。

| 用户名 | 角色 |
|---|---|
| admin | 管理员 |
| teacher1 | 教师 |
| student1 | 学生 |
| student2 | 学生 |

---

## 5 生产环境部署（Docker Compose）

### 5.1 环境要求

| 依赖 | 版本 |
|---|---|
| Docker Engine | 24+ |
| Docker Compose | v2.x |
| 操作系统 | Linux x86_64（推荐 Ubuntu 20.04+） |

### 5.2 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，至少配置：
# MYSQL_ROOT_PASSWORD、MINIO 凭证、DEEPSEEK_API_KEY（启用 AI 时）
```

### 5.3 构建与启动

```bash
# 在开发机或 CI 构建产物后，将 jar 与 dist 置于对应目录
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps
```

### 5.4 验证

| 服务 | 地址 |
|---|---|
| 前端 | `http://<服务器IP>/` |
| 后端 API | `http://<服务器IP>:8080` |
| MinIO 控制台 | `http://<服务器IP>:9001` |

### 5.5 停止与清理

```bash
docker compose -f docker-compose.prod.yml down          # 停止，保留数据
docker compose -f docker-compose.prod.yml down -v       # 停止并删除数据卷
```

---

## 6 配置文件说明

### 6.1 环境变量（.env）

| 变量 | 说明 |
|---|---|
| MYSQL_ROOT_PASSWORD | MySQL root 密码 |
| MINIO_ROOT_USER / MINIO_ROOT_PASSWORD | MinIO 管理员凭证 |
| DEEPSEEK_API_KEY | DeepSeek API 密钥（留空则 AI 不可用） |
| QWEN_API_KEY | 通义千问 API 密钥（多模态场景） |

### 6.2 Spring 配置文件

| 文件 | 环境 | 说明 |
|---|---|---|
| application.yml | 通用 | 全局配置 |
| application-dev.yml | 本地开发 | localhost 连接基础设施 |
| application-docker.yml | 容器化 | 容器网络内服务名连接 |
| application-prod.yml | 生产 | 生产环境覆盖项 |

---

## 7 数据库迁移

Flyway 迁移脚本位于 `server/src/main/resources/db/migration/`，应用启动时自动执行。

| 版本 | 说明 |
|---|---|
| V1 | 初始化表结构（26 张表） |
| V2 | 初始数据（角色 + admin + 系统配置） |
| V3 | 测试数据（课程 + 任务 + 评价标准） |
| V4 | 测试账号（teacher1 / student1 / student2） |
| V5 | 默认标准与评分规则 |

---

## 8 常见问题

| 问题 | 排查 |
|---|---|
| 容器未全部启动 | `docker compose logs <服务名>` |
| MySQL 健康检查失败 | 等待 10–30 秒，首次初始化较慢 |
| 后端连接 MySQL 失败 | 确认 docker profile 激活，检查密码配置 |
| AI 分析不可用 | 检查 `DEEPSEEK_API_KEY` / `QWEN_API_KEY` |
| 端口冲突 | 修改 compose 端口映射或释放占用端口 |
| 上传文件失败 | 检查 MinIO 服务与 bucket 初始化日志 |

---

> **文档结束** | 版本 v2.0 | 2026-08-23
