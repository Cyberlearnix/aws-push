import React, { useState, useEffect } from 'react';
import apiService from '../services/api';

const AdminDashboard = ({ onLogout }) => {
    const [currentView, setCurrentView] = useState('dashboard'); // 'dashboard', 'users', 'analytics', 'settings'
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showUserDetailsModal, setShowUserDetailsModal] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [searchTerm, setSearchTerm] = useState('');
    const [adminStats, setAdminStats] = useState({
        totalUsers: 0,
        activeUsers: 0,
        inactiveUsers: 0,
        newUsersThisMonth: 0,
        totalLogins: 0,
        avgSessionTime: '0 min'
    });
    const [recentActivities, setRecentActivities] = useState([]);

    useEffect(() => {
        loadDashboardData();
    }, []);

    useEffect(() => {
        if (currentView === 'users') {
            loadUsers();
        }
    }, [currentPage, currentView]);

    const loadDashboardData = async () => {
        setLoading(true);
        try {
            await Promise.all([
                loadAdminStats(),
                loadRecentActivities(),
                loadUsers()
            ]);
        } catch (error) {
            console.error('Failed to load dashboard data:', error);
            setError('Failed to load dashboard data: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const loadAdminStats = async () => {
        try {
            const data = await apiService.getAllUsers(1, 1000); // Get all users for stats
            const users = data.users || [];
            
            const activeUsers = users.filter(u => u.status === 'active' || !u.status).length;
            const inactiveUsers = users.filter(u => u.status === 'inactive').length;
            const totalUsers = users.length;
            
            // Calculate new users this month (mock calculation)
            const currentMonth = new Date().getMonth();
            const newUsersThisMonth = users.filter(u => {
                if (!u.createdAt) return false;
                return new Date(u.createdAt).getMonth() === currentMonth;
            }).length;
            
            setAdminStats({
                totalUsers,
                activeUsers,
                inactiveUsers,
                newUsersThisMonth,
                totalLogins: Math.floor(totalUsers * 15.5), // Mock data
                avgSessionTime: '24 min' // Mock data
            });
        } catch (error) {
            console.error('Failed to load admin stats:', error);
        }
    };

    const loadRecentActivities = async () => {
        // Mock recent activities data
        setRecentActivities([
            {
                id: 1,
                type: 'user_created',
                message: 'New user registered: john.doe@email.com',
                timestamp: new Date(Date.now() - 5 * 60000),
                icon: '👤'
            },
            {
                id: 2,
                type: 'user_login',
                message: 'User login: jane.smith@email.com',
                timestamp: new Date(Date.now() - 15 * 60000),
                icon: '🔑'
            },
            {
                id: 3,
                type: 'admin_action',
                message: 'User account deactivated by admin',
                timestamp: new Date(Date.now() - 30 * 60000),
                icon: '⚠️'
            },
            {
                id: 4,
                type: 'system',
                message: 'Database backup completed successfully',
                timestamp: new Date(Date.now() - 60 * 60000),
                icon: '💾'
            },
            {
                id: 5,
                type: 'user_updated',
                message: 'User profile updated: mike.wilson@email.com',
                timestamp: new Date(Date.now() - 90 * 60000),
                icon: '✏️'
            }
        ]);
    };

    const loadUsers = async () => {
        try {
            setLoading(currentView === 'users');
            const data = await apiService.getAllUsers(currentPage, 10);
            setUsers(data.users || []);
            setTotalPages(data.totalPages || 1);
            setError(null);
        } catch (error) {
            console.error('Failed to load users:', error);
            setError('Failed to load users: ' + error.message);
        } finally {
            if (currentView === 'users') {
                setLoading(false);
            }
        }
    };

    const handleDeleteUser = async (userId) => {
        if (!window.confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
            return;
        }

        try {
            await apiService.deleteUser(userId);
            setUsers(users.filter(user => user.id !== userId));
        } catch (error) {
            console.error('Failed to delete user:', error);
            setError('Failed to delete user: ' + error.message);
        }
    };

    const handleToggleUserStatus = async (userId, currentStatus) => {
        try {
            if (currentStatus === 'active') {
                await apiService.deactivateUser(userId);
                addActivity(`User account deactivated: ${users.find(u => u.id === userId)?.email}`, 'admin_action', '⏸️');
            } else {
                await apiService.activateUser(userId);
                addActivity(`User account activated: ${users.find(u => u.id === userId)?.email}`, 'admin_action', '▶️');
            }
            
            // Refresh users list and stats
            loadUsers();
            loadAdminStats();
        } catch (error) {
            console.error('Failed to toggle user status:', error);
            setError('Failed to update user status: ' + error.message);
        }
    };

    const handleBulkDeactivate = async (userIds) => {
        if (!window.confirm(`Are you sure you want to deactivate ${userIds.length} users?`)) {
            return;
        }
        
        try {
            await Promise.all(userIds.map(id => apiService.deactivateUser(id)));
            addActivity(`Bulk deactivated ${userIds.length} users`, 'admin_action', '⏸️');
            loadUsers();
            loadAdminStats();
        } catch (error) {
            console.error('Failed to bulk deactivate users:', error);
            setError('Failed to bulk deactivate users: ' + error.message);
        }
    };

    const handleExportUsers = async () => {
        try {
            // Mock export functionality
            const csvData = users.map(user => ({
                ID: user.id,
                Name: user.fullName || 'N/A',
                Email: user.email,
                Phone: user.phone ? `${user.countryCode || ''} ${user.phone}` : 'N/A',
                Status: user.status || 'active',
                Created: user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'
            }));
            
            const csvString = [
                Object.keys(csvData[0]).join(','),
                ...csvData.map(row => Object.values(row).join(','))
            ].join('\n');
            
            const blob = new Blob([csvString], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `users-export-${new Date().toISOString().split('T')[0]}.csv`;
            a.click();
            
            addActivity('User data exported to CSV', 'system', '📊');
        } catch (error) {
            console.error('Failed to export users:', error);
            setError('Failed to export users: ' + error.message);
        }
    };

    const addActivity = (message, type, icon) => {
        const newActivity = {
            id: Date.now(),
            type,
            message,
            timestamp: new Date(),
            icon
        };
        setRecentActivities(prev => [newActivity, ...prev.slice(0, 4)]);
    };

    const formatTimeAgo = (timestamp) => {
        const diff = Date.now() - new Date(timestamp).getTime();
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);
        
        if (days > 0) return `${days}d ago`;
        if (hours > 0) return `${hours}h ago`;
        if (minutes > 0) return `${minutes}m ago`;
        return 'Just now';
    };

    const filteredUsers = users.filter(user =>
        user.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (loading) {
        return (
            <div className="admin-loading">
                <div className="loading-spinner"></div>
                <p>Loading admin dashboard...</p>
            </div>
        );
    }

    return (
        <div className="admin-dashboard">
            {/* Header */}
            <header className="admin-header">
                <div className="header-content">
                    <div className="admin-title">
                        <h1>Admin Dashboard</h1>
                        <p>Complete System Management</p>
                    </div>
                    
                    <div className="header-actions">
                        <button 
                            className="btn-primary"
                            onClick={() => setShowCreateModal(true)}
                        >
                            ➕ Create User
                        </button>
                        <button 
                            className="btn-secondary"
                            onClick={handleExportUsers}
                        >
                            📊 Export Data
                        </button>
                        <button 
                            className="btn-secondary"
                            onClick={onLogout}
                        >
                            🚪 Logout
                        </button>
                    </div>
                </div>
            </header>

            {/* Navigation */}
            <nav className="admin-nav">
                <div className="nav-container">
                    <button 
                        className={`nav-item ${currentView === 'dashboard' ? 'active' : ''}`}
                        onClick={() => setCurrentView('dashboard')}
                    >
                        <span className="nav-icon">🏠</span>
                        Dashboard
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'users' ? 'active' : ''}`}
                        onClick={() => setCurrentView('users')}
                    >
                        <span className="nav-icon">👥</span>
                        User Management
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'analytics' ? 'active' : ''}`}
                        onClick={() => setCurrentView('analytics')}
                    >
                        <span className="nav-icon">📈</span>
                        Analytics
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'settings' ? 'active' : ''}`}
                        onClick={() => setCurrentView('settings')}
                    >
                        <span className="nav-icon">⚙️</span>
                        System Settings
                    </button>
                </div>
            </nav>

            <main className="admin-main">
                <div className="admin-container">
                    {error && (
                        <div className="error-banner">
                            <span>⚠️</span>
                            {error}
                            <button onClick={() => setError(null)}>✕</button>
                        </div>
                    )}

                    {/* Dashboard Overview */}
                    {currentView === 'dashboard' && (
                        <div className="dashboard-overview">
                            <div className="welcome-section">
                                <h2>Welcome back, Admin! 👋</h2>
                                <p>Here's your system overview and recent activities.</p>
                            </div>

                            {/* Statistics Cards */}
                            <div className="stats-grid">
                                <div className="stat-card primary">
                                    <div className="stat-icon">👥</div>
                                    <div className="stat-content">
                                        <h3>Total Users</h3>
                                        <p className="stat-value">{adminStats.totalUsers}</p>
                                        <p className="stat-description">Registered users</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card success">
                                    <div className="stat-icon">✅</div>
                                    <div className="stat-content">
                                        <h3>Active Users</h3>
                                        <p className="stat-value">{adminStats.activeUsers}</p>
                                        <p className="stat-description">Currently active</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card warning">
                                    <div className="stat-icon">⏸️</div>
                                    <div className="stat-content">
                                        <h3>Inactive Users</h3>
                                        <p className="stat-value">{adminStats.inactiveUsers}</p>
                                        <p className="stat-description">Deactivated accounts</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card info">
                                    <div className="stat-icon">🎆</div>
                                    <div className="stat-content">
                                        <h3>New This Month</h3>
                                        <p className="stat-value">{adminStats.newUsersThisMonth}</p>
                                        <p className="stat-description">New registrations</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card accent">
                                    <div className="stat-icon">🔑</div>
                                    <div className="stat-content">
                                        <h3>Total Logins</h3>
                                        <p className="stat-value">{adminStats.totalLogins.toLocaleString()}</p>
                                        <p className="stat-description">All-time logins</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card secondary">
                                    <div className="stat-icon">⏱️</div>
                                    <div className="stat-content">
                                        <h3>Avg. Session</h3>
                                        <p className="stat-value">{adminStats.avgSessionTime}</p>
                                        <p className="stat-description">Average session time</p>
                                    </div>
                                </div>
                            </div>

                            {/* Quick Actions */}
                            <div className="quick-actions-section">
                                <h3>Quick Actions</h3>
                                <div className="actions-grid">
                                    <button 
                                        className="action-card"
                                        onClick={() => setCurrentView('users')}
                                    >
                                        <div className="action-icon">👥</div>
                                        <div className="action-content">
                                            <h4>Manage Users</h4>
                                            <p>View, edit, and manage user accounts</p>
                                        </div>
                                        <div className="action-arrow">→</div>
                                    </button>
                                    
                                    <button 
                                        className="action-card"
                                        onClick={() => setShowCreateModal(true)}
                                    >
                                        <div className="action-icon">➕</div>
                                        <div className="action-content">
                                            <h4>Create User</h4>
                                            <p>Add new user account manually</p>
                                        </div>
                                        <div className="action-arrow">→</div>
                                    </button>
                                    
                                    <button 
                                        className="action-card"
                                        onClick={handleExportUsers}
                                    >
                                        <div className="action-icon">📊</div>
                                        <div className="action-content">
                                            <h4>Export Data</h4>
                                            <p>Download user data as CSV</p>
                                        </div>
                                        <div className="action-arrow">→</div>
                                    </button>
                                    
                                    <button 
                                        className="action-card"
                                        onClick={() => setCurrentView('analytics')}
                                    >
                                        <div className="action-icon">📈</div>
                                        <div className="action-content">
                                            <h4>View Analytics</h4>
                                            <p>System performance and insights</p>
                                        </div>
                                        <div className="action-arrow">→</div>
                                    </button>
                                    
                                    <button 
                                        className="action-card"
                                        onClick={() => loadDashboardData()}
                                    >
                                        <div className="action-icon">🔄</div>
                                        <div className="action-content">
                                            <h4>Refresh Data</h4>
                                            <p>Update dashboard statistics</p>
                                        </div>
                                        <div className="action-arrow">→</div>
                                    </button>
                                    
                                    <button 
                                        className="action-card"
                                        onClick={() => setCurrentView('settings')}
                                    >
                                        <div className="action-icon">⚙️</div>
                                        <div className="action-content">
                                            <h4>System Settings</h4>
                                            <p>Configure system preferences</p>
                                        </div>
                                        <div className="action-arrow">→</div>
                                    </button>
                                </div>
                            </div>

                            {/* Recent Activities */}
                            <div className="recent-activities-section">
                                <h3>Recent Activities</h3>
                                <div className="activities-list">
                                    {recentActivities.map(activity => (
                                        <div key={activity.id} className="activity-item">
                                            <div className="activity-icon">{activity.icon}</div>
                                            <div className="activity-content">
                                                <p className="activity-message">{activity.message}</p>
                                                <span className="activity-time">{formatTimeAgo(activity.timestamp)}</span>
                                            </div>
                                            <div className={`activity-type ${activity.type}`}></div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* User Management View */}
                    {currentView === 'users' && (
                        <div className="user-management">
                            <div className="section-header">
                                <h2>User Management</h2>
                                <p>Manage all user accounts and permissions</p>
                            </div>
                            
                            {/* Search and Filters */}
                            <div className="admin-controls">
                                <div className="search-container">
                                    <input
                                        type="text"
                                        placeholder="Search users by name or email..."
                                        value={searchTerm}
                                        onChange={(e) => setSearchTerm(e.target.value)}
                                        className="search-input"
                                    />
                                    <div className="search-icon">🔍</div>
                                </div>
                                
                                <div className="admin-actions">
                                    <button 
                                        className="btn-primary"
                                        onClick={() => setShowCreateModal(true)}
                                    >
                                        ➕ Add User
                                    </button>
                                    <button 
                                        className="btn-secondary"
                                        onClick={handleExportUsers}
                                    >
                                        📊 Export
                                    </button>
                                    <button 
                                        className="btn-secondary"
                                        onClick={loadUsers}
                                    >
                                        🔄 Refresh
                                    </button>
                                </div>
                            </div>
                            
                            <div className="user-stats">
                                <div className="stat-item">
                                    <span className="stat-label">Total Users:</span>
                                    <span className="stat-value">{users.length}</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-label">Active:</span>
                                    <span className="stat-value">
                                        {users.filter(u => u.status === 'active' || !u.status).length}
                                    </span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-label">Inactive:</span>
                                    <span className="stat-value">
                                        {users.filter(u => u.status === 'inactive').length}
                                    </span>
                                </div>
                            </div>

                            {/* Users Table */}
                            <div className="users-table-container">
                                <table className="users-table">
                                    <thead>
                                        <tr>
                                            <th>User</th>
                                            <th>Email</th>
                                            <th>Phone</th>
                                            <th>Status</th>
                                            <th>Created</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredUsers.map(user => (
                                            <tr key={user.id} className={user.status === 'inactive' ? 'inactive-user' : ''}>
                                                <td>
                                                    <div className="user-info">
                                                        <div className="user-avatar">
                                                            {user.photo ? (
                                                                <img src={user.photo} alt={user.fullName} />
                                                            ) : (
                                                                <span>{user.fullName?.charAt(0) || '?'}</span>
                                                            )}
                                                        </div>
                                                        <div className="user-details">
                                                            <span className="user-name">{user.fullName || 'N/A'}</span>
                                                            <span className="user-id">ID: {user.id}</span>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>{user.email}</td>
                                                <td>
                                                    {user.phone ? `${user.countryCode || ''} ${user.phone}` : 'N/A'}
                                                </td>
                                                <td>
                                                    <span className={`status-badge ${user.status || 'active'}`}>
                                                        {user.status || 'active'}
                                                    </span>
                                                </td>
                                                <td>
                                                    {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                                                </td>
                                                <td>
                                                    <div className="action-buttons">
                                                        <button
                                                            className="btn-icon"
                                                            onClick={() => {
                                                                setSelectedUser(user);
                                                                setShowUserDetailsModal(true);
                                                            }}
                                                            title="View user details"
                                                        >
                                                            👁️
                                                        </button>
                                                        <button
                                                            className="btn-icon"
                                                            onClick={() => {
                                                                setSelectedUser(user);
                                                                setShowEditModal(true);
                                                            }}
                                                            title="Edit user"
                                                        >
                                                            ✏️
                                                        </button>
                                                        <button
                                                            className={`btn-icon ${user.status === 'active' ? 'deactivate' : 'activate'}`}
                                                            onClick={() => handleToggleUserStatus(user.id, user.status)}
                                                            title={user.status === 'active' ? 'Deactivate user' : 'Activate user'}
                                                        >
                                                            {user.status === 'active' ? '⏸️' : '▶️'}
                                                        </button>
                                                        <button
                                                            className="btn-icon delete"
                                                            onClick={() => handleDeleteUser(user.id)}
                                                            title="Delete user"
                                                        >
                                                            🗑️
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>

                                {filteredUsers.length === 0 && (
                                    <div className="empty-state">
                                        <p>No users found matching your search criteria.</p>
                                    </div>
                                )}

                                {/* Pagination */}
                                {totalPages > 1 && (
                                    <div className="pagination">
                                        <button
                                            className="pagination-btn"
                                            disabled={currentPage === 1}
                                            onClick={() => setCurrentPage(currentPage - 1)}
                                        >
                                            ← Previous
                                        </button>
                                        
                                        <span className="pagination-info">
                                            Page {currentPage} of {totalPages}
                                        </span>
                                        
                                        <button
                                            className="pagination-btn"
                                            disabled={currentPage === totalPages}
                                            onClick={() => setCurrentPage(currentPage + 1)}
                                        >
                                            Next →
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Analytics View */}
                    {currentView === 'analytics' && (
                        <div className="analytics-view">
                            <div className="section-header">
                                <h2>System Analytics</h2>
                                <p>Detailed insights and performance metrics</p>
                            </div>
                            
                            <div className="analytics-grid">
                                <div className="analytics-card">
                                    <h3>User Growth</h3>
                                    <div className="chart-placeholder">
                                        <div className="chart-bar" style={{height: '60%'}}></div>
                                        <div className="chart-bar" style={{height: '75%'}}></div>
                                        <div className="chart-bar" style={{height: '45%'}}></div>
                                        <div className="chart-bar" style={{height: '90%'}}></div>
                                        <div className="chart-bar" style={{height: '70%'}}></div>
                                        <div className="chart-bar" style={{height: '85%'}}></div>
                                    </div>
                                    <p>Monthly user registration trend</p>
                                </div>
                                
                                <div className="analytics-card">
                                    <h3>Login Activity</h3>
                                    <div className="metric-display">
                                        <span className="metric-value">87%</span>
                                        <span className="metric-label">Active Users (30 days)</span>
                                    </div>
                                    <div className="metric-display">
                                        <span className="metric-value">{adminStats.avgSessionTime}</span>
                                        <span className="metric-label">Average Session Time</span>
                                    </div>
                                </div>
                                
                                <div className="analytics-card">
                                    <h3>System Performance</h3>
                                    <div className="performance-metrics">
                                        <div className="metric-row">
                                            <span>API Response Time</span>
                                            <span className="metric-good">125ms</span>
                                        </div>
                                        <div className="metric-row">
                                            <span>Database Queries</span>
                                            <span className="metric-good">45ms avg</span>
                                        </div>
                                        <div className="metric-row">
                                            <span>System Uptime</span>
                                            <span className="metric-excellent">99.9%</span>
                                        </div>
                                        <div className="metric-row">
                                            <span>Error Rate</span>
                                            <span className="metric-excellent">0.02%</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div className="analytics-card">
                                    <h3>Recent Trends</h3>
                                    <div className="trend-list">
                                        <div className="trend-item">
                                            <span className="trend-indicator up">↗</span>
                                            <span>User registrations up 12% this week</span>
                                        </div>
                                        <div className="trend-item">
                                            <span className="trend-indicator down">↘</span>
                                            <span>Login failures down 23% this month</span>
                                        </div>
                                        <div className="trend-item">
                                            <span className="trend-indicator up">↗</span>
                                            <span>Average session time increased</span>
                                        </div>
                                        <div className="trend-item">
                                            <span className="trend-indicator stable">→</span>
                                            <span>System performance stable</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* System Settings View */}
                    {currentView === 'settings' && (
                        <div className="settings-view">
                            <div className="section-header">
                                <h2>System Settings</h2>
                                <p>Configure system preferences and security settings</p>
                            </div>
                            
                            <div className="settings-grid">
                                <div className="settings-card">
                                    <h3>🔐 Security Settings</h3>
                                    <div className="setting-item">
                                        <label>Password Minimum Length</label>
                                        <input type="number" value="6" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>Session Timeout (minutes)</label>
                                        <input type="number" value="30" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>Maximum Login Attempts</label>
                                        <input type="number" value="5" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>
                                            <input type="checkbox" checked readOnly />
                                            Enable Two-Factor Authentication
                                        </label>
                                    </div>
                                </div>
                                
                                <div className="settings-card">
                                    <h3>📧 Email Settings</h3>
                                    <div className="setting-item">
                                        <label>SMTP Server</label>
                                        <input type="text" value="smtp.gmail.com" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>From Email</label>
                                        <input type="email" value="noreply@userservice.com" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>
                                            <input type="checkbox" checked readOnly />
                                            Email Verification Required
                                        </label>
                                    </div>
                                    <div className="setting-item">
                                        <label>
                                            <input type="checkbox" checked readOnly />
                                            Send Welcome Emails
                                        </label>
                                    </div>
                                </div>
                                
                                <div className="settings-card">
                                    <h3>🛠️ System Maintenance</h3>
                                    <div className="maintenance-actions">
                                        <button className="btn-secondary">🔄 Clear Cache</button>
                                        <button className="btn-secondary">📊 Generate Report</button>
                                        <button className="btn-secondary">💾 Backup Database</button>
                                        <button className="btn-secondary">🧹 Clean Logs</button>
                                        <button className="btn-secondary">📈 Update Statistics</button>
                                        <button className="btn-secondary">🔍 System Check</button>
                                    </div>
                                </div>
                                
                                <div className="settings-card">
                                    <h3>⚡ Performance Settings</h3>
                                    <div className="setting-item">
                                        <label>API Rate Limit (per minute)</label>
                                        <input type="number" value="100" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>Cache Duration (hours)</label>
                                        <input type="number" value="24" readOnly />
                                    </div>
                                    <div className="setting-item">
                                        <label>
                                            <input type="checkbox" checked readOnly />
                                            Enable Compression
                                        </label>
                                    </div>
                                    <div className="setting-item">
                                        <label>
                                            <input type="checkbox" checked readOnly />
                                            Enable CDN
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </main>

            {/* User Details Modal */}
            {showUserDetailsModal && selectedUser && (
                <UserDetailsModal
                    user={selectedUser}
                    onClose={() => {
                        setShowUserDetailsModal(false);
                        setSelectedUser(null);
                    }}
                />
            )}

            {/* Create/Edit Modals */}
            {showCreateModal && (
                <CreateUserModal
                    onClose={() => setShowCreateModal(false)}
                    onUserCreated={loadUsers}
                />
            )}
            
            {showEditModal && selectedUser && (
                <EditUserModal
                    user={selectedUser}
                    onClose={() => {
                        setShowEditModal(false);
                        setSelectedUser(null);
                    }}
                    onUserUpdated={loadUsers}
                />
            )}
        </div>
    );
};

// User Details Modal Component
const UserDetailsModal = ({ user, onClose }) => {
    return (
        <div className="modal-overlay">
            <div className="modal user-details-modal">
                <div className="modal-header">
                    <h3>User Details</h3>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>
                
                <div className="modal-content">
                    <div className="user-profile-section">
                        <div className="user-avatar-large">
                            {user.photo ? (
                                <img src={user.photo} alt={user.fullName} />
                            ) : (
                                <span>{user.fullName?.charAt(0) || '?'}</span>
                            )}
                        </div>
                        <div className="user-basic-info">
                            <h4>{user.fullName || 'N/A'}</h4>
                            <p>{user.email}</p>
                            <span className={`status-badge ${user.status || 'active'}`}>
                                {user.status || 'active'}
                            </span>
                        </div>
                    </div>
                    
                    <div className="user-details-grid">
                        <div className="detail-card">
                            <h5>Contact Information</h5>
                            <div className="detail-item">
                                <span className="detail-label">Email:</span>
                                <span className="detail-value">{user.email}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Phone:</span>
                                <span className="detail-value">
                                    {user.phone ? `${user.countryCode || ''} ${user.phone}` : 'Not provided'}
                                </span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Country Code:</span>
                                <span className="detail-value">{user.countryCode || 'Not provided'}</span>
                            </div>
                        </div>
                        
                        <div className="detail-card">
                            <h5>Account Information</h5>
                            <div className="detail-item">
                                <span className="detail-label">User ID:</span>
                                <span className="detail-value">{user.id}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Role:</span>
                                <span className="detail-value">{user.role || 'User'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Status:</span>
                                <span className="detail-value">{user.status || 'Active'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Created:</span>
                                <span className="detail-value">
                                    {user.createdAt ? new Date(user.createdAt).toLocaleString() : 'N/A'}
                                </span>
                            </div>
                        </div>
                        
                        <div className="detail-card">
                            <h5>Additional Information</h5>
                            <div className="detail-item">
                                <span className="detail-label">Address:</span>
                                <span className="detail-value">{user.address || 'Not provided'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Biography:</span>
                                <span className="detail-value">{user.biography || 'Not provided'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Language:</span>
                                <span className="detail-value">{user.language || 'Not provided'}</span>
                            </div>
                        </div>
                        
                        <div className="detail-card">
                            <h5>Social Links</h5>
                            <div className="detail-item">
                                <span className="detail-label">LinkedIn:</span>
                                <span className="detail-value">
                                    {user.linkedinUrl ? (
                                        <a href={user.linkedinUrl} target="_blank" rel="noopener noreferrer">
                                            View Profile
                                        </a>
                                    ) : 'Not provided'}
                                </span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Instagram:</span>
                                <span className="detail-value">{user.instagramUrl || 'Not provided'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Facebook:</span>
                                <span className="detail-value">{user.facebookUrl || 'Not provided'}</span>
                            </div>
                            <div className="detail-item">
                                <span className="detail-label">Internshala:</span>
                                <span className="detail-value">{user.internshalaUrl || 'Not provided'}</span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div className="modal-actions">
                    <button type="button" className="btn-secondary" onClick={onClose}>
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};

// Create User Modal Component
const CreateUserModal = ({ onClose, onUserCreated }) => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        countryCode: '+91',
        phone: '',
        password: '',
        role: 'user'
    });
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // Basic validation
        const newErrors = {};
        if (!formData.fullName.trim()) newErrors.fullName = 'Name is required';
        if (!formData.email.trim()) newErrors.email = 'Email is required';
        if (!formData.password) newErrors.password = 'Password is required';
        
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }
        
        setLoading(true);
        try {
            await apiService.createUser(formData);
            onUserCreated();
            onClose();
        } catch (error) {
            console.error('Failed to create user:', error);
            setErrors({ general: error.message });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Create New User</h3>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>
                
                <form onSubmit={handleSubmit} className="modal-form">
                    <div className="form-group">
                        <label>Full Name</label>
                        <input
                            type="text"
                            value={formData.fullName}
                            onChange={(e) => setFormData({...formData, fullName: e.target.value})}
                            className={errors.fullName ? 'error' : ''}
                        />
                        {errors.fullName && <span className="error-text">{errors.fullName}</span>}
                    </div>
                    
                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({...formData, email: e.target.value})}
                            className={errors.email ? 'error' : ''}
                        />
                        {errors.email && <span className="error-text">{errors.email}</span>}
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label>Country Code</label>
                            <select
                                value={formData.countryCode}
                                onChange={(e) => setFormData({...formData, countryCode: e.target.value})}
                            >
                                <option value="+91">+91 (India)</option>
                                <option value="+1">+1 (US/Canada)</option>
                                <option value="+44">+44 (UK)</option>
                            </select>
                        </div>
                        
                        <div className="form-group">
                            <label>Phone</label>
                            <input
                                type="tel"
                                value={formData.phone}
                                onChange={(e) => setFormData({...formData, phone: e.target.value})}
                            />
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={formData.password}
                            onChange={(e) => setFormData({...formData, password: e.target.value})}
                            className={errors.password ? 'error' : ''}
                        />
                        {errors.password && <span className="error-text">{errors.password}</span>}
                    </div>
                    
                    <div className="form-group">
                        <label>Role</label>
                        <select
                            value={formData.role}
                            onChange={(e) => setFormData({...formData, role: e.target.value})}
                        >
                            <option value="user">User</option>
                            <option value="admin">Admin</option>
                        </select>
                    </div>
                    
                    {errors.general && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.general}
                        </div>
                    )}
                    
                    <div className="modal-actions">
                        <button type="button" className="btn-secondary" onClick={onClose}>
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className={`btn-primary ${loading ? 'loading' : ''}`}
                            disabled={loading}
                        >
                            {loading ? 'Creating...' : 'Create User'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

// Edit User Modal Component (simplified)
const EditUserModal = ({ user, onClose, onUserUpdated }) => {
    const [formData, setFormData] = useState({
        fullName: user.fullName || '',
        phone: user.phone || '',
        countryCode: user.countryCode || '+91',
        role: user.role || 'user'
    });
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        setLoading(true);
        try {
            await apiService.updateUser(user.id, formData);
            onUserUpdated();
            onClose();
        } catch (error) {
            console.error('Failed to update user:', error);
            setErrors({ general: error.message });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header">
                    <h3>Edit User</h3>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>
                
                <form onSubmit={handleSubmit} className="modal-form">
                    <div className="form-group">
                        <label>Full Name</label>
                        <input
                            type="text"
                            value={formData.fullName}
                            onChange={(e) => setFormData({...formData, fullName: e.target.value})}
                        />
                    </div>
                    
                    <div className="form-row">
                        <div className="form-group">
                            <label>Country Code</label>
                            <select
                                value={formData.countryCode}
                                onChange={(e) => setFormData({...formData, countryCode: e.target.value})}
                            >
                                <option value="+91">+91 (India)</option>
                                <option value="+1">+1 (US/Canada)</option>
                                <option value="+44">+44 (UK)</option>
                            </select>
                        </div>
                        
                        <div className="form-group">
                            <label>Phone</label>
                            <input
                                type="tel"
                                value={formData.phone}
                                onChange={(e) => setFormData({...formData, phone: e.target.value})}
                            />
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label>Role</label>
                        <select
                            value={formData.role}
                            onChange={(e) => setFormData({...formData, role: e.target.value})}
                        >
                            <option value="user">User</option>
                            <option value="admin">Admin</option>
                        </select>
                    </div>
                    
                    {errors.general && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.general}
                        </div>
                    )}
                    
                    <div className="modal-actions">
                        <button type="button" className="btn-secondary" onClick={onClose}>
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className={`btn-primary ${loading ? 'loading' : ''}`}
                            disabled={loading}
                        >
                            {loading ? 'Updating...' : 'Update User'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AdminDashboard;