import React, {useState, useEffect, useCallback} from 'react';
import {useParams, useNavigate, useLocation} from 'react-router-dom';
import {clubService} from '../services/clubService';
import {reviewService} from '../services/reviewService';
import {useAuth} from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import {adminService} from '../services/adminService';
import Alert from '../components/common/Alert';

const normalize = (v) => (v ?? '').toString().trim().toLowerCase();
const truthy = (v) => v !== undefined && v !== null && v !== '';
const pick = (...vals) => vals.filter(truthy);
const toStrings = (arr) => arr.map((x) => x.toString());
const toNorms = (arr) => arr.map(normalize);
const intersects = (a, b) => a.some((x) => b.includes(x));

function isReviewAuthor(review, user) {
    if (!review || !user) return false;

    const reviewIds = toStrings(
        pick(review.userId, review.memberId, review.accountId, review.authorId, review.createdById)
    );
    const userIds = toStrings(pick(user.userId, user.id, user.memberId, user.accountId));
    if (reviewIds.length && userIds.length && intersects(reviewIds, userIds)) return true;

    const reviewNames = toNorms(
        pick(
            review.userName,
            review.username,
            review.userNickName,
            review.userNickname,
            review.authorName,
            review.createdBy,
            review.userEmail,
            review.email
        )
    );
    const userNames = toNorms(
        pick(user.userName, user.username, user.nickName, user.nickname, user.name, user.email)
    );
    if (reviewNames.length && userNames.length && intersects(reviewNames, userNames)) return true;

    if (review.isMine === true) return true;

    return false;
}

const ClubDetailPage = () => {
    const {id} = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const {isAuthenticated, user} = useAuth();

    const [club, setClub] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [reviewForm, setReviewForm] = useState({rating: 5, comment: ''});
    const [editingReview, setEditingReview] = useState(null);

    const fetchClubDetail = useCallback(async () => {
        try {
            const response = await clubService.getClub(id);
            setClub(response);
        } catch (err) {
            console.error('클럽 상세 조회 실패:', err);
            setError('클럽 정보를 불러오는데 실패했습니다.');
        }
    }, [id]);

    const fetchClubReviews = useCallback(async () => {
        try {
            const response = await clubService.getClubReviews(id);
            setReviews(response || []);
        } catch (err) {
            console.error('리뷰 조회 실패:', err);
            setReviews([]);
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        if (id) {
            fetchClubDetail();
            fetchClubReviews();
        }
    }, [id, fetchClubDetail, fetchClubReviews]);

    useEffect(() => {
        if (location.state?.refresh && id) {
            fetchClubDetail();
            fetchClubReviews();
        }
    }, [location.state?.refresh, id, fetchClubDetail, fetchClubReviews]);

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        if (!isAuthenticated) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            const reviewData = {clubId: parseInt(id, 10), rating: reviewForm.rating, comment: reviewForm.comment};
            await reviewService.createReview(reviewData);
            setReviewForm({rating: 5, comment: ''});
            fetchClubReviews();
            alert('리뷰가 등록되었습니다.');
        } catch (err) {
            console.error('리뷰 등록 실패:', err);
            alert(err.message);
        }
    };

    const handleEditReview = async (reviewId, updatedData, isAdmin = false) => {
        try {
            if (isAdmin) {
                await adminService.updateReview(reviewId, updatedData);
            }else {
                await reviewService.updateReview(reviewId, updatedData);
            }
            setEditingReview(null);
            fetchClubReviews();
            alert('리뷰가 수정되었습니다.');
        } catch (err) {
            console.error('리뷰 수정 실패:', err);
            alert(err.message);
        }
    };

    const handleDeleteReview = async (reviewId) => {
        if (!window.confirm('정말로 이 리뷰를 삭제하시겠습니까?')) return;
        try {
            await reviewService.deleteReview(reviewId);
            fetchClubReviews();
            alert('리뷰가 삭제되었습니다.');
        } catch (err) {
            console.error('리뷰 삭제 실패:', err);
            alert(err.message);
        }
    };

    // 어드민 권한으로 리뷰 삭제
    const handleAdminDeleteReview = async (reviewId) => {
        if (!window.confirm('관리자 권한으로 이 리뷰를 삭제하시겠습니까?')) return;
        try {
            await adminService.deleteReview(reviewId);
            fetchClubReviews();
            alert('리뷰가 삭제되었습니다.');
        } catch (err) {
            console.error('어드민 리뷰 삭제 실패:', err);
            alert(err.message);
        }
    };
    // 어드민 권한으로 리뷰 수정
    const handleAdminEditReview = async (reviewId, updatedData) => {
        try {
            await adminService.updateReview(reviewId, updatedData);
            setEditingReview(null);
            fetchClubReviews();
            alert('리뷰가 수정되었습니다.');
        } catch (err) {
            console.error('어드민 리뷰 수정 실패:', err);
            alert(err.message);
        }
    };

    const renderStars = (rating) => '★'.repeat(rating) + '☆'.repeat(5 - rating);

    if (loading) return <LoadingSpinner message="클럽 정보를 불러오는 중..."/>;

    if (!club) {
        return (
            <div className="container mt-5">
                <Alert type="danger" message="클럽을 찾을 수 없습니다."/>
                <button onClick={() => navigate('/clubs')} className="btn btn-primary">클럽 목록으로 돌아가기</button>
            </div>
        );
    }

    return (
        <div className="container mt-4">
            {error && (
                <Alert type="danger" message={error} onClose={() => setError('')} dismissible/>
            )}

            {/* 클럽 정보 */}
            <div className="row mb-5">
                <div className="col-md-8">
                    <h1 className="mb-3">{club.name}</h1>
                    <div className="mb-3">
                        <p><strong>📍 위치:</strong> {club.location}</p>
                        <p><strong>📞 전화번호:</strong> {club.callNumber}</p>
                        <p><strong>⭐ 평점:</strong> {club.averageRating} / 5</p>
                        <p><strong>📝 설명:</strong> {club.description}</p>
                    </div>
                </div>
                <div className="col-md-4">
                    {club.photoUrl && (
                        <img src={club.photoUrl} className="img-fluid rounded shadow" alt="클럽 사진"
                             style={{maxHeight: '300px', width: '100%', objectFit: 'cover'}}/>
                    )}
                </div>
            </div>

            {/* 리뷰 목록 */}
            <div className="mb-5">
                <h2 className="mb-4">📋 리뷰 ({reviews.length}개)</h2>

                {reviews.length === 0 ? (
                    <div className="text-center py-4">
                        <p className="text-muted">아직 등록된 리뷰가 없습니다.</p>
                    </div>
                ) : (
                    <div>
                        {reviews.map((review) => (
                            <div key={review.id} className="card mb-3">
                                <div className="card-body">
                                    <div className="d-flex justify-content-between align-items-start mb-2">
                                        <div>
                                            <strong>{review.userNickName || review.userName || review.username || review.authorName || '사용자'}</strong>
                                            <small
                                                className="text-muted ms-2">{new Date(review.createTime).toLocaleDateString()}</small>
                                            <div className="text-warning">{renderStars(review.rating)}</div>
                                        </div>
                                    </div>

                                    {editingReview === review.id ? (
                                        <EditReviewForm review={review} onSave={handleEditReview}
                                                        onCancel={() => setEditingReview(null)}/>
                                    ) : (
                                        <>
                                            <p className="card-text">{review.comment}</p>

                                            {/* 수정/삭제 버튼: 작성자 또는 어드민 */}
                                            {isAuthenticated && (
                                                <div className="mt-2">
                                                    {/* 일반 사용자: 본인 리뷰만 */}
                                                    {isReviewAuthor(review, user) && (
                                                        <>
                                                            <button className="btn btn-sm btn-outline-primary me-2"
                                                                    onClick={() => setEditingReview(review.id)}>수정
                                                            </button>
                                                            <button className="btn btn-sm btn-outline-danger me-2"
                                                                    onClick={() => handleDeleteReview(review.id)}>삭제
                                                            </button>
                                                        </>
                                                    )}

                                                    {/* 어드민: 모든 리뷰 관리 가능 */}
                                                    {user?.role === 'ADMIN' && (
                                                        <>
                                                            <button className="btn btn-sm btn-warning me-2"
                                                                    onClick={() => setEditingReview(review.id)}>
                                                                🛠️ 관리자 수정
                                                            </button>
                                                            <button className="btn btn-sm btn-danger"
                                                                    onClick={() => handleAdminDeleteReview(review.id)}>
                                                                🗑️ 관리자 삭제
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            )}
                                        </>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* 리뷰 작성 폼 */}
            {isAuthenticated ? (
                <div className="card">
                    <div className="card-body">
                        <h3 className="card-title">✍️ 리뷰 작성</h3>
                        <form onSubmit={handleReviewSubmit}>
                            <div className="mb-3">
                                <label htmlFor="rating" className="form-label">평점</label>
                                <select className="form-select" id="rating" value={reviewForm.rating}
                                        onChange={(e) => setReviewForm({
                                            ...reviewForm,
                                            rating: parseInt(e.target.value, 10)
                                        })} required>
                                    <option value={5}>★★★★★ (5점)</option>
                                    <option value={4}>★★★★☆ (4점)</option>
                                    <option value={3}>★★★☆☆ (3점)</option>
                                    <option value={2}>★★☆☆☆ (2점)</option>
                                    <option value={1}>★☆☆☆☆ (1점)</option>
                                </select>
                            </div>
                            <div className="mb-3">
                                <label htmlFor="comment" className="form-label">리뷰 내용</label>
                                <textarea className="form-control" id="comment" rows={4} value={reviewForm.comment}
                                          onChange={(e) => setReviewForm({...reviewForm, comment: e.target.value})}
                                          placeholder="리뷰를 작성해주세요" required/>
                            </div>
                            <button type="submit" className="btn btn-primary">리뷰 등록</button>
                        </form>
                    </div>
                </div>
            ) : (
                <div className="text-center py-4">
                    <p className="text-muted">리뷰를 작성하려면 로그인이 필요합니다.</p>
                    <button onClick={() => navigate('/login')} className="btn btn-primary">로그인하기</button>
                </div>
            )}

            {/* 뒤로가기 버튼 */}
            <div className="text-center mt-4 mb-5">
                <button onClick={() => navigate('/clubs')} className="btn btn-outline-secondary">클럽 목록으로 돌아가기</button>
            </div>
        </div>
    );
};

// 리뷰 수정 폼 컴포넌트
const EditReviewForm = ({review, onSave, onCancel}) => {
    const [editData, setEditData] = useState({rating: review.rating, comment: review.comment});
    const {user} = useAuth();
    const handleSubmit = (e) => {
        e.preventDefault();
        // 어드민인지 확인하여 적절한 핸들러 호출
        if (user?.role === 'ADMIN') {
            // 어드민은 adminService 사용
            onSave(review.id, editData, true); // 세번째 파라미터로 어드민 여부 전달
        } else {
            // 일반 사용자는 기존 방식
            onSave(review.id, editData);
        }
    };
    return (
        <form onSubmit={handleSubmit}>
            <div className="mb-3">
                <label className="form-label">평점</label>
                <select className="form-select form-select-sm" value={editData.rating}
                        onChange={(e) => setEditData({...editData, rating: parseInt(e.target.value, 10)})} required>
                    <option value={5}>★★★★★ (5점)</option>
                    <option value={4}>★★★★☆ (4점)</option>
                    <option value={3}>★★★☆☆ (3점)</option>
                    <option value={2}>★★☆☆☆ (2점)</option>
                    <option value={1}>★☆☆☆☆ (1점)</option>
                </select>
            </div>
            <div className="mb-3">
                <textarea className="form-control" rows={3} value={editData.comment}
                          onChange={(e) => setEditData({...editData, comment: e.target.value})} required/>
            </div>
            <div>
                <button type="submit" className="btn btn-sm btn-primary me-2">{user?.role === 'ADMIN' ? '관리자 수정 완료' : '수정 완료'}</button>
                <button type="button" className="btn btn-sm btn-secondary" onClick={onCancel}>취소</button>
            </div>
        </form>
    );
};

export default ClubDetailPage;
