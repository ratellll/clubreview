import api from './api';

export const userService = {
    // 마이페이지 데이터 조회
    async getMyPage() {
        try {
            const response = await api.get('/users/profile');
            return response.data.data;
        } catch (error) {
            console.error('마이페이지 조회 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('마이페이지 조회에 실패했습니다.');
        }
    },

    // 내 리뷰 목록 조회
    async getMyReviews() {
        try {
            const response = await api.get('/users/profile/reviews');
            return response.data.data;
        } catch (error) {
            console.error('리뷰 목록 조회 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 목록 조회에 실패했습니다.');
        }
    },

    // 리뷰 통계 조회
    async getMyReviewStatistics() {
        try {
            const response = await api.get('/users/profile/reviews/statistics');
            return response.data.data;
        } catch (error) {
            console.error('리뷰 통계 조회 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 통계 조회에 실패했습니다.');
        }
    },

    // 기간별 리뷰 조회
    async getMyReviewsByPeriod(startDate, endDate) {
        try {
            const response = await api.get('/users/profile/reviews/period', {
                params: {
                    startDate: startDate.toISOString(),
                    endDate: endDate.toISOString()
                }
            });
            return response.data.data;
        } catch (error) {
            console.error('기간별 리뷰 조회 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('기간별 리뷰 조회에 실패했습니다.');
        }
    },

    // 닉네임 변경
    async updateNickName(nickName) {
        try {
            const response = await api.put('/users/profile/nickName', { nickName });
            return response.data.data;
        } catch (error) {
            console.error('닉네임 변경 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('닉네임 변경에 실패했습니다.');
        }
    },

    // 비밀번호 변경
    async updatePassword(password) {
        try {
            const response = await api.put('/users/profile/password', { password });
            return response.data;
        } catch (error) {
            console.error('비밀번호 변경 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('비밀번호 변경에 실패했습니다.');
        }
    }
};