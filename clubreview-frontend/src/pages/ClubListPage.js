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
        const initMap = () => {
            if (!mapRef.current) {
                console.log('❌ 지도 컨테이너 없음');
                return;
            }

            if (window.kakao && window.kakao.maps) {
                console.log('✅ Kakao Maps 사용 가능');
                window.kakao.maps.load(() => {
                    console.log('✅ Kakao Maps 로드 완료');
                    // 먼저 지도를 표시하고
                    setIsMapLoaded(true);
                    // 약간의 지연 후 지도 초기화
                    setTimeout(() => {
                        initializeMap();
                    }, 50);
                });
            } else {
                console.log('🔄 Kakao Maps 스크립트 로드 중...');
                const script = document.createElement('script');
                script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=93b4ad501fc7b3941109e59488da8aa9&autoload=false';
                script.onload = () => {
                    console.log('✅ 스크립트 로드 완료');
                    window.kakao.maps.load(() => {
                        console.log('✅ Kakao Maps 초기화 완료');
                        setIsMapLoaded(true);
                        setTimeout(() => {
                            initializeMap();
                        }, 50);
                    });
                };
                script.onerror = () => {
                    console.error('❌ 스크립트 로드 실패');
                    setError('지도를 불러올 수 없습니다.');
                };
                document.head.appendChild(script);
            }
        };

        // DOM이 준비된 후 실행
        if (mapRef.current) {
            initMap();
        } else {
            // DOM이 아직 준비되지 않은 경우 약간 지연
            const timer = setTimeout(initMap, 100);
            return () => clearTimeout(timer);
        }
    }, []);

    // 클럽 데이터 조회
    useEffect(() => {
        fetchClubs();
    }, [sortBy, currentPage]);

    // 지도에 마커 표시
    useEffect(() => {
        if (map && clubs.length > 0) {
            console.log('🔍 마커 표시 시작:', clubs.length + '개 클럽');
            displayMarkersOnMap();

            // 마커 표시 후 지도 크기 재조정
            setTimeout(() => {
                console.log('🔄 마커 표시 후 relayout');
                map.relayout();
            }, 200);
        }
    }, [map, clubs]);

    const initializeMap = () => {
        console.log('🗺️ 지도 생성 시작');

        if (!mapRef.current) {
            console.error('❌ 지도 컨테이너를 찾을 수 없음');
            return;
        }

        if (!window.kakao || !window.kakao.maps) {
            console.error('❌ Kakao Maps API 없음');
            return;
        }

        try {
            const options = {
                center: new window.kakao.maps.LatLng(37.5665, 126.9780),
                level: 8
            };

            console.log('🗺️ 지도 객체 생성 중...');
            const kakaoMap = new window.kakao.maps.Map(mapRef.current, options);

            setMap(kakaoMap);
            console.log('✅ 지도 생성 완료');

            // 지도 크기 재조정을 여러 번 다른 시점에 실행
            setTimeout(() => {
                console.log('🔄 1차 relayout');
                kakaoMap.relayout();
            }, 100);

            setTimeout(() => {
                console.log('🔄 2차 relayout');
                kakaoMap.relayout();
            }, 500);

            setTimeout(() => {
                console.log('🔄 3차 relayout (최종)');
                kakaoMap.relayout();
            }, 1000);

            // 지도 클릭 시 정보창 닫기 (ref 사용으로 수정)
            window.kakao.maps.event.addListener(kakaoMap, 'click', () => {
                console.log('지도 배경 클릭 - 정보창 닫기');
                console.log('현재 정보창:', currentInfoWindowRef.current);
                console.log('현재 마커 개수:', markersRef.current.length);

                if (currentInfoWindowRef.current) {
                    currentInfoWindowRef.current.close();
                    setCurrentInfoWindow(null);
                    currentInfoWindowRef.current = null;

                    // 모든 마커의 정보창 상태 초기화
                    markersRef.current.forEach(marker => {
                        if (marker.isInfoWindowOpen) {
                            console.log('마커 상태 초기화:', marker.clubName);
                            marker.isInfoWindowOpen = false;
                        }
                    });
                    console.log('정보창 닫기 완료');
                } else {
                    console.log('닫을 정보창이 없음');
                }
            });

        } catch (error) {
            console.error('❌ 지도 생성 실패:', error);
            setError('지도 생성에 실패했습니다.');
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

    const markersRef = useRef([]);
    const currentInfoWindowRef = useRef(null);

    const displayMarkersOnMap = () => {
        if (!window.kakao || !window.kakao.maps || !map) return;

        // 기존 마커들 제거
        markersRef.current.forEach(marker => marker.setMap(null));

        // 기존 정보창 닫기
        if (currentInfoWindowRef.current) {
            currentInfoWindowRef.current.close();
            setCurrentInfoWindow(null);
            currentInfoWindowRef.current = null;
        }

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

                // 마커에 정보창 연결
                marker.infoWindow = infoWindow;
                marker.isInfoWindowOpen = false;
                marker.clubName = club.name; // 디버깅용

                // 마커 클릭 이벤트
                window.kakao.maps.event.addListener(marker, 'click', () => {
                    console.log('마커 클릭됨:', club.name);

                    // 현재 클릭한 마커의 정보창이 이미 열려있는지 확인
                    if (marker.isInfoWindowOpen && currentInfoWindowRef.current === infoWindow) {
                        // 같은 마커를 다시 클릭한 경우 - 정보창 닫기
                        console.log('같은 마커 재클릭 - 정보창 닫기');
                        infoWindow.close();
                        setCurrentInfoWindow(null);
                        currentInfoWindowRef.current = null;
                        marker.isInfoWindowOpen = false;
                    } else {
                        // 다른 마커를 클릭했거나 정보창이 닫혀있는 경우

                        // 먼저 현재 열려있는 정보창이 있다면 닫기
                        if (currentInfoWindowRef.current) {
                            console.log('이전 정보창 닫기');
                            currentInfoWindowRef.current.close();
                            // 이전 마커의 상태도 업데이트
                            markersRef.current.forEach(m => {
                                if (m.infoWindow === currentInfoWindowRef.current) {
                                    m.isInfoWindowOpen = false;
                                    console.log('이전 마커 상태 업데이트:', m.clubName);
                                }
                            });
                        }

                        // 새로운 정보창 열기
                        console.log('새 정보창 열림:', club.name);
                        infoWindow.open(map, marker);
                        setCurrentInfoWindow(infoWindow);
                        currentInfoWindowRef.current = infoWindow;
                        marker.isInfoWindowOpen = true;
                    }
                });

                newMarkers.push(marker);
            }
        });

        // ref와 state 모두 업데이트dd

        markersRef.current = newMarkers;
        setMarkers(newMarkers);
        console.log('마커 표시 완료:', newMarkers.length + '개');
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
            if (e.key === 'Escape' && currentInfoWindowRef.current) {
                console.log('ESC 키로 정보창 닫기');
                currentInfoWindowRef.current.close();
                setCurrentInfoWindow(null);
                currentInfoWindowRef.current = null;

                // 모든 마커의 상태 업데이트
                markersRef.current.forEach(m => {
                    if (m.isInfoWindowOpen) {
                        console.log('ESC - 마커 상태 초기화:', m.clubName);
                        m.isInfoWindowOpen = false;
                    }
                });
                document.removeEventListener('keydown', handleEscKey);
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
                // 리사이즈 후 약간의 지연을 두고 relayout
                setTimeout(() => {
                    map.relayout();
                }, 100);
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
                    <div className="mb-4">
                        {!isMapLoaded && (
                            <div
                                style={{
                                    width: '100%',
                                    height: '500px',
                                    display: 'flex',
                                    justifyContent: 'center',
                                    alignItems: 'center',
                                    backgroundColor: '#f8f9fa',
                                    borderRadius: '8px',
                                    border: '1px solid #dee2e6'
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
                                height: '500px',
                                minWidth: '100%',
                                minHeight: '500px',
                                borderRadius: '8px',
                                backgroundColor: '#f8f9fa',
                                display: isMapLoaded ? 'block' : 'none',
                                position: 'relative'
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