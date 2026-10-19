# 图书管理 API 设计样例（演示素材）

## 基础路径

`/api/v1/books`

## 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /books | 分页查询 |
| POST | /books | 新增图书 |
| PUT | /books/{id} | 更新 |
| DELETE | /books/{id} | 删除 |

## 统一响应

```json
{ "code": 200, "message": "ok", "data": {} }
```
