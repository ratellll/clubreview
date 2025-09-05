import React, {useState, useEffect, useCallback, useRef} from 'react';
import {userService} from '../services/userService';
import {useLocation} from 'react-router-dom';
import {reviewService} from '../services/reviewService';
import {authService} from '../services/authService';
import {adminService} from '../services/adminService';
import {clubService} from '../services/clubService';
import {useAuth} from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';


const MyPage = () => {
    const {user, updateUser} = useAuth();
    const location = useLocation();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const [myPageData, setMyPageData] = useState({});
    const [reviews, setReviews] = useState([]);
    const [editingReview, setEditingReview] = useState(null);

    const [nickNameForm, setNickNameForm] = useState({
        nickName: '',
        checked: false,
        available: false,
        message: ''
    });

    // 어드민 상태
    const [adminUsers, setAdminUsers] = useState([]);
    const [adminReviews, setAdminReviews] = useState([]);
    const [adminClubs, setAdminClubs] = useState([]);
    const [reviewSearchNickname, setReviewSearchNickname] = useState('');
    const [searchDebounceTimer, setSearchDebounceTimer] = useState(null);
    const [adminClubsPage, setAdminClubsPage] = useState(0);
    const [adminClubsTotalPages, setAdminClubsTotalPages] = useState(0);
    const [searchNickname, setSearchNickname] = useState('');
    const [activeTab, setActiveTab] = useState(() => (user?.role === 'ADMIN' ? 'club-management' : 'profile'));
    const [clubToEdit, setClubToEdit] = useState(null);
    const [editingClub, setEditingClub] = useState(null);
    const [clubForm, setClubForm] = useState({
        name: '', location: '', description: '', callNumber: '',
        latitude: '', longitude: '', file: null
    });
    const isAdmin = user?.role === 'ADMIN';


    useEffect(() => {
        if (isAdmin && ['profile', 'password', 'my-reviews'].includes(activeTab)) {
            setActiveTab('club-management');
        }
    }, [isAdmin, activeTab]);
    // 카카오맵 관련 상태
    const [searchKeyword, setSearchKeyword] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [selectedPlace, setSelectedPlace] = useState(null);
    const [mapInstance, setMapInstance] = useState(null);
    const searchMarkersRef = useRef([]); // 검색 결과 마커들
    const selectedMarkerRef = useRef(null); // 선택(주소/장소) 마커
    const [markersArray, setMarkersArray] = useState([]); // UI 업데이트용

    // 리뷰 검색 디바운스
    const debouncedReviewSearch = useCallback((nickname) => {
        if (searchDebounceTimer) {
            clearTimeout(searchDebounceTimer);
        }

        const timer = setTimeout(async () => {
            try {
                const reviews = await adminService.getAllReviews(nickname);
                setAdminReviews(reviews);
            } catch (err) {
                setError(err.message);
            }
        }, 300);

        setSearchDebounceTimer(timer);
    }, [searchDebounceTimer]);

    // 리뷰 검색어 변경 핸들러
    const handleReviewSearchChange = (e) => {
        const value = e.target.value;
        setReviewSearchNickname(value);
        debouncedReviewSearch(value);
    };
    // 디버깅을 위한 로그 추가
    useEffect(() => {
        console.log('🔍 Debug - Current user:', user);
        console.log('🔍 Debug - User role:', user?.role);
        console.log('🔍 Debug - Is admin:', isAdmin);
    }, [user, isAdmin]);
    const [passwordForm, setPasswordForm] = useState({
        password: '',
        valid: false,
        message: ''
    });

    useEffect(() => {
        fetchMyPageData();
        fetchMyReviews();
    }, []);

    // 어드민 기능들
    const fetchAdminUsers = async () => {
        if (!isAdmin) return;
        try {
            const users = await adminService.getUsers();
            setAdminUsers(users);
        } catch (err) {
            setError(err.message);
        }
    };

    const fetchAdminReviews = async () => {
        if (!isAdmin) return;
        try {
            const reviews = await adminService.getAllReviews(reviewSearchNickname);
            setAdminReviews(reviews);
        } catch (err) {
            setError(err.message);
        }
    };

    const fetchAdminClubs = async (page = 0) => {
        if (!isAdmin) return;
        try {
            const response = await clubService.getClubs({page, size: 50, sortBy: 'name'}); // 50개씩 페이징
            setAdminClubs(response.content || []);
            setAdminClubsPage(page);
            setAdminClubsTotalPages(response.totalPages || 0);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleSearchUsers = async () => {
        if (!searchNickname.trim()) {
            fetchAdminUsers();
            return;
        }
        try {
            const users = await adminService.searchUsers(searchNickname);
            setAdminUsers(users);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleBanUser = async (userId, days) => {
        if (!window.confirm(`사용자를 ${days}일 동안 정지하시겠습니까?`)) return;
        try {
            await adminService.banUser(userId, days);
            setSuccess('사용자가 정지되었습니다.');
            fetchAdminUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleUnbanUser = async (userId) => {
        if (!window.confirm('사용자 정지를 해제하시겠습니까?')) return;
        try {
            await adminService.unbanUser(userId);
            setSuccess('사용자 정지가 해제되었습니다.');
            fetchAdminUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDeleteUser = async (userId) => {
        if (!window.confirm('정말로 이 사용자를 삭제하시겠습니까?')) return;
        try {
            await adminService.deleteUser(userId);
            setSuccess('사용자가 삭제되었습니다.');
            fetchAdminUsers();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleAdminDeleteReview = async (reviewId) => {
        if (!window.confirm('정말로 이 리뷰를 삭제하시겠습니까?')) return;
        try {
            await adminService.deleteReview(reviewId);
            setSuccess('리뷰가 삭제되었습니다.');
            fetchAdminReviews();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleClubFormSubmit = async (e, isEditMode = false) => {
        e.preventDefault();
        try {
            const formData = new FormData();
            const clubData = {
                name: clubForm.name,
                location: clubForm.location,
                description: clubForm.description,
                callNumber: clubForm.callNumber,
                latitude: parseFloat(clubForm.latitude) || 0.0,
                longitude: parseFloat(clubForm.longitude) || 0.0
            };

            formData.append('club', new Blob([JSON.stringify(clubData)], {
                type: 'application/json'
            }));

            if (clubForm.file) {
                formData.append('file', clubForm.file);
            }

            if (isEditMode && clubToEdit) {
                await clubService.updateClub(clubToEdit.id, formData);
                setSuccess('클럽이 수정되었습니다.');
                setClubToEdit(null);
                setActiveTab('club-list-management'); // 클럽 목록으로 돌아가기
                fetchAdminClubs(); // 클럽 목록 새로고침
            } else if (editingClub) {
                await clubService.updateClub(editingClub.id, formData);
                setSuccess('클럽이 수정되었습니다.');
                setEditingClub(null);
            } else {
                await clubService.createClub(formData);
                setSuccess('클럽이 등록되었습니다.');
            }

            setClubForm({
                name: '', location: '', description: '', callNumber: '',
                latitude: '', longitude: '', file: null
            });

        } catch (err) {
            setError(err.message);
        }
    };

    // 닉네임 전용 change 핸들러
    const handleNicknameFormChange = (e) => {
        const value = e.target.value;
        setNickNameForm(prev => ({
            ...prev,
            nickName: value,
            checked: false,
            available: false,
            message: ''
        }));
    };

    const handleAdminDeleteClub = async (clubId) => {
        if (!window.confirm('정말로 이 클럽을 삭제하시겠습니까?')) return;
        try {
            await clubService.deleteClub(clubId);
            setSuccess('클럽이 삭제되었습니다.');
            fetchAdminClubs(); // 클럽 목록 새로고침
        } catch (err) {
            setError(err.message);
        }
    };
    const handleAdminEditClub = (club) => {
        setClubToEdit(club);
        setClubForm({
            name: club.name,
            location: club.location,
            description: club.description,
            callNumber: club.callNumber,
            latitude: club.latitude.toString(),
            longitude: club.longitude.toString(),
            file: null
        });
        setActiveTab('club-edit'); // 클럽 관리 탭으로 이동
    };
    const handleClubFormCancel = () => {
        setEditingClub(null);
        setClubForm({
            name: '', location: '', description: '', callNumber: '',
            latitude: '', longitude: '', file: null
        });
    };

    const handleClubEditCancel = () => {
        setClubToEdit(null);
        setClubForm({
                name: '', location: '', description: '', callNumber: '',
            latitude: '', longitude: '', file: null
        });
        setActiveTab('club-list-management'); // 클럽 목록으로 돌아가기
    };

    // 카카오맵 장소 검색
    const searchPlaces = () => {
        if (!searchKeyword.trim()) {
            alert('검색어를 입력해주세요');
            return;
        }

        const ps = new window.kakao.maps.services.Places();
        ps.keywordSearch(searchKeyword, (data, status) => {
            if (status === window.kakao.maps.services.Status.OK) {
                setSearchResults(data);
                displayMarkers(data, {fitBounds: true});
            } else {
                alert('검색 결과가 없습니다.');
            }
        });
    };

    // 지도에 마커 표시 (검색 결과 전용)
    const displayMarkers = useCallback((places, {fitBounds = true} = {}) => {
        if (!mapInstance) return;

        // 기존 "검색 결과" 마커만 제거 (선택 마커는 유지)
        searchMarkersRef.current.forEach(m => m.setMap(null));
        searchMarkersRef.current = [];

        const bounds = new window.kakao.maps.LatLngBounds();
        const newMarkers = places.map(place => {
            const pos = new window.kakao.maps.LatLng(place.y, place.x);
            const marker = new window.kakao.maps.Marker({position: pos});
            marker.setMap(mapInstance);
            window.kakao.maps.event.addListener(marker, 'click', () => selectPlace(place));
            bounds.extend(pos);
            return marker;
        });

        searchMarkersRef.current = newMarkers;
        setMarkersArray(newMarkers);
        if (fitBounds) mapInstance.setBounds(bounds);
    }, [mapInstance]);


    // 장소 선택
    const selectPlace = (place) => {
        setSelectedPlace(place);
        setClubForm(prev => ({
            ...prev,
            location: place.address_name,
            latitude: place.y,
            longitude: place.x
        }));
    };


    // 카카오 주소검색(우편번호) → 지오코딩 → 위/경도 자동 입력
    const openPostcodeSearch = useCallback(() => {
        // public/index.html 에서 카카오/다음 스크립트가 이미 로드됨 (autoload=false)
        if (!window.daum?.Postcode || !window.kakao?.maps?.services) {
            alert('지도 스크립트가 아직 로드되지 않았습니다. 잠시 후 다시 시도해주세요.');
            return;
        }

        new window.daum.Postcode({
            oncomplete: function (data) {
                const address = data.address;
                // 주소 필드 채우기
                setClubForm(prev => ({...prev, location: address}));

                // 주소 → 좌표 변환
                const geocoder = new window.kakao.maps.services.Geocoder();
                geocoder.addressSearch(address, function (result, status) {
                    if (status === window.kakao.maps.services.Status.OK) {
                        const {x, y} = result[0]; // x: 경도, y: 위도
                        setClubForm(prev => ({...prev, longitude: x, latitude: y}));

                        // 지도 활성화 되어 있으면 선택 지점 표시
                        try {
                            if (mapInstance) {
                                const pos = new window.kakao.maps.LatLng(y, x);
                                if (selectedMarkerRef.current) selectedMarkerRef.current.setMap(null);
                                const marker = new window.kakao.maps.Marker({position: pos});
                                marker.setZIndex(100);
                                marker.setMap(mapInstance);
                                selectedMarkerRef.current = marker;
                                mapInstance.setCenter(pos);
                            }
                        } catch (_) { /* 지도 미표시 시 무시 */
                        }

                    } else {
                        alert('주소를 좌표로 변환하는 데 실패했습니다.');
                    }
                });
            }
        }).open();
    }, [mapInstance]);

    // 카카오맵 초기화
    const loadKakao = useCallback(() => new Promise((resolve) => {
        if (window.kakao?.maps) {
            window.kakao.maps.load(resolve);
            return;
        }
        let script = document.getElementById('kakao-maps-sdk');
        if (!script) {
            script = document.createElement('script');
            script.id = 'kakao-maps-sdk';
            script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=93b4ad501fc7b3941109e59488da8aa9&libraries=services&autoload=false';
            script.onload = () => window.kakao.maps.load(resolve);
            document.head.appendChild(script);
        } else {
            script.addEventListener('load', () => window.kakao.maps.load(resolve), {once: true});
        }
    }), []);


    // 탭 재진입 시에도 항상 현재 컨테이너에 새 맵을 생성하고 relayout 수행
    const mountMap = useCallback(async () => {
        await loadKakao();
        const containerId = activeTab === 'club-edit' ? 'club-edit-map' : 'club-map';
        const container = document.getElementById(containerId);
        if (!container) return;
        if (mapInstance) mapInstance = null;
        const center = (clubForm.latitude && clubForm.longitude)
            ? new window.kakao.maps.LatLng(clubForm.latitude, clubForm.longitude)
            : new window.kakao.maps.LatLng(37.5665, 126.9780);
        const map = new window.kakao.maps.Map(container, {center, level: 3});
        setMapInstance(map);


        setTimeout(() => { // 왜: 탭 전환 후 표시 문제 해결
            try {
                map.relayout();
                map.setCenter(center);
                // 기존 검색/선택 마커를 새 맵에 재부착
                searchMarkersRef.current.forEach(m => m.setMap(map));
                if (selectedMarkerRef.current) selectedMarkerRef.current.setMap(map);
            } catch (_) {
            }
        }, 0);
    }, [loadKakao, clubForm.latitude, clubForm.longitude]);


    // 줌/이동/idle 시 마커 유지
    useEffect(() => {
        if (!mapInstance) return;
        const restore = () => {
            searchMarkersRef.current.forEach(m => {
                if (!m.getMap()) m.setMap(mapInstance);
            });
            if (selectedMarkerRef.current && !selectedMarkerRef.current.getMap()) {
                selectedMarkerRef.current.setMap(mapInstance);
            }
        };
        window.kakao.maps.event.addListener(mapInstance, 'zoom_changed', restore);
        window.kakao.maps.event.addListener(mapInstance, 'dragend', restore);
        window.kakao.maps.event.addListener(mapInstance, 'idle', restore);
        return () => {
            window.kakao.maps.event.removeListener(mapInstance, 'zoom_changed', restore);
            window.kakao.maps.event.removeListener(mapInstance, 'dragend', restore);
            window.kakao.maps.event.removeListener(mapInstance, 'idle', restore);
        };
    }, [mapInstance]);

    useEffect(() => {
        if (activeTab === 'club-management' || activeTab === 'club-edit') {
            mountMap();
        }
    }, [activeTab, mountMap]);


    // 탭 변경 시 데이터 로딩
    useEffect(() => {
        if (activeTab === 'user-management' && isAdmin) {
            fetchAdminUsers();
        } else if (activeTab === 'review-management' && isAdmin) {
            fetchAdminReviews();
        } else if (activeTab === 'club-list-management' && isAdmin) {
            fetchAdminClubs();
        } else if (activeTab === 'club-edit' && isAdmin) {
            mountMap();
        }
    }, [activeTab, isAdmin]);


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
            setNickNameForm({nickName: '', checked: false, available: false, message: ''});
            updateUser({nickName: nickNameForm.nickName});
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
            setPasswordForm({password: '', valid: false, message: ''});
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
        return <LoadingSpinner message="마이페이지를 불러오는 중..."/>;
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
                    {/* 탭 네비게이션 */}
                    <ul className="nav nav-tabs mb-4">
                        {/* 관리자 계정에서는 '내 정보/비밀번호 변경/내가 쓴 리뷰' 탭 숨김 */}
                        {!isAdmin && (
                            <>
                                <li className="nav-item">
                                    <button className={`nav-link ${activeTab === 'profile' ? 'active' : ''}`}
                                            onClick={() => setActiveTab('profile')}>
                                        내 정보
                                    </button>
                                </li>
                                <li className="nav-item">
                                    <button className={`nav-link ${activeTab === 'my-reviews' ? 'active' : ''}`}
                                            onClick={() => setActiveTab('my-reviews')}>
                                        내가 쓴 리뷰
                                    </button>
                                </li>
                            </>
                        )}
                        {isAdmin && (
                            <>
                                <li className="nav-item">
                                    <button className={`nav-link ${activeTab === 'user-management' ? 'active' : ''}`}
                                            onClick={() => setActiveTab('user-management')}>
                                        회원 관리
                                    </button>
                                </li>
                                <li className="nav-item">
                                    <button className={`nav-link ${activeTab === 'club-management' ? 'active' : ''}`}
                                            onClick={() => setActiveTab('club-management')}>
                                        클럽 등록
                                    </button>
                                </li>
                                <li className="nav-item">
                                    <button className={`nav-link ${activeTab === 'review-management' ? 'active' : ''}`}
                                            onClick={() => setActiveTab('review-management')}>
                                        리뷰 관리
                                    </button>
                                </li>
                                <li className="nav-item">
                                    <button
                                        className={`nav-link ${activeTab === 'club-list-management' ? 'active' : ''}`}
                                        onClick={() => setActiveTab('club-list-management')}>
                                        클럽 목록 관리
                                    </button>
                                </li>
                                <li className="nav-item">
                                    <button className={`nav-link ${activeTab === 'club-edit' ? 'active' : ''}`}
                                            onClick={() => setActiveTab('club-edit')}
                                            disabled={!clubToEdit}>
                                        클럽 수정
                                    </button>
                                </li>
                            </>
                        )}
                    </ul>
                    {/* 일반 사용자 - 내 정보: 프로필  닉네임 변경  비밀번호 변경 함께 표시 */}
                    {activeTab === 'profile' && !isAdmin && (
                        <>
                            <div className="card mb-4">
                                <div className="card-body">
                                    <h3 className="card-title">👤 내 정보</h3>
                                    <div className="row">
                                        <div className="col-md-4">
                                            <strong>아이디:</strong> {myPageData.user?.userName || user?.userName}</div>
                                        <div className="col-md-4"><strong>닉네임:</strong> {myPageData.user?.nickName}
                                        </div>
                                        <div className="col-md-4"><strong>전화번호:</strong> {myPageData.user?.phoneNumber}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* 닉네임 변경 */}
                            <div className="card mb-4">
                                <div className="card-body">
                                    <h3 className="card-title">✏️ 닉네임 변경</h3>
                                    <form onSubmit={handleNickNameSubmit}>
                                        <div className="mb-3">
                                            <div className="input-group">
                                                <input
                                                    type="text"
                                                    className={`form-control ${nickNameForm.checked ? (nickNameForm.available ? 'is-valid' : 'is-invalid') : ''}`}
                                                    value={nickNameForm.nickName}
                                                    onChange={handleNicknameFormChange}
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
                                                <div
                                                    className={`mt-1 small ${nickNameForm.available ? 'text-success' : 'text-danger'}`}>
                                                    {nickNameForm.message}
                                                </div>
                                            )}
                                        </div>
                                        <button type="submit" className="btn btn-primary"
                                                disabled={!nickNameForm.checked || !nickNameForm.available}>
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
                                                className={`form-control ${passwordForm.valid ? 'is-valid' : (passwordForm.password ? 'is-invalid' : '')}`}
                                                value={passwordForm.password}
                                                onChange={(e) => validatePassword(e.target.value)}
                                                placeholder="새로운 비밀번호를 입력하세요 (최소 8자)"
                                            />
                                            {passwordForm.message && (
                                                <div
                                                    className={`mt-1 small ${passwordForm.valid ? 'text-success' : 'text-danger'}`}>
                                                    {passwordForm.message}
                                                </div>
                                            )}
                                        </div>
                                        <button type="submit" className="btn btn-primary"
                                                disabled={!passwordForm.valid}>비밀번호 변경
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </>
                    )}

                    {/* 관리자 - 회원 관리 탭 */}
                    {activeTab === 'user-management' && isAdmin && (
                        <div className="card">
                            <div className="card-body">
                                <h3 className="card-title">👥 회원 관리</h3>

                                {/* 사용자 검색 */}
                                <div className="mb-4">
                                    <div className="input-group">
                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="닉네임으로 검색"
                                            value={searchNickname}
                                            onChange={(e) => setSearchNickname(e.target.value)}
                                        />
                                        <button className="btn btn-primary" onClick={handleSearchUsers}>
                                            검색
                                        </button>
                                        <button className="btn btn-secondary" onClick={fetchAdminUsers}>
                                            전체보기
                                        </button>
                                    </div>
                                </div>

                                {/* 사용자 목록 */}
                                <div className="table-responsive">
                                    <table className="table table-striped">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>닉네임</th>
                                            <th>아이디</th>
                                            <th>상태</th>
                                            <th>관리</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {adminUsers.map(user => (
                                            <tr key={user.id}>
                                                <td>{user.id}</td>
                                                <td>{user.nickName}</td>
                                                <td>{user.userName}</td>
                                                <td>
                                                    {user.banEndTime ? (
                                                        <span className="badge bg-danger">
                                                                정지됨 ({new Date(user.banEndTime).toLocaleDateString()})
                                                            </span>
                                                    ) : (
                                                        <span className="badge bg-success">활성</span>
                                                    )}
                                                </td>
                                                <td>
                                                    {user.banEndTime ? (
                                                        <button
                                                            className="btn btn-success btn-sm me-1"
                                                            onClick={() => handleUnbanUser(user.id)}
                                                        >
                                                            정지해제
                                                        </button>
                                                    ) : (
                                                        <>
                                                            <button
                                                                className="btn btn-warning btn-sm me-1"
                                                                onClick={() => handleBanUser(user.id, 7)}
                                                            >
                                                                7일 정지
                                                            </button>
                                                            <button
                                                                className="btn btn-warning btn-sm me-1"
                                                                onClick={() => handleBanUser(user.id, 30)}
                                                            >
                                                                30일 정지
                                                            </button>
                                                        </>
                                                    )}
                                                    <button
                                                        className="btn btn-danger btn-sm"
                                                        onClick={() => handleDeleteUser(user.id)}
                                                    >
                                                        삭제
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 관리자 - 클럽 관리 탭 */}
                    {activeTab === 'club-management' && isAdmin && (
                        <div className="card">
                            <div className="card-body">
                                <h3 className="card-title">🏢 클럽 등록</h3>

                                {/* 클럽 등록/수정 폼 */}
                                {/* 카카오맵 장소 검색 */}
                                <div className="mb-4">
                                    <h5>장소 검색</h5>
                                    {/*<div className="input-group mb-3">*/}
                                    {/*    <input*/}
                                    {/*        type="text"*/}
                                    {/*        className="form-control"*/}
                                    {/*        placeholder="장소명을 입력하세요"*/}
                                    {/*        value={searchKeyword}*/}
                                    {/*        onChange={(e) => setSearchKeyword(e.target.value)}*/}
                                    {/*        onKeyPress={(e) => e.key === 'Enter' && searchPlaces()}*/}
                                    {/*    />*/}
                                    {/*    <button className="btn btn-primary" onClick={searchPlaces}>*/}
                                    {/*        검색*/}
                                    {/*    </button>*/}
                                    {/*</div>*/}

                                    {/* 지도 */}
                                    <div id="club-map"
                                         style={{width: '100%', height: '300px', marginBottom: '10px'}}></div>

                                    {/* 검색 결과 */}
                                    {searchResults.length > 0 && (
                                        <div className="search-results" style={{maxHeight: '200px', overflowY: 'auto'}}>
                                            {searchResults.map((place, index) => (
                                                <div key={index}
                                                     className={`border p-2 mb-2 cursor-pointer ${selectedPlace?.id === place.id ? 'bg-primary text-white' : ''}`}
                                                     onClick={() => selectPlace(place)}>
                                                    <strong>{place.place_name}</strong><br/>
                                                    <small>{place.address_name}</small>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <form onSubmit={handleClubFormSubmit} className="mb-4">
                                    <div className="row">
                                        <div className="col-md-6">
                                            <div className="mb-3">
                                                <label className="form-label">클럽명</label>
                                                <input
                                                    type="text"
                                                    className="form-control"
                                                    value={clubForm.name}
                                                    onChange={(e) => setClubForm({...clubForm, name: e.target.value})}
                                                    required
                                                />
                                            </div>
                                        </div>
                                        <div className="col-md-6">
                                            <div className="mb-3">
                                                <label className="form-label">위치</label>
                                                <div className="input-group">
                                                    <input
                                                        type="text"
                                                        className="form-control"
                                                        placeholder="주소를 입력하거나 오른쪽 버튼으로 검색"
                                                        value={clubForm.location}
                                                        onChange={(e) => setClubForm({
                                                            ...clubForm,
                                                            location: e.target.value
                                                        })}
                                                        required
                                                    />
                                                    {/* 카카오 우편번호 검색 버튼 */}
                                                    <button
                                                        type="button"
                                                        className="btn btn-outline-secondary"
                                                        onClick={openPostcodeSearch}
                                                    >
                                                        주소 검색
                                                    </button>
                                                </div>
                                                <input type="hidden" name="latitude" value={clubForm.latitude ?? ''}
                                                       readOnly/>
                                                <input type="hidden" name="longitude" value={clubForm.longitude ?? ''}
                                                       readOnly/>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="row">
                                        <div className="col-md-6">
                                            <div className="mb-3">
                                                <label className="form-label">전화번호</label>
                                                <input
                                                    type="text"
                                                    className="form-control"
                                                    value={clubForm.callNumber}
                                                    onChange={(e) => setClubForm({
                                                        ...clubForm,
                                                        callNumber: e.target.value
                                                    })}
                                                    required
                                                />
                                            </div>
                                        </div>
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label">설명</label>
                                        <textarea
                                            className="form-control"
                                            rows="3"
                                            value={clubForm.description}
                                            onChange={(e) => setClubForm({...clubForm, description: e.target.value})}
                                            required
                                        ></textarea>
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label">사진</label>
                                        <input
                                            type="file"
                                            className="form-control"
                                            accept="image/*"
                                            onChange={(e) => setClubForm({...clubForm, file: e.target.files[0]})}
                                            required={!editingClub}
                                        />
                                    </div>
                                    <div className="d-flex gap-2">
                                        <button type="submit" className="btn btn-primary">
                                            등록
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-secondary ms-2"
                                            onClick={handleClubFormCancel}
                                        >
                                            취소
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    )}

                    {/* 관리자 - 클럽 수정 탭 */}
                    {activeTab === 'club-edit' && isAdmin && clubToEdit && (
                        <div className="card">
                            <div className="card-body">
                                <h3 className="card-title">🔧 클럽 수정: {clubToEdit.name}</h3>

                                {/* 현재 클럽 정보 표시 */}
                                <div className="alert alert-info mb-4">
                                    <strong>수정 중인 클럽:</strong> {clubToEdit.name}<br/>
                                    <strong>현재 위치:</strong> {clubToEdit.location}<br/>
                                    <strong>현재 평점:</strong> ⭐ {clubToEdit.averageRating.toFixed(1)}
                                </div>

                                {/* 카카오맵 장소 검색 */}
                                <div className="mb-4">
                                    <h5>장소 검색</h5>
                                    <div className="input-group mb-3">
                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="장소명을 입력하세요"
                                            value={searchKeyword}
                                            onChange={(e) => setSearchKeyword(e.target.value)}
                                            onKeyPress={(e) => e.key === 'Enter' && searchPlaces()}
                                        />
                                        <button className="btn btn-primary" onClick={searchPlaces}>
                                            검색
                                        </button>
                                    </div>

                                    {/* 지도 */}
                                    <div id="club-edit-map"
                                         style={{width: '100%', height: '300px', marginBottom: '10px'}}></div>

                                    {/* 검색 결과 */}
                                    {searchResults.length > 0 && (
                                        <div className="search-results" style={{maxHeight: '200px', overflowY: 'auto'}}>
                                            {searchResults.map((place, index) => (
                                                <div key={index}
                                                     className={`border p-2 mb-2 cursor-pointer ${selectedPlace?.id === place.id ? 'bg-primary text-white' : ''}`}
                                                     onClick={() => selectPlace(place)}>
                                                    <strong>{place.place_name}</strong><br/>
                                                    <small>{place.address_name}</small>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <form onSubmit={(e) => {
                                    e.preventDefault();
                                    handleClubFormSubmit(e, true); // 수정 모드로 전달
                                }} className="mb-4">
                                    <div className="row">
                                        <div className="col-md-6">
                                            <div className="mb-3">
                                                <label className="form-label">클럽명</label>
                                                <input
                                                    type="text"
                                                    className="form-control"
                                                    value={clubForm.name}
                                                    onChange={(e) => setClubForm({...clubForm, name: e.target.value})}
                                                    required
                                                />
                                            </div>
                                        </div>
                                        <div className="col-md-6">
                                            <div className="mb-3">
                                                <label className="form-label">위치</label>
                                                <div className="input-group">
                                                    <input
                                                        type="text"
                                                        className="form-control"
                                                        placeholder="주소를 입력하거나 오른쪽 버튼으로 검색"
                                                        value={clubForm.location}
                                                        onChange={(e) => setClubForm({
                                                            ...clubForm,
                                                            location: e.target.value
                                                        })}
                                                        required
                                                    />
                                                    <button
                                                        type="button"
                                                        className="btn btn-outline-secondary"
                                                        onClick={openPostcodeSearch}
                                                    >
                                                        주소 검색
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="row">
                                        <div className="col-md-6">
                                            <div className="mb-3">
                                                <label className="form-label">전화번호</label>
                                                <input
                                                    type="text"
                                                    className="form-control"
                                                    value={clubForm.callNumber}
                                                    onChange={(e) => setClubForm({
                                                        ...clubForm,
                                                        callNumber: e.target.value
                                                    })}
                                                    required
                                                />
                                            </div>
                                        </div>
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label">설명</label>
                                        <textarea
                                            className="form-control"
                                            rows="3"
                                            value={clubForm.description}
                                            onChange={(e) => setClubForm({...clubForm, description: e.target.value})}
                                            required
                                        ></textarea>
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label">사진 변경 (선택사항)</label>
                                        <input
                                            type="file"
                                            className="form-control"
                                            accept="image/*"
                                            onChange={(e) => setClubForm({...clubForm, file: e.target.files[0]})}
                                        />
                                        <small className="text-muted">새 사진을 선택하지 않으면 기존 사진이 유지됩니다.</small>
                                    </div>
                                    <div className="d-flex gap-2">
                                        <button type="submit" className="btn btn-success">
                                            🔧 수정 완료
                                        </button>
                                        <button
                                            type="button"
                                            className="btn btn-secondary"
                                            onClick={handleClubEditCancel}
                                        >
                                            취소
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    )}
                    {/* 관리자 - 클럽 목록 관리 탭 */}
                    {activeTab === 'club-list-management' && isAdmin && (
                        <div className="card">
                            <div className="card-body">
                                <h3 className="card-title">🏢 클럽 목록 관리</h3>

                                {adminClubs.length === 0 ? (
                                    <div className="text-center py-4">
                                        <p className="text-muted">등록된 클럽이 없습니다.</p>
                                    </div>
                                ) : (
                                    <div className="table-responsive">
                                        <table className="table table-striped">
                                            <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>클럽명</th>
                                                <th>위치</th>
                                                <th>전화번호</th>
                                                <th>평점</th>
                                                <th>관리</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            {adminClubs.map(club => (
                                                <tr key={club.id}>
                                                    <td>{club.id}</td>
                                                    <td>
                                                        <strong>{club.name}</strong>
                                                        {club.photoUrl && (
                                                            <div className="mt-1">
                                                                <img
                                                                    src={club.photoUrl}
                                                                    alt="클럽 사진"
                                                                    style={{
                                                                        width: '50px',
                                                                        height: '30px',
                                                                        objectFit: 'cover'
                                                                    }}
                                                                    className="rounded"
                                                                />
                                                            </div>
                                                        )}
                                                    </td>
                                                    <td>
                                                        <small>{club.location}</small>
                                                    </td>
                                                    <td>{club.callNumber}</td>
                                                    <td>
                                                                                            <span
                                                                                                className="badge bg-warning text-dark">
                                                                ⭐ {club.averageRating.toFixed(1)}
                                                                                            </span>
                                                    </td>
                                                    <td>
                                                        <button
                                                            className="btn btn-primary btn-sm me-2"
                                                            onClick={() => handleAdminEditClub(club)}
                                                        >
                                                            수정
                                                        </button>
                                                        <button
                                                            className="btn btn-danger btn-sm"
                                                            onClick={() => handleAdminDeleteClub(club.id)}
                                                        >
                                                            삭제
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                                {/* 클럽 목록 페이지네이션 */}
                                {adminClubsTotalPages > 1 && (
                                    <nav className="mt-4">
                                        <ul className="pagination justify-content-center">
                                            <li className={`page-item ${adminClubsPage === 0 ? 'disabled' : ''}`}>
                                                <button className="page-link"
                                                        onClick={() => fetchAdminClubs(adminClubsPage - 1)}
                                                        disabled={adminClubsPage === 0}>이전
                                                </button>
                                            </li>
                                            {[...Array(adminClubsTotalPages)].map((_, i) => (
                                                <li key={i}
                                                    className={`page-item ${i === adminClubsPage ? 'active' : ''}`}>
                                                    <button className="page-link"
                                                            onClick={() => fetchAdminClubs(i)}>{i + 1}</button>
                                                </li>
                                            ))}
                                            <li className={`page-item ${adminClubsPage === adminClubsTotalPages - 1 ? 'disabled' : ''}`}>
                                                <button className="page-link"
                                                        onClick={() => fetchAdminClubs(adminClubsPage + 1)}
                                                        disabled={adminClubsPage === adminClubsTotalPages - 1}>다음
                                                </button>
                                            </li>
                                        </ul>
                                    </nav>
                                )}
                                <div className="mt-4">
                                    <button
                                        className="btn btn-success"
                                        onClick={() => {
                                            setEditingClub(null);
                                            handleClubFormCancel();
                                            setActiveTab('club-management');
                                        }}
                                    >
                                        새 클럽 등록
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 관리자 - 리뷰 관리 탭 */}
                    {activeTab === 'review-management' && isAdmin && (
                        <div className="card">
                            <div className="card-body">
                                <h3 className="card-title">📝 리뷰 관리</h3>
                                {/* 작성자 닉네임 검색 */}
                                <div className="mb-4">
                                    <div className="input-group">
                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="작성자 닉네임으로 검색"
                                            value={reviewSearchNickname}
                                            onChange={handleReviewSearchChange}
                                        />
                                    </div>
                                </div>

                                <div className="table-responsive">
                                    <table className="table table-striped">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>클럽</th>
                                            <th>작성자</th>
                                            <th>평점</th>
                                            <th>내용</th>
                                            <th>작성일</th>
                                            <th>관리</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {adminReviews.map(review => (
                                            <tr key={review.id}>
                                                <td>{review.id}</td>
                                                <td>{review.clubName}</td>
                                                <td>{review.userNickName}</td>
                                                <td>
                                                                    <span className="badge bg-warning">
                                                            {'★'.repeat(review.rating)}
                                                                    </span>
                                                </td>
                                                <td>
                                                    <div style={{
                                                        maxWidth: '200px',
                                                        overflow: 'hidden',
                                                        textOverflow: 'ellipsis'
                                                    }}>
                                                        {review.comment}
                                                    </div>
                                                </td>
                                                <td>{new Date(review.createTime).toLocaleDateString()}</td>
                                                <td>
                                                    <button
                                                        className="btn btn-danger btn-sm"
                                                        onClick={() => handleAdminDeleteReview(review.id)}
                                                    >
                                                        삭제
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    )}
                    {/* 내 리뷰 목록 */}
                    {activeTab === 'my-reviews' && !isAdmin && (
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
                    )}
                </div>
            </div>
        </div>
    );
};


// 리뷰 수정 폼 컴포넌트
const EditReviewForm = ({review, onSave, onCancel}) => {
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