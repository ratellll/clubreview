import api from './api';

export const reviewService = {
    async createReview(reviewData) {
        try {
            // JSON 형태로 전송 (새 REST API에 맞춤)
            const response = await api.post('/reviews', {
                clubId: reviewData.clubId,
                rating: reviewData.rating,
                comment: reviewData.comment
            });
            return response.data;
        } catch (error) {
            console.error('리뷰 작성 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 작성에 실패했습니다.');
        }
    },

    async updateReview(reviewId, reviewData) {
        try {
            // JSON 형태로 전송
            const response = await api.put(`/reviews/${reviewId}`, {
                comment: reviewData.comment,
                rating: reviewData.rating
            });
            return response.data;
        } catch (error) {
            console.error('리뷰 수정 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 수정에 실패했습니다.');
        }
    },

    async deleteReview(reviewId) {
        try {
            const response = await api.delete(`/reviews/${reviewId}`);
            return response.data;
        } catch (error) {
            console.error('리뷰 삭제 실패:', error);
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('리뷰 삭제에 실패했습니다.');
        }
    },

    async getReview(reviewId) {
        try {
            const response = await api.get(`/reviews/${reviewId}`);
            return response.data.data;
        } catch (error) {
            console.error('리뷰 조회 실패:', error);
            throw error;
        }
    }
};