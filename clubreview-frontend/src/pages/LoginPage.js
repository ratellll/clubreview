import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/common/Alert';

const LoginPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { login, isAuthenticated } = useAuth();

    const [formData, setFormData] = useState({
        userName: '',
        password: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // 로그인된 사용자는 클럽 목록으로 리다이렉트
    useEffect(() => {
        if (isAuthenticated) {
            navigate('/clubs');
        }
    }, [isAuthenticated, navigate]);

    // 회원가입 성공 메시지 표시
    useEffect(() => {
        if (location.state?.message) {
            setSuccess(location.state.message);
        }
    }, [location.state]);

    const from = location.state?.from?.pathname || '/clubs';

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        // 입력 시 오류 메시지 초기화
        if (error) setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // 유효성 검사
        if (!formData.userName.trim()) {
            setError('아이디를 입력해주세요.');
            return;
        }
        if (!formData.password.trim()) {
            setError('비밀번호를 입력해주세요.');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await login(formData);
            console.log('로그인 성공! 이동:', from);
            navigate(from, { replace: true });
        } catch (err) {
            console.error('로그인 실패:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container mt-5">
            <div className="row justify-content-center">
                <div className="col-md-6 col-lg-5">
                    <div className="card shadow">
                        <div className="card-body p-5">
                            {/* 헤더 */}
                            <div className="text-center mb-4">
                                <h1 className="h3 text-primary">🎉 클럽의 민족</h1>
                                <p className="text-muted">로그인하여 클럽 리뷰를 확인하세요</p>
                            </div>

                            {/* 알림 메시지 */}
                            {success && (
                                <Alert
                                    type="success"
                                    message={success}
                                    onClose={() => setSuccess('')}
                                    dismissible
                                />
                            )}

                            {error && (
                                <Alert
                                    type="danger"
                                    message={error}
                                    onClose={() => setError('')}
                                    dismissible
                                />
                            )}

                            {/* 로그인 폼 */}
                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label htmlFor="userName" className="form-label">
                                        아이디 <span className="text-danger">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        className={`form-control ${error && !formData.userName ? 'is-invalid' : ''}`}
                                        id="userName"
                                        name="userName"
                                        value={formData.userName}
                                        onChange={handleChange}
                                        placeholder="아이디를 입력하세요"
                                        disabled={loading}
                                        autoComplete="userName"
                                    />
                                </div>

                                <div className="mb-4">
                                    <label htmlFor="password" className="form-label">
                                        비밀번호 <span className="text-danger">*</span>
                                    </label>
                                    <input
                                        type="password"
                                        className={`form-control ${error && !formData.password ? 'is-invalid' : ''}`}
                                        id="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        placeholder="비밀번호를 입력하세요"
                                        disabled={loading}
                                        autoComplete="current-password"
                                    />
                                </div>

                                <button
                                    type="submit"
                                    className="btn btn-primary w-100 py-2"
                                    disabled={loading}
                                >
                                    {loading ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                            로그인 중...
                                        </>
                                    ) : (
                                        '로그인'
                                    )}
                                </button>
                            </form>

                            {/* 테스트 계정 안내 */}
                            <div className="mt-4 p-3 bg-light rounded">
                                <small className="text-muted">
                                    <strong>테스트 계정:</strong><br />
                                    아이디: user1, 비밀번호: password123<br />
                                    아이디: admin, 비밀번호: admin123
                                </small>
                            </div>

                            {/* 회원가입 링크 */}
                            <div className="text-center mt-4">
                                <p className="mb-0">
                                    계정이 없으신가요?
                                    <Link to="/register" className="text-decoration-none ms-1">
                                        회원가입
                                    </Link>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;