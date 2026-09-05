# 学生管理系统

基于 JDK 21、Spring Boot 3.x、MyBatis-Plus、Vue 3、MySQL 8.x 和 Redis 7.x 的前后端分离学生管理系统。

## 功能

- 管理员：学生、课程、院系、班级、通知和首页统计。
- 教师：本人课程、选课名单、成绩录入、考勤管理和通知。
- 学生：个人课程、成绩、考勤和通知查看。
- JWT REST API 认证，Redis 保存会话和 JWT 黑名单。
- 成绩总评按平时 30%、期中 30%、期末 40% 自动计算。
- 后端采用 Spring MVC 分层，Controller 只负责 REST 请求适配，Service 负责业务规则，Mapper 层统一由 MyBatis-Plus Starter 管理。
- 数据库读写统一通过 MyBatis-Plus Mapper 完成；简单 CRUD 使用 `BaseMapper`，关联查询使用 Mapper 自定义 SQL，业务代码不直接使用 JdbcTemplate 或其他 JDBC API。

## 环境

- JDK 21，推荐 `JAVA_HOME=D:/Environment/Java/jdk-21.0.10`
- Maven 3.9+
- Node.js 20+
- MySQL 8.x
- Redis 7.x，Docker 映射端口 `6379`

## 初始化数据库

先创建数据库并执行：

```text
src/main/resources/db/schema.sql
src/main/resources/db/data.sql
```

也可以在 MySQL 客户端执行 `database/init.sql` 中的脚本。默认连接参数为：

```text
数据库：student_management
用户名：root
密码：root
地址：localhost:3306
```

可通过 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USERNAME` 和 `MYSQL_PASSWORD` 覆盖。

## 启动 Redis

```bash
docker run --name student-redis -p 6379:6379 -d redis:7-alpine
```

如果 Redis 已经部署在 Docker 并映射到本机 `6379`，无需修改配置。可通过 `REDIS_HOST`、`REDIS_PORT` 和 `REDIS_PASSWORD` 覆盖。

## 启动后端

```bash
set JAVA_HOME=D:\Environment\Java\jdk-21.0.10
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 演示账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 教师 | `teacher01` | `123456` |
| 学生 | `student01` | `123456` |

## 项目文档

实施进度、MVC 分层和 MyBatis-Plus 技术约束见 `docs/IMPLEMENTATION_PLAN.md`。课程论文需按照任务书要求补充需求分析、系统设计、实现截图、测试结果、结论、致谢和参考文献。
