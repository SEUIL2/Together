// src/api/meetingApi.js
import axios from 'axios'

const api = axios.create({
  baseURL: '/api/meeting',  // 상대 경로로 변경
  withCredentials: true
})

// 요청 인터셉터로 Authorization 헤더 추가
api.interceptors.request.use(config => {
  const token = localStorage.getItem('authHeader')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

export default api
