import api from './api';

export const clubService = {
    async getClubs(params = {}) {
        try {
            const response = await api.get('/clubs', { params });
            return response.data.data;
        } catch (error) {
            console.error('클럽 목록 조회 실패:', error);
            throw error;
        }
    },

    async getClub(id) {
        try {
            const response = await api.get(`/clubs/${id}`);
            return response.data.data;
        } catch (error) {
            console.error('클럽 상세 조회 실패:', error);
            throw error;
        }
    },

    async getClubReviews(id, params = {}) {
        try {
            const response = await api.get(`/clubs/${id}/reviews`, { params });
            console.log('클럽 리뷰 API 응답:', response.data); // 디버깅용
            return response.data.data; // ApiResponse 구조에 맞게 data.data 반환
        } catch (error) {
            console.error('클럽 리뷰 조회 실패:', error);
            throw error;
        }
    }
};