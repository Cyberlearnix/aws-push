// src/App.jsx
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthContainer from './components/AuthContainer';
import UserProfilePage from './components/UserProfilePage';
import AdminDashboard from './components/AdminDashboard';
import InstructorDashboard from './components/InstructorDashboard';
import UserDashboard from './components/UserDashboard';
import apiService from './services/api';

const App = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userRole, setUserRole] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkAuthStatus = async () => {
            const token = localStorage.getItem('accessToken');
            if (token) {
                try {
                    // Get user role from token or API
                    const role = apiService.getUserRole();
                    if (role) {
                        setIsLoggedIn(true);
                        setUserRole(role);
                    } else {
                        // Try to get profile from API if role not in token
                        const profile = await apiService.getUserProfile();
                        setIsLoggedIn(true);
                        setUserRole(profile.role?.toLowerCase() || 'student');
                    }
                } catch (error) {
                    // Token invalid, clear storage
                    localStorage.clear();
                    setIsLoggedIn(false);
                    setUserRole(null);
                }
            }
            setLoading(false);
        };

        checkAuthStatus();
    }, []);

    const handleLogout = () => {
        localStorage.clear();
        setIsLoggedIn(false);
        setUserRole(null);
    };

    if (loading) {
        return (
            <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '100vh',
                fontSize: '18px'
            }}>
                Loading...
            </div>
        );
    }

    return (
        <Router>
            <Routes>
                <Route 
                    path="/" 
                    element={
                        <AuthContainer 
                            onLogin={() => setIsLoggedIn(true)}
                            onRoleChange={(role) => setUserRole(role)}
                        />
                    } 
                />
                <Route
                    path="/dashboard"
                    element={<UserDashboard onLogout={handleLogout} />}
                />
                <Route
                    path="/admin"
                    element={<AdminDashboard onLogout={handleLogout} />}
                />
                <Route
                    path="/instructor"
                    element={<InstructorDashboard onLogout={handleLogout} />}
                />
                <Route
                    path="/profile"
                    element={<UserProfilePage onLogout={handleLogout} />}
                />
            </Routes>
        </Router>
    );
};

export default App;