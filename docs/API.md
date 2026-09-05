# REST API 说明

## 基本约定

- 基础地址：`http://localhost:8080`
- 接口前缀：`/api`
- 请求格式：`Content-Type: application/json`
- 认证请求头：`Authorization: Bearer <token>`
- OpenAPI JSON：`GET /v3/api-docs`
- 项目内接口快照：`docs/openapi.json`
- Swagger UI 已关闭，避免直接暴露交互式接口页面。

成功响应统一为：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

常见失败响应：

```json
{
  "code": 401,
  "message": "未登录或登录已失效"
}
```

分页响应的 `data` 结构：

```json
{
  "records": [],
  "total": 0,
  "current": 1,
  "size": 10
}
```

## 认证接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 匿名 | 登录并创建 Redis 会话 |
| POST | `/api/auth/logout` | 可选 | 删除会话并加入 JWT 黑名单 |
| GET | `/api/auth/me` | 已登录 | 查询当前用户资料 |
| PUT | `/api/auth/profile` | 已登录 | 修改当前用户显示名称 |

登录请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

登录成功时，`data.token` 为 JWT，`data.user.role` 为 `ADMIN`、`TEACHER` 或
`STUDENT`。

## 用户与角色

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/roles` | ADMIN | 查询系统角色 |
| GET | `/api/users?page=1&size=10&keyword=` | ADMIN | 用户分页查询 |
| POST | `/api/users` | ADMIN | 创建用户，密码为空时使用 `123456` |
| PUT | `/api/users/{id}` | ADMIN | 修改用户 |
| DELETE | `/api/users/{id}` | ADMIN | 删除未关联业务档案的用户 |

创建或修改用户请求：

```json
{
  "username": "student02",
  "realName": "李同学",
  "role": "STUDENT",
  "password": "123456",
  "status": "1"
}
```

## 组织与人员

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/departments` | ADMIN | 查询院系 |
| POST | `/api/departments` | ADMIN | 新增院系 |
| PUT | `/api/departments/{id}` | ADMIN | 修改院系 |
| DELETE | `/api/departments/{id}` | ADMIN | 删除院系 |
| GET | `/api/classes` | ADMIN | 查询班级及所属院系 |
| POST | `/api/classes` | ADMIN | 新增班级 |
| PUT | `/api/classes/{id}` | ADMIN | 修改班级 |
| DELETE | `/api/classes/{id}` | ADMIN | 删除班级 |
| GET | `/api/students?page=1&size=10&keyword=` | ADMIN | 学生分页查询 |
| POST | `/api/students` | ADMIN | 新增学生 |
| PUT | `/api/students/{id}` | ADMIN | 修改学生 |
| DELETE | `/api/students/{id}` | ADMIN | 删除学生 |
| GET | `/api/teachers` | ADMIN | 查询教师 |
| POST | `/api/teachers` | ADMIN | 新增教师及其登录账号 |
| PUT | `/api/teachers/{id}` | ADMIN | 修改教师 |
| DELETE | `/api/teachers/{id}` | ADMIN | 删除教师 |

新增院系请求：

```json
{
  "name": "计算机科学与技术学院",
  "code": "CS",
  "description": "负责计算机相关专业教学"
}
```

新增学生请求：

```json
{
  "studentNo": "S2025002",
  "name": "李同学",
  "gender": "女",
  "phone": "13900000002",
  "email": "student02@example.com",
  "departmentId": 1,
  "classId": 1,
  "admissionYear": 2025,
  "status": "在读"
}
```

## 教学业务

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/courses` | 已登录 | 管理员查看全部，教师查看本人课程，学生查看已选课程 |
| GET | `/api/courses/available` | STUDENT | 查询学生尚未选择的课程 |
| POST | `/api/courses` | ADMIN | 新增课程 |
| PUT | `/api/courses/{id}` | ADMIN | 修改课程 |
| DELETE | `/api/courses/{id}` | ADMIN | 删除课程 |
| GET | `/api/enrollments?courseId=` | 已登录 | 按角色查询选课关系 |
| POST | `/api/enrollments` | ADMIN/STUDENT | 管理员或学生选课 |
| DELETE | `/api/enrollments/{id}` | ADMIN/STUDENT | 管理员删除或学生退选 |
| GET | `/api/grades` | 已登录 | 教师查看本人课程成绩，学生查看个人成绩 |
| PUT | `/api/grades/{enrollmentId}` | ADMIN/TEACHER | 保存成绩并计算总评 |
| GET | `/api/attendance` | 已登录 | 按角色查询考勤 |
| PUT | `/api/attendance` | ADMIN/TEACHER | 保存课程考勤 |

课程请求：

```json
{
  "courseNo": "CS101",
  "name": "软件工程",
  "credit": 3.0,
  "hours": 48,
  "semester": "2026-2027-1",
  "teacherId": 1,
  "description": "软件工程基础课程"
}
```

成绩请求：

```json
{
  "usualScore": 80,
  "midtermScore": 90,
  "finalScore": 100
}
```

总评计算公式为 `平时成绩 * 30% + 期中成绩 * 30% + 期末成绩 * 40%`，
结果保留两位小数，满 60 分为 `合格`。

考勤请求：

```json
{
  "courseId": 1,
  "studentId": 1,
  "attendanceDate": "2026-09-05",
  "status": "出勤",
  "remark": ""
}
```

考勤状态支持 `出勤`、`迟到`、`请假`、`缺勤`。

## 通知与首页

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/notices` | 已登录 | 查询当前角色可见通知 |
| POST | `/api/notices` | ADMIN/TEACHER | 发布通知 |
| DELETE | `/api/notices/{id}` | ADMIN | 删除通知 |
| GET | `/api/dashboard/summary` | 已登录 | 查询角色对应的首页统计 |

发布通知请求：

```json
{
  "title": "期末考试安排",
  "content": "请按时参加考试。",
  "targetRole": "ALL"
}
```

## 权限边界

- 前端菜单只用于改善操作体验，后端 Spring Security 和 Service 负责最终授权。
- 教师只能维护本人负责课程的成绩和考勤。
- 学生只能查看自己的课程、成绩、考勤和可见通知。
- Redis 会话、JWT 签名、JWT 黑名单和数据库账号状态共同参与认证校验。
- Redis 不可用时，登录、登出和依赖 Redis 的安全操作返回明确的 `503`。
