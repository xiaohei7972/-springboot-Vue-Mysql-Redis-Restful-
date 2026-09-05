# 学生管理系统实施计划

## 总体架构

项目采用前后端分离的 Spring MVC 架构。后端按照 Controller、Service、Mapper、Entity、DTO/VO、Config、Security、Exception 分层：

- Controller：接收 REST 请求、绑定路径参数和请求体，调用 Service 并返回统一响应，不直接访问数据库。
- Service：实现业务规则、角色权限、数据范围、事务边界和成绩计算。
- Mapper：使用 MyBatis-Plus Mapper、`BaseMapper`、Wrapper 或 Mapper 自定义 SQL 访问 MySQL。
- Entity：映射数据库表，仅用于持久化，不直接作为对外接口契约。
- DTO/VO：用于请求参数和返回数据的接口模型；列表和关联查询逐步从 Map 迁移为明确的 VO。
- Config、Security、Exception：分别负责应用配置、安全认证授权和统一异常处理。

前端采用 Vue 3 + TypeScript，使用 Axios 调用 `/api` REST 风格接口。后端是最终权限边界，前端菜单隐藏不作为授权依据。

| 阶段 | 工作内容 | 技术方案 | 状态 | 验收结果 |
| --- | --- | --- | --- | --- |
| 1. 项目初始化 | 后端、前端、数据库目录及 Git 仓库初始化 | JDK 21、Spring Boot 3.x、Vue 3、Vite | 已完成 | 项目已接入远程 `main` 分支 |
| 2. 数据库设计 | 用户、角色、院系、班级、学生、教师、课程、选课、成绩、考勤、通知和日志表 | MySQL 8.x、utf8mb4 | 已完成 | `database/init.sql` 可初始化结构和演示数据 |
| 3. 持久层实现 | 数据库实体、Mapper、条件查询、分页和关联查询 | MyBatis-Plus Starter、`BaseMapper`、Wrapper、Mapper 自定义查询 | MVC 基础分层已完成，实体化 CRUD 持续补齐 | Controller 不直接访问数据库；用户模块已使用 `BaseMapper`，教学模块通过 Mapper 完成基础写入和关联查询 |
| 4. 认证授权 | 登录、JWT、Redis 会话、黑名单、角色权限和数据范围 | Spring Security、JWT、Redis 7.x | 已完成基础版 | 管理员、教师、学生三类账号可认证 |
| 5. 教学业务 API | 学生、院系、班级、教师、课程、选课、成绩、考勤、通知和统计 | REST `/api` 接口 + Service 业务层 | 已完成 MVC 基础版 | Controller 仅负责接口适配，业务权限和成绩计算由 Service 统一处理 |
| 6. 前端页面 | 登录、首页、数据列表、表单、分页、权限菜单和个人中心 | Vue 3、TypeScript、Element Plus、Pinia、Axios | 已完成基础版 | 已提供登录、首页、核心列表和管理表单 |
| 7. 联调与测试 | 后端构建、前端构建、接口测试、权限测试和缓存测试 | Maven、npm、Spring Boot Test | 进行中 | JDK 21 后端编译通过；前端构建、真实 MySQL/Redis 联调和自动化测试待完成 |
| 8. 课程交付 | README、API 说明、演示账号、论文素材、答辩流程 | Markdown、OpenAPI JSON、截图素材 | 待完成 | 满足任务书课程论文和答辩交付要求 |

## 持久层约束

- 所有业务数据库读写必须通过 MyBatis-Plus 管理的 Mapper 层完成，禁止在 Controller、Service 中使用 JDBC。
- Controller 不直接依赖 Mapper、`JdbcTemplate`、`Connection`、`PreparedStatement` 或手写 JDBC 资源管理；Controller 只依赖 Service。
- Service 不拼接 SQL，不直接操作 JDBC，只通过 Mapper 完成持久化。
- 表字段使用实体属性映射，数据库下划线字段统一映射为 Java 驼峰字段。
- 简单 CRUD 使用 `BaseMapper` 和 `QueryWrapper`/`LambdaQueryWrapper`。
- 多表关联、聚合、成绩计算和数据权限查询使用 Mapper 层自定义查询。
- 分页查询最终统一迁移到 MyBatis-Plus `Page`/`IPage`，并转换为项目的 `PageResult`；现有首版学生查询暂保留 Mapper 分页 SQL。
- 新增业务表时必须同时新增 Entity、Mapper、Service 和 Controller（如该资源需要对外提供 API）。
- 教学模块现有基础查询中的 Map 返回值、实体 Mapper 和 `Page` 分页将在下一阶段逐步补齐为明确的 DTO/VO、`BaseMapper` 和 `IPage`。

## MVC 验收标准

- 后端启动类使用 Spring Boot Web MVC。
- 每个 REST 控制器只处理 HTTP 层职责，不包含 SQL、成绩计算或角色数据范围判断。
- 每个写操作的事务边界位于 Service 层，并由后端完成权限校验。
- MySQL 数据库访问统一使用 MyBatis-Plus，代码库中不得新增业务侧 JDBC 访问。
- 前端通过 Axios 调用 REST API，不绕过后端直接连接 MySQL 或 Redis。

## 演示账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 教师 | `teacher01` | `123456` |
| 学生 | `student01` | `123456` |
