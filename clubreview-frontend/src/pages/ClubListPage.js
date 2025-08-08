import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { clubService } from '../services/clubService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';

const ClubListPage = () => {
    const navigate = useNavigate();
    const mapRef = useRef(null);
    const [clubs, setClubs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [sortBy, setSortBy] = useState('name');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [map, setMap] = useState(null);
    const [markers, setMarkers] = useState([]);
    const [currentInfoWindow, setCurrentInfoWindow] = useState(null);
    const [isMapLoaded, setIsMapLoaded] = useState(false);

    // Kakao 지도 스크립트 로드
    useEffect(() => {
        // DOM이 완전히 렌더링될 때까지 약간 지연
        const timer = setTimeout(() => {
            console.log('🔍 DOM 렌더링 후 지도 초기화 시도');
            console.log('mapRef.current:', mapRef.current);

            if (!mapRef.current) {
                console.error('❌ 지도 컨테이너가 아직 준비되지 않음');
                return;
            }

            // HTML에서 이미 로드된 경우 체크
            if (window.kakao && window.kakao.maps) {
                console.log('✅ Kakao Maps이 이미 로드됨');
                window.kakao.maps.load(() => {
                    console.log('✅ Kakao Maps 초기화 완료');
                    setIsMapLoaded(true);
                    initializeMap();
                });
            } else {
                console.log('🔄 Kakao Maps 스크립트 동적 로드 시작');
                // 기존 스크립트가 있는지 확인
                const existingScript = document.querySelector('script[src*="dapi.kakao.com"]');

                if (existingScript) {
                    console.log('📜 기존 Kakao 스크립트 발견, 로드 대기 중...');
                    existingScript.onload = () => {
                        window.kakao.maps.load(() => {
                            console.log('✅ 기존 스크립트로 Kakao Maps 초기화 완료');
                            setIsMapLoaded(true);
                            initializeMap();
                        });
                    };
                } else {
                    console.log('📜 새 Kakao 스크립트 생성 중...');
                    const script = document.createElement('script');
                    script.async = true;
                    script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=93b4ad501fc7b3941109e59488da8aa9&autoload=false';

                    script.onload = () => {
                        console.log('✅ 새 스크립트 로드 완료');
                        window.kakao.maps.load(() => {
                            console.log('✅ 새 스크립트로 Kakao Maps 초기화 완료');
                            setIsMapLoaded(true);
                            initializeMap();
                        });
                    };

                    script.onerror = () => {
                        console.error('❌ Kakao 스크립트 로드 실패');
                        setError('지도를 불러오는데 실패했습니다.');
                    };

                    document.head.appendChild(script);
                }
            }
        }, 100); // 100ms 지연으로 DOM 렌더링 대기

        return () => clearTimeout(timer);
    }, []);

    // 클럽 데이터 조회
    useEffect(() => {
        fetchClubs();
    }, [sortBy, currentPage]);

    // 지도에 마커 표시
    useEffect(() => {
        if (map && clubs.length > 0 && isMapLoaded) {
            displayMarkersOnMap();

            // 마커 표시 후 지도 크기 한 번 더 재조정
            setTimeout(() => {
                console.log('🔄 마커 표시 후 지도 크기 재조정');
                map.relayout();
            }, 200);
        }
    }, [map, clubs, isMapLoaded]);

    const initializeMap = () => {
        console.log('🗺️ 지도 초기화 시작');
        console.log('mapRef.current:', mapRef.current);
        console.log('window.kakao:', !!window.kakao);
        console.log('window.kakao.maps:', !!window.kakao?.maps);

        // ref가 null인 경우 DOM에서 직접 찾기
        let container = mapRef.current;
        if (!container) {
            console.log('🔍 ref가 null이므로 DOM에서 직접 찾는 중...');
            container = document.getElementById('kakao-map-container');
            if (!container) {
                console.error('❌ 지도 컨테이너를 찾을 수 없음 (ref와 DOM 모두 실패)');
                return;
            }
            console.log('✅ DOM에서 지도 컨테이너 찾음');
        }

        if (!window.kakao || !window.kakao.maps) {
            console.error('❌ Kakao Maps API를 사용할 수 없음');
            return;
        }

        try {
            const options = {
                center: new window.kakao.maps.LatLng(37.5665, 126.9780), // 서울 중심
                level: 8
            };

            console.log('🗺️ 지도 객체 생성 중...');
            const kakaoMap = new window.kakao.maps.Map(container, options);

            // 지도 컨테이너 CSS 강제 조정
            const mapContainer = container.querySelector('.MapWrap') || container;
            if (mapContainer) {
                mapContainer.style.width = '100%';
                mapContainer.style.maxWidth = '100%';
                mapContainer.style.overflow = 'hidden';
                mapContainer.style.boxSizing = 'border-box';
            }

            // 지도 크기 재조정 (중요!)
            setTimeout(() => {
                console.log('🔄 지도 크기 재조정 시작');
                kakaoMap.relayout();

                // 추가 CSS 조정
                const allMapElements = container.querySelectorAll('*');
                allMapElements.forEach(el => {
                    if (el.style.width && el.style.width.includes('px')) {
                        const parent = el.parentElement;
                        if (parent && parent.offsetWidth < parseInt(el.style.width)) {
                            el.style.width = '100%';
                            el.style.maxWidth = '100%';
                        }
                    }
                });

                console.log('✅ 지도 크기 재조정 완료');
            }, 100);

            setMap(kakaoMap);
            console.log('✅ 지도 객체 생성 완료');

            // 지도 클릭 시 정보창 닫기
            window.kakao.maps.event.addListener(kakaoMap, 'click', () => {
                if (currentInfoWindow) {
                    currentInfoWindow.close();
                    setCurrentInfoWindow(null);
                }
            });

            console.log('✅ 지도 이벤트 리스너 등록 완료');
        } catch (error) {
            console.error('❌ 지도 초기화 중 오류:', error);
            setError('지도 초기화에 실패했습니다: ' + error.message);
        }
    };

    const fetchClubs = async () => {
        try {
            setLoading(true);
            const params = {
                page: currentPage,
                size: 20,
                sortBy: sortBy
            };
            const response = await clubService.getClubs(params);

            // 정렬된 데이터 받기
            let sortedClubs = response.content || [];
            if (sortBy === 'name') {
                sortedClubs = sortedClubs.sort((a, b) => a.name.localeCompare(b.name));
            } else if (sortBy === 'rating') {
                sortedClubs = sortedClubs.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
            }

            setClubs(sortedClubs);
            setTotalPages(response.totalPages || 0);
            setError('');
        } catch (err) {
            console.error('클럽 목록 조회 실패:', err);
            setError('클럽 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const displayMarkersOnMap = () => {
        if (!window.kakao || !window.kakao.maps || !map) return;

        // 기존 마커들 제거
        markers.forEach(marker => marker.setMap(null));

        const newMarkers = [];

        clubs.forEach(club => {
            if (club.latitude && club.longitude) {
                const marker = new window.kakao.maps.Marker({
                    position: new window.kakao.maps.LatLng(club.latitude, club.longitude),
                    map: map
                });

                const infoWindowContent = `
                    <div style="padding: 15px; width: 300px;">
                        <h5 style="margin-bottom: 10px; color: #007bff; cursor: pointer;" 
                            onclick="window.clubListPageInstance.navigateToClub(${club.id})">
                            ${club.name}
                        </h5>
                        <p style="margin: 5px 0;"><strong>위치:</strong> ${club.location || 'N/A'}</p>
                        <p style="margin: 5px 0;"><strong>전화번호:</strong> ${club.callNumber || 'N/A'}</p>
                        <p style="margin: 5px 0;"><strong>설명:</strong> ${club.description || 'N/A'}</p>
                        <p style="margin: 5px 0;"><strong>평점:</strong> ⭐ ${club.averageRating || 0} / 5</p>
                        ${club.photoUrl ? `
                            <img src="${club.photoUrl}" 
                                 alt="클럽 사진" 
                                 style="width: 100%; max-height: 150px; object-fit: cover; margin-top: 10px; cursor: pointer; display: block;"
                                 onclick="window.clubListPageInstance.showPhotoModal('${club.photoUrl}')" 
                                 onerror="this.style.display='none'" />
                        ` : ''}
                    </div>
                `;

                const infoWindow = new window.kakao.maps.InfoWindow({
                    content: infoWindowContent
                });

                // 마커 클릭 이벤트
                window.kakao.maps.event.addListener(marker, 'click', () => {
                    // 이전 정보창 닫기
                    if (currentInfoWindow) {
                        currentInfoWindow.close();
                    }
                    infoWindow.open(map, marker);
                    setCurrentInfoWindow(infoWindow);
                });

                newMarkers.push(marker);
            }
        });

        setMarkers(newMarkers);
    };

    const navigateToClub = (clubId) => {
        navigate(`/clubs/${clubId}`);
    };

    const showPhotoModal = (photoUrl) => {
        // 기존 모달이 있다면 제거
        const existingModal = document.querySelector('.photo-modal');
        if (existingModal) {
            existingModal.remove();
        }

        // 새 모달 생성
        const modal = document.createElement('div');
        modal.className = 'photo-modal';
        modal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.8); display: flex; justify-content: center;
            align-items: center; z-index: 1000; cursor: pointer;
        `;

        modal.innerHTML = `
            <img src="${photoUrl}" 
                 style="max-width: 90%; max-height: 90%; object-fit: contain;" 
                 alt="클럽 사진 확대" />
        `;

        // 클릭으로 닫기
        modal.onclick = (e) => {
            if (e.target === modal) { // 배경 클릭 시에만 닫기
                closePhotoModal();
            }
        };

        // ESC 키로 닫기
        const handleEscKey = (e) => {
            if (e.key === 'Escape') {
                closePhotoModal();
            }
        };

        // 모달 닫기 함수
        const closePhotoModal = () => {
            if (modal && modal.parentNode) {
                modal.remove();
            }
            document.removeEventListener('keydown', handleEscKey);
        };

        document.addEventListener('keydown', handleEscKey);
        document.body.appendChild(modal);
    };

    const handleSortChange = (newSort) => {
        setSortBy(newSort);
        setCurrentPage(0);
    };

    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // 전역 객체로 메서드 노출 (인포윈도우에서 호출하기 위해)
    useEffect(() => {
        window.clubListPageInstance = {
            navigateToClub,
            showPhotoModal
        };

        return () => {
            delete window.clubListPageInstance;
        };
    }, []);

    // 윈도우 리사이즈 시 지도 크기 재조정
    useEffect(() => {
        const handleResize = () => {
            if (map) {
                console.log('🔄 윈도우 리사이즈 - 지도 크기 재조정');
                map.relayout();
            }
        };

        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, [map]);

    if (loading) {
        return <LoadingSpinner message="클럽 목록을 불러오는 중..." />;
    }

    return (
        <div className="container mt-4" style={{ maxWidth: '1200px', padding: '0 15px' }}>
            <div className="row">
                <div className="col-12" style={{ padding: '0 15px' }}>
                    <h1 className="mb-4">🎉 클럽 목록</h1>

                    {error && (
                        <Alert
                            type="danger"
                            message={error}
                            onClose={() => setError('')}
                            dismissible
                        />
                    )}

                    {/* 카카오 지도 */}
                    <div className="mb-4" style={{ overflow: 'hidden', borderRadius: '8px' }}>
                        {!isMapLoaded && (
                            <div
                                style={{
                                    width: '100%',
                                    height: '500px',
                                    display: 'flex',
                                    justifyContent: 'center',
                                    alignItems: 'center',
                                    backgroundColor: '#f8f9fa',
                                    borderRadius: '8px'
                                }}
                            >
                                <p className="text-muted">지도를 불러오는 중...</p>
                            </div>
                        )}
                        <div
                            id="kakao-map-container"
                            ref={mapRef}
                            style={{
                                width: '100%',
                                maxWidth: '100%',
                                height: '500px',
                                minHeight: '500px',
                                borderRadius: '8px',
                                backgroundColor: '#f8f9fa',
                                visibility: isMapLoaded ? 'visible' : 'hidden',
                                position: isMapLoaded ? 'static' : 'absolute',
                                overflow: 'hidden',
                                boxSizing: 'border-box'
                            }}
                        ></div>
                        {error && error.includes('지도') && (
                            <div className="text-center py-3">
                                <small className="text-danger">
                                    지도를 불러올 수 없습니다. 네트워크 연결을 확인해주세요.
                                </small>
                            </div>
                        )}
                    </div>

                    {/* 정렬 버튼 */}
                    <div className="mb-4">
                        <button
                            className={`btn ${sortBy === 'name' ? 'btn-primary' : 'btn-outline-secondary'} me-2`}
                            onClick={() => handleSortChange('name')}
                        >
                            이름 순 정렬
                        </button>
                        <button
                            className={`btn ${sortBy === 'rating' ? 'btn-primary' : 'btn-outline-secondary'}`}
                            onClick={() => handleSortChange('rating')}
                        >
                            평점 순 정렬
                        </button>
                    </div>

                    {/* 클럽 리스트 */}
                    {clubs.length === 0 ? (
                        <div className="text-center py-5">
                            <p className="text-muted">등록된 클럽이 없습니다.</p>
                        </div>
                    ) : (
                        <>
                            <div className="list-group">
                                {clubs.map(club => (
                                    <div key={club.id} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div>
                                            <button
                                                className="btn btn-link p-0 text-start text-decoration-none"
                                                onClick={() => navigateToClub(club.id)}
                                                style={{ color: '#007bff', fontSize: '1.1rem', fontWeight: '500' }}
                                            >
                                                {club.name}
                                            </button>
                                            <div className="mt-1">
                                                <small className="text-muted">
                                                    📍 {club.location} | ⭐ {club.averageRating}/5
                                                </small>
                                            </div>
                                        </div>
                                        <div>
                                            <button
                                                className="btn btn-outline-primary btn-sm"
                                                onClick={() => navigateToClub(club.id)}
                                            >
                                                상세보기
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* 페이지네이션 */}
                            {totalPages > 1 && (
                                <nav className="mt-4">
                                    <ul className="pagination justify-content-center">
                                        <li className={`page-item ${currentPage === 0 ? 'disabled' : ''}`}>
                                            <button
                                                className="page-link"
                                                onClick={() => handlePageChange(currentPage - 1)}
                                                disabled={currentPage === 0}
                                            >
                                                이전
                                            </button>
                                        </li>
                                        {[...Array(totalPages)].map((_, i) => (
                                            <li key={i} className={`page-item ${i === currentPage ? 'active' : ''}`}>
                                                <button
                                                    className="page-link"
                                                    onClick={() => handlePageChange(i)}
                                                >
                                                    {i + 1}
                                                </button>
                                            </li>
                                        ))}
                                        <li className={`page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`}>
                                            <button
                                                className="page-link"
                                                onClick={() => handlePageChange(currentPage + 1)}
                                                disabled={currentPage === totalPages - 1}
                                            >
                                                다음
                                            </button>
                                        </li>
                                    </ul>
                                </nav>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ClubListPage;