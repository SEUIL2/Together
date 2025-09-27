// src/api/meetingApi.js
import axios from 'axios'

const token = localStorage.getItem('authHeader') // 여기만 바꾸면 돼!

const api = axios.create({
  baseURL: 'http://localhost:8081/meeting', // 백엔드 주소
  // baseURL: 'http://25.12.59.4:3000/meeting', // 건우 주소
  // baseURL: 'http://15.165.234.3/meeting', // 서버 주소
  withCredentials: true,
  headers: {
    Authorization: token ? token : ''
  }
})


export default api
