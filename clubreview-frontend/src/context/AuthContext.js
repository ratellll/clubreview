import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { userService } from '../services/userService';

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const checkAuthStatus = async () => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                // JWT 토큰에서 사용자 정보 추출
                const payload = JSON.parse(atob(token.split('.')[1]));

                // 토큰 만료 시간 확인
                if (payload.exp * 1000 > Date.now()) {
                    try {
                        const userProfile = await userService.getMyPage();
                        setUser({
                                userName: payload.sub,
                                nickName: userProfile.user.nickName,
                            role: userProfile.user.role
                        });
                        setIsAuthenticated(true);
                    } catch (error) {
                        console.warn('사용자 프로필 조회 실패, 기본 정보로 설정:', error);
                        setUser({ username: payload.sub });
                        setIsAuthenticated(true);
                    }
                } else {
                    // 토큰이 만료된 경우
                    localStorage.removeItem('token');
                }
                
            } catch (error) {
                console.error('토큰 파싱 오류:', error);
                localStorage.removeItem('token');
            }
        }
        setLoading(false);
    };

    const login = async (credentials) => {
        try {
            const response = await authService.login(credentials);
            localStorage.setItem('token', response.token);
            try {
                const userProfile = await userService.getMyPage();
                setUser({
                        username: response.userName,
                        nickName: userProfile.user.nickName,
                    role: userProfile.user.role
                });
                setIsAuthenticated(true);
            } catch (error) {
                console.warn('사용자 프로필 조회 실패, 기본 정보로 설정:', error);
                setUser({ username: response.username });
                setIsAuthenticated(true);
            }
            return response;
        } catch (error) {

            if (error.response?.status === 403) {
                alert(`❌ ${error.response.data.message}`);
                throw new Error('계정이 정지되었습니다.');
            }
            throw error;
        }
    };

    const register = async (userData) => {
        try {
            const response = await authService.register(userData);
            return response;
        } catch (error) {
            throw error;
        }
    };

    const updateUser = (updateData) =>{
        setUser(prevUser => ({
            ...prevUser,
            ...updateData
        }));
    };

    const refreshUser = async () => {
        await checkAuthStatus();
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
        setIsAuthenticated(false);
    };

    const value = {
        user,
        isAuthenticated,
        loading,
        login,
        register,
        logout,
        updateUser,
        refreshUser
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};