import api from './api';

export const adminService = {
    // 사용자 목록 조회
    async getUsers() {
        try {
            const response = await api.get('/admin/users');
            return response.data.data;
        } catch (error) {
            console.error('사용자 목록 조회 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('사용자 목록 조회에 실패했습니다.');
        }
    },

    // 사용자 검색
    async searchUsers(nickname) {
        try {
            const response = await api.get(`/admin/users/search?nickname=${encodeURIComponent(nickname)}`);
            return response.data.data;
        } catch (error) {
            console.error('사용자 검색 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('사용자 검색에 실패했습니다.');
        }
    },

    // 사용자 벤
    async banUser(userId, days) {
        try {
            const response = await api.post(`/admin/users/${userId}/ban?days=${days}`);
            return response.data.data;
        } catch (error) {
            console.error('사용자 벤 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('사용자 벤에 실패했습니다.');
        }
    },

    // 사용자 벤 해제
    async unbanUser(userId) {
        try {
            const response = await api.post(`/admin/users/${userId}/unban`);
            return response.data.data;
        } catch (error) {
            console.error('사용자 벤 해제 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('사용자 벤 해제에 실패했습니다.');
        }
    },

    // 사용자 삭제
    async deleteUser(userId) {
        try {
            const response = await api.delete(`/admin/users/${userId}`);
            return response.data;
        } catch (error) {
            console.error('사용자 삭제 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('사용자 삭제에 실패했습니다.');
        }
    },

    // 모든 리뷰 조회
    async getAllReviews() {
        try {
            const response = await api.get('/admin/reviews');
            return response.data.data;
        } catch (error) {
            console.error('모든 리뷰 조회 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('모든 리뷰 조회에 실패했습니다.');
        }
    },

    // 관리자 권한으로 리뷰 수정
    async updateReview(reviewId, reviewData) {
        try {
            const response = await api.put(`/reviews/admin/${reviewId}`, reviewData);
            return response.data.data;
        } catch (error) {
            console.error('리뷰 수정 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 수정에 실패했습니다.');
        }
    },

    // 관리자 권한으로 리뷰 삭제
    async deleteReview(reviewId) {
        try {
            const response = await api.delete(`/reviews/admin/${reviewId}`);
            return response.data;
        } catch (error) {
            console.error('리뷰 삭제 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 삭제에 실패했습니다.');
        }
    }
};