// // src/pages/ClubListPage.js
// // 변경 요약:
// // 1) 뒤로가기(BFCache 포함) 복귀 시 지도 타일/레이아웃 타이밍으로 마커 미표시 → pageshow/visibilitychange/popstate/tilesloaded 시 relayout+재마커링.
// // 2) ESC 전역 처리 강화 → 인포윈도우/사진 모달 닫기 일원화(전역 keydown, 상태 동기화).
// // 3) 최신 clubs 상태 접근 보장 → clubsRef 도입, displayMarkersOnMap(sourceClubs)로 일관 사용.
// // 4) ESLint 경고 정리 → 사용 안 하는 state 제거, effect 의존성 useCallback으로 안정화.
//
// import React, { useState, useEffect, useRef, useCallback } from 'react';
// import { useNavigate, useLocation } from 'react-router-dom';
// import { clubService } from '../services/clubService';
// import LoadingSpinner from '../components/common/LoadingSpinner';
// import Alert from '../components/common/Alert';
//
// const ClubListPage = () => {
//     const navigate = useNavigate();
//     const location = useLocation();
//     const mapRef = useRef(null);
//
//     const [clubs, setClubs] = useState([]);
//     const [loading, setLoading] = useState(true);
//     const [error, setError] = useState('');
//     const [sortBy, setSortBy] = useState('name');
//     const [currentPage, setCurrentPage] = useState(0);
//     const [totalPages, setTotalPages] = useState(0);
//     const [isMapLoaded, setIsMapLoaded] = useState(false);
//     const [isMapInitializing, setIsMapInitializing] = useState(false);
//
//     // refs
//     const markersRef = useRef([]);
//     const currentInfoWindowRef = useRef(null);
//     const mapInstanceRef = useRef(null);
//     const isMapReadyRef = useRef(false);
//     const clubsRef = useRef([]); // 최신 clubs 접근용
//
//     useEffect(() => {
//         clubsRef.current = clubs;
//     }, [clubs]);
//
//     const closeCurrentInfoWindow = useCallback(() => {
//         if (!currentInfoWindowRef.current) return;
//         const closing = currentInfoWindowRef.current;
//         closing.close();
//         currentInfoWindowRef.current = null;
//         markersRef.current.forEach((m) => {
//             if (m.infoWindow === closing) m.isInfoWindowOpen = false;
//         });
//     }, []);
//
//     const displayMarkersOnMap = useCallback((sourceClubs = clubsRef.current) => {
//         if (!window.kakao || !window.kakao.maps || !mapInstanceRef.current) return;
//
//         // 기존 마커/인포윈도우 정리
//         markersRef.current.forEach((marker) => marker.setMap(null));
//         markersRef.current = [];
//         closeCurrentInfoWindow();
//
//         const list = Array.isArray(sourceClubs) ? sourceClubs : [];
//         const newMarkers = [];
//         const bounds = new window.kakao.maps.LatLngBounds();
//
//         list.forEach((club) => {
//             const lat = Number(club.latitude);
//             const lng = Number(club.longitude);
//             if (!Number.isFinite(lat) || !Number.isFinite(lng)) return;
//             try {
//                 const position = new window.kakao.maps.LatLng(lat, lng);
//                 const marker = new window.kakao.maps.Marker({ position, map: mapInstanceRef.current });
//
//                 const infoWindowContent = `
//           <div style="padding: 15px; width: 300px;">
//             <h5 style="margin-bottom: 10px; color: #007bff; cursor: pointer;" onclick="window.clubListPageInstance.navigateToClub(${club.id})">${club.name}</h5>
//             <p style="margin: 5px 0;"><strong>위치:</strong> ${club.location || 'N/A'}</p>
//             <p style="margin: 5px 0;"><strong>전화번호:</strong> ${club.callNumber || 'N/A'}</p>
//             <p style="margin: 5px 0;"><strong>설명:</strong> ${club.description || 'N/A'}</p>
//             <p style="margin: 5px 0;"><strong>평점:</strong> ⭐ ${club.averageRating || 0} / 5</p>
//             ${club.photoUrl ? `<img src="${club.photoUrl}" alt="클럽 사진" style="width: 100%; max-height: 150px; object-fit: cover; margin-top: 10px; cursor: pointer; display: block;" onclick="window.clubListPageInstance.showPhotoModal('${club.photoUrl}')" onerror="this.style.display='none'" />` : ''}
//           </div>
//         `;
//                 const infoWindow = new window.kakao.maps.InfoWindow({ content: infoWindowContent });
//                 marker.infoWindow = infoWindow;
//                 marker.isInfoWindowOpen = false;
//
//                 window.kakao.maps.event.addListener(marker, 'click', () => {
//                     if (marker.isInfoWindowOpen && currentInfoWindowRef.current === infoWindow) {
//                         infoWindow.close();
//                         currentInfoWindowRef.current = null;
//                         marker.isInfoWindowOpen = false;
//                     } else {
//                         if (currentInfoWindowRef.current) {
//                             const prev = currentInfoWindowRef.current;
//                             prev.close();
//                             markersRef.current.forEach((m) => {
//                                 if (m.infoWindow === prev) m.isInfoWindowOpen = false;
//                             });
//                         }
//                         infoWindow.open(mapInstanceRef.current, marker);
//                         currentInfoWindowRef.current = infoWindow;
//                         marker.isInfoWindowOpen = true;
//                     }
//                 });
//
//                 newMarkers.push(marker);
//                 bounds.extend(position);
//             } catch (e) {
//                 console.error('Marker create fail:', e);
//             }
//         });
//
//         markersRef.current = newMarkers;
//
//         if (newMarkers.length > 1) {
//             try { mapInstanceRef.current.setBounds(bounds); } catch {}
//         } else if (newMarkers.length === 1) {
//             try { mapInstanceRef.current.setCenter(newMarkers[0].getPosition()); } catch {}
//         }
//     }, [closeCurrentInfoWindow]);
//
//     const forceRelayoutAndRedraw = useCallback(() => {
//         if (!mapInstanceRef.current) return;
//         try { mapInstanceRef.current.relayout(); } catch {}
//         if (clubsRef.current && clubsRef.current.length > 0) {
//             displayMarkersOnMap(clubsRef.current);
//         }
//     }, [displayMarkersOnMap]);
//
//     const initializeMap = () => {
//         if (mapInstanceRef.current) return;
//         if (!mapRef.current) return;
//         if (!window.kakao || !window.kakao.maps) return;
//
//         try {
//             const options = { center: new window.kakao.maps.LatLng(37.5665, 126.9780), level: 8 };
//             const kakaoMap = new window.kakao.maps.Map(mapRef.current, options);
//
//             mapInstanceRef.current = kakaoMap;
//             setIsMapInitializing(false);
//             isMapReadyRef.current = true;
//
//             // 타일 최초 로딩 후 1회 리레이아웃+리드로우
//             let tilesOnce = false;
//             window.kakao.maps.event.addListener(kakaoMap, 'tilesloaded', () => {
//                 if (tilesOnce) return; tilesOnce = true;
//                 forceRelayoutAndRedraw();
//             });
//
//             setTimeout(() => {
//                 try { kakaoMap.relayout(); } catch {}
//                 if (clubsRef.current && clubsRef.current.length > 0) {
//                     displayMarkersOnMap(clubsRef.current);
//                 }
//             }, 300);
//
//             window.kakao.maps.event.addListener(kakaoMap, 'click', () => {
//                 closeCurrentInfoWindow();
//             });
//         } catch (error) {
//             console.error('❌ 지도 생성 실패:', error);
//             setError('지도 생성에 실패했습니다.');
//             setIsMapInitializing(false);
//         }
//     };
//
//     const initMap = useCallback(async () => {
//         if (isMapInitializing || isMapReadyRef.current) return;
//         setIsMapInitializing(true);
//
//         if (!mapRef.current) {
//             setIsMapInitializing(false);
//             return;
//         }
//
//         const boot = () => {
//             window.kakao.maps.load(() => {
//                 setIsMapLoaded(true);
//                 setTimeout(() => initializeMap(), 50);
//             });
//         };
//
//         if (window.kakao && window.kakao.maps) {
//             boot();
//         } else {
//             const script = document.createElement('script');
//             script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=93b4ad501fc7b3941109e59488da8aa9&autoload=false';
//             script.onload = () => boot();
//             script.onerror = () => {
//                 setError('지도를 불러올 수 없습니다.');
//                 setIsMapInitializing(false);
//             };
//             document.head.appendChild(script);
//         }
//     }, [isMapInitializing]);
//
//     const fetchClubs = useCallback(async () => {
//         try {
//             setLoading(true);
//             const params = { page: currentPage, size: 20, sortBy };
//             const response = await clubService.getClubs(params);
//             let sortedClubs = response.content || [];
//             if (sortBy === 'name') sortedClubs = sortedClubs.sort((a, b) => a.name.localeCompare(b.name));
//             else if (sortBy === 'rating') sortedClubs = sortedClubs.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
//             setClubs(sortedClubs);
//             setTotalPages(response.totalPages || 0);
//             setError('');
//             if (mapInstanceRef.current && isMapReadyRef.current) displayMarkersOnMap(sortedClubs);
//         } catch (err) {
//             console.error('클럽 목록 조회 실패:', err);
//             setError('클럽 목록을 불러오는데 실패했습니다.');
//         } finally {
//             setLoading(false);
//         }
//     }, [currentPage, sortBy, displayMarkersOnMap]);
//
//     const resetMapAndData = useCallback(() => {
//         if (mapInstanceRef.current) {
//             markersRef.current.forEach((m) => m.setMap(null));
//             markersRef.current = [];
//             closeCurrentInfoWindow();
//             mapInstanceRef.current = null;
//             setIsMapLoaded(false);
//             setIsMapInitializing(false);
//             isMapReadyRef.current = false;
//             setTimeout(() => { initMap(); }, 100);
//         }
//         fetchClubs();
//     }, [closeCurrentInfoWindow, initMap, fetchClubs]);
//
//     // 최초 지도 스크립트 로드
//     useEffect(() => {
//         const timer = setTimeout(() => { initMap(); }, 300);
//         return () => {
//             clearTimeout(timer);
//             if (window.clubListPageInstance) delete window.clubListPageInstance;
//         };
//     }, [initMap]);
//
//     // 클럽 데이터 조회 트리거
//     useEffect(() => {
//         fetchClubs();
//     }, [fetchClubs]);
//
//     // 라우팅 state에 따른 리셋
//     useEffect(() => {
//         if (location.state?.refresh) {
//             resetMapAndData();
//             window.history.replaceState(null, '');
//         }
//     }, [location.state?.refresh, resetMapAndData]);
//
//     // clubs 변경 시 마커 표시
//     useEffect(() => {
//         if (mapInstanceRef.current && clubs.length > 0 && isMapReadyRef.current) displayMarkersOnMap(clubs);
//     }, [clubs, displayMarkersOnMap]);
//
//     // 전역 ESC 처리(인포윈도우/사진 모달 모두 닫기)
//     useEffect(() => {
//         const onEsc = (e) => {
//             const k = e.key || e.code;
//             if (k === 'Escape' || k === 'Esc' || e.keyCode === 27) {
//                 closeCurrentInfoWindow();
//                 const modal = document.querySelector('.photo-modal');
//                 if (modal) modal.remove();
//             }
//         };
//         window.addEventListener('keydown', onEsc);
//         return () => window.removeEventListener('keydown', onEsc);
//     }, [closeCurrentInfoWindow]);
//
//     // 히스토리/가시성/복귀 이벤트에서 맵 재도색 보장
//     useEffect(() => {
//         const onPageShow = () => requestAnimationFrame(forceRelayoutAndRedraw);
//         const onVisible = () => { if (document.visibilityState === 'visible') forceRelayoutAndRedraw(); };
//         const onPop = () => setTimeout(forceRelayoutAndRedraw, 0);
//
//         window.addEventListener('pageshow', onPageShow);
//         document.addEventListener('visibilitychange', onVisible);
//         window.addEventListener('popstate', onPop);
//
//         try {
//             const nav = performance.getEntriesByType && performance.getEntriesByType('navigation');
//             if (nav && nav[0] && nav[0].type === 'back_forward') setTimeout(forceRelayoutAndRedraw, 0);
//         } catch {}
//
//         return () => {
//             window.removeEventListener('pageshow', onPageShow);
//             document.removeEventListener('visibilitychange', onVisible);
//             window.removeEventListener('popstate', onPop);
//         };
//     }, [forceRelayoutAndRedraw]);
//
//     const navigateToClub = useCallback((clubId) => {
//         navigate(`/clubs/${clubId}`);
//     }, [navigate]);
//
//     const showPhotoModal = useCallback((photoUrl) => {
//         const existingModal = document.querySelector('.photo-modal');
//         if (existingModal) existingModal.remove();
//
//         const modal = document.createElement('div');
//         modal.className = 'photo-modal';
//         modal.style.cssText = `position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.8); display: flex; justify-content: center; align-items: center; z-index: 1000; cursor: pointer;`;
//         modal.innerHTML = `<img src="${photoUrl}" style="max-width: 90%; max-height: 90%; object-fit: contain;" alt="클럽 사진 확대" />`;
//         modal.onclick = (e) => { if (e.target === modal) modal.remove(); };
//         document.body.appendChild(modal);
//     }, []);
//
//     // 전역 호출용 메서드 노출
//     useEffect(() => {
//         window.clubListPageInstance = { navigateToClub, showPhotoModal };
//         return () => { delete window.clubListPageInstance; };
//     }, [navigateToClub, showPhotoModal]);
//
//     // 윈도우 리사이즈 시 지도 크기 재조정
//     useEffect(() => {
//         const handleResize = () => {
//             if (mapInstanceRef.current) setTimeout(() => { try { mapInstanceRef.current.relayout(); } catch {} }, 100);
//         };
//         window.addEventListener('resize', handleResize);
//         return () => window.removeEventListener('resize', handleResize);
//     }, []);
//
//     if (loading) return <LoadingSpinner message="클럽 목록을 불러오는 중..." />;
//
//     return (
//         <div className="container mt-4" style={{ maxWidth: '1200px', padding: '0 15px' }}>
//             <div className="row">
//                 <div className="col-12" style={{ padding: '0 15px' }}>
//                     <h1 className="mb-4">🎉 클럽 목록</h1>
//
//                     {error && (
//                         <Alert type="danger" message={error} onClose={() => setError('')} dismissible />
//                     )}
//
//                     {/* 카카오 지도 */}
//                     <div className="mb-4">
//                         {!isMapLoaded && (
//                             <div style={{ width: '100%', height: '500px', display: 'flex', justifyContent: 'center', alignItems: 'center', backgroundColor: '#f8f9fa', borderRadius: '8px', border: '1px solid #dee2e6' }}>
//                                 <p className="text-muted">지도를 불러오는 중...</p>
//                             </div>
//                         )}
//                         <div
//                             id="kakao-map-container"
//                             ref={mapRef}
//                             style={{ width: '100%', height: '500px', minWidth: '100%', minHeight: '500px', borderRadius: '8px', backgroundColor: '#f8f9fa', display: isMapLoaded ? 'block' : 'none', position: 'relative' }}
//                         />
//                         {error && error.includes('지도') && (
//                             <div className="text-center py-3">
//                                 <small className="text-danger">지도를 불러올 수 없습니다. 네트워크 연결을 확인해주세요.</small>
//                             </div>
//                         )}
//                     </div>
//
//                     {/* 정렬 버튼 */}
//                     <div className="mb-4">
//                         <button className={`btn ${sortBy === 'name' ? 'btn-primary' : 'btn-outline-secondary'} me-2`} onClick={() => setSortBy('name')}>이름 순 정렬</button>
//                         <button className={`btn ${sortBy === 'rating' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => setSortBy('rating')}>평점 순 정렬</button>
//                     </div>
//
//                     {/* 클럽 리스트 */}
//                     {clubs.length === 0 ? (
//                         <div className="text-center py-5">
//                             <p className="text-muted">등록된 클럽이 없습니다.</p>
//                         </div>
//                     ) : (
//                         <>
//                             <div className="list-group">
//                                 {clubs.map((club) => (
//                                     <div key={club.id} className="list-group-item d-flex justify-content-between align-items-center">
//                                         <div>
//                                             <button className="btn btn-link p-0 text-start text-decoration-none" onClick={() => navigateToClub(club.id)} style={{ color: '#007bff', fontSize: '1.1rem', fontWeight: '500' }}>
//                                                 {club.name}
//                                             </button>
//                                             <div className="mt-1">
//                                                 <small className="text-muted">📍 {club.location} | ⭐ {club.averageRating}/5</small>
//                                             </div>
//                                         </div>
//                                         <div>
//                                             <button className="btn btn-outline-primary btn-sm" onClick={() => navigateToClub(club.id)}>상세보기</button>
//                                         </div>
//                                     </div>
//                                 ))}
//                             </div>
//
//                             {totalPages > 1 && (
//                                 <nav className="mt-4">
//                                     <ul className="pagination justify-content-center">
//                                         <li className={`page-item ${currentPage === 0 ? 'disabled' : ''}`}>
//                                             <button className="page-link" onClick={() => setCurrentPage((p) => p - 1)} disabled={currentPage === 0}>이전</button>
//                                         </li>
//                                         {[...Array(totalPages)].map((_, i) => (
//                                             <li key={i} className={`page-item ${i === currentPage ? 'active' : ''}`}>
//                                                 <button className="page-link" onClick={() => setCurrentPage(i)}>{i + 1}</button>
//                                             </li>
//                                         ))}
//                                         <li className={`page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`}>
//                                             <button className="page-link" onClick={() => setCurrentPage((p) => p + 1)} disabled={currentPage === totalPages - 1}>다음</button>
//                                         </li>
//                                     </ul>
//                                 </nav>
//                             )}
//                         </>
//                     )}
//                 </div>
//             </div>
//         </div>
//     );
// };
//
// export default ClubListPage;
