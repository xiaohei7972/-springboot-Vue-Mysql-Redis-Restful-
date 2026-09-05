<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Avatar, Bell, Calendar, Collection, DataAnalysis, Delete, Edit, House, Lock, Monitor,
  Plus, School, Search, Setting, User, UserFilled, Notebook
} from '@element-plus/icons-vue'
import http from './api'

type Role = 'ADMIN' | 'TEACHER' | 'STUDENT'
type MenuKey = 'dashboard' | 'students' | 'courses' | 'enrollments' | 'grades' | 'attendance' | 'notices' | 'departments'

const token = ref(localStorage.getItem('student_token') || '')
const user = ref<any>(JSON.parse(localStorage.getItem('student_user') || 'null'))
const loginForm = reactive({ username: 'admin', password: '123456' })
const loginLoading = ref(false)
const activeMenu = ref<MenuKey>('dashboard')
const loading = ref(false)
const keyword = ref('')
const rows = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const stats = ref<Record<string, any>>({})
const notices = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref('')
const form = reactive<Record<string, any>>({})

const loggedIn = computed(() => Boolean(token.value && user.value))
const role = computed<Role>(() => user.value?.role || 'ADMIN')
const isAdmin = computed(() => role.value === 'ADMIN')
const isTeacher = computed(() => role.value === 'TEACHER')
const pageTitle = computed(() => ({
  dashboard: '工作台', students: '学生管理', courses: '课程管理', enrollments: '选课管理',
  grades: '成绩管理', attendance: '考勤管理', notices: '通知公告', departments: '组织管理'
}[activeMenu.value]))

const menus = computed(() => {
  const base = [{ key: 'dashboard', label: '工作台', icon: House }]
  if (isAdmin.value) return [...base, { key: 'students', label: '学生管理', icon: Avatar }, { key: 'courses', label: '课程管理', icon: Notebook }, { key: 'departments', label: '组织管理', icon: School }, { key: 'notices', label: '通知公告', icon: Bell }]
  if (isTeacher.value) return [...base, { key: 'courses', label: '我的课程', icon: Notebook }, { key: 'enrollments', label: '选课名单', icon: Avatar }, { key: 'grades', label: '成绩录入', icon: DataAnalysis }, { key: 'attendance', label: '考勤管理', icon: Calendar }, { key: 'notices', label: '通知公告', icon: Bell }]
  return [...base, { key: 'courses', label: '我的课程', icon: Notebook }, { key: 'enrollments', label: '我的选课', icon: Collection }, { key: 'grades', label: '我的成绩', icon: DataAnalysis }, { key: 'attendance', label: '我的考勤', icon: Calendar }, { key: 'notices', label: '通知公告', icon: Bell }]
})

const columns = computed(() => {
  const map: Record<string, any[]> = {
    students: [{ prop: 'studentNo', label: '学号' }, { prop: 'name', label: '姓名' }, { prop: 'gender', label: '性别' }, { prop: 'departmentName', label: '院系' }, { prop: 'className', label: '班级' }, { prop: 'phone', label: '联系方式' }, { prop: 'status', label: '状态' }],
    courses: [{ prop: 'courseNo', label: '课程编号' }, { prop: 'name', label: '课程名称' }, { prop: 'credit', label: '学分' }, { prop: 'hours', label: '学时' }, { prop: 'semester', label: '学期' }, { prop: 'teacherName', label: '授课教师' }],
    enrollments: [{ prop: 'courseName', label: '课程' }, { prop: 'studentNo', label: '学号' }, { prop: 'studentName', label: '学生' }, { prop: 'status', label: '状态' }, { prop: 'enrolledAt', label: '选课时间' }],
    grades: [{ prop: 'courseName', label: '课程' }, { prop: 'studentNo', label: '学号' }, { prop: 'studentName', label: '学生' }, { prop: 'usualScore', label: '平时' }, { prop: 'midtermScore', label: '期中' }, { prop: 'finalScore', label: '期末' }, { prop: 'totalScore', label: '总评' }, { prop: 'gradeStatus', label: '结果' }],
    attendance: [{ prop: 'courseName', label: '课程' }, { prop: 'studentNo', label: '学号' }, { prop: 'studentName', label: '学生' }, { prop: 'attendanceDate', label: '日期' }, { prop: 'status', label: '状态' }, { prop: 'remark', label: '备注' }],
    notices: [{ prop: 'title', label: '标题' }, { prop: 'targetRole', label: '接收范围' }, { prop: 'publisherName', label: '发布人' }, { prop: 'publishedAt', label: '发布时间' }]
  }
  return map[activeMenu.value] || []
})

async function login() {
  loginLoading.value = true
  try {
    const data: any = await http.post('/api/auth/login', loginForm)
    token.value = data.token
    user.value = data.user
    localStorage.setItem('student_token', data.token)
    localStorage.setItem('student_user', JSON.stringify(data.user))
    await loadDashboard()
    ElMessage.success('登录成功')
  } catch (error: any) {
    ElMessage.error(error.message)
  } finally {
    loginLoading.value = false
  }
}

async function logout() {
  try { await http.post('/api/auth/logout') } catch { /* clear local state even when token expired */ }
  token.value = ''
  user.value = null
  localStorage.clear()
}

async function loadDashboard() {
  try {
    const data: any = await http.get('/api/dashboard/summary')
    stats.value = data
    notices.value = data.recentNotices || []
  } catch (error: any) { ElMessage.error(error.message) }
}

async function loadRows() {
  if (activeMenu.value === 'dashboard') return loadDashboard()
  loading.value = true
  try {
    const url = `/api/${activeMenu.value}`
    const data: any = await http.get(url, { params: activeMenu.value === 'students' ? { page: page.value, size: pageSize.value, keyword: keyword.value } : {} })
    if (activeMenu.value === 'students') {
      rows.value = data.records
      total.value = data.total
    } else rows.value = data || []
  } catch (error: any) { ElMessage.error(error.message) }
  finally { loading.value = false }
}

function selectMenu(key: MenuKey) {
  activeMenu.value = key
  page.value = 1
  keyword.value = ''
  loadRows()
}

function openCreate() {
  Object.keys(form).forEach(key => delete form[key])
  dialogType.value = activeMenu.value
  dialogTitle.value = `新增${pageTitle.value.replace('管理', '')}`
  dialogVisible.value = true
}

function openEdit(row: any) {
  Object.keys(form).forEach(key => delete form[key])
  Object.assign(form, row)
  dialogType.value = activeMenu.value
  dialogTitle.value = `编辑${pageTitle.value.replace('管理', '')}`
  dialogVisible.value = true
}

async function submitForm() {
  try {
    const endpoint = `/api/${dialogType.value}`
    const data = { ...form }
    if (dialogType.value === 'departments') await http.put(`${endpoint}/${data.id}`, data)
    else if (data.id) await http.put(`${endpoint}/${data.id}`, data)
    else await http.post(endpoint, data)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadRows()
  } catch (error: any) { ElMessage.error(error.message) }
}

async function removeRow(row: any) {
  try {
    await ElMessageBox.confirm('删除后不可恢复，确定继续吗？', '确认删除', { type: 'warning' })
    await http.delete(`/api/${activeMenu.value}/${row.id}`)
    ElMessage.success('删除成功')
    await loadRows()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message)
  }
}

function formatRole(value: string) {
  return ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生', ALL: '全部用户' } as any)[value] || value
}

onMounted(() => { if (loggedIn.value) loadDashboard() })
</script>

<template>
  <div v-if="!loggedIn" class="login-page">
    <div class="login-brand">
      <div class="brand-mark"><School /></div>
      <div><h1>学生管理系统</h1><p>教学数据 · 统一管理</p></div>
    </div>
    <el-card class="login-card" shadow="never">
      <div class="eyebrow">WELCOME BACK</div>
      <h2>登录系统</h2>
      <p class="login-tip">使用演示账号进入管理工作台</p>
      <el-form @submit.prevent="login">
        <el-form-item><el-input v-model="loginForm.username" size="large" placeholder="用户名" :prefix-icon="User" /></el-form-item>
        <el-form-item><el-input v-model="loginForm.password" size="large" type="password" show-password placeholder="密码" :prefix-icon="Lock" @keyup.enter="login" /></el-form-item>
        <el-button class="login-button" type="primary" size="large" :loading="loginLoading" @click="login">进入系统</el-button>
      </el-form>
      <div class="demo-hint">演示账号：admin / 123456</div>
    </el-card>
  </div>

  <el-container v-else class="app-shell">
    <el-aside width="232px" class="side">
      <div class="side-logo"><div class="brand-mark small"><School /></div><div><strong>学生管理系统</strong><span>ACADEMIC CENTER</span></div></div>
      <div class="side-caption">工作空间</div>
      <el-menu :default-active="activeMenu" class="side-menu" @select="selectMenu">
        <el-menu-item v-for="item in menus" :key="item.key" :index="item.key">
          <component :is="item.icon" /><span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="side-footer"><div class="status-dot"></div><span>系统运行正常</span></div>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div><div class="crumb">学生管理系统 / {{ pageTitle }}</div><h2>{{ pageTitle }}</h2></div>
        <div class="top-actions"><el-button text circle :icon="Bell" /><el-divider direction="vertical" /><el-dropdown>
          <span class="user-trigger"><el-avatar :size="34"><UserFilled /></el-avatar><span>{{ user.realName }}</span><span class="role-pill">{{ formatRole(user.role) }}</span></span>
          <template #dropdown><el-dropdown-menu><el-dropdown-item :icon="Setting">个人设置</el-dropdown-item><el-dropdown-item divided @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown></div>
      </el-header>
      <el-main class="main">
        <template v-if="activeMenu === 'dashboard'">
          <div class="welcome-row"><div><h3>你好，{{ user.realName }}</h3><p>今天也要高效完成教学管理工作。</p></div><el-button type="primary" :icon="Monitor" @click="selectMenu(isAdmin ? 'students' : 'courses')">进入管理</el-button></div>
          <div class="stat-grid">
            <div class="stat-card accent-blue"><span>学生总数</span><strong>{{ stats.studentCount ?? 0 }}</strong><div><Avatar /> 当前在读学生</div></div>
            <div class="stat-card accent-green"><span>{{ isAdmin ? '教师总数' : isTeacher ? '授课课程' : '我的课程' }}</span><strong>{{ isAdmin ? stats.teacherCount : stats.courseCount ?? 0 }}</strong><div><Notebook /> 教学资源概览</div></div>
            <div class="stat-card accent-orange"><span>{{ isAdmin ? '课程总数' : isTeacher ? '选课学生' : '已录成绩' }}</span><strong>{{ isAdmin ? stats.courseCount : isTeacher ? stats.studentCount : stats.gradeCount ?? 0 }}</strong><div><DataAnalysis /> 数据实时更新</div></div>
            <div class="stat-card accent-purple"><span>{{ isAdmin ? '院系数量' : isTeacher ? '考勤记录' : '通知公告' }}</span><strong>{{ isAdmin ? stats.departmentCount : isTeacher ? stats.attendanceCount : stats.noticeCount ?? 0 }}</strong><div><Bell /> 最近业务动态</div></div>
          </div>
          <div class="dashboard-grid"><el-card shadow="never" class="panel"><template #header><div class="panel-title">最近通知 <el-button text type="primary" @click="selectMenu('notices')">查看全部</el-button></div></template><div v-for="notice in notices" :key="notice.id" class="notice-row"><div class="notice-icon"><Bell /></div><div><strong>{{ notice.title }}</strong><span>{{ formatRole(notice.targetRole) }} · {{ notice.publishedAt }}</span></div></div><el-empty v-if="!notices.length" description="暂无通知" /></el-card>
          <el-card shadow="never" class="panel quick-panel"><template #header><div class="panel-title">快捷入口</div></template><div class="quick-grid"><button v-for="item in menus.slice(1, 5)" :key="item.key" @click="selectMenu(item.key as MenuKey)"><component :is="item.icon" /><span>{{ item.label }}</span></button></div></el-card></div>
        </template>
        <template v-else>
          <div class="toolbar"><div class="search-box" v-if="activeMenu === 'students'"><el-input v-model="keyword" placeholder="搜索学号、姓名或手机号" clearable @keyup.enter="loadRows"><template #prefix><Search /></template></el-input><el-button type="primary" :icon="Search" @click="loadRows">查询</el-button></div><span v-else class="record-count">共 {{ rows.length }} 条记录</span><div class="toolbar-actions"><el-button v-if="['students','courses','departments','notices'].includes(activeMenu) && (isAdmin || (isTeacher && activeMenu === 'notices'))" type="primary" :icon="Plus" @click="openCreate">新增</el-button><el-button :icon="DataAnalysis" @click="loadRows">刷新</el-button></div></div>
          <el-card shadow="never" class="table-panel"><el-table v-loading="loading" :data="rows" stripe height="calc(100vh - 285px)"><el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" min-width="120"><template #default="{ row }"><el-tag v-if="column.prop === 'status' || column.prop === 'gradeStatus'" :type="row[column.prop] === '合格' || row[column.prop] === '出勤' || row[column.prop] === '在读' ? 'success' : 'warning'" effect="light">{{ row[column.prop] }}</el-tag><span v-else-if="column.prop === 'targetRole'">{{ formatRole(row[column.prop]) }}</span><span v-else>{{ row[column.prop] ?? '-' }}</span></template></el-table-column><el-table-column v-if="isAdmin && ['students','courses','departments','notices'].includes(activeMenu)" fixed="right" label="操作" width="150"><template #default="{ row }"><el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button><el-button link type="danger" :icon="Delete" @click="removeRow(row)">删除</el-button></template></el-table-column><el-table-column v-if="isTeacher && activeMenu === 'grades'" fixed="right" label="操作" width="100"><template #default="{ row }"><el-button link type="primary" :icon="Edit" @click="openEdit(row)">录入</el-button></template></el-table-column></el-table><div v-if="activeMenu === 'students'" class="pagination"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total" @change="loadRows" /></div></el-card>
        </template>
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form label-position="top" class="dialog-form">
      <template v-if="dialogType === 'students'"><el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item><el-form-item label="性别"><el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></template>
      <template v-else-if="dialogType === 'departments'"><el-form-item label="院系名称"><el-input v-model="form.name" /></el-form-item><el-form-item label="院系编码"><el-input v-model="form.code" /></el-form-item><el-form-item label="简介"><el-input v-model="form.description" type="textarea" /></el-form-item></template>
      <template v-else-if="dialogType === 'courses'"><el-form-item label="课程编号"><el-input v-model="form.courseNo" /></el-form-item><el-form-item label="课程名称"><el-input v-model="form.name" /></el-form-item><el-form-item label="学分"><el-input-number v-model="form.credit" :min="0" :max="10" /></el-form-item><el-form-item label="学时"><el-input-number v-model="form.hours" :min="1" :max="200" /></el-form-item><el-form-item label="学期"><el-input v-model="form.semester" /></el-form-item><el-form-item label="授课教师ID"><el-input-number v-model="form.teacherId" :min="1" /></el-form-item></template>
      <template v-else-if="dialogType === 'notices'"><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item><el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item><el-form-item label="接收范围"><el-select v-model="form.targetRole"><el-option label="全部用户" value="ALL" /><el-option label="教师" value="TEACHER" /><el-option label="学生" value="STUDENT" /></el-select></el-form-item></template>
      <template v-else-if="dialogType === 'grades'"><el-form-item label="平时成绩"><el-input-number v-model="form.usualScore" :min="0" :max="100" /></el-form-item><el-form-item label="期中成绩"><el-input-number v-model="form.midtermScore" :min="0" :max="100" /></el-form-item><el-form-item label="期末成绩"><el-input-number v-model="form.finalScore" :min="0" :max="100" /></el-form-item></template>
    </el-form>
    <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">保存</el-button></template>
  </el-dialog>
</template>
