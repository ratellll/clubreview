import api from './api';

export const authService = {
    async login(credentials) {
        try {
            console.log('🚀 로그인 요청:', credentials);
            const response = await api.post('/auth/login', credentials);
            console.log('✅ 로그인 응답:', response.data);
            return response.data.data;
        } catch (error) {
            console.error('❌ 로그인 오류:', error.response || error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('로그인에 실패했습니다.');
        }
    },

    async register(userData) {
        try {
            console.log('🚀 회원가입 요청:', userData);
            const response = await api.post('/auth/register', userData);
            console.log('✅ 회원가입 응답:', response.data);
            return response.data;
        } catch (error) {
            console.error('❌ 회원가입 오류:', error.response || error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('회원가입에 실패했습니다.');
        }
    },

    async checkDuplicate(type, value) {
        try {
            const response = await api.get('/auth/check', {
                params: { type, value }
            });
            return response.data.data; // boolean 값
        } catch (error) {
            console.error('❌ 중복 확인 오류:', error.response || error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('중복 확인에 실패했습니다.');
        }
    }
};