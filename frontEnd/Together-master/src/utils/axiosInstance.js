// src/utils/axiosInstance.js
import axios from 'axios'

const instance = axios.create({
    baseURL: 'http://localhost:8081', // 필요에 따라 수정
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
})

export default instance
