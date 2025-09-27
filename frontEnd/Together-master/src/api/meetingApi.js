// src/api/meetingApi.js
import axios from 'axios'
import { API_BASE_URL } from '../config'

const token = localStorage.getItem('authHeader') // 여기만 바꾸면 돼!

const api = axios.create({
  baseURL: `${API_BASE_URL}/meeting`,
  withCredentials: true,
  headers: {
    Authorization: token ? token : ''
  }
})


export default api
