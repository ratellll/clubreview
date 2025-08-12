import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import Alert from '../components/common/Alert';

const RegisterPage = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        userName: '',
        password: '',
        nickName: '',
        phoneNumber: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [validationResults, setValidationResults] = useState({
        username: { checked: false, available: false, message: '' },
        nickName: { checked: false, available: false, message: '' },
        phoneNumber: { checked: false, available: false, message: '' },
        password: { valid: false, message: '' }
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });

        // 입력값 변경 시 해당 필드의 검증 상태 초기화
        if (name !== 'password') {
            setValidationResults(prev => ({
                ...prev,
                [name]: { checked: false, available: false, message: '' }
            }));
        } else {
            // 비밀번호 실시간 검증
            validatePassword(value);
        }

        if (error) setError('');
    };

    const validatePassword = (password) => {
        const isValid = password.length >= 8;
        setValidationResults(prev => ({
            ...prev,
            password: {
                valid: isValid,
                message: isValid ? '사용 가능한 비밀번호입니다.' : '비밀번호는 최소 8자 이상이어야 합니다.'
            }
        }));
    };

    const checkDuplicate = async (type) => {
        const value = formData[type].trim();

        // 기본 유효성 검사
        if (!value) {
            updateValidationResult(type, false, false, `${getDisplayName(type)}을(를) 입력해주세요.`);
            return;
        }

        // 추가 유효성 검사
        if (type === 'userName' && value.length < 4) {
            updateValidationResult(type, false, false, '아이디는 최소 4자 이상이어야 합니다.');
            return;
        }

        if (type === 'nickName' && !/^[가-힣]+$/.test(value)) {
            updateValidationResult(type, false, false, '닉네임은 한글만 입력 가능합니다.');
            return;
        }

        if (type === 'phoneNumber' && !/^\d{11}$/.test(value)) {
            updateValidationResult(type, false, false, '휴대폰 번호는 하이픈 없이 11자리를 입력해주세요.');
            return;
        }

        try {
            const isAvailable = await authService.checkDuplicate(type, value);
            const displayName = getDisplayName(type);

            if (isAvailable) {
                updateValidationResult(type, true, true, `사용 가능한 ${displayName}입니다.`);
            } else {
                updateValidationResult(type, true, false, `이미 사용 중인 ${displayName}입니다.`);
            }
        } catch (error) {
            updateValidationResult(type, false, false, '중복 체크에 실패했습니다. 다시 시도하세요.');
        }
    };

    const updateValidationResult = (type, checked, available, message) => {
        setValidationResults(prev => ({
            ...prev,
            [type]: { checked, available, message }
        }));
    };

    const getDisplayName = (type) => {
        const names = {
            userName: '아이디',
            nickName: '닉네임',
            phoneNumber: '휴대폰 번호'
        };
        return names[type] || '항목';
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // 유효성 검사
        if (!validationResults.userName.checked || !validationResults.userName.available) {
            setError('아이디 중복 체크를 완료하세요.');
            return;
        }

        if (!validationResults.nickName.checked || !validationResults.nickName.available) {
            setError('닉네임 중복 체크를 완료하세요.');
            return;
        }

        if (!validationResults.phoneNumber.checked || !validationResults.phoneNumber.available) {
            setError('휴대폰 번호 중복 체크를 완료하세요.');
            return;
        }

        if (!validationResults.password.valid) {
            setError('비밀번호가 유효하지 않습니다.');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await authService.register(formData);
            alert('회원가입이 완료되었습니다!');
            navigate('/login', {
                state: { message: '회원가입이 완료되었습니다. 로그인해주세요.' }
            });
        } catch (err) {
            console.error('회원가입 실패:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const formatPhoneNumber = (value) => {
        // 숫자만 추출
        const numbers = value.replace(/[^0-9]/g, '');
        // 11자리 제한
        return numbers.slice(0, 11);
    };

    const handlePhoneNumberChange = (e) => {
        const formatted = formatPhoneNumber(e.target.value);
        setFormData({ ...formData, phoneNumber: formatted });

        // 휴대폰 번호 검증 상태 초기화
        setValidationResults(prev => ({
            ...prev,
            phoneNumber: { checked: false, available: false, message: '' }
        }));
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
                                <p className="text-muted">새 계정을 만들어보세요</p>
                            </div>

                            {/* 알림 메시지 */}
                            {error && (
                                <Alert
                                    type="danger"
                                    message={error}
                                    onClose={() => setError('')}
                                    dismissible
                                />
                            )}

                            {/* 회원가입 폼 */}
                            <form onSubmit={handleSubmit}>
                                {/* 아이디 */}
                                <div className="mb-3">
                                    <label htmlFor="username" className="form-label">
                                        아이디 <span className="text-danger">*</span>
                                    </label>
                                    <div className="input-group">
                                        <input
                                            type="text"
                                            className={`form-control ${validationResults.userName.checked ?
                                                (validationResults.userName.available ? 'is-valid' : 'is-invalid') : ''}`}
                                            id="username"
                                            name="username"
                                            value={formData.useNname}
                                            onChange={handleChange}
                                            placeholder="아이디를 입력하세요"
                                            disabled={loading || validationResults.userName.available}
                                            required
                                        />
                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={() => checkDuplicate('userName')}
                                            disabled={loading || validationResults.userName.available || !formData.userName.trim()}
                                        >
                                            중복 체크
                                        </button>
                                    </div>
                                    {validationResults.userName.message && (
                                        <div className={`mt-1 small ${validationResults.userName.available ? 'text-success' : 'text-danger'}`}>
                                            {validationResults.userName.message}
                                        </div>
                                    )}
                                </div>

                                {/* 비밀번호 */}
                                <div className="mb-3">
                                    <label htmlFor="password" className="form-label">
                                        비밀번호 <span className="text-danger">*</span>
                                    </label>
                                    <input
                                        type="password"
                                        className={`form-control ${validationResults.password.valid ? 'is-valid' :
                                            (formData.password ? 'is-invalid' : '')}`}
                                        id="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        placeholder="비밀번호를 입력하세요 (최소 8자)"
                                        disabled={loading}
                                        required
                                    />
                                    {validationResults.password.message && (
                                        <div className={`mt-1 small ${validationResults.password.valid ? 'text-success' : 'text-danger'}`}>
                                            {validationResults.password.message}
                                        </div>
                                    )}
                                </div>

                                {/* 닉네임 */}
                                <div className="mb-3">
                                    <label htmlFor="nickname" className="form-label">
                                        닉네임 <span className="text-danger">*</span>
                                    </label>
                                    <div className="input-group">
                                        <input
                                            type="text"
                                            className={`form-control ${validationResults.nickName.checked ?
                                                (validationResults.nickName.available ? 'is-valid' : 'is-invalid') : ''}`}
                                            id="nickname"
                                            name="nickname"
                                            value={formData.nickName}
                                            onChange={handleChange}
                                            placeholder="닉네임을 입력하세요 (한글만)"
                                            disabled={loading || validationResults.nickName.available}
                                            pattern="^[가-힣]+$"
                                            required
                                        />
                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={() => checkDuplicate('nickName')}
                                            disabled={loading || validationResults.nickName.available || !formData.nickName.trim()}
                                        >
                                            중복 체크
                                        </button>
                                    </div>
                                    {validationResults.nickName.message && (
                                        <div className={`mt-1 small ${validationResults.nickName.available ? 'text-success' : 'text-danger'}`}>
                                            {validationResults.nickName.message}
                                        </div>
                                    )}
                                </div>

                                {/* 휴대폰 번호 */}
                                <div className="mb-4">
                                    <label htmlFor="phoneNumber" className="form-label">
                                        휴대폰 번호 <span className="text-danger">*</span>
                                    </label>
                                    <div className="input-group">
                                        <input
                                            type="tel"
                                            className={`form-control ${validationResults.phoneNumber.checked ?
                                                (validationResults.phoneNumber.available ? 'is-valid' : 'is-invalid') : ''}`}
                                            id="phoneNumber"
                                            name="phoneNumber"
                                            value={formData.phoneNumber}
                                            onChange={handlePhoneNumberChange}
                                            placeholder="01012345678"
                                            disabled={loading || validationResults.phoneNumber.available}
                                            maxLength={11}
                                            required
                                        />
                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={() => checkDuplicate('phoneNumber')}
                                            disabled={loading || validationResults.phoneNumber.available || !formData.phoneNumber.trim()}
                                        >
                                            중복 체크
                                        </button>
                                    </div>
                                    {validationResults.phoneNumber.message && (
                                        <div className={`mt-1 small ${validationResults.phoneNumber.available ? 'text-success' : 'text-danger'}`}>
                                            {validationResults.phoneNumber.message}
                                        </div>
                                    )}
                                </div>

                                <button
                                    type="submit"
                                    className="btn btn-primary w-100 py-2"
                                    disabled={loading}
                                >
                                    {loading ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                            가입 중...
                                        </>
                                    ) : (
                                        '회원가입'
                                    )}
                                </button>
                            </form>

                            {/* 로그인 링크 */}
                            <div className="text-center mt-4">
                                <p className="mb-0">
                                    이미 계정이 있으신가요?
                                    <Link to="/login" className="text-decoration-none ms-1">
                                        로그인
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

export default RegisterPage;