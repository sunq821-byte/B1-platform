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

## 本地启动

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

### 2. 启动后端

```powershell
cd D:\Project\B1\server
$env:JAVA_HOME = "D:\jdk-21"
mvn spring-boot:run
```

- API 地址：http://localhost:8080
- Swagger 文档：http://localhost:8080/doc.html
- 默认管理员：admin / admin123

### 3. 启动前端

```powershell
cd D:\Project\B1\B1_Platform
npm install    # 首次运行
npm run dev
```

前端开发服务器：http://localhost:5173

## 开发说明

- 数据库迁移使用 Flyway，新建迁移文件放在 `server/src/main/resources/db/migration/`，命名格式 `V{序号}__{描述}.sql`
- 后端接口文档自动生成，启动后访问 `/doc.html`
- 前端 Mock 数据在 `B1_Platform/src/mock/` 目录
