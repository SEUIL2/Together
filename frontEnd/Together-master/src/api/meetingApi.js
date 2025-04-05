// src/api/meetingApi.js
import axios from 'axios'

const token = localStorage.getItem('authHeader') // 여기만 바꾸면 돼!

const api = axios.create({
  baseURL: 'http://localhost:8081/meeting',
  withCredentials: true,
  headers: {
    Authorization: token ? token : ''
  }
})

export default api
