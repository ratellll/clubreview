import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
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
    const { isAuthenticated, user, logout } = useAuth();

    const handleLogout = () => {
        logout();
        window.location.href = '/login';
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-light bg-light">
            <div className="container">
                <Link className="navbar-brand" to={isAuthenticated ? "/clubs" : "/login"}>
                    🎉 클럽의 민족
                </Link>
                <div className="navbar-nav ms-auto">
                    <Link className="nav-link" to="/clubs">클럽 목록</Link>

                    {isAuthenticated ? (
                        <>
                            <Link className="nav-link" to="/mypage">마이페이지</Link>
                            <span className="nav-link">안녕하세요, {user?.username}님!</span>
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
                            <Link className="nav-link" to="/login">로그인</Link>
                            <Link className="nav-link" to="/register">회원가입</Link>
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