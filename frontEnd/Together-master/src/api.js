import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8081', // Spring Boot 백엔드 주소
    headers: {
        'Content-Type': 'application/json',
    },
});


// 요청 보내기 전에 Authorization 헤더 자동 삽입
axios.interceptors.request.use((config) => {
    const authHeader = localStorage.getItem("authHeader");
    if (authHeader) {
        config.headers.Authorization = authHeader;
    }
    return config;
});

export default api;