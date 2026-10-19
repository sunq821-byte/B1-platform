# 衡迹（B1）——高校实践教学成果评价与管理平台

基于 AI 的高校实践教学成果评价与管理平台。

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
├── docs/              # 产品与设计文档（见下方「文档目录」）
├── deploy/            # 部署说明（含龙芯）
├── Material/          # 演示素材（源码样例、实训报告、MinIO 种子）
└── docker-compose.yml # 基础设施编排
```

## 文档目录

完整索引与阅读顺序见 [docs/README.md](docs/README.md)。

```
docs/
├── README.md                              # 文档索引、阅读顺序与维护原则
├── 01-PRD-产品需求规格说明书.md              # 产品需求（唯一真理源）
├── 02-UI-Design-System.md                 # UI 视觉规范与组件样式
├── 03-Database-Design.md                  # 数据库设计（ER、字段、索引）
├── 04-API-Contract.md                     # REST API 接口契约
├── 05-Deployment-Guide.md                 # 本地开发与生产部署
├── 06-答辩评审问答手册-AI开发与评分体系.md    # 答辩评审 Q&A
├── 07-版本演进说明-比赛提交版与当前版对比.md  # 比赛版 vs 当前版
├── 数据库表设计表格.md                        # 核心表摘要（报告粘贴用）
├── 截图/                                  # 本地，不入库
├── 立项材料/                              # 本地，不入库
└── 软著/                                  # 本地，不入库
```

| 文档 | 用途 |
|------|------|
| `01-PRD` | 功能需求、验收标准、用户操作手册 |
| `02-UI-Design-System` | 配色、布局、组件规范 |
| `03-Database-Design` | 核心表、Flyway 迁移依据 |
| `04-API-Contract` | 前后端联调接口定义 |
| `05-Deployment-Guide` | 环境搭建与上线步骤 |
| `deploy/README-loongarch.md` | 龙芯 LoongArch64 全 Docker 部署 |

> `docs/截图`、`docs/立项材料`、`docs/软著` 仅本地保留，已由 `.gitignore` 排除。

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
- Swagger 文档：http://localhost:8080/swagger-ui.html
- 默认管理员：admin / admin123

开发环境默认开启 Material 种子同步（`b1.material.seed-enabled=true`）。生产 / Docker 配置已显式关闭，避免误清 MinIO 与库内种子数据。

### 4. 启动前端

```powershell
cd D:\Project\B1\B1_Platform
npm install    # 首次运行
npm run dev
```

前端开发服务器：http://127.0.0.1:5173

## 种子账号

首次启动时 Flyway 自动灌入测试数据，包含以下账号：

| 用户名 | 密码 | 真实姓名 | 角色 |
|---|---|---|---|
| `admin` | `admin123` | 系统管理员 | 管理员 |
| `teacher1` | `123456` | 张教授 | 教师 |
| `student1` | `123456` | 李明 | 学生 |
| `student2` | `123456` | 王芳 | 学生（**待教师审核**《图书管理系统》） |
| `student3` | `123456` | 赵强 | 学生（**已驳回**，AI 次数已重置，可重新试交） |
| `student4` | `123456` | 孙丽 | 学生（**待确认提交**《Servlet 会话管理》） |
| `student5` | `123456` | 周杰 | 学生（**AI 次数已用尽**，仍可确认；教师可「还原 AI 次数」） |
| `student6` | `123456` | 吴敏 | 学生（未提交） |
| `student7` | `123456` | 郑浩 | 学生（截止未完成） |
| `student8` | `123456` | 钱雪 | 学生（**待教师审核**《图书管理系统》） |
| `teacher2` | `123456` | 陈副教授 | 教师 |
| `teacher3` | `123456` | 刘讲师 | 教师 |

> 演示素材目录：`Material/`（Flyway 结构/种子迁移 + MinIO 自动同步）。教师复核请用 `teacher1` 登录审核工作台；还原 AI 次数见审核页右上角按钮。

## 开发说明

- 数据库迁移使用 Flyway，新建迁移文件放在 `server/src/main/resources/db/migration/`，命名格式 `V{序号}__{描述}.sql`
- 演示素材优先放在 `Material/`，由开发环境 `MaterialSeedRunner` 同步；避免再新增巨型「重置种子」类 Flyway 迁移
- 后端接口文档自动生成（SpringDoc），启动后访问 `/swagger-ui.html`（OpenAPI JSON：`/v3/api-docs`）
- 前端默认直连后端 API（Vite 代理，`VITE_API_BASE_URL` 留空即可）
- 密钥等敏感配置放在 `server/src/main/resources/application-local.yml`（不提交），生产环境通过操作系统环境变量注入

## 部署

| 场景 | 方式 | 说明 |
|---|---|---|
| Windows / x86 开发 | `docker compose up -d` 起三件套 + 本地 `mvn spring-boot:run` / `npm run dev` | 见上文「本地启动」，可热更新调试 |
| 龙芯（LoongArch64）机器 | 全 Docker：`docker compose -f docker-compose.prod.yml up -d` | 前后端也打进容器，机器无需装 JDK/Node，见 [deploy/README-loongarch.md](deploy/README-loongarch.md) |

> ⚠️ `docker-compose.prod.yml` 使用 loong64 专用镜像，**不能在 x86 Windows 上运行**；反之 x86 的全 Docker 镜像也不能在龙芯上运行。两套编排各自对应目标架构。
