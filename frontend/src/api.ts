import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

function clearAuthentication() {
  localStorage.removeItem('student_token')
  localStorage.removeItem('student_user')
}

http.interceptors.request.use(config => {
  const token = localStorage.getItem('student_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  response => {
    const payload = response.data
    if (payload.code === 401) {
      clearAuthentication()
      window.location.reload()
    }
    if (payload.code !== 0) return Promise.reject(new Error(payload.message || '请求失败'))
    return payload.data
  },
  error => {
    if (error.response?.status === 401) {
      clearAuthentication()
      window.location.reload()
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '网络请求失败'))
  }
)

export default http
