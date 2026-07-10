# B1 平台龙芯 Docker 化部署 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 B1 平台补齐前后端容器化，提供在龙芯（LoongArch64）虚拟机上一键 `docker compose up` 拉起 5 容器完整平台的部署方案。

**Architecture:** 预构建产物（开发机产出 jar + dist）COPY 进 loong64 基础镜像；nginx 容器托管前端并反代 `/api` 到后端；新增 docker profile 适配容器网络与内网 MinIO；业务代码零改动。

**Tech Stack:** Docker Compose、nginx:latest、openjdk:21、mysql:8.4.6、redis:latest、cr.loongnix.cn/minio/minio（均为 loong64）。

---

## 参考基线（写代码前必读）

- 设计文档：`docs/superpowers/specs/2026-07-10-loongarch-docker-deploy-design.md`
- 现有基础设施编排：`docker-compose.yml`（MySQL/Redis/MinIO，含字符集/时区参数，可复用）
- 后端公共配置：`server/src/main/resources/application.yml`（active profile = `${SPRING_PROFILES_ACTIVE:dev}`）
- docker profile 蓝本：`server/src/main/resources/application-dev.yml`
- MinIO 配置绑定类：`server/src/main/java/com/b1/infrastructure/minio/MinioConfig.java`（`@ConfigurationProperties(prefix="b1.minio")`，字段 `endpoint/accessKey/secretKey/secure`）
- 后端产物：`server/target/b1-platform-1.0.0-SNAPSHOT.jar`（fat jar）
- 前端 API 基址：`B1_Platform/src/api/request.ts:10`（baseURL 留空 → 相对路径）
- 现有 gitignore 规则：`.gitignore`（whitelist 模式，`*` 忽略一切，靠 `!` 放行）

## 文件结构

| 文件 | 责任 | 新建/修改 |
|---|---|---|
| `server/Dockerfile` | 后端镜像：openjdk:21 + jar | 新建 |
| `server/.dockerignore` | 缩小后端构建上下文 | 新建 |
| `server/src/main/resources/application-docker.yml` | 容器内 profile：服务名连接、MinIO secure:false | 新建 |
| `B1_Platform/Dockerfile` | 前端镜像：nginx + dist + conf | 新建 |
| `B1_Platform/nginx.conf` | SPA 路由回退 + /api 反代 | 新建 |
| `B1_Platform/.dockerignore` | 缩小前端构建上下文 | 新建 |
| `docker-compose.prod.yml` | 5+1 容器编排 | 新建 |
| `.env.example` | 密钥/凭据模板 | 新建 |
| `.gitignore` | 放行新文件、忽略 `.env` | 修改 |
| `deploy/README-loongarch.md` | 部署步骤文档 | 新建 |

> **重要 gitignore 前提**：本仓库 `.gitignore` 是 whitelist 模式（`*` 忽略一切）。新增在 `server/`、`B1_Platform/`、`docs/` 下的文件因已有 `!server/**` 等规则会被跟踪；但**根目录**的 `docker-compose.prod.yml`、`.env.example`、`deploy/` 需显式 `!` 放行，否则被 `*` 吞掉。Task 8 专门处理。

---

## Task 1: 后端 Dockerfile 与 .dockerignore

**Files:**
- Create: `server/Dockerfile`
- Create: `server/.dockerignore`

- [ ] **Step 1: 写 server/.dockerignore**

只允许构建上下文包含目标 jar，排除源码/中间产物（加速传输、避免误 COPY）。

```
*
!target/b1-platform-1.0.0-SNAPSHOT.jar
```

- [ ] **Step 2: 写 server/Dockerfile**

```dockerfile
FROM openjdk:21

WORKDIR /app

# 预构建产物：开发机 mvn package 产出的 fat jar
COPY target/b1-platform-1.0.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# profile 由 compose 的 SPRING_PROFILES_ACTIVE=docker 注入
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- [ ] **Step 3: 验证 jar 存在（COPY 源必须在）**

Run: `ls -la server/target/b1-platform-1.0.0-SNAPSHOT.jar`
Expected: 显示 ~100MB 的 jar 文件。若不存在，先在开发机跑 `cd server && mvn package -DskipTests`。

- [ ] **Step 4: Commit**

```bash
git add server/Dockerfile server/.dockerignore
git commit -m "build: 新增后端 Dockerfile（openjdk21 + fat jar）"
```

---

## Task 2: 后端 docker profile 配置

**Files:**
- Create: `server/src/main/resources/application-docker.yml`

以 `application-dev.yml` 为蓝本，把 `localhost` 换成 compose 服务名，MinIO 保持 HTTP（`secure:false`），凭据全走环境变量（`.env` 注入）。**不改 application-prod.yml。**

- [ ] **Step 1: 写 application-docker.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/b1_dev?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:root}
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

b1:
  minio:
    endpoint: ${MINIO_ENDPOINT:http://minio:9000}
    access-key: ${MINIO_ACCESS_KEY:minioadmin}
    secret-key: ${MINIO_SECRET_KEY:minioadmin}
    secure: false
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY:}
      base-url: https://api.deepseek.com/v1
      model: deepseek-chat
    qwen:
      api-key: ${QWEN_API_KEY:}
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      model: qwen-vl-max

logging:
  level:
    root: INFO
    com.b1: INFO
```

- [ ] **Step 2: 验证 YAML 语法**

Run: `cd server && python -c "import yaml; yaml.safe_load(open('src/main/resources/application-docker.yml', encoding='utf-8')); print('YAML OK')"`
Expected: `YAML OK`

- [ ] **Step 3: Commit**

```bash
git add server/src/main/resources/application-docker.yml
git commit -m "feat: 新增 docker profile 适配容器网络与内网 MinIO"
```

---

## Task 3: 前端 nginx 配置

**Files:**
- Create: `B1_Platform/nginx.conf`

SPA 单页路由回退到 index.html；`/api` 反代到后端容器；文件上传放宽 body 大小（后端允许 50MB，见 application.yml multipart）。

- [ ] **Step 1: 写 B1_Platform/nginx.conf**

```nginx
server {
    listen 80;
    server_name _;

    # 上传文件最大 100MB，与后端 max-request-size 对齐
    client_max_body_size 100m;

    root /usr/share/nginx/html;
    index index.html;

    # SPA 前端路由：未命中的路径回退到 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反代到后端容器（compose 服务名 backend）
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 180s;
    }
}
```

> 说明：`proxy_read_timeout 180s` 覆盖后端 AI 调用（application.yml `b1.ai.timeout: 120000` = 120s）。

- [ ] **Step 2: Commit**

```bash
git add B1_Platform/nginx.conf
git commit -m "feat: 新增前端 nginx 配置（SPA 路由 + API 反代）"
```

---

## Task 4: 前端 Dockerfile 与 .dockerignore

**Files:**
- Create: `B1_Platform/Dockerfile`
- Create: `B1_Platform/.dockerignore`

- [ ] **Step 1: 写 B1_Platform/.dockerignore**

```
*
!dist
!nginx.conf
```

- [ ] **Step 2: 写 B1_Platform/Dockerfile**

```dockerfile
FROM nginx:latest

# 替换默认站点配置
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/b1.conf

# 预构建产物：开发机 npm run build 产出的静态资源
COPY dist /usr/share/nginx/html

EXPOSE 80
```

- [ ] **Step 3: 验证 dist 存在**

Run: `ls B1_Platform/dist/index.html`
Expected: 文件存在。若不存在，在开发机跑 `cd B1_Platform && npm run build`。

- [ ] **Step 4: Commit**

```bash
git add B1_Platform/Dockerfile B1_Platform/.dockerignore
git commit -m "build: 新增前端 Dockerfile（nginx + dist）"
```

---

## Task 5: docker-compose.prod.yml 编排

**Files:**
- Create: `docker-compose.prod.yml`

5 常驻容器 + 1 一次性 minio-init。依赖健康检查后再起 backend。镜像全部用已验证的 loong64 tag。

- [ ] **Step 1: 写 docker-compose.prod.yml**

```yaml
services:
  mysql:
    image: mysql:latest
    container_name: b1-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      MYSQL_ROOT_HOST: "%"
      MYSQL_DATABASE: b1_dev
      TZ: Asia/Shanghai
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-time-zone=+08:00
      - --mysql-native-password=ON
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - b1-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-p${MYSQL_ROOT_PASSWORD:-root}"]
      interval: 10s
      timeout: 5s
      retries: 10
    restart: unless-stopped

  redis:
    image: redis:latest
    container_name: b1-redis
    command: redis-server --save 60 1 --loglevel warning
    volumes:
      - redis-data:/data
    networks:
      - b1-net
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 10
    restart: unless-stopped

  minio:
    image: cr.loongnix.cn/minio/minio:latest
    container_name: b1-minio
    ports:
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER:-minioadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD:-minioadmin}
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data
    networks:
      - b1-net
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 10s
      timeout: 5s
      retries: 10
    restart: unless-stopped

  minio-init:
    image: cr.loongnix.cn/minio/minio:latest
    container_name: b1-minio-init
    depends_on:
      minio:
        condition: service_healthy
    entrypoint: >
      /bin/sh -c "
      mc alias set local http://minio:9000 ${MINIO_ROOT_USER:-minioadmin} ${MINIO_ROOT_PASSWORD:-minioadmin};
      mc mb --ignore-existing local/submissions;
      mc mb --ignore-existing local/reports;
      echo 'buckets ready';
      exit 0;
      "
    networks:
      - b1-net

  backend:
    build:
      context: ./server
      dockerfile: Dockerfile
    image: b1-backend:loong64
    container_name: b1-backend
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      minio:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      REDIS_HOST: redis
      REDIS_PORT: "6379"
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: ${MINIO_ROOT_USER:-minioadmin}
      MINIO_SECRET_KEY: ${MINIO_ROOT_PASSWORD:-minioadmin}
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY:-}
      QWEN_API_KEY: ${QWEN_API_KEY:-}
    networks:
      - b1-net
    restart: unless-stopped

  frontend:
    build:
      context: ./B1_Platform
      dockerfile: Dockerfile
    image: b1-frontend:loong64
    container_name: b1-frontend
    depends_on:
      - backend
    ports:
      - "80:80"
    networks:
      - b1-net
    restart: unless-stopped

networks:
  b1-net:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
  minio-data:
```

- [ ] **Step 2: 验证 compose 文件语法（开发机 docker 可用则跑）**

Run: `docker compose -f docker-compose.prod.yml config --quiet && echo "compose OK"`
Expected: `compose OK`（无输出表示语法正确）。若开发机无 docker，改用 YAML 校验：`python -c "import yaml; yaml.safe_load(open('docker-compose.prod.yml', encoding='utf-8')); print('YAML OK')"`

- [ ] **Step 3: Commit**

```bash
git add docker-compose.prod.yml
git commit -m "feat: 新增生产 compose 编排（五容器 + minio 建桶）"
```

---

## Task 6: .env.example 密钥模板

**Files:**
- Create: `.env.example`

- [ ] **Step 1: 写 .env.example**

```
# ===== 数据库 =====
MYSQL_ROOT_PASSWORD=root

# ===== MinIO 对象存储 =====
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin

# ===== AI 密钥（留空则 AI 评分不可用，其余功能正常）=====
DEEPSEEK_API_KEY=
QWEN_API_KEY=
```

- [ ] **Step 2: Commit（放行留到 Task 8）**

```bash
git add .env.example
git commit -m "chore: 新增部署密钥模板 .env.example"
```

> 注：此时因根目录 gitignore `*` 规则，`git add .env.example` 可能无效。若 `git status` 看不到该文件，跳过本 commit，统一在 Task 8 放行后一起提交。执行时用 `git add -f .env.example` 强制暂存以验证内容无误，但**最终提交依赖 Task 8 的 gitignore 规则**。

---

## Task 7: 部署文档

**Files:**
- Create: `deploy/README-loongarch.md`

- [ ] **Step 1: 写 deploy/README-loongarch.md**

````markdown
# B1 平台龙芯（LoongArch64）Docker 部署指南

## 前提

- 目标机：龙芯虚拟机，已装原生 `linux/loong64` Docker（`docker version` 显示 OS/Arch: linux/loong64）
- 镜像源已配置 `lcr.loongnix.cn`（`docker info` 可见 Registry Mirrors）

## 步骤

### 1. 开发机打包（Windows / x86，产物跨架构通用）

```powershell
# 后端 fat jar
cd D:\Project\B1\server
mvn package -DskipTests

# 前端静态资源
cd D:\Project\B1\B1_Platform
npm install   # 首次
npm run build
```

产物：`server/target/b1-platform-1.0.0-SNAPSHOT.jar`、`B1_Platform/dist/`。

### 2. 传输到龙芯机

把整个项目目录（含上一步产出的 jar 与 dist/）拷到龙芯机，例如 `~/B1`。

### 3. 配置密钥

```bash
cd ~/B1
cp .env.example .env
vi .env          # 按需填 DEEPSEEK_API_KEY / QWEN_API_KEY（留空则 AI 不可用）
```

### 4. 构建镜像（仅 COPY，秒级）

```bash
docker compose -f docker-compose.prod.yml build
```

### 5. 启动

```bash
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps    # 全部 Up；b1-minio-init 退出(Exited 0)属正常
```

### 6. 验证

- 浏览器访问 `http://<龙芯机IP>/`
- 登录：教师 `teacher1/123456`，学生 `student1/123456`
- MinIO 控制台（可选排查）：`http://<龙芯机IP>:9001`

## 常见问题

- **backend 起不来**：`docker compose -f docker-compose.prod.yml logs backend`。多为 mysql 未就绪，healthcheck 会自动等待，稍候重试。
- **上传文件失败**：确认 `b1-minio-init` 已成功建桶（`docker logs b1-minio-init` 显示 `buckets ready`）。
- **首次启动慢**：Flyway 建表 + MySQL 初始化，首次约 1-2 分钟。
- **镜像拉不到**：确认在龙芯机执行（非 x86），且镜像源可达。

## 停止 / 清理

```bash
docker compose -f docker-compose.prod.yml down          # 停止容器，保留数据卷
docker compose -f docker-compose.prod.yml down -v       # 连数据卷一起删（慎用）
```
````

- [ ] **Step 2: Commit（deploy/ 放行留到 Task 8）**

```bash
git add deploy/README-loongarch.md
git commit -m "docs: 新增龙芯 Docker 部署指南"
```

> 注：同 Task 6，根目录 `deploy/` 可能被 gitignore `*` 忽略，Task 8 放行后再确认提交。

---

## Task 8: .gitignore 放行根目录新文件

**Files:**
- Modify: `.gitignore`

现有 `.gitignore` 为 whitelist 模式：`*` 忽略一切，靠 `!` 放行。根目录的 `docker-compose.prod.yml`、`.env.example`、`deploy/` 需显式放行；`.env`（真实密钥）必须保持忽略。

- [ ] **Step 1: 读现有 .gitignore 确认结构**

Run: `cat .gitignore`
Expected: 看到 `*` 开头、`!server/**`、`!docs/**` 等放行规则。

- [ ] **Step 2: 在 .gitignore 的 "Track only these" 区块追加放行规则**

在 `!docs/**` 之后、`# Exclude build outputs` 之前插入：

```
# Allow deploy artifacts (root-level, otherwise swallowed by the leading *)
!docker-compose.prod.yml
!.env.example
!deploy/
!deploy/**
```

并在文件末尾（`# Exclude runtime logs` 区块附近）追加，确保真实密钥永不入库：

```
# Exclude real secrets (only the .example template is tracked)
.env
```

- [ ] **Step 3: 验证放行生效**

Run: `git check-ignore -v docker-compose.prod.yml .env.example deploy/README-loongarch.md .env 2>&1; echo "---"; git status --short`
Expected: `.env` 被忽略（check-ignore 命中）；`docker-compose.prod.yml`、`.env.example`、`deploy/README-loongarch.md` **不**被忽略（在 git status 中可见为待提交）。

- [ ] **Step 4: 补提交此前被 gitignore 挡住的文件**

```bash
git add .gitignore docker-compose.prod.yml .env.example deploy/README-loongarch.md
git commit -m "chore: 放行部署文件（compose/.env.example/deploy），忽略真实 .env"
```

- [ ] **Step 5: 确认 .env 不会被误提交**

Run: `printf 'DEEPSEEK_API_KEY=sk-test\n' > .env && git status --short .env; git check-ignore .env && echo ".env correctly ignored"; rm .env`
Expected: `.env correctly ignored`，且 git status 不显示 .env。

---

## Task 9: 整体交付验证（文档核对）

**Files:** 无（仅核对）

- [ ] **Step 1: 核对所有新增文件已入库**

Run: `git ls-files -- server/Dockerfile server/.dockerignore server/src/main/resources/application-docker.yml B1_Platform/Dockerfile B1_Platform/nginx.conf B1_Platform/.dockerignore docker-compose.prod.yml .env.example deploy/README-loongarch.md .gitignore`
Expected: 10 个文件路径全部列出（除 .dockerignore 若被上层规则影响需确认）。

- [ ] **Step 2: 确认 .dockerignore 被跟踪**

Run: `git check-ignore -v server/.dockerignore B1_Platform/.dockerignore 2>&1 || echo "not ignored (good)"`
Expected: 不被忽略（因在 `!server/**`、`!B1_Platform/**` 放行范围内）。若被忽略，在 Task 8 的放行区块补 `!server/.dockerignore` `!B1_Platform/.dockerignore`。

- [ ] **Step 3: 最终提交（如有遗漏）**

```bash
git status
# 若有未提交的新文件，git add 后 commit
```

---

## Self-Review 记录

- **Spec 覆盖**：架构（Task 5）、预构建策略（Task 1/4 + Task 7 文档）、10 个文件清单（Task 1-8 全覆盖）、docker profile 配置（Task 2）、nginx 反代（Task 3）、建桶（Task 5 minio-init）、启动顺序 healthcheck（Task 5）、部署流程（Task 7）、.env 密钥（Task 6/8）—— 均有对应任务。
- **占位符**：无 TBD/TODO；所有配置/代码块为完整内容。
- **类型/命名一致性**：compose 服务名 `mysql/redis/minio/backend/frontend` 在 application-docker.yml（`mysql:3306`/`redis`/`minio:9000`）、nginx.conf（`backend:8080`）、compose environment（`REDIS_HOST: redis` 等）三处一致；MinIO 环境变量名 `MINIO_ACCESS_KEY/MINIO_SECRET_KEY` 与 MinioConfig 的 `b1.minio.access-key/secret-key`（application-docker.yml `${MINIO_ACCESS_KEY}`）对应一致。
- **gitignore 陷阱**：已在 Task 8 专门处理根目录 whitelist 放行，并单列 `.env` 忽略验证（Task 8 Step 5）。
