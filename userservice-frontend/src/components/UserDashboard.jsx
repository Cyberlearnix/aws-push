import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const UserDashboard = ({ user, onLogout }) => {
    const [showUserMenu, setShowUserMenu] = useState(false);
    
    if (!user) {
        return (
            <div className="dashboard-loading">
                <div className="loading-spinner"></div>
                <p>Loading your dashboard...</p>
            </div>
        );
    }

    const getInitials = (name) => {
        return name
            .split(' ')
            .map(word => word[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Not available';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <div className="dashboard">
            {/* Header */}
            <header className="dashboard-header">
                <div className="header-content">
                    <div className="logo">
                        <h1>UserService</h1>
                    </div>
                    
                    <div className="user-menu">
                        <button 
                            className="user-profile-btn"
                            onClick={() => setShowUserMenu(!showUserMenu)}
                        >
                            <div className="avatar">
                                {user.avatar ? (
                                    <img src={user.avatar} alt={user.fullName} />
                                ) : (
                                    <span className="avatar-text">{getInitials(user.fullName || 'User')}</span>
                                )}
                            </div>
                            <span className="user-name">{user.fullName || 'User'}</span>
                            <span className="dropdown-arrow">▼</span>
                        </button>
                        
                        {showUserMenu && (
                            <div className="user-dropdown">
                                <div className="dropdown-header">
                                    <strong>{user.fullName || 'User'}</strong>
                                    <span>{user.email}</span>
                                </div>
                                <div className="dropdown-divider"></div>
                                <Link to="/profile" className="dropdown-item">
                                    <span>📝</span>
                                    Edit Profile
                                </Link>
                                <button onClick={onLogout} className="dropdown-item logout-btn">
                                    <span>🚪</span>
                                    Sign out
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="dashboard-main">
                <div className="dashboard-container">
                    {/* Welcome Section */}
                    <section className="welcome-section">
                        <div className="welcome-content">
                            <h2>Welcome back, {user.fullName?.split(' ')[0] || 'Student'}! 📚</h2>
                            <p>Ready to continue your learning journey today.</p>
                        </div>
                    </section>

                    {/* Student Stats Cards */}
                    <section className="stats-section">
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-icon">📚</div>
                                <div className="stat-content">
                                    <h3>Enrolled Courses</h3>
                                    <p className="stat-value">3</p>
                                    <p className="stat-description">Active learning paths</p>
                                </div>
                            </div>
                            
                            <div className="stat-card">
                                <div className="stat-icon">📝</div>
                                <div className="stat-content">
                                    <h3>Assignments</h3>
                                    <p className="stat-value">2 Due</p>
                                    <p className="stat-description">Pending submissions</p>
                                </div>
                            </div>
                            
                            <div className="stat-card">
                                <div className="stat-icon">🎯</div>
                                <div className="stat-content">
                                    <h3>Overall Grade</h3>
                                    <p className="stat-value">85.2%</p>
                                    <p className="stat-description">Keep up the good work!</p>
                                </div>
                            </div>
                            
                            <div className="stat-card">
                                <div className="stat-icon">⏱️</div>
                                <div className="stat-content">
                                    <h3>Study Time</h3>
                                    <p className="stat-value">24h</p>
                                    <p className="stat-description">This week</p>
                                </div>
                            </div>
                        </div>
                    </section>

                    {/* Recent Activity */}
                    <section className="activity-section">
                        <h3>Recent Activity</h3>
                        <div className="activity-feed">
                            <div className="activity-item">
                                <div className="activity-icon">📝</div>
                                <div className="activity-content">
                                    <p><strong>Assignment submitted:</strong> Mathematics Homework #5</p>
                                    <span className="activity-time">2 hours ago</span>
                                </div>
                            </div>
                            <div className="activity-item">
                                <div className="activity-icon">📚</div>
                                <div className="activity-content">
                                    <p><strong>Course accessed:</strong> Introduction to Physics</p>
                                    <span className="activity-time">1 day ago</span>
                                </div>
                            </div>
                            <div className="activity-item">
                                <div className="activity-icon">🎯</div>
                                <div className="activity-content">
                                    <p><strong>Quiz completed:</strong> Biology Chapter 3 Quiz - Score: 92%</p>
                                    <span className="activity-time">2 days ago</span>
                                </div>
                            </div>
                        </div>
                    </section>

                    {/* Student Quick Actions */}
                    <section className="actions-section">
                        <h3>Student Portal</h3>
                        <div className="actions-grid">
                            <div className="action-card">
                                <div className="action-icon">📚</div>
                                <div className="action-content">
                                    <h4>My Courses</h4>
                                    <p>Access your enrolled courses</p>
                                </div>
                                <div className="action-arrow">→</div>
                            </div>
                            
                            <div className="action-card">
                                <div className="action-icon">📝</div>
                                <div className="action-content">
                                    <h4>Assignments</h4>
                                    <p>View and submit assignments</p>
                                </div>
                                <div className="action-arrow">→</div>
                            </div>
                            
                            <div className="action-card">
                                <div className="action-icon">📈</div>
                                <div className="action-content">
                                    <h4>Grades</h4>
                                    <p>Check your academic progress</p>
                                </div>
                                <div className="action-arrow">→</div>
                            </div>
                            
                            <div className="action-card">
                                <div className="action-icon">📅</div>
                                <div className="action-content">
                                    <h4>Schedule</h4>
                                    <p>View your class timetable</p>
                                </div>
                                <div className="action-arrow">→</div>
                            </div>
                            
                            <Link to="/profile" className="action-card">
                                <div className="action-icon">✏️</div>
                                <div className="action-content">
                                    <h4>Edit Profile</h4>
                                    <p>Update your personal information</p>
                                </div>
                                <div className="action-arrow">→</div>
                            </Link>
                            
                            <div className="action-card">
                                <div className="action-icon">🎥</div>
                                <div className="action-content">
                                    <h4>Resources</h4>
                                    <p>Access study materials and videos</p>
                                </div>
                                <div className="action-arrow">→</div>
                            </div>
                        </div>
                    </section>

                    {/* Profile Info */}
                    <section className="profile-section">
                        <h3>Account Information</h3>
                        <div className="profile-info">
                            <div className="info-item">
                                <span className="info-label">Email:</span>
                                <span className="info-value">{user.email}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Phone:</span>
                                <span className="info-value">{user.phone ? `${user.countryCode} ${user.phone}` : 'Not provided'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Role:</span>
                                <span className="info-value">{user.role || 'User'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Account ID:</span>
                                <span className="info-value">{user.id || 'N/A'}</span>
                            </div>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    );
};

export default UserDashboard;