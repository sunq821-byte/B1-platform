# B1 智慧实训平台

基于大模型的软件实训教学检查评价与报表系统。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus |
| 后端 | Spring Boot 3.5 + MyBatis Plus + Sa-Token + JDK 21 |
| 数据 | MySQL 8.0 + Redis 7 + MinIO |
| AI | OpenAI Compatible / DeepSeek / Qwen |

## 项目结构

```
B1/
├── B1_Platform/       # 前端（Vue 3 + Vite）
├── server/            # 后端（Spring Boot）
├── docs/              # 设计文档
├── prototypes/        # 原型设计
└── docker-compose.yml # 基础设施编排
```

## 本地启动（Windows / x86 开发）

> 适用于 Windows 或 x86 macOS/Linux 开发机。基础设施用 Docker 跑（`docker-compose.yml` 是标准 x86 镜像），前后端跑源码方便热更新调试。
> 龙芯（LoongArch64）机器请改用全 Docker 部署，见文末「部署」。

### 1. 启动基础设施

确保 Docker Desktop 已运行，然后启动 MySQL、Redis、MinIO：

```powershell
cd D:\Project\B1
docker compose up -d
docker compose ps    # 确认 b1-mysql / b1-redis / b1-minio 均为 Up
```

端口映射：

| 服务 | 端口 |
|---|---|
| MySQL 8.0 | 3306 |
| Redis 7 | 6379 |
| MinIO API | 9000 |
| MinIO Console | 9001 |

### 2. 配置密钥（首次运行必做）

AI 密钥不入库，需在本地创建 `server/src/main/resources/application-local.yml`（该文件已被 `.gitignore` 忽略，不会提交）：

```yaml
DEEPSEEK_API_KEY: sk-你的-deepseek-密钥
QWEN_API_KEY: sk-你的-qwen-密钥
```

`application-dev.yml` 通过 `spring.config.import` 自动加载此文件。若缺失该文件应用仍可启动（AI 功能不可用）。

### 3. 启动后端

```powershell
cd D:\Project\B1\server
$env:JAVA_HOME = "D:\jdk-21"
mvn spring-boot:run
```

- API 地址：http://localhost:8080
- Swagger 文档：http://localhost:8080/doc.html
- 默认管理员：admin / admin123

### 4. 启动前端

```powershell
cd D:\Project\B1\B1_Platform
npm install    # 首次运行
npm run dev
```

前端开发服务器：http://localhost:3000

## 种子账号

首次启动时 Flyway 自动灌入测试数据，包含以下账号：

| 用户名 | 密码 | 真实姓名 | 角色 |
|---|---|---|---|
| `admin` | `admin123` | 系统管理员 | 管理员 |
| `teacher1` | `123456` | 张教授 | 教师 |
| `student1` | `123456` | 李明 | 学生 |
| `student2` | `123456` | 王芳 | 学生 |

> 若需新增/修改账号，编辑 `server/src/main/resources/db/migration/V4__test_accounts.sql` 后重新启动即可。

## 开发说明

- 数据库迁移使用 Flyway，新建迁移文件放在 `server/src/main/resources/db/migration/`，命名格式 `V{序号}__{描述}.sql`
- 后端接口文档自动生成，启动后访问 `/doc.html`
- 前端 Mock 数据在 `B1_Platform/src/mock/` 目录
- 密钥等敏感配置放在 `server/src/main/resources/application-local.yml`（不提交），生产环境通过操作系统环境变量注入

## 部署

| 场景 | 方式 | 说明 |
|---|---|---|
| Windows / x86 开发 | `docker compose up -d` 起三件套 + 本地 `mvn spring-boot:run` / `npm run dev` | 见上文「本地启动」，可热更新调试 |
| 龙芯（LoongArch64）机器 | 全 Docker：`docker compose -f docker-compose.prod.yml up -d` | 前后端也打进容器，机器无需装 JDK/Node，见 [deploy/README-loongarch.md](deploy/README-loongarch.md) |

> ⚠️ `docker-compose.prod.yml` 使用 loong64 专用镜像，**不能在 x86 Windows 上运行**；反之 x86 的全 Docker 镜像也不能在龙芯上运行。两套编排各自对应目标架构。
