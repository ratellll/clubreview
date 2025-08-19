```diff
*** 파일: src/pages/MyPage.js
@@
-    const [activeTab, setActiveTab] = useState('profile');
    // 관리자 로그인 시 기본 탭을 'club-management'로 설정
    const [activeTab, setActiveTab] = useState(() => (user?.role === 'ADMIN' ? 'club-management' : 'profile'));
@@
-    const isAdmin = user?.role === 'ADMIN';
    const isAdmin = user?.role === 'ADMIN';

    // 관리자라면 '내 정보' 탭으로 진입하지 않도록 보정
    useEffect(() => {
        if (user?.role === 'ADMIN' && activeTab === 'profile') {
            setActiveTab('club-management');
        }
    }, [user]);
@@
-                    {/* 탭 네비게이션 */}
-                    <ul className="nav nav-tabs mb-4">
-                        <li className="nav-item">
-                            <button className={`
nav - link
$
{
    activeTab === 'profile' ? 'active' : ''
}
`}
-                                    onClick={() => setActiveTab('profile')}>
-                                내 정보
-                            </button>
-                        </li>
                    {/* 탭 네비게이션 */}
                    <ul className="nav nav-tabs mb-4">
                        {/* 관리자 계정에서는 '내 정보' 탭 숨김 */}
                        {!isAdmin && (
                            <li className="nav-item">
                                <button className={`
nav - link
$
{
    activeTab === 'profile' ? 'active' : ''
}
`}
                                        onClick={() => setActiveTab('profile')}>
                                    내 정보
                                </button>
                            </li>
                        )}
@@
-                                <li className="nav-item">
-                                    <button className={`
nav - link
$
{
    activeTab === 'club-management' ? 'active' : ''
}
`}
-                                            onClick={() => setActiveTab('club-management')}>
-                                        클럽 관리
-                                    </button>
-                                </li>
                                <li className="nav-item">
                                    <button className={`
nav - link
$
{
    activeTab === 'club-management' ? 'active' : ''
}
`}
                                            onClick={() => setActiveTab('club-management')}>
                                        클럽 등록
                                    </button>
                                </li>
@@
-                            <div className="card-body">
-                                <h3 className="card-title">🏢 클럽 관리</h3>
                            <div className="card-body">
                                <h3 className="card-title">🏢 클럽 등록</h3>
@@
-                    {activeTab === 'profile' && (
                    {/* 관리자 계정에서는 '내 정보' 섹션 자체 비노출 */}
                    {activeTab === 'profile' && !isAdmin && (
                         <div className="card mb-4">
                             <div className="card-body">
                                 <h3 className="card-title">👤 내 정보</h3>
                                 {/* ... 기존 내 정보 폼 ... */}
                             </div>
                         </div>
                     )}
@@
-                                                <label className="form-label">위치</label>
-                                                <input
-                                                    type="text"
-                                                    className="form-control"
-                                                    value={clubForm.location}
-                                                    onChange={(e) => setClubForm({
-                                                        ...clubForm,
-                                                        location: e.target.value
-                                                    })}
-                                                    required
-                                                />
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
@@
     // 카카오맵 장소 검색
     const searchPlaces = () => {
         if (!searchKeyword.trim()) {
             alert('검색어를 입력해주세요');
             return;
         }
@@
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
                setClubForm(prev => ({ ...prev, location: address }));

                // 주소 → 좌표 변환
                const geocoder = new window.kakao.maps.services.Geocoder();
                geocoder.addressSearch(address, function (result, status) {
                    if (status === window.kakao.maps.services.Status.OK) {
                        const { x, y } = result[0]; // x: 경도, y: 위도
                        setClubForm(prev => ({ ...prev, longitude: x, latitude: y }));

                        // 지도 활성화 되어 있으면 선택 지점 표시
                        try {
                            if (mapInstance) {
                                const pos = new window.kakao.maps.LatLng(y, x);
                                const marker = new window.kakao.maps.Marker({ position: pos });
                                markersArray.forEach(m => m.setMap(null));
                                marker.setMap(mapInstance);
                                setMarkersArray([marker]);
                                mapInstance.setCenter(pos);
                            }
                        } catch (_) { /* 지도 미표시 시 무시 */ }
                    } else {
                        alert('주소를 좌표로 변환하는 데 실패했습니다.');
                    }
                });
            }
        }).open();
    }, [mapInstance, markersArray]);
```


