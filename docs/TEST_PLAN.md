# 测试计划与执行记录

## 测试范围

- 后端单元测试：JWT 签发与解析、登录、账号状态、个人资料、用户角色约束、成绩计算、教师数据权限和学生部分更新。
- REST 冒烟测试：未登录访问、三类账号登录、管理员数据查询、教师越权、学生成绩和考勤查询。
- 前端构建测试：TypeScript 类型检查和 Vite 生产构建。
- 联调环境：JDK 21、MySQL 8.x、Docker Redis 7.x，后端临时验证端口可使用 8081。

## 执行命令

后端单元测试：

```powershell
$env:JAVA_HOME = $env:JDK21
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn test
```

前端构建：

```powershell
cd frontend
npm run build
```

REST 冒烟测试：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/api-smoke-test.ps1
```

如果后端使用临时端口 8081：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/api-smoke-test.ps1 -BaseUrl http://localhost:8081
```

## 验收标准

- `admin`、`teacher01`、`student01` 均可使用密码 `123456` 登录。
- 未登录请求返回统一响应 `code=401`。
- 教师访问用户管理返回 `code=403`。
- 学生只能看到个人课程、成绩和考勤数据。
- 成绩总评按照平时 30%、期中 30%、期末 40% 计算。
- 前端生产构建和后端 Maven 测试均成功。

## 当前执行记录

| 项目 | 状态 | 说明 |
| --- | --- | --- |
| JDK 21 Maven 环境 | 通过 | Maven 使用 JDK 21.0.10；启动项目的终端需将 `$env:JDK21\bin` 放在旧 JDK 路径之前 |
| 前端 TypeScript 检查 | 通过 | `npx vue-tsc --noEmit` |
| 前端生产构建 | 通过 | 使用 Vite `runner` 配置加载器和临时输出目录完成构建；默认 `frontend/dist` 旧目录存在权限占用 |
| 后端单元测试 | 通过 | 使用项目 `target/m2repo` 离线依赖缓存执行，12 个测试全部通过 |
| REST 冒烟测试 | 待执行 | 本机检查到 8080、8081、3306、6379 均未监听，Docker API 也不可用 |

后续联调时，先启动 MySQL、Docker Redis 和后端，再执行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/api-smoke-test.ps1 -BaseUrl http://localhost:8080
```

临时测试产生的数据必须在测试结束后清理，不覆盖演示账号和初始化数据。
