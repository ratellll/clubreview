import api from './api';

export const clubService = {
    async getClubs(params = {}) {
        try {
            const response = await api.get('/clubs', {params});
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
            const response = await api.get(`/clubs/${id}/reviews`, {params});
            console.log('클럽 리뷰 API 응답:', response.data); // 디버깅용
            return response.data.data; // ApiResponse 구조에 맞게 data.data 반환
        } catch (error) {
            console.error('클럽 리뷰 조회 실패:', error);
            throw error;
        }
    },
    // 클럽 등록 (관리자)
    async createClub(formData) {
        try {
            const response = await api.post('/clubs', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data.data;
        } catch (error) {
            console.error('클럽 등록 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('클럽 등록에 실패했습니다.');
        }
    },

    // 클럽 수정 (관리자)
    async updateClub(clubId, formData) {
        try {
            const response = await api.put(`/clubs/${clubId}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data.data;
        } catch (error) {
            console.error('클럽 수정 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('클럽 수정에 실패했습니다.');
        }
    },

    // 클럽 삭제 (관리자)
    async deleteClub(clubId) {
        try {
            const response = await api.delete(`/clubs/${clubId}`);
            return response.data;
        } catch (error) {
            console.error('클럽 삭제 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('클럽 삭제에 실패했습니다.');
        }
    }
    };