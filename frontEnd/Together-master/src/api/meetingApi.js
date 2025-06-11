// src/api/meetingApi.js
import axios from 'axios'

const token = localStorage.getItem('authHeader') // 여기만 바꾸면 돼!

const api = axios.create({
  baseURL: 'http://25.12.59.4:3000/meeting',
  withCredentials: true,
  headers: {
    Authorization: token ? token : ''
  }
})

export default api
