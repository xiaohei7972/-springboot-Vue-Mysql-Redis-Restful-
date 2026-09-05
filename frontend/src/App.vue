<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Avatar, Bell, Calendar, Collection, DataAnalysis, Delete, Edit, House, Lock, Monitor,
  Plus, School, Search, Setting, User, UserFilled, Notebook
} from '@element-plus/icons-vue'
import http from './api'

type Role = 'ADMIN' | 'TEACHER' | 'STUDENT'
type MenuKey = 'dashboard' | 'users' | 'roles' | 'students' | 'teachers' | 'courses' | 'enrollments' | 'grades' | 'attendance' | 'notices' | 'departments' | 'classes'

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
const formRef = ref<FormInstance>()
const profileRef = ref<FormInstance>()
const submitLoading = ref(false)
const profileVisible = ref(false)
const profileLoading = ref(false)
const profileForm = reactive({ realName: '' })
const reference = reactive({
  departments: [] as any[],
  classes: [] as any[],
  teachers: [] as any[],
  courses: [] as any[],
  availableCourses: [] as any[],
  students: [] as any[],
  enrollments: [] as any[]
})

const loggedIn = computed(() => Boolean(token.value && user.value))
const role = computed<Role>(() => user.value?.role || 'ADMIN')
const isAdmin = computed(() => role.value === 'ADMIN')
const isTeacher = computed(() => role.value === 'TEACHER')
const isStudent = computed(() => role.value === 'STUDENT')
const pageTitle = computed(() => ({
  dashboard: '工作台', users: '用户管理', roles: '角色说明', students: '学生管理', teachers: '教师管理',
  courses: '课程管理', enrollments: '选课管理', grades: '成绩管理', attendance: '考勤管理',
  notices: '通知公告', departments: '院系管理', classes: '班级管理'
}[activeMenu.value]))

const menus = computed(() => {
  const base = [{ key: 'dashboard', label: '工作台', icon: House }]
  if (isAdmin.value) return [...base,
    { key: 'users', label: '用户管理', icon: User },
    { key: 'roles', label: '角色说明', icon: Setting },
    { key: 'students', label: '学生管理', icon: Avatar },
    { key: 'teachers', label: '教师管理', icon: UserFilled },
    { key: 'courses', label: '课程管理', icon: Notebook },
    { key: 'enrollments', label: '选课管理', icon: Collection },
    { key: 'grades', label: '成绩管理', icon: DataAnalysis },
    { key: 'attendance', label: '考勤管理', icon: Calendar },
    { key: 'departments', label: '院系管理', icon: School },
    { key: 'classes', label: '班级管理', icon: Collection },
    { key: 'notices', label: '通知公告', icon: Bell }]
  if (isTeacher.value) return [...base, { key: 'courses', label: '我的课程', icon: Notebook }, { key: 'enrollments', label: '选课名单', icon: Avatar }, { key: 'grades', label: '成绩录入', icon: DataAnalysis }, { key: 'attendance', label: '考勤管理', icon: Calendar }, { key: 'notices', label: '通知公告', icon: Bell }]
  return [...base, { key: 'courses', label: '我的课程', icon: Notebook }, { key: 'enrollments', label: '我的选课', icon: Collection }, { key: 'grades', label: '我的成绩', icon: DataAnalysis }, { key: 'attendance', label: '我的考勤', icon: Calendar }, { key: 'notices', label: '通知公告', icon: Bell }]
})

const columns = computed(() => {
  const map: Record<string, any[]> = {
    users: [{ prop: 'username', label: '用户名' }, { prop: 'realName', label: '姓名' }, { prop: 'role', label: '角色' }, { prop: 'status', label: '状态' }, { prop: 'createdAt', label: '创建时间' }],
    roles: [{ prop: 'code', label: '角色编码' }, { prop: 'name', label: '角色名称' }, { prop: 'description', label: '权限范围' }],
    students: [{ prop: 'studentNo', label: '学号' }, { prop: 'name', label: '姓名' }, { prop: 'gender', label: '性别' }, { prop: 'departmentName', label: '院系' }, { prop: 'className', label: '班级' }, { prop: 'phone', label: '联系方式' }, { prop: 'status', label: '状态' }],
    teachers: [{ prop: 'teacherNo', label: '工号' }, { prop: 'name', label: '姓名' }, { prop: 'username', label: '登录账号' }, { prop: 'title', label: '职称' }, { prop: 'departmentName', label: '院系' }, { prop: 'phone', label: '联系方式' }, { prop: 'userStatus', label: '账号状态' }],
    classes: [{ prop: 'name', label: '班级名称' }, { prop: 'code', label: '班级编码' }, { prop: 'departmentName', label: '所属院系' }, { prop: 'gradeYear', label: '年级' }],
    departments: [{ prop: 'name', label: '院系名称' }, { prop: 'code', label: '院系编码' }, { prop: 'description', label: '简介' }, { prop: 'createdAt', label: '创建时间' }],
    courses: [{ prop: 'courseNo', label: '课程编号' }, { prop: 'name', label: '课程名称' }, { prop: 'credit', label: '学分' }, { prop: 'hours', label: '学时' }, { prop: 'semester', label: '学期' }, { prop: 'teacherName', label: '授课教师' }],
    enrollments: [{ prop: 'courseName', label: '课程' }, { prop: 'studentNo', label: '学号' }, { prop: 'studentName', label: '学生' }, { prop: 'status', label: '状态' }, { prop: 'enrolledAt', label: '选课时间' }],
    grades: [{ prop: 'courseName', label: '课程' }, { prop: 'studentNo', label: '学号' }, { prop: 'studentName', label: '学生' }, { prop: 'usualScore', label: '平时' }, { prop: 'midtermScore', label: '期中' }, { prop: 'finalScore', label: '期末' }, { prop: 'totalScore', label: '总评' }, { prop: 'gradeStatus', label: '结果' }],
    attendance: [{ prop: 'courseName', label: '课程' }, { prop: 'studentNo', label: '学号' }, { prop: 'studentName', label: '学生' }, { prop: 'attendanceDate', label: '日期' }, { prop: 'status', label: '状态' }, { prop: 'remark', label: '备注' }],
    notices: [{ prop: 'title', label: '标题' }, { prop: 'targetRole', label: '接收范围' }, { prop: 'publisherName', label: '发布人' }, { prop: 'publishedAt', label: '发布时间' }]
  }
  return map[activeMenu.value] || []
})

const rules = computed<FormRules>(() => {
  const required = (message: string) => [{ required: true, message, trigger: 'blur' }]
  return {
    username: required('请输入用户名'),
    realName: required('请输入姓名'),
    role: [{ required: true, message: '请选择角色', trigger: 'change' }],
    studentNo: required('请输入学号'),
    teacherNo: required('请输入工号'),
    name: required('请输入名称'),
    code: required('请输入编码'),
    departmentId: [{ required: true, message: '请选择院系', trigger: 'change' }],
    classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
    courseNo: required('请输入课程编号'),
    semester: required('请输入学期'),
    courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
    studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
    attendanceDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
    title: required('请输入标题'),
    content: required('请输入通知内容'),
    usualScore: [{ required: true, message: '请输入平时成绩', trigger: 'change' }],
    midtermScore: [{ required: true, message: '请输入期中成绩', trigger: 'change' }],
    finalScore: [{ required: true, message: '请输入期末成绩', trigger: 'change' }]
  }
})

const attendanceStudents = computed(() => {
  const courseId = form.courseId == null ? null : String(form.courseId)
  return reference.enrollments
    .filter(item => courseId == null || String(item.courseId) === courseId)
    .reduce((items: any[], item) => {
      if (!items.some(existing => String(existing.studentId) === String(item.studentId))) {
        items.push(item)
      }
      return items
    }, [])
})

const enrollmentCourses = computed(() => isStudent.value ? reference.availableCourses : reference.courses)

const canCreate = computed(() =>
  (isAdmin.value && ['users', 'students', 'teachers', 'courses', 'enrollments', 'attendance', 'departments', 'classes', 'notices'].includes(activeMenu.value))
  || (isTeacher.value && activeMenu.value === 'notices')
  || (isStudent.value && activeMenu.value === 'enrollments'))

const canEditRows = computed(() =>
  isAdmin.value && ['users', 'students', 'teachers', 'courses', 'grades', 'attendance', 'departments', 'classes'].includes(activeMenu.value)
  || isTeacher.value && ['grades', 'attendance'].includes(activeMenu.value))

const canDeleteRows = computed(() =>
  isAdmin.value && ['users', 'students', 'teachers', 'courses', 'enrollments', 'departments', 'classes', 'notices'].includes(activeMenu.value)
  || isStudent.value && activeMenu.value === 'enrollments')

const createLabel = computed(() => activeMenu.value === 'enrollments' ? '选课' : '新增')

async function login() {
  loginLoading.value = true
  try {
    const data: any = await http.post('/api/auth/login', loginForm)
    token.value = data.token
    user.value = data.user
    localStorage.setItem('student_token', data.token)
    localStorage.setItem('student_user', JSON.stringify(data.user))
    await loadDashboard()
    await loadReferences()
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
  localStorage.removeItem('student_token')
  localStorage.removeItem('student_user')
}

async function loadDashboard() {
  try {
    const data: any = await http.get('/api/dashboard/summary')
    stats.value = data
    notices.value = data.recentNotices || []
  } catch (error: any) { ElMessage.error(error.message) }
}

async function loadReferences() {
  if (!loggedIn.value) return
  try {
    const requests: Promise<any>[] = []
    if (isAdmin.value) {
      requests.push(
        http.get('/api/departments'),
        http.get('/api/classes'),
        http.get('/api/teachers'),
        http.get('/api/students', { params: { page: 1, size: 100 } })
      )
    }
    requests.push(http.get('/api/courses'), http.get('/api/enrollments'))
    if (isStudent.value) requests.push(http.get('/api/courses/available'))
    const results = await Promise.all(requests)
    let index = 0
    if (isAdmin.value) {
      reference.departments = results[index++] || []
      reference.classes = results[index++] || []
      reference.teachers = results[index++] || []
      reference.students = results[index++]?.records || []
    }
    reference.courses = results[index++] || []
    reference.enrollments = results[index++] || []
    reference.availableCourses = isStudent.value ? (results[index++] || []) : []
  } catch (error: any) {
    ElMessage.error(`基础数据加载失败：${error.message}`)
  }
}

async function loadRows() {
  if (activeMenu.value === 'dashboard') return loadDashboard()
  loading.value = true
  try {
    const url = `/api/${activeMenu.value}`
    const data: any = await http.get(url, { params: ['students', 'users'].includes(activeMenu.value) ? { page: page.value, size: pageSize.value, keyword: keyword.value } : {} })
    if (['students', 'users'].includes(activeMenu.value)) {
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
  if (activeMenu.value === 'enrollments') {
    form.courseId = enrollmentCourses.value[0]?.id
  }
  if (activeMenu.value === 'attendance') {
    form.courseId = reference.courses[0]?.id
    form.studentId = attendanceStudents.value[0]?.studentId
    form.attendanceDate = new Date().toISOString().slice(0, 10)
    form.status = '出勤'
  }
  if (activeMenu.value === 'notices') {
    form.targetRole = 'ALL'
  }
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
  if (formRef.value && !(await formRef.value.validate().catch(() => false))) return
  submitLoading.value = true
  try {
    const endpoint = `/api/${dialogType.value}`
    const data = { ...form }
    if (dialogType.value === 'grades') {
      await http.put(`${endpoint}/${data.enrollmentId}`, {
        usualScore: data.usualScore,
        midtermScore: data.midtermScore,
        finalScore: data.finalScore
      })
    } else if (dialogType.value === 'attendance') {
      await http.put(endpoint, data)
    } else if (data.id) {
      await http.put(`${endpoint}/${data.id}`, data)
    } else {
      await http.post(endpoint, data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadRows()
    await loadReferences()
  } catch (error: any) { ElMessage.error(error.message) }
  finally { submitLoading.value = false }
}

async function removeRow(row: any) {
  try {
    await ElMessageBox.confirm('删除后不可恢复，确定继续吗？', '确认删除', { type: 'warning' })
    await http.delete(`/api/${activeMenu.value}/${row.id}`)
    ElMessage.success('删除成功')
    await loadRows()
    await loadReferences()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message)
  }
}

function openProfile() {
  profileForm.realName = user.value?.realName || ''
  profileVisible.value = true
}

async function saveProfile() {
  if (profileRef.value && !(await profileRef.value.validate().catch(() => false))) return
  profileLoading.value = true
  try {
    await http.put('/api/auth/profile', profileForm)
    user.value = { ...user.value, realName: profileForm.realName.trim() }
    localStorage.setItem('student_user', JSON.stringify(user.value))
    profileVisible.value = false
    ElMessage.success('个人资料已更新')
  } catch (error: any) { ElMessage.error(error.message) }
  finally { profileLoading.value = false }
}

function formatRole(value: string) {
  return ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生', ALL: '全部用户' } as any)[value] || value
}

onMounted(() => {
  if (loggedIn.value) {
    loadDashboard()
    loadReferences()
  }
})
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
        <div class="top-actions"><el-button text circle :icon="Bell" @click="selectMenu('notices')" /><el-divider direction="vertical" /><el-dropdown>
          <span class="user-trigger"><el-avatar :size="34"><UserFilled /></el-avatar><span>{{ user.realName }}</span><span class="role-pill">{{ formatRole(user.role) }}</span></span>
          <template #dropdown><el-dropdown-menu><el-dropdown-item :icon="Setting" @click="openProfile">个人设置</el-dropdown-item><el-dropdown-item divided @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
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
          <div class="toolbar"><div class="search-box" v-if="['students', 'users'].includes(activeMenu)"><el-input v-model="keyword" :placeholder="activeMenu === 'users' ? '搜索用户名或姓名' : '搜索学号、姓名或手机号'" clearable @keyup.enter="loadRows"><template #prefix><Search /></template></el-input><el-button type="primary" :icon="Search" @click="loadRows">查询</el-button></div><span v-else class="record-count">共 {{ rows.length }} 条记录</span><div class="toolbar-actions"><el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreate">{{ createLabel }}</el-button><el-button :icon="DataAnalysis" @click="loadRows">刷新</el-button></div></div>
          <el-card shadow="never" class="table-panel"><el-table v-loading="loading" :data="rows" stripe height="calc(100vh - 285px)"><el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" min-width="120"><template #default="{ row }"><el-tag v-if="column.prop === 'status' || column.prop === 'gradeStatus' || column.prop === 'userStatus'" :type="row[column.prop] === '合格' || row[column.prop] === '出勤' || row[column.prop] === '在读' || row[column.prop] === '1' ? 'success' : 'warning'" effect="light">{{ column.prop === 'status' && (row[column.prop] === '1' || row[column.prop] === '0') ? (row[column.prop] === '1' ? '启用' : '禁用') : row[column.prop] }}</el-tag><span v-else-if="column.prop === 'targetRole' || column.prop === 'role'">{{ formatRole(row[column.prop]) }}</span><span v-else>{{ row[column.prop] ?? '-' }}</span></template></el-table-column><el-table-column v-if="canEditRows || canDeleteRows" fixed="right" label="操作" width="180"><template #default="{ row }"><el-button v-if="canEditRows" link type="primary" :icon="Edit" @click="openEdit(row)">{{ isTeacher && activeMenu === 'grades' ? '录入' : isTeacher && activeMenu === 'attendance' ? '登记' : '编辑' }}</el-button><el-button v-if="canDeleteRows" link type="danger" :icon="Delete" @click="removeRow(row)">{{ isStudent ? '退选' : '删除' }}</el-button></template></el-table-column></el-table><div v-if="['students','users'].includes(activeMenu)" class="pagination"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total" @change="loadRows" /></div></el-card>
        </template>
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
      <template v-if="dialogType === 'users'">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" autocomplete="off" /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色" prop="role"><el-select v-model="form.role"><el-option label="管理员" value="ADMIN" /><el-option label="教师" value="TEACHER" /><el-option label="学生" value="STUDENT" /></el-select></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="新增默认 123456，编辑留空表示不修改" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="启用" value="1" /><el-option label="禁用" value="0" /></el-select></el-form-item>
      </template>
      <template v-else-if="dialogType === 'students'">
        <el-form-item label="学号" prop="studentNo"><el-input v-model="form.studentNo" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="form.gender" clearable><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
        <el-form-item label="院系" prop="departmentId"><el-select v-model="form.departmentId" clearable><el-option v-for="item in reference.departments" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="班级"><el-select v-model="form.classId" clearable><el-option v-for="item in reference.classes" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="入学年份"><el-input-number v-model="form.admissionYear" :min="2000" :max="2100" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="在读" value="在读" /><el-option label="毕业" value="毕业" /><el-option label="休学" value="休学" /></el-select></el-form-item>
      </template>
      <template v-else-if="dialogType === 'teachers'">
        <el-form-item label="登录用户名" prop="username"><el-input v-model="form.username" :disabled="Boolean(form.id)" /></el-form-item>
        <el-form-item label="初始/重置密码"><el-input v-model="form.password" type="password" show-password placeholder="新增默认 123456，编辑留空表示不修改" /></el-form-item>
        <el-form-item label="工号" prop="teacherNo"><el-input v-model="form.teacherNo" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="院系"><el-select v-model="form.departmentId" clearable><el-option v-for="item in reference.departments" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item v-if="form.id" label="账号状态"><el-select v-model="form.userStatus"><el-option label="启用" value="1" /><el-option label="禁用" value="0" /></el-select></el-form-item>
      </template>
      <template v-else-if="dialogType === 'classes'">
        <el-form-item label="班级名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="班级编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="院系" prop="departmentId"><el-select v-model="form.departmentId"><el-option v-for="item in reference.departments" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="年级"><el-input-number v-model="form.gradeYear" :min="2000" :max="2100" /></el-form-item>
      </template>
      <template v-else-if="dialogType === 'departments'">
        <el-form-item label="院系名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="院系编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </template>
      <template v-else-if="dialogType === 'courses'">
        <el-form-item label="课程编号" prop="courseNo"><el-input v-model="form.courseNo" /></el-form-item>
        <el-form-item label="课程名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="学分"><el-input-number v-model="form.credit" :min="0" :max="10" :precision="1" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="form.hours" :min="1" :max="200" /></el-form-item>
        <el-form-item label="学期" prop="semester"><el-input v-model="form.semester" /></el-form-item>
        <el-form-item label="授课教师"><el-select v-model="form.teacherId" clearable><el-option v-for="item in reference.teachers" :key="item.id" :label="`${item.name} (${item.teacherNo})`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="课程简介"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </template>
      <template v-else-if="dialogType === 'enrollments'">
        <el-form-item label="课程" prop="courseId"><el-select v-model="form.courseId"><el-option v-for="item in enrollmentCourses" :key="item.id" :label="`${item.courseNo} · ${item.name}`" :value="item.id" /></el-select></el-form-item>
        <el-form-item v-if="isAdmin" label="学生" prop="studentId"><el-select v-model="form.studentId"><el-option v-for="item in reference.students" :key="item.id" :label="`${item.studentNo} · ${item.name}`" :value="item.id" /></el-select></el-form-item>
      </template>
      <template v-else-if="dialogType === 'attendance'">
        <el-form-item label="课程" prop="courseId"><el-select v-model="form.courseId"><el-option v-for="item in reference.courses" :key="item.id" :label="`${item.courseNo} · ${item.name}`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="学生" prop="studentId"><el-select v-model="form.studentId"><el-option v-for="item in attendanceStudents" :key="item.studentId" :label="`${item.studentNo} · ${item.studentName}`" :value="item.studentId" /></el-select></el-form-item>
        <el-form-item label="日期" prop="attendanceDate"><el-date-picker v-model="form.attendanceDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="考勤状态" prop="status"><el-select v-model="form.status"><el-option label="出勤" value="出勤" /><el-option label="迟到" value="迟到" /><el-option label="请假" value="请假" /><el-option label="缺勤" value="缺勤" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </template>
      <template v-else-if="dialogType === 'notices'">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="接收范围"><el-select v-model="form.targetRole"><el-option label="全部用户" value="ALL" /><el-option label="教师" value="TEACHER" /><el-option label="学生" value="STUDENT" /></el-select></el-form-item>
      </template>
      <template v-else-if="dialogType === 'grades'">
        <el-form-item label="平时成绩" prop="usualScore"><el-input-number v-model="form.usualScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="期中成绩" prop="midtermScore"><el-input-number v-model="form.midtermScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="期末成绩" prop="finalScore"><el-input-number v-model="form.finalScore" :min="0" :max="100" :precision="2" /></el-form-item>
      </template>
    </el-form>
    <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="profileVisible" title="个人设置" width="420px">
    <el-form ref="profileRef" :model="profileForm" label-position="top">
      <el-form-item label="登录账号"><el-input :model-value="user?.username" disabled /></el-form-item>
      <el-form-item label="姓名" prop="realName" :rules="[{ required: true, message: '请输入姓名', trigger: 'blur' }]"><el-input v-model="profileForm.realName" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="profileVisible = false">取消</el-button><el-button type="primary" :loading="profileLoading" @click="saveProfile">保存</el-button></template>
  </el-dialog>
</template>
