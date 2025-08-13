import React, { useState, useEffect } from 'react';
import { userService } from '../services/userService';
import { useLocation } from 'react-router-dom';
import { reviewService } from '../services/reviewService';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';

const MyPage = () => {
    const { user, updateUser } = useAuth();
    const location = useLocation();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const [myPageData, setMyPageData] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [editingReview, setEditingReview] = useState(null);

    const [nickNameForm, setNickNameForm] = useState({
        nickName: '',
        checked: false,
        available: false,
        message: ''
    });

    const [passwordForm, setPasswordForm] = useState({
        password: '',
        valid: false,
        message: ''
    });

    useEffect(() => {
        fetchMyPageData();
        fetchMyReviews();
    }, []);

    useEffect(() => {
        if (location.state?.refresh) {
                        console.log('마이페이지 새로고침됨');
                        fetchMyPageData(); // 사용자 정보 다시 로드
                        fetchMyReviews(); // 리뷰 목록 다시 로드
                    }
            }, [location.state?.refresh]);

    const fetchMyPageData = async () => {
        try {
            const data = await userService.getMyPage();
            setMyPageData(data);
            setError('');
        } catch (err) {
            console.error('마이페이지 조회 실패:', err);
            setError('사용자 정보를 불러오는데 실패했습니다.');
        }
    };

    const fetchMyReviews = async () => {
        try {
            const reviewData = await userService.getMyReviews();
            setReviews(reviewData);
            setError('');
        } catch (err) {
            console.error('리뷰 조회 실패:', err);
            setError('리뷰 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const checkNicknameDuplicate = async () => {
        const nickName = nickNameForm.nickName.trim();

        if (!nickName) {
            setNickNameForm(prev => ({
                ...prev,
                checked: false,
                available: false,
                message: '닉네임을 입력하세요.'
            }));
            return;
        }

        if (!/^[가-힣]+$/.test(nickName)) {
            setNickNameForm(prev => ({
                ...prev,
                checked: false,
                available: false,
                message: '닉네임은 한글만 입력 가능합니다.'
            }));
            return;
        }

        if (nickName.length < 2 || nickName.length > 5) {
            setNickNameForm(prev => ({
                ...prev,
                checked: false,
                available: false,
                message: '닉네임은 2자 이상, 5자 이하여야 합니다.'
            }));
            return;
        }

        try {
            const isAvailable = await authService.checkDuplicate('nickName', nickName);
            setNickNameForm(prev => ({
                ...prev,
                checked: true,
                available: isAvailable,
                message: isAvailable ? '사용 가능한 닉네임입니다.' : '이미 사용 중인 닉네임입니다.'
            }));
        } catch (err) {
            setNickNameForm(prev => ({
                ...prev,
                checked: false,
                available: false,
                message: '중복 체크에 실패했습니다.'
            }));
        }
    };

    const validatePassword = (password) => {
        const isValid = password.length >= 8;
        setPasswordForm(prev => ({
            ...prev,
            password,
            valid: isValid,
            message: isValid ? '사용 가능한 비밀번호입니다.' : '비밀번호는 최소 8자 이상이어야 합니다.'
        }));
    };

    const handleNickNameSubmit = async (e) => {
        e.preventDefault();

        if (!nickNameForm.checked || !nickNameForm.available) {
            setError('닉네임 중복 체크를 완료하세요.');
            return;
        }

        try {
            await userService.updateNickName(nickNameForm.nickName);
            setSuccess('닉네임이 변경되었습니다.');
            setNickNameForm({ nickName: '', checked: false, available: false, message: '' });
            updateUser({ nickName: nickNameForm.nickName });
            fetchMyPageData(); // 데이터 새로고침
        } catch (err) {
            setError(err.message);
        }
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();

        if (!passwordForm.valid) {
            setError('비밀번호가 유효하지 않습니다.');
            return;
        }

        try {
            await userService.updatePassword(passwordForm.password);
            setSuccess('비밀번호가 변경되었습니다.');
            setPasswordForm({ password: '', valid: false, message: '' });
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEditReview = async (reviewId, updatedData) => {
        try {
            // reviewService 사용하여 실제 API 호출
            await reviewService.updateReview(reviewId, updatedData);

            setEditingReview(null);
            fetchMyReviews(); // 리뷰 목록 새로고침
            setSuccess('리뷰가 수정되었습니다.');
        } catch (err) {
            console.error('리뷰 수정 실패:', err);
            setError(err.message);
        }
    };

    const handleDeleteReview = async (reviewId) => {
        if (window.confirm('정말로 이 리뷰를 삭제하시겠습니까?')) {
            try {
                // reviewService 사용하여 실제 API 호출
                await reviewService.deleteReview(reviewId);

                fetchMyReviews(); // 리뷰 목록 새로고침
                setSuccess('리뷰가 삭제되었습니다.');
            } catch (err) {
                console.error('리뷰 삭제 실패:', err);
                setError(err.message);
            }
        }
    };

    if (loading) {
        return <LoadingSpinner message="마이페이지를 불러오는 중..." />;
    }

    return (
        <div className="container mt-4">
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <h1 className="mb-4">🌟 마이페이지</h1>

                    {/* 알림 메시지 */}
                    {error && (
                        <Alert
                            type="danger"
                            message={error}
                            onClose={() => setError('')}
                            dismissible
                        />
                    )}

                    {success && (
                        <Alert
                            type="success"
                            message={success}
                            onClose={() => setSuccess('')}
                            dismissible
                        />
                    )}

                    {/* 내 정보 */}
                    {myPageData && (
                        <div className="card mb-4">
                            <div className="card-body">
                                <h3 className="card-title">👤 내 정보</h3>
                                <div className="row">
                                    <div className="col-md-4">
                                        <strong>아이디:</strong> {myPageData.user?.userName || user?.userName}
                                    </div>
                                    <div className="col-md-4">
                                        <strong>닉네임:</strong> {myPageData.user?.nickName}
                                    </div>
                                    <div className="col-md-4">
                                        <strong>전화번호:</strong> {myPageData.user?.phoneNumber}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 닉네임 변경 */}
                    <div className="card mb-4">
                        <div className="card-body">
                            <h3 className="card-title">✏️ 닉네임 변경</h3>
                            <form onSubmit={handleNickNameSubmit}>
                                <div className="mb-3">
                                    <div className="input-group">
                                        <input
                                            type="text"
                                            className={`form-control ${nickNameForm.checked ?
                                                (nickNameForm.available ? 'is-valid' : 'is-invalid') : ''}`}
                                            value={nickNameForm.nickName}
                                            onChange={(e) => setNickNameForm(prev => ({
                                                ...prev,
                                                nickName: e.target.value,
                                                checked: false,
                                                available: false,
                                                message: ''
                                            }))}
                                            placeholder="새로운 닉네임을 입력하세요"
                                            disabled={nickNameForm.available}
                                        />
                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={checkNicknameDuplicate}
                                            disabled={nickNameForm.available || !nickNameForm.nickName.trim()}
                                        >
                                            중복 체크
                                        </button>
                                    </div>
                                    {nickNameForm.message && (
                                        <div className={`mt-1 small ${nickNameForm.available ? 'text-success' : 'text-danger'}`}>
                                            {nickNameForm.message}
                                        </div>
                                    )}
                                </div>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={!nickNameForm.checked || !nickNameForm.available}
                                >
                                    닉네임 변경
                                </button>
                            </form>
                        </div>
                    </div>

                    {/* 비밀번호 변경 */}
                    <div className="card mb-4">
                        <div className="card-body">
                            <h3 className="card-title">🔐 비밀번호 변경</h3>
                            <form onSubmit={handlePasswordSubmit}>
                                <div className="mb-3">
                                    <input
                                        type="password"
                                        className={`form-control ${passwordForm.valid ? 'is-valid' :
                                            (passwordForm.password ? 'is-invalid' : '')}`}
                                        value={passwordForm.password}
                                        onChange={(e) => validatePassword(e.target.value)}
                                        placeholder="새로운 비밀번호를 입력하세요 (최소 8자)"
                                    />
                                    {passwordForm.message && (
                                        <div className={`mt-1 small ${passwordForm.valid ? 'text-success' : 'text-danger'}`}>
                                            {passwordForm.message}
                                        </div>
                                    )}
                                </div>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={!passwordForm.valid}
                                >
                                    비밀번호 변경
                                </button>
                            </form>
                        </div>
                    </div>

                    {/* 내 리뷰 목록 */}
                    <div className="card">
                        <div className="card-body">
                            <h3 className="card-title">📝 내가 쓴 리뷰 ({reviews.length}개)</h3>

                            {reviews.length === 0 ? (
                                <div className="text-center py-4">
                                    <p className="text-muted">작성한 리뷰가 없습니다.</p>
                                </div>
                            ) : (
                                <div>
                                    {reviews.map(review => (
                                        <div key={review.id} className="border-bottom pb-3 mb-3">
                                            <div className="d-flex justify-content-between align-items-start">
                                                <div>
                                                    <strong className="text-primary">
                                                        🎉 {review.clubName || '클럽명'}
                                                    </strong>
                                                    <div className="text-warning">
                                                        {'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}
                                                    </div>
                                                    <p className="mt-2">{review.comment}</p>
                                                    <small className="text-muted">
                                                        {new Date(review.createTime).toLocaleDateString()}
                                                    </small>
                                                </div>
                                                <div>
                                                    <button
                                                        className="btn btn-sm btn-outline-primary me-2"
                                                        onClick={() => setEditingReview(review.id)}
                                                    >
                                                        수정
                                                    </button>
                                                    <button
                                                        className="btn btn-sm btn-outline-danger"
                                                        onClick={() => handleDeleteReview(review.id)}
                                                    >
                                                        삭제
                                                    </button>
                                                </div>
                                            </div>

                                            {/* 리뷰 수정 폼 */}
                                            {editingReview === review.id && (
                                                <EditReviewForm
                                                    review={review}
                                                    onSave={handleEditReview}
                                                    onCancel={() => setEditingReview(null)}
                                                />
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

// 리뷰 수정 폼 컴포넌트
const EditReviewForm = ({ review, onSave, onCancel }) => {
    const [editData, setEditData] = useState({
        rating: review.rating,
        comment: review.comment
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(review.id, editData);
    };

    return (
        <form onSubmit={handleSubmit} className="mt-3 p-3 bg-light rounded">
            <div className="mb-3">
                <label className="form-label">평점</label>
                <select
                    className="form-select form-select-sm"
                    value={editData.rating}
                    onChange={(e) => setEditData({...editData, rating: parseInt(e.target.value)})}
                    required
                >
                    <option value={5}>★★★★★ (5점)</option>
                    <option value={4}>★★★★☆ (4점)</option>
                    <option value={3}>★★★☆☆ (3점)</option>
                    <option value={2}>★★☆☆☆ (2점)</option>
                    <option value={1}>★☆☆☆☆ (1점)</option>
                </select>
            </div>
            <div className="mb-3">
                <label className="form-label">리뷰 내용</label>
                <textarea
                    className="form-control"
                    rows={3}
                    value={editData.comment}
                    onChange={(e) => setEditData({...editData, comment: e.target.value})}
                    required
                ></textarea>
            </div>
            <div>
                <button type="submit" className="btn btn-sm btn-primary me-2">
                    수정 완료
                </button>
                <button type="button" className="btn btn-sm btn-secondary" onClick={onCancel}>
                    취소
                </button>
            </div>
        </form>
    );
};

export default MyPage;