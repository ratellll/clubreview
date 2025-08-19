import axios from 'axios';

const api = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL + '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// JWT 토큰 자동 설정
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 응답 인터셉터
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        } else if (error.response?.status === 403) {
            alert('❌ 계정이 정지되어 접근할 수 없습니다.');
        }
        return Promise.reject(error);
    }
);

export default api;