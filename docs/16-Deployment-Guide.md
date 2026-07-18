# 软件安装包及部署文档

## 基于大模型的软件实训教学检查评价与报表系统

| 文档信息 | |
|---|---|
| 文档版本 | v1.0 |
| 创建日期 | 2026-07-18 |
| 文档状态 | 正式发布 |
| 适用版本 | B1 Platform v1.0.0-SNAPSHOT |

---

## 1 系统概述

B1 智慧实训平台采用前后端分离架构，支持 x86 开发环境与 LoongArch64（龙芯/银河麒麟）生产环境双模式部署。

| 组件 | 技术 | 端口 |
|---|---|---|
| 前端（SPA） | Vue 3 + TypeScript + Vite + Nginx | 80 |
| 后端 API | Spring Boot 3.5 + JDK 21 | 8080 |
| 数据库 | MySQL 8.0 | 3306 |
| 缓存 | Redis 7 | 6379 |
| 对象存储 | MinIO | 9000 (API), 9001 (Console) |

---

## 2 部署模式总览

| 场景 | 目标架构 | 方式 |
|---|---|---|
| 本地开发 | Windows / x86 Linux | Docker 基础设施 + 本地源码运行 |
| 生产部署 | 龙芯 LoongArch64 | 全 Docker 容器化 |

---

## 3 安装包获取

### 3.1 源码获取

```bash
git clone git@github.com:sunq821-byte/B1-platform.git
cd B1
```

### 3.2 预构建产物（可选）

若目标机无法安装 JDK/Maven/Node，可在开发机预构建后传输：

| 产物 | 构建命令 | 产出路径 |
|---|---|---|
| 后端 Fat Jar | `cd server && mvn package -DskipTests` | `server/target/b1-platform-1.0.0-SNAPSHOT.jar` |
| 前端静态资源 | `cd B1_Platform && npm install && npm run build` | `B1_Platform/dist/` |

---

## 4 x86 本地开发部署（Windows / Linux）

### 4.1 环境要求

| 依赖 | 版本要求 | 说明 |
|---|---|---|
| JDK | 21+ | 后端编译与运行 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 20+ | 前端构建与开发服务器 |
| Docker Desktop | 24+ | 基础设施容器运行 |
| Git | 2.x | 源码管理 |

### 4.2 启动基础设施

```bash
docker compose up -d
docker compose ps    # 确认 b1-mysql / b1-redis / b1-minio 均为 Up
```

### 4.3 启动后端

```bash
cd server
mvn spring-boot:run
```

首次启动 Flyway 自动建表并灌入种子数据。API 地址：`http://localhost:8080`，Swagger 文档：`http://localhost:8080/doc.html`。

### 4.4 启动前端

```bash
cd B1_Platform
npm install       # 首次运行
npm run dev
```

开发服务器：`http://localhost:3000`，内置 Vite Proxy 将 `/api` 反代到后端。

### 4.5 默认账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| teacher1 | 123456 | 教师 |
| student1 | 123456 | 学生 |
| student2 | 123456 | 学生 |

---

## 5 龙芯 LoongArch64 生产部署

### 5.1 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| Docker Engine | 24+ (linux/loong64) | 原生龙芯 Docker |
| Docker Compose | v2.x | 容器编排 |
| 镜像仓库 | lcr.loongnix.cn 可达 | 龙芯官方容器仓库 |

### 5.2 配置

```bash
cp .env.example .env
vi .env    # 可选填入 DEEPSEEK_API_KEY / QWEN_API_KEY
```

### 5.3 构建产物（在开发机完成）

```bash
# Windows 开发机
cd server && mvn package -DskipTests
cd B1_Platform && npm install && npm run build
```

将 `server/target/*.jar` 和 `B1_Platform/dist/` 传输至目标机项目对应目录。

### 5.4 构建镜像

```bash
docker-compose -f docker-compose.prod.yml build
```

### 5.5 启动

```bash
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.prod.yml ps
```

### 5.6 验证

- 前端访问：`http://<目标机IP>/`
- MinIO 控制台：`http://<目标机IP>:9001`

### 5.7 停止与清理

```bash
docker-compose -f docker-compose.prod.yml down          # 停止，保留数据
docker-compose -f docker-compose.prod.yml down -v       # 连数据卷一起删除
```

---

## 6 配置文件说明

### 6.1 环境变量（.env）

| 变量 | 默认值 | 说明 |
|---|---|---|
| MYSQL_ROOT_PASSWORD | root | MySQL root 密码 |
| MINIO_ROOT_USER | minioadmin | MinIO 管理员用户 |
| MINIO_ROOT_PASSWORD | minioadmin | MinIO 管理员密码 |
| DEEPSEEK_API_KEY | (空) | DeepSeek API 密钥，留空则 AI 不可用 |
| QWEN_API_KEY | (空) | 通义千问 API 密钥，留空则 AI 不可用 |
| JDK_IMAGE | lcr.loongnix.cn/library/openjdk:21 | 后端基础镜像 |
| NGINX_IMAGE | lcr.loongnix.cn/library/nginx:latest | 前端基础镜像 |

### 6.2 Spring 配置文件

| 文件 | 环境 | 说明 |
|---|---|---|
| application.yml | 通用 | 全局配置 |
| application-dev.yml | 本地开发 | localhost 连接 MySQL/Redis/MinIO |
| application-docker.yml | 容器化 | 容器网络内服务名连接 |
| application-local.yml | 本地密钥 | gitignore，存放 API Key |

---

## 7 数据库

### 7.1 迁移管理

使用 Flyway 进行数据库版本管理，迁移脚本位于 `server/src/main/resources/db/migration/`：

| 版本 | 说明 |
|---|---|
| V1 | 初始化表结构（26 张表） |
| V2 | 初始数据（角色 + admin 用户 + 系统配置） |
| V3 | 测试数据（课程 + 任务 + 评价标准） |
| V4 | 测试账号（teacher1 / student1 / student2） |
| V5 | 默认标准与评分规则 |

### 7.2 自动执行

应用启动时 Flyway 自动检测并执行未应用的迁移脚本，无需手动建库建表。

---

## 8 常见问题

| 问题 | 排查 |
|---|---|
| 容器未全部启动 | `docker-compose logs <服务名>` 查看日志 |
| MySQL 健康检查失败 | 等待 10-30 秒，首次初始化较慢 |
| 后端连接 MySQL 失败 | 确认 docker profile 激活，检查 DB_PASSWORD |
| AI 评分不可用 | 检查 API Key 是否正确填写 |
| 端口冲突 | 修改 compose 中端口映射或停用占用程序 |
| 镜像拉取失败 | 龙芯环境确认 lcr.loongnix.cn 可达 |
| 上传文件失败 | `docker logs b1-minio-init` 确认建桶成功 |
