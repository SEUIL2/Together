// src/api/index.js
import axios from 'axios';
import { API_BASE_URL } from './config';

const api = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,             // 쿠키 전송 허용
    // 헤더는 form-data 요청 시 자동 지정 안 하는 게 안전합니다
}); // ← 꼭 세미콜론!

// 요청 전에 Authorization 헤더 붙이기
api.interceptors.request.use(config => {
    const basic = localStorage.getItem('authHeader');
    if (basic) {
        // 이미 'Basic xxx' 형식으로 저장되어 있다면 그대로,
        // JWT 방식일 땐 `Bearer ${token}` 으로 바꾸세요.
        config.headers.Authorization = basic;
    }
    return config;
});

export default api;
