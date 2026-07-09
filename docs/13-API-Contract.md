# API Contract v1.0

## 基于大模型的软件实训教学检查评价与报表系统

---

| 字段 | 值 |
|---|---|
| **文档名称** | API Contract（接口契约） |
| **文档版本** | v1.1 |
| **文档状态** | Formal Release |
| **作者** | Backend Architect |
| **最后更新** | 2026-07-09 |
| **适用范围** | 前端 Axios、后端 Controller、Mock.js、OpenAPI 文档生成 |
| **文档定位** | 本项目唯一接口真相（Single Source of Truth）。所有 API 接口以本文档为准 |
| **前置文档** | 00-Architecture-Baseline, 01-PRD, 02-SDS, 06-API-Mock-Spec, 10-Backend-Architecture-Design, 11-Database-Design, 12-Backend-Specification |

---

## 目录

1. [General Conventions](#1-general-conventions)
2. [Auth Module](#2-auth-module)
3. [User Module](#3-user-module)
4. [Student APIs](#4-student-apis)
5. [Teacher APIs](#5-teacher-apis)
6. [Admin APIs](#6-admin-apis)
7. [File APIs](#7-file-apis)
8. [Notification APIs](#8-notification-apis)
9. [Error Code Reference](#9-error-code-reference)

---

## 1. General Conventions

### 1.1 Base URL

| 环境 | Base URL |
|---|---|
| 开发 | `http://localhost:8080` |
| 测试 | `http://test-api.b1.example.com` |
| 生产 | `https://api.b1.example.com` |

### 1.2 API Version

All APIs are prefixed with `/api/v1/`.

### 1.3 Authentication

Token-based authentication via **Sa-Token** (UUID format, Redis storage).

- Header: `Authorization: Bearer {token_value}`
- Token issued by login, stored in Redis with 2-hour TTL
- Refresh via `/api/v1/auth/refresh` with refresh token (7-day TTL)

### 1.4 Role Prefixes

| Prefix | Role | Access |
|---|---|---|
| `/api/v1/auth/**` | Public | No authentication required |
| `/api/v1/user/**` | Authenticated | Any logged-in user |
| `/api/v1/student/**` | Student | `student` role only |
| `/api/v1/teacher/**` | Teacher | `teacher` role only |
| `/api/v1/admin/**` | Admin | `admin` role only |
| `/api/v1/files/**` | Authenticated | Any logged-in user (download enforces per-file authorization) |

### 1.5 Unified Response: `Result<T>`

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

| Field | Type | Description |
|---|---|---|
| `code` | `int` | Business status code: 0 = success |
| `message` | `String` | Human-readable message |
| `data` | `T` / `null` | Response payload; null on error |
| `success` | `boolean` | Derived: code == 0 |
| `timestamp` | `long` | Server response timestamp (ms) |
| `traceId` | `String` | Distributed trace ID (UUID first 8 chars) |

### 1.6 Paginated Response: `PageResult<T>`

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 20,
    "total": 156,
    "totalPages": 8
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

Pagination params: `page` (default 1), `pageSize` (default 20, max 100).

### 1.7 ID Serialization

All Snowflake IDs (BIGINT, 19 digits) are serialized as **String** in JSON to avoid JavaScript Number precision loss.

### 1.8 Time Format

All timestamps use ISO 8601 with timezone offset: `yyyy-MM-ddTHH:mm:ss.SSS+08:00`.

### 1.9 URL Naming

- All lowercase, kebab-case: `/api/v1/teacher/tasks/{taskId}/submissions`
- Resource names use plural nouns
- No verbs in URL paths
- No file extensions

### 1.10 HTTP Methods

| Method | Semantic | Idempotent |
|---|---|---|
| `GET` | Query resource(s) | Yes |
| `POST` | Create resource | No |
| `PUT` | Full update | Yes |
| `PATCH` | Partial update | No |
| `DELETE` | Logical delete | Yes |

### 1.11 Layered Call Chain

```
Frontend → Controller → Service (interface) → ServiceImpl → Mapper → Database
```

- Controller: parameter binding, validation, calls Service, wraps Result
- Service: business logic, transaction management, calls Mapper
- Mapper: data access via MyBatis-Plus BaseMapper
- All modules follow this chain; no cross-layer calls

---

## 2. Auth Module

**Controller**: `AuthController`  
**Base Path**: `/api/v1/auth`

### 2.1 Login

---

#### API Information

| Field | Value |
|---|---|
| **Name** | User Login |
| **Function** | Authenticate user credentials and issue access/refresh tokens |
| **Permission** | Public (no authentication required) |
| **Controller** | `AuthController` |
| **Method** | `POST` |
| **URL** | `/api/v1/auth/login` |
| **Version** | v1 |
| **Description** | Validates username/password, checks account status (enabled, not locked), returns Sa-Token UUID tokens. Login failure count tracked; 5 consecutive failures locks account for 15 minutes. |

---

#### Request

| Element | Value |
|---|---|
| **Headers** | `Content-Type: application/json`, `X-Trace-Id: {uuid}` |
| **Authorization** | None |
| **Content-Type** | `application/json` |
| **Path Variables** | None |
| **Query Parameters** | None |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `username` | String | Yes | `@NotBlank`, max 64 chars | Username |
| `password` | String | Yes | `@NotBlank`, max 128 chars | Plain-text password (transmitted over HTTPS only) |

```json
{
  "username": "zhangsan",
  "password": "Abc@123456"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "refreshToken": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "tokenExpireTime": "2026-07-05T10:30:00.000+08:00",
    "userInfo": {
      "userId": "1234567890123456789",
      "username": "zhangsan",
      "realName": "张三",
      "roleCode": "student",
      "roleName": "学生",
      "avatarUrl": null
    }
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

| Field | Type | Description |
|---|---|---|
| `accessToken` | String | Sa-Token UUID, 2-hour TTL |
| `refreshToken` | String | Refresh token, 7-day TTL |
| `tokenExpireTime` | String | Access token expiration (ISO 8601) |
| `userInfo.userId` | String | Snowflake user ID (serialized as String) |
| `userInfo.roleCode` | String | `student` / `teacher` / `admin` |

---

#### Error Codes

| Code | Type | Message | When |
|---|---|---|---|
| 2004 | Business | 用户名或密码错误 | Credentials mismatch |
| 2005 | Business | 账号已被锁定，请15分钟后再试 | Account locked |
| 2006 | Business | 账号已被禁用 | Account disabled |
| 2007 | Business | 验证码错误 | CAPTCHA mismatch (if enabled) |
| 4001 | Validation | 用户名不能为空 / 密码不能为空 | Missing required fields |
| 5001 | System | 服务器繁忙，请稍后重试 | Unexpected server error |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | Yes | Yes |

(Public endpoint — all roles can access)

---

#### Sequence

```
Frontend (LoginPage)
  → POST /api/v1/auth/login
  → AuthController.login(LoginRequest)
  → AuthService.login(username, password)
  → UserMapper.selectOne(Wrappers.lambdaQuery(User::getUsername, username))
  → [User found] → BCrypt.checkpw(password, user.password)
  → [Valid] → StpUtil.login(user.id)  // Sa-Token issues UUID token
  → Redis.set("token:access:{uuid}", userInfo, 2h TTL)
  → OperationLogAspect → INSERT operation_log
  → Response: Result<LoginVO> { accessToken, refreshToken, userInfo }
```

---

### 2.2 Logout

---

#### API Information

| Field | Value |
|---|---|
| **Name** | User Logout |
| **Function** | Invalidate current access token |
| **Permission** | Authenticated |
| **Controller** | `AuthController` |
| **Method** | `POST` |
| **URL** | `/api/v1/auth/logout` |
| **Version** | v1 |
| **Description** | Revokes the current Sa-Token session. Token is removed from Redis and added to blacklist for its remaining validity period. |

---

#### Request

| Element | Value |
|---|---|
| **Headers** | `Authorization: Bearer {token}`, `Content-Type: application/json` |
| **Authorization** | Required (any role) |
| **Content-Type** | `application/json` |
| **Path Variables** | None |
| **Query Parameters** | None |
| **Request Body** | None |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "登出成功",
  "data": null,
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message | When |
|---|---|---|---|
| 2001 | Auth | 请先登录 | Not logged in |
| 2003 | Auth | 登录凭证无效 | Token already revoked |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | Yes | Yes |

---

#### Sequence

```
Frontend (Header/UserMenu → Logout)
  → POST /api/v1/auth/logout
  → AuthController.logout()
  → StpUtil.logout()  // Sa-Token revokes current token
  → Redis.del("token:access:{uuid}")
  → Redis.sadd("token:blacklist:{uuid}", tokenId, remainingTTL)
  → Response: Result<Void>
```

---

### 2.3 Refresh Token

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Refresh Access Token |
| **Function** | Issue a new access token using the refresh token |
| **Permission** | Public (requires valid refresh token) |
| **Controller** | `AuthController` |
| **Method** | `POST` |
| **URL** | `/api/v1/auth/refresh` |
| **Version** | v1 |
| **Description** | When access token expires (2h), use refresh token to obtain a new access token without re-login. Old access token is revoked. Refresh token has 7-day TTL. |

---

#### Request

| Element | Value |
|---|---|
| **Headers** | `Content-Type: application/json` |
| **Authorization** | None (uses refresh token in body) |
| **Content-Type** | `application/json` |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `refreshToken` | String | Yes | `@NotBlank` | The refresh token from login response |

```json
{
  "refreshToken": "b2c3d4e5-f6a7-8901-bcde-f12345678901"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "tokenExpireTime": "2026-07-05T12:30:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message | When |
|---|---|---|---|
| 2008 | Auth | 登录已过期，请重新登录 | Refresh token expired (>7 days) |
| 2003 | Auth | 登录凭证无效 | Refresh token not found or invalid |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | Yes | Yes |

---

#### Sequence

```
Frontend (Axios interceptor → 401 response)
  → POST /api/v1/auth/refresh
  → AuthController.refresh(RefreshRequest)
  → AuthService.refreshAccessToken(refreshToken)
  → Redis.get("token:refresh:{userId}")
  → [Valid] → StpUtil.logout(oldTokenId)  // revoke old access token
  → StpUtil.login(userId)  // issue new access token
  → Redis.set("token:access:{newUuid}", userInfo, 2h TTL)
  → Response: Result<RefreshVO>
```

---

## 3. User Module

**Controller**: `UserController`  
**Base Path**: `/api/v1/user`

### 3.1 Get Current User Info

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get Current User Profile |
| **Function** | Return the authenticated user's profile information and role permissions |
| **Permission** | Authenticated (any role) |
| **Controller** | `UserController` |
| **Method** | `GET` |
| **URL** | `/api/v1/user/me` |
| **Version** | v1 |
| **Description** | Returns the current logged-in user's profile. Used by the frontend to populate the header user menu and determine accessible routes. |

---

#### Request

| Element | Value |
|---|---|
| **Headers** | `Authorization: Bearer {token}` |
| **Authorization** | Required |
| **Path Variables** | None |
| **Query Parameters** | None |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": "1234567890123456789",
    "username": "zhangsan",
    "realName": "张三",
    "email": "zhangsan@example.com",
    "phone": "138****5678",
    "avatarUrl": null,
    "roleCode": "student",
    "roleName": "学生",
    "studentNo": "20240101001",
    "className": "软件技术2401班",
    "college": "信息工程学院",
    "createdAt": "2026-03-01T00:00:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message | When |
|---|---|---|---|
| 2001 | Auth | 请先登录 | Not logged in |
| 2003 | Auth | 登录凭证无效 | Token invalid/expired |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | Yes | Yes |

---

#### Sequence

```
Frontend (App.vue onMounted / route guard)
  → GET /api/v1/user/me
  → UserController.me()
  → UserService.getCurrentUser(StpUtil.getLoginIdAsLong())
  → UserMapper.selectById(userId)
  → UserRoleMapper.selectByUserId(userId) → RoleMapper.selectById(roleId)
  → UserConvert.toUserInfoVO(user, role)
  → Response: Result<UserInfoVO>
```

---

### 3.2 Update Current User Profile

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Update Current User Profile |
| **Function** | Update the authenticated user's own profile (email, phone, avatar) |
| **Permission** | Authenticated (any role) |
| **Controller** | `UserController` |
| **Method** | `PUT` |
| **URL** | `/api/v1/user/me` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Headers** | `Authorization: Bearer {token}`, `Content-Type: application/json` |
| **Authorization** | Required |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `email` | String | No | `@Email` | Email address |
| `phone` | String | No | `@Pattern(regexp = "^1[3-9]\\d{9}$")` | Phone number |
| `avatarUrl` | String | No | — | Avatar URL (from file upload) |

```json
{
  "email": "zhangsan@example.com",
  "phone": "13812345678"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "更新成功",
  "data": {
    "userId": "1234567890123456789",
    "email": "zhangsan@example.com",
    "phone": "138****5678"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message | When |
|---|---|---|---|
| 4002 | Validation | 邮箱格式不正确 | Invalid email format |
| 4002 | Validation | 手机号格式不正确 | Invalid phone format |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | Yes | Yes |

---

#### Sequence

```
Frontend (ProfilePage)
  → PUT /api/v1/user/me
  → UserController.updateMe(@Valid UpdateProfileRequest)
  → UserService.updateProfile(StpUtil.getLoginIdAsLong(), request)
  → UserMapper.updateById(user)
  → Redis.del("user:info:" + userId)
  → Response: Result<UserInfoVO>
```

---

### 3.3 Change Password

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Change Password |
| **Function** | Change the current user's login password |
| **Permission** | Authenticated (any role) |
| **Controller** | `UserController` |
| **Method** | `PUT` |
| **URL** | `/api/v1/user/me/password` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Headers** | `Authorization: Bearer {token}`, `Content-Type: application/json` |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `oldPassword` | String | Yes | `@NotBlank` | Current password |
| `newPassword` | String | Yes | `@NotBlank`, `@Size(min=8, max=64)` | New password (8+ chars) |

```json
{
  "oldPassword": "Abc@123456",
  "newPassword": "Xyz@789012"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "密码修改成功，请重新登录",
  "data": null,
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message | When |
|---|---|---|---|
| 2004 | Business | 原密码错误 | oldPassword doesn't match |
| 4004 | Validation | 密码长度不能少于8个字符 | newPassword too short |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | Yes | Yes |

---

#### Sequence

```
Frontend (ChangePasswordPage / Dialog)
  → PUT /api/v1/user/me/password
  → UserController.changePassword(@Valid ChangePasswordRequest)
  → UserService.changePassword(StpUtil.getLoginIdAsLong(), oldPwd, newPwd)
  → UserMapper.selectById(userId)
  → [BCrypt.checkpw(oldPwd, user.password) = true]
  → user.setPassword(BCrypt.hashpw(newPwd))
  → UserMapper.updateById(user)
  → StpUtil.logout()  // force re-login
  → Response: Result<Void>
```

---

## 4. Student APIs

**Base Path**: `/api/v1/student`  
**Role**: `student`

### 4.1 List My Tasks

---

#### API Information

| Field | Value |
|---|---|
| **Name** | List Student Tasks |
| **Function** | Return paginated training tasks assigned to the current student |
| **Permission** | Student |
| **Controller** | `StudentTaskController` |
| **Method** | `GET` |
| **URL** | `/api/v1/student/tasks` |
| **Version** | v1 |
| **Description** | Returns tasks from all courses the student is enrolled in. Supports filtering by status, course, and keyword search. |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |

**Query Parameters**:

| Field | Type | Required | Default | Description |
|---|---|---|---|---|
| `page` | Integer | No | 1 | Page number |
| `pageSize` | Integer | No | 20 | Items per page (max 100) |
| `status` | String | No | — | Filter: `PUBLISHED` / `CLOSED` |
| `courseId` | String | No | — | Filter by course |
| `keyword` | String | No | — | Search in task name / description |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "taskId": "1234567890123456789",
        "taskName": "Spring Boot 图书管理系统实训",
        "courseName": "Java Web开发",
        "teacherName": "王建国",
        "deadline": "2026-07-15T23:59:59.000+08:00",
        "totalScore": 100,
        "submissionType": "GIT_ZIP",
        "status": "PUBLISHED",
        "mySubmissionStatus": "NOT_SUBMITTED",
        "createdAt": "2026-06-25T10:00:00.000+08:00"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 8,
    "totalPages": 1
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

**`mySubmissionStatus` enum**: `NOT_SUBMITTED` / `SUBMITTED` / `AI_EVALUATING` / `AI_COMPLETED` / `TEACHER_SCORING` / `COMPLETED` / `REJECTED`

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 2001 | Auth | 请先登录 |
| 3001 | Permission | 您没有权限访问此功能 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (StudentTaskListPage)
  → GET /api/v1/student/tasks?page=1&pageSize=20&status=PUBLISHED
  → StudentTaskController.listTasks(PageQuery, status, courseId, keyword)
  → StudentTaskService.listStudentTasks(userId, page, status, courseId, keyword)
  → CourseStudentMapper.selectByUserId(userId) → List<courseId>
  → TrainingTaskMapper.selectPage(page, Wrappers.lambdaQuery(...).in(courseId).eq(status))
  → [For each task] SubmissionMapper.selectOne(userId, taskId) → mySubmissionStatus
  → TaskConvert.toStudentTaskListVO(page)
  → Response: PageResult<StudentTaskVO>
```

---

### 4.2 Get Task Detail

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get Task Detail |
| **Function** | Return full task details including evaluation dimensions |
| **Permission** | Student |
| **Controller** | `StudentTaskController` |
| **Method** | `GET` |
| **URL** | `/api/v1/student/tasks/{taskId}` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Path Variables** | `taskId` — String, 19-digit Snowflake ID |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "1234567890123456789",
    "taskName": "Spring Boot 图书管理系统实训",
    "courseName": "Java Web开发",
    "teacherName": "王建国",
    "teacherEmail": "wangjg@example.com",
    "description": "使用 Spring Boot + MyBatis + MySQL 实现图书管理系统的增删改查功能...",
    "deadline": "2026-07-15T23:59:59.000+08:00",
    "totalScore": 100,
    "submissionType": "GIT_ZIP",
    "submitLimit": 3,
    "evaluationDimensions": [
      { "dimensionName": "代码规范", "weight": 25, "maxScore": 25 },
      { "dimensionName": "功能完成度", "weight": 30, "maxScore": 30 },
      { "dimensionName": "创新性", "weight": 20, "maxScore": 20 },
      { "dimensionName": "文档完整性", "weight": 15, "maxScore": 15 },
      { "dimensionName": "Git规范", "weight": 10, "maxScore": 10 }
    ],
    "attachments": [
      { "fileId": "3456789012345678901", "fileName": "实训任务要求.pdf", "fileSize": 204800 }
    ],
    "mySubmissionStatus": "NOT_SUBMITTED",
    "mySubmitCount": 0,
    "createdAt": "2026-06-25T10:00:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1001 | Business | 请求的资源不存在 |
| 3003 | Permission | 您没有权限访问该资源 (task not assigned to student) |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (StudentTaskDetailPage)
  → GET /api/v1/student/tasks/{taskId}
  → StudentTaskController.getTask(taskId)
  → StudentTaskService.getStudentTaskDetail(userId, taskId)
  → TrainingTaskMapper.selectById(taskId)
  → [Verify student is in class assigned to this task]
  → StandardDimensionMapper.selectByStandardId(task.standardId)
  → SubmissionMapper.selectOne(userId, taskId) → submitCount, status
  → FileStorageMapper.selectByIds(task.attachmentIds)
  → TaskConvert.toStudentTaskDetailVO(task, dimensions, submission, files)
  → Response: Result<StudentTaskDetailVO>
```

---

### 4.3 Submit Work

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Submit Training Work |
| **Function** | Submit training work (Git URL, ZIP upload, or online code) |
| **Permission** | Student |
| **Controller** | `SubmissionController` |
| **Method** | `POST` |
| **URL** | `/api/v1/student/tasks/{taskId}/submissions` |
| **Version** | v1 |
| **Description** | Creates or overwrites a submission for the task. Validates deadline, submit count limit, and task status. Triggers async AI analysis if auto-analyze is enabled. |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Content-Type** | `application/json` |
| **Path Variables** | `taskId` — String, 19-digit Snowflake ID |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `submissionType` | String | Yes | `@NotBlank`, enum: `GIT_URL`/`ZIP_UPLOAD`/`ONLINE_CODE` | Submission method |
| `gitUrl` | String | Conditional | Required if GIT_URL | Git repository URL |
| `gitBranch` | String | No | Default `main` | Git branch name |
| `zipFileId` | String | Conditional | Required if ZIP_UPLOAD | File ID from upload API |
| `onlineCode` | String | Conditional | Required if ONLINE_CODE | Code content |
| `remark` | String | No | `@Size(max=500)` | Submission notes |

```json
{
  "submissionType": "GIT_URL",
  "gitUrl": "https://github.com/zhangsan/book-manager.git",
  "gitBranch": "main",
  "remark": "完成所有基本功能，单元测试覆盖率85%"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "提交成功",
  "data": {
    "submissionId": "4567890123456789012",
    "taskId": "1234567890123456789",
    "submissionType": "GIT_URL",
    "status": "SUBMITTED",
    "submitCount": 1,
    "maxSubmitCount": 3,
    "submittedAt": "2026-06-30T10:00:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1005 | Business | 已达到提交次数上限（{max}次） |
| 1006 | Business | 该实训任务已于 {deadline} 截止 |
| 1003 | Business | 任务状态不允许提交 |
| 4001 | Validation | 提交方式不能为空 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (SubmitPage)
  → POST /api/v1/student/tasks/{taskId}/submissions
  → SubmissionController.submit(taskId, @Valid SubmitRequest)
  → SubmissionService.submit(userId, taskId, request)
  → [Validate] TrainingTaskMapper.selectById(taskId) → check deadline, status
  → [Validate] SubmissionMapper.selectOne(userId, taskId) → check submitCount < limit
  → [Transaction] SubmissionMapper.insert/submitCount+1 → SubmissionFileMapper.insert
  → [Event] ApplicationEventPublisher.publish(SubmissionCreatedEvent)
  → [Async] AIAnalysisService.startAnalysis(submissionId)  // if auto-analyze enabled
  → Response: Result<SubmissionVO>
```

---

### 4.4 Upload Report

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Upload Training Report |
| **Function** | Upload a training report document (PDF/DOCX) |
| **Permission** | Student |
| **Controller** | `SubmissionController` |
| **Method** | `POST` |
| **URL** | `/api/v1/student/tasks/{taskId}/reports` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Content-Type** | `multipart/form-data` |
| **Path Variables** | `taskId` — String |

**Form Data**:

| Field | Type | Required | Validation |
|---|---|---|---|
| `file` | File | Yes | PDF/DOCX, max 20MB |
| `title` | String | No | Default: original filename |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "报告上传成功",
  "data": {
    "reportId": "5678901234567890123",
    "fileId": "3456789012345678902",
    "fileName": "Spring_Boot实训报告_张三.docx",
    "fileSize": 512000,
    "fileType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "uploadedAt": "2026-06-30T10:05:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 7001 | File | 文件大小不能超过 20MB |
| 7002 | File | 不支持的文件类型，支持：PDF、DOCX |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (ReportUpload)
  → POST /api/v1/student/tasks/{taskId}/reports (multipart/form-data)
  → SubmissionController.uploadReport(taskId, file, title)
  → FileService.upload(file, "submissions") → MinIO
  → FileStorageMapper.insert(fileEntity)
  → SubmissionFileMapper.insert(submissionId, fileId)
  → Response: Result<ReportVO>
```

---

### 4.5 Verify Git Repository

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Verify Git Repository |
| **Function** | Validate Git repo URL accessibility and branch existence |
| **Permission** | Student |
| **Controller** | `GitController` |
| **Method** | `POST` |
| **URL** | `/api/v1/student/tasks/{taskId}/git-verify` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Content-Type** | `application/json` |
| **Path Variables** | `taskId` — String |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `gitUrl` | String | Yes | `@NotBlank`, must be valid HTTPS Git URL | Repository URL |
| `gitBranch` | String | No | Default `main` | Branch name |
| `accessToken` | String | No | — | Private repo access token |

```json
{
  "gitUrl": "https://github.com/zhangsan/book-manager.git",
  "gitBranch": "main"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "仓库验证通过",
  "data": {
    "valid": true,
    "repoName": "book-manager",
    "defaultBranch": "main",
    "branches": ["main", "develop", "feature/login"],
    "latestCommit": {
      "commitId": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0",
      "message": "feat: 完成图书借阅功能",
      "author": "张三",
      "committedAt": "2026-06-29T22:30:00.000+08:00"
    }
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 8001 | Git | 仓库地址无效，请检查是否为有效的公开仓库地址 |
| 8002 | Git | 无法访问该仓库，请检查仓库权限 |
| 8004 | Git | 指定的分支不存在 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (SubmitPage → Git URL input)
  → POST /api/v1/student/tasks/{taskId}/git-verify
  → GitController.verifyGit(taskId, GitVerifyRequest)
  → GitService.verifyRepository(gitUrl, gitBranch, accessToken)
  → JGit.lsRemoteRepository(url) → validate reachability
  → JGit.lsRemoteRepository(url).getRefs() → extract branches
  → Response: Result<GitVerifyVO>
```

---

### 4.6 Trigger AI Analysis

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Trigger AI Analysis |
| **Function** | Start AI-powered code/document analysis on a submission |
| **Permission** | Student |
| **Controller** | `AIController` |
| **Method** | `POST` |
| **URL** | `/api/v1/student/submissions/{submissionId}/ai-evaluate` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Path Variables** | `submissionId` — String |
| **Request Body** | None |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "AI分析任务已创建",
  "data": {
    "analyzeId": "6789012345678901234",
    "submissionId": "4567890123456789012",
    "status": "PENDING",
    "estimatedSeconds": 30,
    "createdAt": "2026-06-30T10:01:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 6008 | AI | AI 正在分析中，请等待分析完成 |
| 6001 | AI | AI 分析服务暂时不可用 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (SubmissionDetailPage → "开始AI分析" button)
  → POST /api/v1/student/submissions/{submissionId}/ai-evaluate
  → AIController.startAnalysis(submissionId)
  → AIService.startAnalysis(submissionId)
  → [Validate] submission.status != ANALYZING
  → AiAnalysisMapper.insert(analysisId, status=PENDING)
  → [Async] @Async AIAnalysisTask.run(analysisId)
  → PromptBuilder.buildSystemPrompt(dimensions, task.gradingRule)
      · dimensions ← task.standardId(=1000) 的四维度
      · gradingRule ← 教师任务级评分细则，非空时追加"本任务特定评分细则"段落
  → Response: Result<AIAnalyzeVO>
```

---

### 4.7 Get AI Analysis Result

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get AI Analysis Result |
| **Function** | Poll AI analysis progress and get final results |
| **Permission** | Student |
| **Controller** | `AIController` |
| **Method** | `GET` |
| **URL** | `/api/v1/student/submissions/{submissionId}/ai-result` |
| **Version** | v1 |
| **Description** | Returns analysis progress (PENDING → ANALYZING → COMPLETED/FAILED). Frontend polls this endpoint every 3-5 seconds until COMPLETED. |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Path Variables** | `submissionId` — String |

---

#### Response

**In Progress**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "analyzeId": "6789012345678901234",
    "status": "PROCESSING",
    "progress": 60,
    "currentDimension": "功能完成度",
    "result": null,
    "startedAt": "2026-06-30T10:01:05.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

**Completed**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "analyzeId": "6789012345678901234",
    "status": "COMPLETED",
    "progress": 100,
    "result": {
      "overallScore": 82,
      "dimensions": [
        {
          "dimensionName": "代码规范",
          "score": 20,
          "maxScore": 25,
          "weight": 25,
          "comment": "代码整体规范，方法命名符合Java规范，但部分方法缺少JavaDoc注释",
          "suggestions": ["建议为所有public方法添加JavaDoc注释"],
          "codeReferences": ["BookController.java:45 - 缺少方法注释"]
        }
      ],
      "summary": "该同学较好地完成了图书管理系统的基本功能开发...",
      "strengths": ["功能实现完整", "Git提交规范"],
      "weaknesses": ["代码注释不够完善"],
      "improvementPlan": "1.补充JavaDoc注释；2.增加分类筛选功能..."
    },
    "completedAt": "2026-06-30T10:01:25.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

**AI Result Fields**:

| Field | Type | Description |
|---|---|---|
| `overallScore` | Integer | AI total score (0-100) |
| `dimensions[].score` | Integer | Dimension score |
| `dimensions[].maxScore` | Integer | Dimension max score |
| `dimensions[].weight` | Integer | Weight percentage |
| `dimensions[].comment` | String | AI comment |
| `dimensions[].suggestions` | Array[String] | Improvement suggestions |
| `dimensions[].codeReferences` | Array[String] | Code locations referenced |
| `summary` | String | Overall summary |
| `strengths` | Array[String] | Strengths |
| `weaknesses` | Array[String] | Weaknesses |
| `improvementPlan` | String | Improvement plan |

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 6003 | AI | AI 分析超时 |
| 6004 | AI | AI 分析结果格式异常 |
| 6009 | AI | AI 分析已取消 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes | No | No |

---

#### Sequence

```
Frontend (poll every 5s)
  → GET /api/v1/student/submissions/{submissionId}/ai-result
  → AIController.getAIResult(submissionId)
  → AIService.getAnalysisResult(submissionId)
  → AiAnalysisMapper.selectOne(Wrappers.lambdaQuery(AiAnalysis::getSubmissionId, submissionId))
  → [PENDING/PROCESSING] → return progress
  → [COMPLETED] → AiAnalysisDetailMapper.selectByAnalysisId(analysisId)
  → AIConvert.toAIResultVO(analysis, details)
  → Response: Result<AIResultVO>
```

---

### 4.8 Get Evaluation Detail (with Teacher Review)

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get Full Evaluation |
| **Function** | Get complete evaluation including AI analysis and teacher review |
| **Permission** | Student |
| **Controller** | `SubmissionController` |
| **Method** | `GET` |
| **URL** | `/api/v1/student/submissions/{submissionId}/evaluation` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |
| **Path Variables** | `submissionId` — String |

---

#### Response

**Success (reviewed)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "submissionId": "4567890123456789012",
    "taskName": "Spring Boot 图书管理系统实训",
    "courseName": "Java Web开发",
    "submittedAt": "2026-06-30T10:00:00.000+08:00",
    "aiEvaluation": {
      "overallScore": 82,
      "summary": "该同学较好地完成了...",
      "completedAt": "2026-06-30T10:01:25.000+08:00"
    },
    "teacherEvaluation": {
      "overallScore": 80,
      "comment": "AI评价总体客观，功能完成度评分偏高，扣减2分。代码注释确实不足。",
      "dimensions": [
        { "dimensionName": "代码规范", "score": 18, "maxScore": 25 },
        { "dimensionName": "功能完成度", "score": 24, "maxScore": 30 }
      ],
      "scoredBy": "王建国",
      "scoredAt": "2026-07-01T14:00:00.000+08:00",
      "publishedAt": "2026-07-01T15:00:00.000+08:00"
    },
    "status": "COMPLETED",
    "finalScore": 80
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1001 | Business | 提交记录不存在 |
| 3003 | Permission | 无权查看此提交 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes (own) | No | No |

---

#### Sequence

```
Frontend (SubmissionDetailPage → Evaluation tab)
  → GET /api/v1/student/submissions/{submissionId}/evaluation
  → SubmissionController.getEvaluation(submissionId)
  → SubmissionService.getEvaluationDetail(userId, submissionId)
  → SubmissionMapper.selectById(submissionId) → verify ownership
  → AiAnalysisMapper.selectOne(submissionId) → AI result
  → TeacherReviewMapper.selectOne(submissionId) → teacher review
  → ScoreRecordMapper.selectOne(submissionId) → final score
  → ReviewConvert.toEvaluationVO(...)
  → Response: Result<EvaluationVO>
```

---

### 4.9 Growth Profile

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get Growth Profile |
| **Function** | Return student's cross-course competency development data |
| **Permission** | Student |
| **Controller** | `ReportController` |
| **Method** | `GET` |
| **URL** | `/api/v1/student/growth-profile` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |

**Query Parameters**:

| Field | Type | Required | Description |
|---|---|---|---|
| `semester` | String | No | Filter by semester, e.g. `2026-SPRING` |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "studentInfo": {
      "userId": "1234567890123456789",
      "realName": "张三",
      "studentNo": "20240101001",
      "className": "软件技术2401班",
      "totalCourses": 6,
      "totalTasks": 15,
      "averageScore": 84.2
    },
    "radarData": {
      "categories": ["代码规范", "功能完成度", "创新设计", "文档撰写", "团队协作", "工程素养"],
      "current": [78, 85, 72, 80, 75, 82],
      "classAverage": [72, 78, 68, 74, 70, 75]
    },
    "scoreTrend": {
      "xAxis": ["2026-03", "2026-04", "2026-05", "2026-06"],
      "series": [
        { "name": "张三", "data": [78, 82, 85, 80] },
        { "name": "班级平均", "data": [72, 74, 76, 75] }
      ]
    },
    "dimensionHistory": [
      { "dimensionName": "代码规范", "scores": [70, 75, 78, 78], "trend": "UP" }
    ],
    "recentTasks": [
      {
        "taskId": "1234567890123456789",
        "taskName": "Spring Boot 图书管理系统实训",
        "courseName": "Java Web开发",
        "score": 80,
        "completedAt": "2026-07-01T15:00:00.000+08:00"
      }
    ]
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes (own) | No | No |

---

#### Sequence

```
Frontend (GrowthProfilePage)
  → GET /api/v1/student/growth-profile
  → ReportController.getGrowthProfile()
  → ReportService.getGrowthProfile(userId)
  → ScoreRecordMapper.selectList(userId) → aggregate dimension scores
  → ScoreRecordMapper.groupByMonth(userId) → trend data
  → StatisticsSnapshotMapper.selectOne(DASHBOARD, classId) → class averages
  → ReportConvert.toGrowthProfileVO(...)
  → Response: Result<GrowthProfileVO>
```

---

### 4.10 Export Personal Report (PDF)

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Export Personal Report |
| **Function** | Download personal growth report as PDF |
| **Permission** | Student |
| **Controller** | `ReportController` |
| **Method** | `GET` |
| **URL** | `/api/v1/student/reports/export` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (student) |

**Query Parameters**:

| Field | Type | Required | Description |
|---|---|---|---|
| `semester` | String | No | Filter by semester |

---

#### Response

**Success (200)**: Binary stream

- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="成长报告_张三_2026春季学期.pdf"`

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 9001 | Export | 报表导出失败 |
| 9003 | Export | 报表导出超时 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| Yes (own) | No | No |

---

#### Sequence

```
Frontend (GrowthProfilePage → Export button)
  → GET /api/v1/student/reports/export?semester=2026-SPRING
  → ReportController.exportPersonalReport(semester)
  → ReportService.generatePersonalReport(userId, semester)
  → [Async] ReportGenerator.generate(userId, semester) → OpenPDF
  → MinIO.upload(report, "reports")
  → ReportMapper.insert(reportEntity)
  → Response: ResponseEntity<Resource> (binary PDF)
```

---

## 5. Teacher APIs

**Base Path**: `/api/v1/teacher`  
**Role**: `teacher`

### 5.1 Dashboard Statistics

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Teacher Dashboard |
| **Function** | Return teacher's homepage statistics overview |
| **Permission** | Teacher |
| **Controller** | `TeacherDashboardController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/dashboard` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Query Parameters** | None |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "overview": {
      "totalClasses": 3,
      "totalStudents": 120,
      "totalTasks": 8,
      "submissionRate": 0.85,
      "gradingRate": 0.62,
      "averageScore": 78.5,
      "pendingReviews": 15
    },
    "classRankings": [
      { "classId": "1111111111111111111", "className": "软件技术2401班", "averageScore": 82.3, "submissionRate": 0.92 }
    ],
    "recentSubmissions": [
      { "submissionId": "...", "studentName": "张三", "taskName": "...", "status": "AI_COMPLETED", "submittedAt": "..." }
    ],
    "deadlineTasks": [
      { "taskId": "...", "taskName": "...", "deadline": "...", "submissionCount": 35, "totalStudents": 40 }
    ]
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Sequence

```
Frontend (TeacherDashboardPage)
  → GET /api/v1/teacher/dashboard
  → TeacherDashboardController.dashboard()
  → TeacherDashboardService.getDashboard(teacherId)
  → StatisticsSnapshotMapper.selectOne(DASHBOARD, null, latest) → overview
  → CourseMapper.countByTeacher(teacherId) → totalClasses
  → TrainingTaskMapper.countByTeacher(teacherId) → totalTasks
  → TeacherReviewMapper.count(Wrappers.lambdaQuery(TeacherReview::getStatus, "PENDING")) → pendingReviews
  → SubmissionMapper.selectLatest(teacherId, limit=5) → recent
  → Response: Result<TeacherDashboardVO>
```

---

### 5.2 List Classes

---

#### API Information

| Field | Value |
|---|---|
| **Name** | List My Classes |
| **Function** | Return paginated list of classes taught by the teacher |
| **Permission** | Teacher |
| **Controller** | `TeacherClassController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/classes` |
| **Version** | v1 |

---

#### Request

**Query Parameters**: `page`, `pageSize`, `keyword`

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "classId": "1111111111111111111",
        "className": "软件技术2401班",
        "college": "信息工程学院",
        "studentCount": 40,
        "courseName": "Java Web开发",
        "semester": "2026-SPRING",
        "createdAt": "2026-03-01T00:00:00.000+08:00"
      }
    ],
    "page": 1, "pageSize": 20, "total": 3, "totalPages": 1
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| No | Yes | No |

---

#### Sequence

```
Frontend (ClassListPage)
  → GET /api/v1/teacher/classes?page=1
  → TeacherClassController.listClasses(page, keyword)
  → ClassService.listTeacherClasses(teacherId, page, keyword)
  → CourseTeacherMapper.selectByUserId(teacherId) → courseIds
  → CourseClassMapper.selectByCourseIds(courseIds) → classIds
  → ClassMapper.selectPage(page, classIds)
  → Response: PageResult<TeacherClassVO>
```

---

### 5.3 Get Class Detail

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get Class Detail |
| **Function** | Return class details including task list and student stats |
| **Permission** | Teacher |
| **Controller** | `TeacherClassController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/classes/{classId}` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Path Variables** | `classId` — String |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "classId": "1111111111111111111",
    "className": "软件技术2401班",
    "college": "信息工程学院",
    "studentCount": 40,
    "courseName": "Java Web开发",
    "semester": "2026-SPRING",
    "tasks": [
      { "taskId": "...", "taskName": "...", "deadline": "...", "status": "PUBLISHED", "submissionRate": 0.88 }
    ]
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

### 5.4 List Tasks (Teacher View)

---

#### API Information

| Field | Value |
|---|---|
| **Name** | List Teacher Tasks |
| **Function** | Return paginated list of tasks created by the teacher |
| **Permission** | Teacher |
| **Controller** | `TeacherTaskController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/tasks` |
| **Version** | v1 |

---

#### Request

**Query Parameters**: `page`, `pageSize`, `keyword`, `status`, `classId`, `courseId`

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "taskId": "1234567890123456789",
        "taskName": "Spring Boot 图书管理系统实训",
        "courseName": "Java Web开发",
        "classCount": 2,
        "totalStudents": 80,
        "submittedCount": 70,
        "gradedCount": 50,
        "status": "PUBLISHED",
        "deadline": "2026-07-15T23:59:59.000+08:00",
        "createdAt": "2026-06-25T10:00:00.000+08:00"
      }
    ],
    "page": 1, "pageSize": 20, "total": 8, "totalPages": 1
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

### 5.5 Create Task

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Create Training Task |
| **Function** | Create and publish a new training task to classes |
| **Permission** | Teacher |
| **Controller** | `TeacherTaskController` |
| **Method** | `POST` |
| **URL** | `/api/v1/teacher/tasks` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Content-Type** | `application/json` |

**Request Body**:

| Field | Type | Required | Validation | Description |
|---|---|---|---|---|
| `taskName` | String | Yes | `@NotBlank`, max 256 chars | Task name |
| `courseId` | String | Yes | `@NotBlank` | Course ID |
| `submissionType` | String | Yes | `@NotBlank`, enum: `GIT_URL`/`ZIP_ONLY`/`ONLINE_CODE`/`FILE_UPLOAD` | Submission method |
| `maxSubmitCount` | Integer | Yes | `@NotNull` | Max submission attempts |
| `totalScore` | Number | Yes | `@NotNull` | Total score |
| `description` | String | No | — | Task description (Markdown) |
| `requirement` | String | No | — | Task requirement (Markdown) |
| `endTime` | String | No | ISO 8601 datetime | Submission deadline |
| `allowLate` | Integer | No | 0=不允许, 1=允许 | Whether late submission allowed |
| `gradingRule` | String | No | — | 教师自定义评分细则（R/S/R/O 原文，`Role：…\nSkill：…\nRule：…` 拼接，注入 AI 提示） |
| `trainingClassId` | Long | No | — | Target class ID |

> **融合说明**：`standardId` 不再由前端传入。后端在 `createTask` 时固定写入系统默认标准 `id=1000`（提供四维评分框架）。教师的个性化评分诉求通过 `gradingRule` 表达。

```json
{
  "taskName": "Spring Boot 图书管理系统实训",
  "courseId": "1",
  "submissionType": "GIT_URL",
  "maxSubmitCount": 3,
  "totalScore": 100,
  "description": "使用 Spring Boot + MyBatis + MySQL 实现图书管理系统的增删改查功能...",
  "endTime": "2026-07-15T23:59:59",
  "gradingRule": "Role：资深阅卷教师\nSkill：精准打分\nRule：命名不规范扣2分"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "任务创建成功",
  "data": {
    "taskId": "1234567890123456790",
    "taskName": "Spring Boot 图书管理系统实训",
    "status": "PUBLISHED",
    "createdAt": "2026-06-30T14:30:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 4001 | Validation | 任务名称不能为空 |
| 4001 | Validation | 截止时间不能早于当前时间 |
| 3003 | Permission | 无权操作此课程 |

---

#### Permission

| Student | Teacher | Admin |
|---|---|---|
| No | Yes | No |

---

#### Sequence

```
Frontend (TaskCreatePage)
  → POST /api/v1/teacher/tasks
  → TeacherTaskController.createTask(@Valid CreateTaskRequest)
  → TrainingTaskService.createTask(teacherId, request)
  → [Validate] CourseService.verifyTeacher(teacherId, courseId)
  → [Transaction] TrainingTaskMapper.insert(task)
  → TrainingClassMapper.insert(taskId, classIds)
  → [Event] ApplicationEventPublisher.publish(TaskPublishedEvent)
  → [Async] NotificationListener.sendTaskNotification(taskId, classIds)
  → Response: Result<TaskVO>
```

---

### 5.6 Update Task

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Update Task |
| **Function** | Update an existing training task |
| **Permission** | Teacher |
| **Controller** | `TeacherTaskController` |
| **Method** | `PUT` |
| **URL** | `/api/v1/teacher/tasks/{taskId}` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Content-Type** | `application/json` |
| **Path Variables** | `taskId` — String |

**Request Body**: Same structure as Create, all fields optional (partial update semantics used — only sent fields are updated).

> **融合说明**：`gradingRule` 采用非空判定更新（`dto.getGradingRule() != null` 时才写入）。编辑弹窗回填依赖列表/详情接口回传的 `gradingRule` 原文，避免以空串覆盖已存细则。`standardId` 不参与更新，始终保持系统默认标准。

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "更新成功",
  "data": { "taskId": "...", "taskName": "...", "updatedAt": "..." },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1003 | Business | 当前任务状态下不允许修改（已有学生提交） |
| 3003 | Permission | 无权修改此任务 |

---

### 5.7 Delete Task

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Delete Task |
| **Function** | Logically delete a training task |
| **Permission** | Teacher |
| **Controller** | `TeacherTaskController` |
| **Method** | `DELETE` |
| **URL** | `/api/v1/teacher/tasks/{taskId}` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Path Variables** | `taskId` — String |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null,
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1003 | Business | 无法删除：已有学生完成提交 |

---

### 5.8 List Class Students

---

#### API Information

| Field | Value |
|---|---|
| **Name** | List Class Students |
| **Function** | Return students in a class with submission statistics |
| **Permission** | Teacher |
| **Controller** | `TeacherClassController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/classes/{classId}/students` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Path Variables** | `classId` — String |
| **Query Parameters** | `page`, `pageSize`, `keyword`, `taskId` (optional, to show per-task stats) |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": "...",
        "studentNo": "20240101001",
        "realName": "张三",
        "email": "zhangsan@example.com",
        "submissionStatus": "COMPLETED",
        "score": 80,
        "submittedAt": "2026-06-30T10:00:00.000+08:00"
      }
    ],
    "page": 1, "pageSize": 20, "total": 40, "totalPages": 2
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

### 5.9 List Submissions for Task

---

#### API Information

| Field | Value |
|---|---|
| **Name** | List Submissions |
| **Function** | Return all submissions for a task with status and scores |
| **Permission** | Teacher |
| **Controller** | `TeacherSubmissionController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/tasks/{taskId}/submissions` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Path Variables** | `taskId` — String |
| **Query Parameters** | `page`, `pageSize`, `status`, `keyword`, `sortField`, `sortOrder` |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "submissionId": "4567890123456789012",
        "studentName": "张三",
        "studentNo": "20240101001",
        "className": "软件技术2401班",
        "submitType": "GIT_URL",
        "status": "AI_COMPLETED",
        "aiScore": 82,
        "finalScore": null,
        "reviewStatus": "PENDING",
        "submitCount": 1,
        "submittedAt": "2026-06-30T10:00:00.000+08:00"
      }
    ],
    "page": 1, "pageSize": 20, "total": 70, "totalPages": 4
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

### 5.10 Get AI Preview

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get AI Analysis Preview |
| **Function** | Preview AI analysis results for teacher review |
| **Permission** | Teacher |
| **Controller** | `TeacherReviewController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/submissions/{submissionId}/ai-preview` |
| **Version** | v1 |

---

#### Response

Returns the complete AI analysis result (same structure as Student 4.7 AI Result), plus detailed deduction items (`ai_analysis_detail` records) for teacher to review and approve/reject/adjust.

Key additional fields:

| Field | Type | Description |
|---|---|---|
| `details[]` | Array | Individual deduction items from AI |
| `details[].detailId` | String | Deduction item ID |
| `details[].issueType` | String | Issue type |
| `details[].severity` | String | `MINOR` / `MAJOR` / `CRITICAL` |
| `details[].filePath` | String | Source file path |
| `details[].lineNumber` | Integer | Line number |
| `details[].reason` | String | Deduction reason |
| `details[].suggestion` | String | Improvement suggestion |
| `details[].suggestDeduct` | Decimal | Suggested deduction score |
| `details[].confidence` | Decimal | AI confidence (0.00-1.00) |

---

### 5.11 Submit Manual Score

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Submit Teacher Score |
| **Function** | Teacher reviews AI deductions and submits final score |
| **Permission** | Teacher |
| **Controller** | `TeacherReviewController` |
| **Method** | `PUT` |
| **URL** | `/api/v1/teacher/submissions/{submissionId}/score` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Content-Type** | `application/json` |
| **Path Variables** | `submissionId` — String |

**Request Body**:

| Field | Type | Required | Description |
|---|---|---|---|
| `overallComment` | String | No | Teacher's overall comment |
| `items` | Array | Yes | Reviewed deduction items |
| `items[].detailId` | String | Conditional | AI detail ID (null for manual additions) |
| `items[].action` | String | Yes | `ADOPT` / `REJECT` / `ADJUST` / `MANUAL_ADD` |
| `items[].deductScore` | Decimal | Yes | Final deduction score |
| `items[].reason` | String | No | Teacher's reason for adjustment |
| `items[].dimensionId` | String | No | Dimension ID (required for MANUAL_ADD) |

```json
{
  "overallComment": "AI评价总体客观，功能完成度评分偏高，扣减2分。",
  "items": [
    { "detailId": "d1", "action": "ADOPT", "deductScore": 5.0, "reason": "" },
    { "detailId": "d2", "action": "ADJUST", "deductScore": 6.0, "reason": "缺少分类查询功能影响较大" },
    { "detailId": null, "action": "MANUAL_ADD", "deductScore": 2.0, "dimensionId": "dim3", "reason": "前段代码缺少错误处理" }
  ]
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "评分已保存",
  "data": {
    "submissionId": "...",
    "totalScore": 80,
    "aiScore": 82,
    "status": "REVIEWING",
    "updatedAt": "2026-07-01T14:00:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1003 | Business | 该提交状态不允许评分 |
| 1008 | Business | 成绩已发布不可修改 |

---

#### Sequence

```
Frontend (ReviewWorkbench → Score)
  → PUT /api/v1/teacher/submissions/{submissionId}/score
  → TeacherReviewController.score(submissionId, @Valid ScoreRequest)
  → ReviewService.submitScore(teacherId, submissionId, request)
  → [Transaction]
    → TeacherReviewMapper.update(review, status=REVIEWING)
    → ReviewItemMapper.deleteByReviewId(reviewId)  // remove old items
    → ReviewItemMapper.insert(items)  // insert reviewed items
    → Aggregate finalScore = totalScore - sum(deductScores)
    → TeacherReviewMapper.update(totalScore)
  → Response: Result<ReviewVO>
```

---

### 5.12 Publish Scores

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Publish Scores |
| **Function** | Publish final scores for all reviewed submissions in a task |
| **Permission** | Teacher |
| **Controller** | `TeacherReviewController` |
| **Method** | `POST` |
| **URL** | `/api/v1/teacher/tasks/{taskId}/publish-scores` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Path Variables** | `taskId` — String |
| **Request Body** | None |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "成绩发布成功",
  "data": {
    "publishedCount": 65,
    "failedCount": 0,
    "publishedAt": "2026-07-01T15:00:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 1003 | Business | 存在未完成复核的提交，请先完成所有复核 |

---

#### Sequence

```
Frontend (TaskDetail → "发布成绩" button)
  → POST /api/v1/teacher/tasks/{taskId}/publish-scores
  → TeacherReviewController.publishScores(taskId)
  → ReviewService.publishScores(teacherId, taskId)
  → [Transaction, REPEATABLE_READ]
    → TeacherReviewMapper.selectPublishedReviews(taskId)
    → [For each review] ScoreRecordMapper.insert(scoreRecord)
    → TeacherReviewMapper.updateStatus(PUBLISHED)
    → SubmissionMapper.updateStatus(REVIEWED)
  → [Event] ScoresPublishedEvent
  → [Async] NotificationListener.sendScoreNotifications(userIds)
  → Redis.del("stats:dashboard:teacher")
  → Redis.del("stats:progress:" + taskId)
  → Response: Result<PublishVO>
```

---

### 5.13 Reject Submission

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Reject Submission |
| **Function** | Return a submission to the student for revision |
| **Permission** | Teacher |
| **Controller** | `TeacherReviewController` |
| **Method** | `POST` |
| **URL** | `/api/v1/teacher/submissions/{submissionId}/reject` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Content-Type** | `application/json` |
| **Path Variables** | `submissionId` — String |

**Request Body**:

| Field | Type | Required | Description |
|---|---|---|---|
| `rejectReason` | String | Yes | Reason for rejection (shown to student) |
| `resubmitDeadline` | String | No | Extended deadline for resubmission |

```json
{
  "rejectReason": "代码缺少单元测试，请补充至少80%覆盖率的测试用例后重新提交",
  "resubmitDeadline": "2026-07-05T23:59:59.000+08:00"
}
```

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "已退回",
  "data": {
    "submissionId": "...",
    "status": "REJECTED",
    "rejectedAt": "2026-07-01T14:30:00.000+08:00"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

### 5.14 Class Report

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Get Class Report |
| **Function** | Return class-level statistics report |
| **Permission** | Teacher |
| **Controller** | `ReportController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/classes/{classId}/report` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Path Variables** | `classId` — String |
| **Query Parameters** | `taskId` (optional), `semester` (optional) |

---

#### Response

Returns aggregated class statistics: score distribution, dimension averages, submission rate, pass rate, ranking within class.

---

### 5.15 Export Excel

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Export Task Scores to Excel |
| **Function** | Download task score sheet as Excel file |
| **Permission** | Teacher |
| **Controller** | `ReportController` |
| **Method** | `GET` |
| **URL** | `/api/v1/teacher/tasks/{taskId}/export-excel` |
| **Version** | v1 |

---

#### Response

**Success (200)**: Binary stream

- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename="成绩表_Spring_Boot图书管理系统实训.xlsx"`

---

#### Sequence

```
Frontend (TaskDetail → "导出Excel" button)
  → GET /api/v1/teacher/tasks/{taskId}/export-excel
  → ReportController.exportExcel(taskId)
  → ReportService.exportTaskExcel(taskId)
  → [Async] ScoreRecordMapper.selectByTaskId(taskId)
  → EasyExcel.write(stream, ScoreExcelVO.class).sheet("成绩表").doWrite(data)
  → FileStorageMapper.insert(fileEntity) → MinIO.upload(excelFile, "reports")
  → Response: ResponseEntity<Resource>
```

---

### 5.16 Remind Students

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Send Submission Reminder |
| **Function** | Send notification to students who haven't submitted |
| **Permission** | Teacher |
| **Controller** | `NotificationController` |
| **Method** | `POST` |
| **URL** | `/api/v1/teacher/tasks/{taskId}/remind` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required (teacher) |
| **Path Variables** | `taskId` — String |
| **Query Parameters** | `classId` (optional, defaults to all assigned classes) |

**Request Body**: None

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "催交通知已发送",
  "data": { "notifiedCount": 10 },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

## 6. Admin APIs

**Base Path**: `/api/v1/admin`  
**Role**: `admin`

### 6.1 Dashboard

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Admin Dashboard |
| **Function** | Return system-wide statistics for admin homepage |
| **Permission** | Admin |
| **Controller** | `AdminDashboardController` |
| **Method** | `GET` |
| **URL** | `/api/v1/admin/dashboard` |
| **Version** | v1 |

---

#### Response

Key data blocks: total users (by role), total courses, total tasks, overall submission rate, AI analysis stats (success rate, avg score), system health (DB/Redis/MinIO/AI service status), recent operations log.

---

### 6.2 User Management

#### 6.2.1 List Users

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/users` |
| **Query** | `page`, `pageSize`, `roleCode`, `keyword`, `status` |

#### 6.2.2 Create User

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/admin/users` |
| **Body** | `username`, `password`, `realName`, `roleCode`, `email`, `phone`, `studentNo` (if student), `classId` (if student) |

#### 6.2.3 Get User Detail

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/users/{userId}` |

#### 6.2.4 Update User

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/users/{userId}` |
| **Body** | `realName`, `email`, `phone`, `status`, `roleCode` |

#### 6.2.5 Delete User

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/users/{userId}` |

#### 6.2.6 Batch Import Users

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/admin/users/batch-import` |
| **Content-Type** | `multipart/form-data` |
| **Form Data** | `file` (Excel file) |

---

### 6.3 Class Management

#### 6.3.1 List Classes

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/classes` |
| **Query** | `page`, `pageSize`, `keyword`, `grade`, `major` |

#### 6.3.2 Create Class

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/admin/classes` |
| **Body** | `classCode`, `className`, `grade`, `major`, `department` |

#### 6.3.3 Update Class

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/classes/{classId}` |

#### 6.3.4 Delete Class

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/admin/classes/{classId}` |

---

### 6.4 Course Management

#### 6.4.1 List All Courses

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/courses` |
| **Query** | `page`, `pageSize`, `semester`, `status`, `keyword` |

---

### 6.5 System Configuration

#### 6.5.1 Get Configs

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/system/config` |

Returns all `system_config` key-value pairs.

#### 6.5.2 Update Config

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/admin/system/config` |
| **Body** | Map of config key-value pairs |

```json
{
  "configs": {
    "current_semester": "2026-2027-1",
    "max_upload_size": "100",
    "auto_analyze": "true",
    "maintenance_mode": "false"
  }
}
```

---

### 6.6 Operation Logs

#### 6.6.1 List Logs

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/logs` |
| **Query** | `page`, `pageSize`, `module`, `operation`, `userId`, `startDate`, `endDate`, `result` |

#### 6.6.2 Get Log Detail

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/logs/{logId}` |

---

### 6.7 System Statistics

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/admin/statistics` |
| **Query** | `startDate`, `endDate`, `type` (`DASHBOARD`/`CLASS_PROGRESS`/`AI_QUALITY`) |

---

## 7. File APIs

**Base Path**: `/api/v1/files`  
**Permission**: Authenticated

### 7.1 Upload File

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Upload File |
| **Function** | Upload a file to MinIO and return file metadata |
| **Permission** | Authenticated |
| **Controller** | `FileController` |
| **Method** | `POST` |
| **URL** | `/api/v1/files/upload` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required |
| **Content-Type** | `multipart/form-data` |

**Form Data**:

| Field | Type | Required | Validation |
|---|---|---|---|
| `file` | File | Yes | Max 50MB, whitelisted extensions |
| `bucket` | String | No | Default: `submissions` |

---

#### Response

**Success (200)**:

```json
{
  "code": 0,
  "message": "上传成功",
  "data": {
    "fileId": "3456789012345678901",
    "originalName": "project.zip",
    "fileSize": 2048000,
    "contentType": "application/zip",
    "fileMd5": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
    "accessUrl": "/api/v1/files/3456789012345678901/download"
  },
  "success": true,
  "timestamp": 1716710400000,
  "traceId": "a1b2c3d4"
}
```

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 7001 | File | 文件大小不能超过 50MB |
| 7002 | File | 不支持的文件类型 |
| 7003 | File | 文件上传失败 |

---

#### Sequence

```
Frontend (FileUpload component)
  → POST /api/v1/files/upload (multipart/form-data)
  → FileController.upload(file, bucket)
  → FileService.upload(file, bucket)
  → [Validate] magic number check, extension whitelist, size limit
  → MinioClient.putObject(bucket, objectKey, stream, metadata)
  → FileStorageMapper.insert(fileEntity)
  → Response: Result<FileVO>
```

---

### 7.2 Download File

---

#### API Information

| Field | Value |
|---|---|
| **Name** | Download File |
| **Function** | Download a file by ID (with access control) |
| **Permission** | Authenticated |
| **Controller** | `FileController` |
| **Method** | `GET` |
| **URL** | `/api/v1/files/{fileId}/download` |
| **Version** | v1 |

---

#### Request

| Element | Value |
|---|---|
| **Authorization** | Required |
| **Path Variables** | `fileId` — String |

---

#### Response

**Success (200)**: Binary stream with Content-Type from `file_storage.content_type`.

---

#### Error Codes

| Code | Type | Message |
|---|---|---|
| 7004 | File | 文件不存在或已被删除 |
| 3003 | Permission | 无权访问此文件 |

---

#### Sequence

```
Frontend (Download link)
  → GET /api/v1/files/{fileId}/download
  → FileController.download(fileId)
  → FileService.getDownloadStream(fileId, userId)
  → FileStorageMapper.selectById(fileId)
  → [Permission check] verify user has access to this file
  → MinioClient.getPresignedObjectUrl(bucket, objectKey, 15min TTL)
  → Response: ResponseEntity<Resource> (binary stream)
```

---

### 7.3 Preview File

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/files/{fileId}/preview` |

Returns inline-disposition binary stream (for images, PDF preview in browser).

---

## 8. Notification APIs

**Base Path**: `/api/v1/student/notifications` or `/api/v1/user/notifications`

### 8.1 List Notifications

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/user/notifications` |
| **Query** | `page`, `pageSize`, `isRead`, `type` |

### 8.2 Mark as Read

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/user/notifications/{notificationId}/read` |

### 8.3 Mark All as Read

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/user/notifications/read-all` |

### 8.4 Unread Count

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/user/notifications/unread-count` |

---

## 9. Error Code Reference

### 9.1 Code Ranges

| Range | Category | Description |
|---|---|---|
| 0 | Success | Request completed successfully |
| 1000-1999 | Business | Business logic errors |
| 2000-2999 | Auth | Authentication errors |
| 3000-3999 | Permission | Authorization errors |
| 4000-4999 | Validation | Parameter validation errors |
| 5000-5999 | System | Server internal errors |
| 6000-6999 | AI | AI analysis errors |
| 7000-7999 | File | File upload/download errors |
| 8000-8999 | Git | Git operation errors |
| 9000-9999 | Export | Report export errors |

### 9.2 Complete Error Code Catalog

#### Business (1000-1999)

| Code | Message | Description |
|---|---|---|
| 1001 | 资源不存在 | Requested resource not found |
| 1002 | 资源已存在 | Duplicate resource |
| 1003 | 操作不允许 | State transition not allowed |
| 1004 | 数据已被修改 | Optimistic lock conflict |
| 1005 | 提交次数已达上限 | Submission limit exceeded |
| 1006 | 任务已截止 | Deadline passed |
| 1007 | 账号不存在 | User account not found |
| 1008 | 成绩已发布不可修改 | Score already published |

#### Auth (2000-2999)

| Code | Message | Description |
|---|---|---|
| 2001 | 请先登录 | Not logged in |
| 2002 | Token 已过期 | Access token expired |
| 2003 | 登录凭证无效 | Token invalid/forged |
| 2004 | 用户名或密码错误 | Bad credentials |
| 2005 | 账号已锁定 | Account locked (5 failed attempts) |
| 2006 | 账号已被禁用 | Account disabled |
| 2007 | 验证码错误 | CAPTCHA mismatch |
| 2008 | 登录已过期 | Refresh token expired |

#### Permission (3000-3999)

| Code | Message | Description |
|---|---|---|
| 3001 | 无权限访问 | Role insufficient |
| 3002 | 角色不存在 | Role not in system |
| 3003 | 无权访问该资源 | Resource-level permission denied |
| 3004 | IP 限制 | IP not in whitelist |

#### Validation (4000-4999)

| Code | Message | Description |
|---|---|---|
| 4001 | 参数校验失败 | Generic validation failure |
| 4002 | 参数格式错误 | Format invalid |
| 4003 | 参数值非法 | Value out of range |
| 4004 | 字段长度超限 | String too long |
| 4005 | 必填字段为空 | Required field missing |
| 4006 | 数据已过期 | Stale data |

#### System (5000-5999)

| Code | Message | Description |
|---|---|---|
| 5001 | 服务器繁忙 | Internal server error |
| 5002 | 数据服务异常 | Database error |
| 5003 | 缓存服务异常 | Redis error |
| 5004 | 第三方服务不可用 | External service down |
| 5005 | 请求超时 | Request timeout |
| 5006 | 操作过于频繁 | Rate limit hit |
| 5007 | 服务降级中 | Circuit breaker open |

#### AI (6000-6999)

| Code | Message | Description |
|---|---|---|
| 6001 | AI 服务不可用 | AI service unreachable |
| 6002 | AI 模型调用失败 | LLM API error |
| 6003 | AI 分析超时 | Analysis exceeded 120s |
| 6004 | AI 返回格式异常 | JSON parse failure |
| 6005 | Token 额度不足 | LLM quota exhausted |
| 6006 | Prompt 模板异常 | Template syntax error |
| 6007 | 模型不存在 | Model name not found |
| 6008 | AI 正在分析中 | Duplicate analysis request |
| 6009 | AI 分析已取消 | Analysis cancelled |

#### File (7000-7999)

| Code | Message | Description |
|---|---|---|
| 7001 | 文件大小超限 | Size exceeds max |
| 7002 | 文件类型不支持 | Type not whitelisted |
| 7003 | 文件上传失败 | Upload error |
| 7004 | 文件不存在 | File not found in MinIO |
| 7005 | 文件已损坏 | Corrupted file |
| 7006 | 文件数量超限 | Too many files |

#### Git (8000-8999)

| Code | Message | Description |
|---|---|---|
| 8001 | 仓库地址无效 | Repo URL unreachable |
| 8002 | 无法访问该仓库 | No permission to repo |
| 8003 | 仓库克隆失败 | Clone error |
| 8004 | 分支不存在 | Branch not found |

#### Export (9000-9999)

| Code | Message | Description |
|---|---|---|
| 9001 | 报表导出失败 | Export generation failed |
| 9002 | 导出数据量过大 | Too many records |
| 9003 | 报表导出超时 | Export timed out |
| 9004 | 导出格式不支持 | Unsupported format |

---

## Appendix A: Cross-Document Consistency Matrix

| This Document | References | Status |
|---|---|---|
| 1. General Conventions | 00-Architecture-Baseline §3-6, 12-Backend-Spec §4-12 | Consistent |
| 2. Auth Module | 06-API-Mock §5 | Consistent (Sa-Token replaces Spring Security) |
| 4. Student APIs | 06-API-Mock §6 | Consistent (reorganized to 6-section format) |
| 5. Teacher APIs | 06-API-Mock §7-8 | Consistent (research director merged into teacher per ADR-011) |
| 6. Admin APIs | 06-API-Mock §9 | Consistent |
| 7. File APIs | 06-API-Mock §11, 12-Backend-Spec §19 | Consistent |
| 9. Error Codes | 06-API-Mock §4, 12-Backend-Spec §11.2 | Consistent |
| Response Format | 00-Architecture-Baseline §6.6, 12-Backend-Spec §12 | Consistent (Result\<T\> + PageResult\<T\>) |
| ID Serialization | 00-Architecture-Baseline §6.9, 06-API-Mock §1.14 | Consistent (Snowflake as String) |
| Role Model | 00-Architecture-Baseline §4 | Consistent (3 roles) |
| API Paths | 00-Architecture-Baseline §4 | Consistent (6 path prefixes) |
| Module List | 00-Architecture-Baseline §5.3 | Consistent (14 modules) |
| Technology | 00-Architecture-Baseline §3 | Consistent (Sa-Token, MyBatis-Plus, MinIO, JGit, EasyExcel, OpenPDF) |

---

## Appendix B: API Index (Quick Lookup)

| # | Module | Method | URL | Permission |
|---|---|---|---|---|
| 1 | Auth | POST | `/api/v1/auth/login` | Public |
| 2 | Auth | POST | `/api/v1/auth/logout` | Authenticated |
| 3 | Auth | POST | `/api/v1/auth/refresh` | Public |
| 4 | User | GET | `/api/v1/user/me` | Authenticated |
| 5 | User | PUT | `/api/v1/user/me` | Authenticated |
| 6 | User | PUT | `/api/v1/user/me/password` | Authenticated |
| 7 | Student | GET | `/api/v1/student/tasks` | Student |
| 8 | Student | GET | `/api/v1/student/tasks/{taskId}` | Student |
| 9 | Student | POST | `/api/v1/student/tasks/{taskId}/submissions` | Student |
| 10 | Student | POST | `/api/v1/student/tasks/{taskId}/reports` | Student |
| 11 | Student | POST | `/api/v1/student/tasks/{taskId}/git-verify` | Student |
| 12 | Student | POST | `/api/v1/student/submissions/{submissionId}/ai-evaluate` | Student |
| 13 | Student | GET | `/api/v1/student/submissions/{submissionId}/ai-result` | Student |
| 14 | Student | GET | `/api/v1/student/submissions/{submissionId}/evaluation` | Student |
| 15 | Student | GET | `/api/v1/student/growth-profile` | Student |
| 16 | Student | GET | `/api/v1/student/reports/export` | Student |
| 17 | Teacher | GET | `/api/v1/teacher/dashboard` | Teacher |
| 18 | Teacher | GET | `/api/v1/teacher/classes` | Teacher |
| 19 | Teacher | GET | `/api/v1/teacher/classes/{classId}` | Teacher |
| 20 | Teacher | GET | `/api/v1/teacher/tasks` | Teacher |
| 21 | Teacher | POST | `/api/v1/teacher/tasks` | Teacher |
| 22 | Teacher | PUT | `/api/v1/teacher/tasks/{taskId}` | Teacher |
| 23 | Teacher | DELETE | `/api/v1/teacher/tasks/{taskId}` | Teacher |
| 24 | Teacher | GET | `/api/v1/teacher/classes/{classId}/students` | Teacher |
| 25 | Teacher | GET | `/api/v1/teacher/tasks/{taskId}/submissions` | Teacher |
| 26 | Teacher | GET | `/api/v1/teacher/submissions/{submissionId}/ai-preview` | Teacher |
| 27 | Teacher | PUT | `/api/v1/teacher/submissions/{submissionId}/score` | Teacher |
| 28 | Teacher | POST | `/api/v1/teacher/tasks/{taskId}/publish-scores` | Teacher |
| 29 | Teacher | POST | `/api/v1/teacher/submissions/{submissionId}/reject` | Teacher |
| 30 | Teacher | GET | `/api/v1/teacher/classes/{classId}/report` | Teacher |
| 31 | Teacher | GET | `/api/v1/teacher/tasks/{taskId}/export-excel` | Teacher |
| 32 | Teacher | POST | `/api/v1/teacher/tasks/{taskId}/remind` | Teacher |
| 33 | Admin | GET | `/api/v1/admin/dashboard` | Admin |
| 34 | Admin | GET | `/api/v1/admin/users` | Admin |
| 35 | Admin | POST | `/api/v1/admin/users` | Admin |
| 36 | Admin | GET | `/api/v1/admin/users/{userId}` | Admin |
| 37 | Admin | PUT | `/api/v1/admin/users/{userId}` | Admin |
| 38 | Admin | DELETE | `/api/v1/admin/users/{userId}` | Admin |
| 39 | Admin | POST | `/api/v1/admin/users/batch-import` | Admin |
| 40 | Admin | GET | `/api/v1/admin/classes` | Admin |
| 41 | Admin | POST | `/api/v1/admin/classes` | Admin |
| 42 | Admin | PUT | `/api/v1/admin/classes/{classId}` | Admin |
| 43 | Admin | DELETE | `/api/v1/admin/classes/{classId}` | Admin |
| 44 | Admin | GET | `/api/v1/admin/courses` | Admin |
| 45 | Admin | GET | `/api/v1/admin/system/config` | Admin |
| 46 | Admin | PUT | `/api/v1/admin/system/config` | Admin |
| 47 | Admin | GET | `/api/v1/admin/logs` | Admin |
| 48 | Admin | GET | `/api/v1/admin/logs/{logId}` | Admin |
| 49 | Admin | GET | `/api/v1/admin/statistics` | Admin |
| 50 | File | POST | `/api/v1/files/upload` | Authenticated |
| 51 | File | GET | `/api/v1/files/{fileId}/download` | Authenticated |
| 52 | File | GET | `/api/v1/files/{fileId}/preview` | Authenticated |
| 53 | Notification | GET | `/api/v1/user/notifications` | Authenticated |
| 54 | Notification | PUT | `/api/v1/user/notifications/{id}/read` | Authenticated |
| 55 | Notification | PUT | `/api/v1/user/notifications/read-all` | Authenticated |
| 56 | Notification | GET | `/api/v1/user/notifications/unread-count` | Authenticated |

---

*This document is the Single Source of Truth for all API interfaces in the B1 Platform. All frontend Axios calls, backend Controller implementations, Mock.js simulations, and OpenAPI auto-generation must conform to this contract. In case of conflict with any other document, this contract takes precedence for API definitions.*
