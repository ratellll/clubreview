import React, { useState, useEffect } from 'react';
import { clubService } from '../../services/clubService';

const ApiTest = () => {
    const [clubs, setClubs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const testApi = async () => {
            try {
                const clubData = await clubService.getClubs({ page: 0, size: 5 });
                setClubs(clubData.content || []);
                setError(null);
            } catch (err) {
                setError('백엔드 연결 실패: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        testApi();
    }, []);

    if (loading) return <div className="text-center">로딩 중...</div>;

    return (
        <div className="mt-4">
            <h3>🔌 백엔드 연동 테스트</h3>
            {error ? (
                <div className="alert alert-danger">{error}</div>
            ) : (
                <div className="alert alert-success">
                    ✅ 백엔드 연결 성공! {clubs.length}개 클럽 데이터 조회됨
                </div>
            )}

            {clubs.length > 0 && (
                <div>
                    <h4>클럽 목록:</h4>
                    <ul className="list-group">
                        {clubs.map(club => (
                            <li key={club.id} className="list-group-item">
                                <strong>{club.name}</strong> - {club.location}
                                <br />
                                <small>평점: {club.averageRating}/5</small>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default ApiTest;