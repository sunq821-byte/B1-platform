# B1 平台龙芯（LoongArch）Docker 化部署设计

## 背景与目标

将 B1 智慧实训平台部署到**龙芯（LoongArch64）虚拟机**上运行。目标机器已具备：

- 原生 `linux/loong64` Docker 24.0.9（Client/Server 均为 loong64）
- 已安装 JDK 21

**核心结论**：因目标机为原生 loong64 Docker，只要在该机器上 `docker build`，产物即为 loong64 镜像，不存在跨架构问题。无需交叉编译、无需 QEMU。

**动机**：队友的虚拟机上未配置任何开发环境（无 MySQL/Redis/MinIO/Maven/Node）。全部用 Docker 跑，队友只需 `docker compose up` 即可拉起完整平台，免去环境搭建。

**当前缺口**：项目现有 `docker-compose.yml` 只编排了 MySQL/Redis/MinIO 三个基础设施，前端与后端**尚未容器化**。本设计补齐容器化并提供龙芯一键部署。

## 基础镜像可用性（已在目标机验证）

镜像源配置：`lcr.loongnix.cn` / `docker.mirrors.ustc.edu.cn` / `mirror.baidubce.com`。

规律：docker.io 的 `:latest` tag 可经镜像源拉到 loong64 版；带具体版本号的 tag（如 `mysql:8.0`）镜像源未缓存，会回退到被墙的 Docker Hub 而超时。

| 镜像 | 状态 | 用途 |
|---|---|---|
| `openjdk:21` | ✅ 已拉取 | 后端运行时 |
| `nginx:latest` | ✅ 已拉取 | 前端静态托管 + API 反代 |
| `mysql:latest`（= **8.4.6 loongarch64**，8.x LTS） | ✅ 已拉取 | 数据库 |
| `redis:latest` | ✅ 已拉取 | 缓存 / 会话 |
| `cr.loongnix.cn/minio/minio:latest` | ✅ 已拉取 | 对象存储 |

关键注意：
- MySQL 为 **8.4.6**（非 9.x），`mysql_native_password` 仍支持（默认关闭，需 `--mysql-native-password=ON` 显式开启）。Flyway 兼容。
- **不可使用 Alpine 变体**（如 `redis:7-alpine`）：Alpine 基于 musl，龙芯支持极少。统一用 debian 基底的 `:latest`。
- MinIO 使用龙芯官方仓库路径 `cr.loongnix.cn/minio/minio:latest`。

## 架构

5 个常驻容器 + 1 个一次性初始化容器，由单个 `docker-compose.prod.yml` 编排，同一 bridge 网络 `b1-net`：

```
docker-compose.prod.yml  (network: b1-net)
├─ frontend   nginx:latest                    :80        托管 dist/，反代 /api → backend:8080
├─ backend    openjdk:21 + fat jar            :8080      SPRING_PROFILES_ACTIVE=docker
├─ mysql      mysql:8.4.6                      :3306      数据卷持久化，--mysql-native-password=ON
├─ redis      redis:latest                     :6379      数据卷持久化
├─ minio      cr.loongnix.cn/minio/minio       :9000/9001 数据卷持久化
└─ minio-init cr.loongnix.cn/minio/mc          （一次性）  创建 submissions/reports 桶后退出
```

对外暴露端口：前端 `80`（可按需调整）、MinIO 控制台 `9001`（便于排查）。MySQL/Redis/backend 端口可仅在内网暴露。

### 数据流

浏览器 → `frontend:80`（nginx）
- 静态资源：nginx 直接返回 `dist/` 内文件；未命中路由回退到 `index.html`（SPA）
- `/api/*`：nginx 反代到 `backend:8080`
- backend → `mysql:3306` / `redis:6379` / `minio:9000`（均用 compose 服务名解析）

## 构建策略：预构建产物

在 **Windows 开发机**（本会话已验证 mvn/npm 可用）产出与架构无关的产物，再传龙芯机由 Docker COPY 进 loong64 基础镜像。绕开在龙芯上安装 maven/node 及联网拉依赖，构建快、成功率高。

- 后端：`mvn package -DskipTests` → `server/target/b1-platform-1.0.0-SNAPSHOT.jar`（Spring Boot 可执行 fat jar，约 100MB，纯 JVM 字节码，跨架构通用）
- 前端：`npm run build` → `B1_Platform/dist/`（静态 HTML/JS/CSS，跨架构通用）

Dockerfile 仅做 `FROM <loong64 基础镜像>` + `COPY 产物`，不在镜像内编译。

## 新增文件清单

| 文件 | 作用 |
|---|---|
| `server/Dockerfile` | `FROM openjdk:21`，COPY jar，`ENTRYPOINT ["java","-jar","/app/app.jar"]` |
| `server/.dockerignore` | 排除 target 下除目标 jar 外的内容、源码等 |
| `B1_Platform/Dockerfile` | `FROM nginx:latest`，COPY `dist/` 与 `nginx.conf` |
| `B1_Platform/nginx.conf` | SPA 路由回退（try_files）+ `/api` 反代 `backend:8080` |
| `B1_Platform/.dockerignore` | 排除 node_modules、src 等 |
| `server/src/main/resources/application-docker.yml` | 容器内配置：服务名连接、MinIO `secure:false` |
| `docker-compose.prod.yml` | 5+1 容器编排 |
| `.env.example` | 密钥/凭据模板（提交到 git） |
| `.gitignore`（修改） | 追加 `.env` 忽略规则 |
| `deploy/README-loongarch.md` | 龙芯部署步骤文档 |

## 配置适配（代码零改动，仅新增配置文件）

### 新增 `application-docker.yml`

复用现有 `application-prod.yml` 的环境变量占位思路，但针对容器网络与内网 MinIO 定制，**不改动 `application-prod.yml`**（保留给真实生产 HTTPS 环境）：

- `spring.datasource.url` = `jdbc:mysql://mysql:3306/b1_dev?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai`
- `spring.data.redis.host` = `redis`
- `b1.minio.endpoint` = `http://minio:9000`，`b1.minio.secure` = `false`
- `spring.flyway.enabled` = `true`（首次自动建表，复用现有 5 个迁移脚本）
- AI 密钥、MinIO 凭据经环境变量注入（来自 `.env`）

> secure 矛盾说明：`application-prod.yml:30` 为 `secure: true`（HTTPS），但 compose 内 MinIO 为 HTTP。新增独立 docker profile 用 `secure:false` 解决，避免污染 prod 配置。

### nginx.conf 核心

```nginx
location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;   # SPA 路由回退
}
location /api {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

前端 `src/api/request.ts` 的 `baseURL` 未设时走相对路径（`/api/v1/...`），天然适配 nginx 反代，**前端代码零改动**。

### 密钥与凭据（.env 注入）

`.env`（不提交，加入 .gitignore）+ `.env.example`（提交，作模板）：

```
# .env.example
DB_PASSWORD=root
MYSQL_ROOT_PASSWORD=root
REDIS_HOST=redis
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
DEEPSEEK_API_KEY=
QWEN_API_KEY=
```

AI 密钥留空时平台仍可启动（登录/任务/报表可用），仅 AI 评分不可用。

## 启动顺序与健康检查

- mysql / redis / minio 定义 healthcheck
- backend `depends_on` 三者 `condition: service_healthy`，确保依赖就绪后再启动（避免 Flyway 连接失败）
- `minio-init` 使用 `mc` 客户端：等 minio 就绪 → 创建 `submissions`、`reports` 桶 → 退出（后端 `putObject` 不会自动建桶，默认桶名 `submissions`，报表上传用 `reports`）

## 部署流程（deploy/README-loongarch.md 内容概要）

1. **开发机打包**：`mvn package -DskipTests`（server/）+ `npm run build`（B1_Platform/）
2. **传输**：把整个项目（含新产出的 jar 与 dist/）传到龙芯机
3. **配置**：`cp .env.example .env`，按需填 AI 密钥
4. **构建**：`docker compose -f docker-compose.prod.yml build`（仅 COPY，秒级）
5. **启动**：`docker compose -f docker-compose.prod.yml up -d`
6. **验证**：`docker compose ps` 全 Up；浏览器访问 `http://<龙芯机IP>:80`，用 `teacher1/123456`、`student1/123456` 登录

## 不在范围（YAGNI）

- HTTPS / TLS 证书（内网/演示场景；`application-prod.yml` 的 `secure:true` 留给真实生产）
- 镜像内多阶段构建（改用预构建产物）
- 任何业务代码改动
- 学院报告导出、AI SSE 等既有未完成功能

## 关键复用点

- `application-prod.yml` 的环境变量占位模式 —— docker profile 沿用
- 现有 5 个 Flyway 迁移脚本（`server/src/main/resources/db/migration/`）—— 首次启动自动建表建数据
- 现有 `docker-compose.yml` 的 MySQL 字符集/时区参数 —— prod compose 复用
- 前端相对路径 API 调用（`request.ts` baseURL 留空）—— 天然适配反代
