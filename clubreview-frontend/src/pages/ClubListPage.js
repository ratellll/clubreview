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

    // Kakao 지도 스크립트 로드
    useEffect(() => {
        const script = document.createElement('script');
        script.async = true;
        script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=93b4ad501fc7b3941109e59488da8aa9&autoload=false';
        document.head.appendChild(script);

        script.onload = () => {
            window.kakao.maps.load(() => {
                initializeMap();
            });
        };

        return () => {
            document.head.removeChild(script);
        };
    }, []);

    // 클럽 데이터 조회
    useEffect(() => {
        fetchClubs();
    }, [sortBy, currentPage]);

    // 지도에 마커 표시
    useEffect(() => {
        if (map && clubs.length > 0) {
            displayMarkersOnMap();
        }
    }, [map, clubs]);

    const initializeMap = () => {
        if (!mapRef.current) return;

        const container = mapRef.current;
        const options = {
            center: new window.kakao.maps.LatLng(37.5665, 126.9780), // 서울 중심
            level: 8
        };

        const kakaoMap = new window.kakao.maps.Map(container, options);
        setMap(kakaoMap);

        // 지도 클릭 시 정보창 닫기
        window.kakao.maps.event.addListener(kakaoMap, 'click', () => {
            if (currentInfoWindow) {
                currentInfoWindow.close();
                setCurrentInfoWindow(null);
            }
        });
    };

    const fetchClubs = async () => {
        try {
            setLoading(true);
            const params = {
                page: currentPage,
                size: 20,
                sort: sortBy === 'rating' ? 'averageRating,desc' : 'name,asc'
            };
            const response = await clubService.getClubs(params);
            setClubs(response.content || []);
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
                                 style="width: 100%; max-height: 150px; object-fit: cover; margin-top: 10px; cursor: pointer;"
                                 onclick="window.clubListPageInstance.showPhotoModal('${club.photoUrl}')" />
                        ` : ''}
                    </div>
                `;

                const infoWindow = new window.kakao.maps.InfoWindow({
                    content: infoWindowContent
                });

                // 마커 클릭 이벤트
                window.kakao.maps.event.addListener(marker, 'click', () => {
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
        // 간단한 모달 구현
        const modal = document.createElement('div');
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

        modal.onclick = () => document.body.removeChild(modal);
        document.body.appendChild(modal);

        // ESC 키로 닫기
        const handleEsc = (e) => {
            if (e.key === 'Escape') {
                document.body.removeChild(modal);
                document.removeEventListener('keydown', handleEsc);
            }
        };
        document.addEventListener('keydown', handleEsc);
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

    if (loading) {
        return <LoadingSpinner message="클럽 목록을 불러오는 중..." />;
    }

    return (
        <div className="container mt-4">
            <div className="row">
                <div className="col-12">
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
                        <div
                            ref={mapRef}
                            style={{ width: '100%', height: '500px', borderRadius: '8px' }}
                        ></div>
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