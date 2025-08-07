import api from './api';

export const reviewService = {
    async createReview(reviewData) {
        try {
            // FormData 형태로 전송 (Controller의 @RequestParam에 맞춤)
            const formData = new FormData();
            formData.append('clubId', reviewData.clubId);
            formData.append('rating', reviewData.rating);
            formData.append('comment', reviewData.comment);

            const response = await api.post('/reviews/add', formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
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
            // FormData 형태로 전송
            const formData = new FormData();
            formData.append('comment', reviewData.comment);
            formData.append('rating', reviewData.rating);
            formData.append('clubId', reviewData.clubId);

            const response = await api.post(`/reviews/user/edit/${reviewId}`, formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
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

    async deleteReview(reviewId, clubId) {
        try {
            const formData = new FormData();
            formData.append('clubId', clubId);

            const response = await api.post(`/reviews/user/delete/${reviewId}`, formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            });
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