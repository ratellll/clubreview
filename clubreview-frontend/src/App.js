import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ClubListPage from './pages/ClubListPage';
import ClubDetailPage from './pages/ClubDetailPage';
import MyPage from './pages/MyPage';
import ProtectedRoute from './components/common/ProtectedRoute';
import './App.css';

function Navigation() {
    const { isAuthenticated, user, logout, updateUser } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        window.location.href = '/login';
    };

    const handleNavigation = (path) => {
        if (location.pathname === path) {
            // 같은 경로인 경우 강제 새로고침
            navigate(path, { replace: true, state: { refresh: Date.now() } });
        } else {
            // 다른 경로인 경우 일반 이동
            navigate(path);
        }
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-light bg-light">
            <div className="container">
                <button className="navbar-brand btn btn-link"
                        onClick={() => handleNavigation(isAuthenticated ? "/clubs" : "/login")}
                        style={{ border: 'none', background: 'none', textDecoration: 'none' }}>
                🎉 클럽의 민족
                </button>
                <div className="navbar-nav ms-auto">
                    <button className="nav-link btn btn-link"
                            onClick={() => handleNavigation("/clubs")}
                            style={{ border: 'none', background: 'none' }}>
                            클럽 목록
                    </button>
                    {isAuthenticated ? (
                        <>
                        <button className="nav-link btn btn-link"
                                onClick={() => handleNavigation("/mypage")}
                                style={{ border: 'none', background: 'none' }}>
                            마이페이지
                            </button>
                        <span className="nav-link">안녕하세요, {user?.nickName}님!</span>
                            <button
                                className="nav-link btn btn-link"
                                onClick={handleLogout}
                                style={{ border: 'none', background: 'none' }}
                            >
                                로그아웃
                            </button>
                        </>
                    ) : (
                        <>
                        <button className="nav-link btn btn-link"
                                onClick={() => handleNavigation("/login")}
                                style={{ border: 'none', background: 'none' }}>
                            로그인
                        </button>
                        <button className="nav-link btn btn-link"
                                onClick={() => handleNavigation("/register")}
                                style={{ border: 'none', background: 'none' }}>
                            회원가입
                        </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}


function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="App">
                    <Navigation />

                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/clubs" element={<ClubListPage />} />
                        <Route path="/clubs/:id" element={<ClubDetailPage />} />
                        <Route path="/mypage" element={
                            <ProtectedRoute>
                                <MyPage />
                            </ProtectedRoute>
                        } />
                    </Routes>
                </div>
            </Router>
        </AuthProvider>
    );
}

export default App;