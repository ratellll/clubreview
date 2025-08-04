import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ClubListPage from './pages/ClubListPage';
import './App.css';

function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="App">
                    <nav className="navbar navbar-expand-lg navbar-light bg-light">
                        <div className="container">
                            <a className="navbar-brand" href="/">🎉 클럽의 민족</a>
                            <div className="navbar-nav ms-auto">
                                <a className="nav-link" href="/clubs">클럽 목록</a>
                                <a className="nav-link" href="/login">로그인</a>
                                <a className="nav-link" href="/register">회원가입</a>
                            </div>
                        </div>
                    </nav>

                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/clubs" element={<ClubListPage />} />
                    </Routes>
                </div>
            </Router>
        </AuthProvider>
    );
}

export default App;