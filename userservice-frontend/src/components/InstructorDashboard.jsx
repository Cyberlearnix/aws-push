import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import instructorApi from '../services/instructorApi';
import '../styles/InstructorDashboard.css';

const InstructorDashboard = ({ user, onLogout }) => {
    const navigate = useNavigate();
    const [currentView, setCurrentView] = useState('dashboard');
    const [students, setStudents] = useState([]);
    const [courses, setCourses] = useState([]);
    const [selectedCourseId, setSelectedCourseId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [instructorId, setInstructorId] = useState(null);
    const [instructorStats, setInstructorStats] = useState({
        totalStudents: 0,
        activeCourses: 0,
        completedAssignments: 0,
        avgGrade: 0,
        totalEarnings: 0,
    });
    const [createCourseForm, setCreateCourseForm] = useState({
        title: '',
        description: '',
        published: true,
        submitting: false,
    });
    const [editCourseId, setEditCourseId] = useState(null);
    const [editCourseForm, setEditCourseForm] = useState({
        title: '',
        description: '',
        published: true,
        submitting: false,
    });

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const id = decodeSubFromToken(token);
        setInstructorId(id);
        if (!id) {
            setError('Could not determine instructor ID from token');
            setLoading(false);
            return;
        }
        loadInstructorData(id);
    }, []);

    // When selected course changes and we're on students view, reload students
    useEffect(() => {
        if (currentView === 'students' && instructorId && selectedCourseId) {
            loadStudents(instructorId, selectedCourseId);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedCourseId, currentView]);

    const loadInstructorData = async (id) => {
        setLoading(true);
        try {
            await Promise.all([
                loadCourses(id),
                loadInstructorStats(id),
            ]);
            // After courses are loaded, load students for the first course (if any)
            const firstCourseId = (courses && courses[0]?.id) || null;
            if (firstCourseId) {
                setSelectedCourseId(firstCourseId);
                await loadStudents(id, firstCourseId);
            }
        } catch (error) {
            console.error('Failed to load instructor data:', error);
            setError('Failed to load instructor data: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const loadStudents = async (id, courseId) => {
        if (!courseId) { setStudents([]); return; }
        try {
            const data = await instructorApi.listStudents(id, courseId);
            // Map to UI-friendly fields
            const mapped = data.map(s => ({
                id: s.studentId,
                fullName: s.name,
                email: s.email,
                course: `Course #${courseId}`,
                progress: Math.round(s.progressPercent || 0),
                lastActivity: '-',
                grade: '-',
            }));
            setStudents(mapped);
        } catch (error) {
            console.error('Failed to load students:', error);
        }
    };

    const loadCourses = async (id) => {
        try {
            const list = await instructorApi.listCourses(id);
            const mapped = list.map(c => ({
                id: c.id,
                name: c.title,
                students: 0,
                status: c.published ? 'Active' : 'Draft',
                startDate: new Date().toISOString(),
                endDate: new Date().toISOString(),
            }));
            setCourses(mapped);
            if (!selectedCourseId && mapped.length > 0) {
                setSelectedCourseId(mapped[0].id);
            }
        } catch (error) {
            console.error('Failed to load courses:', error);
        }
    };

    const beginEditCourse = (course) => {
        setEditCourseId(course.id);
        setEditCourseForm({
            title: course.name,
            description: '',
            published: course.status === 'Active',
            submitting: false,
        });
    };

    const submitEditCourse = async () => {
        if (!instructorId || !editCourseId) return;
        try {
            setEditCourseForm((f) => ({ ...f, submitting: true }));
            await instructorApi.updateCourse(instructorId, editCourseId, {
                title: editCourseForm.title,
                description: editCourseForm.description,
                published: !!editCourseForm.published,
            });
            await loadCourses(instructorId);
            await loadInstructorStats(instructorId);
            setEditCourseId(null);
            setEditCourseForm({ title: '', description: '', published: true, submitting: false });
        } catch (err) {
            setError(err.message || 'Failed to update course');
            setEditCourseForm((f) => ({ ...f, submitting: false }));
        }
    };

    const deleteCourseAction = async (courseId) => {
        if (!instructorId) return;
        const ok = window.confirm('Delete this course? This action cannot be undone.');
        if (!ok) return;
        try {
            await instructorApi.deleteCourse(instructorId, courseId);
            await loadCourses(instructorId);
            await loadInstructorStats(instructorId);
            if (selectedCourseId === courseId) setSelectedCourseId(null);
        } catch (err) {
            setError(err.message || 'Failed to delete course');
        }
    };

    const loadInstructorStats = async (id) => {
        try {
            const dash = await instructorApi.getDashboard(id);
            const earn = await instructorApi.getEarnings(id);
            setInstructorStats({
                totalStudents: dash.totalStudents,
                activeCourses: dash.totalCourses,
                completedAssignments: 0,
                avgGrade: Math.round(dash.averageRating * 10) / 10,
                totalEarnings: earn.totalEarnings,
            });
        } catch (error) {
            console.error('Failed to load instructor stats:', error);
        }
    };

    const decodeSubFromToken = (token) => {
        try {
            if (!token) return null;
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.sub || null;
        } catch {
            return null;
        }
    };

    if (loading) {
        return (
            <div className="instructor-loading">
                <div className="loading-spinner"></div>
                <p>Loading instructor dashboard...</p>
            </div>
        );
    }

    return (
        <div className="instructor-dashboard">
            {/* Header */}
            <header className="instructor-header">
                <div className="header-content">
                    <div className="instructor-title">
                        <h1>Instructor Dashboard</h1>
                        <p>Manage your courses and students</p>
                    </div>
                    
                    <div className="header-actions">
                        <button 
                            className="btn-primary"
                            onClick={() => setCurrentView('create-course')}
                        >
                            📚 Create Course
                        </button>
                        <button 
                            className="btn-secondary"
                            onClick={() => setCurrentView('assignments')}
                        >
                            📝 Assignments
                        </button>
                        <button 
                            className="btn-secondary"
                            onClick={() => { try { onLogout?.(); } finally { navigate('/'); } }}
                        >
                            🚪 Logout
                        </button>
                    </div>
                </div>
            </header>

            {/* Navigation */}
            <nav className="instructor-nav">
                <div className="nav-container">
                    <button 
                        className={`nav-item ${currentView === 'dashboard' ? 'active' : ''}`}
                        onClick={() => setCurrentView('dashboard')}
                    >
                        <span className="nav-icon">🏠</span>
                        Dashboard
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'students' ? 'active' : ''}`}
                        onClick={() => setCurrentView('students')}
                    >
                        <span className="nav-icon">👨‍🎓</span>
                        My Students
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'courses' ? 'active' : ''}`}
                        onClick={() => setCurrentView('courses')}
                    >
                        <span className="nav-icon">📚</span>
                        My Courses
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'grades' ? 'active' : ''}`}
                        onClick={() => setCurrentView('grades')}
                    >
                        <span className="nav-icon">📊</span>
                        Grades & Analytics
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'modules' ? 'active' : ''}`}
                        onClick={() => setCurrentView('modules')}
                    >
                        <span className="nav-icon">🧩</span>
                        Modules
                    </button>
                    <button 
                        className={`nav-item ${currentView === 'communication' ? 'active' : ''}`}
                        onClick={() => setCurrentView('communication')}
                    >
                        <span className="nav-icon">💬</span>
                        Communication
                    </button>
                </div>
            </nav>
            <main className="instructor-main">
                <div className="instructor-container">
                    {error && (
                        <div className="error-banner">
                            <span>⚠️</span>
                            {error}
                            <button onClick={() => setError(null)}>✕</button>
                        </div>
                    )}

                    {/* Modules View */}
                    {currentView === 'modules' && (
                        <div className="modules-view">
                            <div className="section-header">
                                <h2>Course Modules</h2>
                                <p>Add, update or delete modules for a course.</p>
                            </div>

                            <div style={{ marginBottom: 16, display: 'flex', gap: 12, alignItems: 'center' }}>
                                <label htmlFor="modsCourse"><strong>Course:</strong></label>
                                <select
                                    id="modsCourse"
                                    value={selectedCourseId || ''}
                                    onChange={(e) => setSelectedCourseId(Number(e.target.value))}
                                >
                                    {courses.map(c => (
                                        <option key={c.id} value={c.id}>{c.name} (#{c.id})</option>
                                    ))}
                                </select>
                            </div>

                            {/* Add Module */}
                            <div className="form-card">
                                <h3>Add Module</h3>
                                <div className="form-row">
                                    <label>Title</label>
                                    <input type="text" id="modAddTitle" placeholder="Module title" />
                                </div>
                                <div className="form-row">
                                    <label>Content</label>
                                    <textarea id="modAddContent" rows={4} placeholder="Module markdown or text"></textarea>
                                </div>
                                <div className="form-actions">
                                    <button className="btn-primary" onClick={async () => {
                                        if (!instructorId || !selectedCourseId) { setError('Select a course'); return; }
                                        const title = document.getElementById('modAddTitle').value;
                                        const content = document.getElementById('modAddContent').value;
                                        try {
                                            await instructorApi.addModule(instructorId, selectedCourseId, { title, content });
                                            alert('Module added');
                                            document.getElementById('modAddTitle').value = '';
                                            document.getElementById('modAddContent').value = '';
                                        } catch (e) {
                                            setError(e.message || 'Failed to add module');
                                        }
                                    }}>Add Module</button>
                                </div>
                            </div>

                            {/* Update Module */}
                            <div className="form-card" style={{ marginTop: 16 }}>
                                <h3>Update Module</h3>
                                <div className="form-row">
                                    <label>Module ID</label>
                                    <input type="number" id="modUpdId" placeholder="e.g., 1" />
                                </div>
                                <div className="form-row">
                                    <label>Title</label>
                                    <input type="text" id="modUpdTitle" placeholder="New title" />
                                </div>
                                <div className="form-row">
                                    <label>Content</label>
                                    <textarea id="modUpdContent" rows={4} placeholder="New content"></textarea>
                                </div>
                                <div className="form-actions">
                                    <button className="btn-primary" onClick={async () => {
                                        if (!instructorId || !selectedCourseId) { setError('Select a course'); return; }
                                        const moduleId = Number(document.getElementById('modUpdId').value);
                                        const title = document.getElementById('modUpdTitle').value;
                                        const content = document.getElementById('modUpdContent').value;
                                        try {
                                            await instructorApi.updateModule(instructorId, selectedCourseId, moduleId, { title, content });
                                            alert('Module updated');
                                        } catch (e) {
                                            setError(e.message || 'Failed to update module');
                                        }
                                    }}>Update Module</button>
                                </div>
                            </div>

                            {/* Delete Module */}
                            <div className="form-card" style={{ marginTop: 16 }}>
                                <h3>Delete Module</h3>
                                <div className="form-row">
                                    <label>Module ID</label>
                                    <input type="number" id="modDelId" placeholder="e.g., 1" />
                                </div>
                                <div className="form-actions">
                                    <button className="btn-danger" onClick={async () => {
                                        if (!instructorId || !selectedCourseId) { setError('Select a course'); return; }
                                        const moduleId = Number(document.getElementById('modDelId').value);
                                        const ok = window.confirm('Delete this module?');
                                        if (!ok) return;
                                        try {
                                            await instructorApi.deleteModule(instructorId, selectedCourseId, moduleId);
                                            alert('Module deleted');
                                        } catch (e) {
                                            setError(e.message || 'Failed to delete module');
                                        }
                                    }}>Delete Module</button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Communication View */}
                    {currentView === 'communication' && (
                        <div className="communication-view">
                            <div className="section-header">
                                <h2>Course Communication</h2>
                                <p>Post announcements and send messages to students.</p>
                            </div>

                            <div style={{ marginBottom: 16, display: 'flex', gap: 12, alignItems: 'center' }}>
                                <label htmlFor="commCourse"><strong>Course:</strong></label>
                                <select
                                    id="commCourse"
                                    value={selectedCourseId || ''}
                                    onChange={(e) => setSelectedCourseId(Number(e.target.value))}
                                >
                                    {courses.map(c => (
                                        <option key={c.id} value={c.id}>{c.name} (#{c.id})</option>
                                    ))}
                                </select>
                            </div>

                            {/* Announcement */}
                            <div className="form-card">
                                <h3>Post Announcement</h3>
                                <div className="form-row">
                                    <label>Title</label>
                                    <input type="text" id="annTitle" placeholder="Announcement title" />
                                </div>
                                <div className="form-row">
                                    <label>Message</label>
                                    <textarea id="annMsg" rows={3} placeholder="Write your announcement"></textarea>
                                </div>
                                <div className="form-actions">
                                    <button className="btn-primary" onClick={async () => {
                                        if (!instructorId || !selectedCourseId) { setError('Select a course'); return; }
                                        const title = document.getElementById('annTitle').value;
                                        const message = document.getElementById('annMsg').value;
                                        try {
                                            await instructorApi.postAnnouncement(instructorId, selectedCourseId, { title, message });
                                            alert('Announcement posted');
                                        } catch (e) {
                                            setError(e.message || 'Failed to post announcement');
                                        }
                                    }}>Post Announcement</button>
                                </div>
                            </div>

                            {/* Message */}
                            <div className="form-card" style={{ marginTop: 16 }}>
                                <h3>Send Message</h3>
                                <div className="form-row">
                                    <label>Subject</label>
                                    <input type="text" id="msgSubject" placeholder="Subject" />
                                </div>
                                <div className="form-row">
                                    <label>Message</label>
                                    <textarea id="msgBody" rows={3} placeholder="Write your message"></textarea>
                                </div>
                                <div className="form-actions">
                                    <button className="btn-primary" onClick={async () => {
                                        if (!instructorId || !selectedCourseId) { setError('Select a course'); return; }
                                        const subject = document.getElementById('msgSubject').value;
                                        const message = document.getElementById('msgBody').value;
                                        try {
                                            await instructorApi.sendMessage(instructorId, selectedCourseId, { subject, message });
                                            alert('Message sent');
                                        } catch (e) {
                                            setError(e.message || 'Failed to send message');
                                        }
                                    }}>Send Message</button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Create Course View */}
                    {currentView === 'create-course' && (
                        <div className="create-course-view">
                            <div className="section-header">
                                <h2>Create New Course</h2>
                                <p>Fill in the details and publish when ready.</p>
                            </div>

                            <form
                                className="form-card"
                                onSubmit={async (e) => {
                                    e.preventDefault();
                                    if (!instructorId) { setError('Missing instructor ID'); return; }
                                    try {
                                        setCreateCourseForm((f) => ({ ...f, submitting: true }));
                                        await instructorApi.createCourse(instructorId, {
                                            title: createCourseForm.title,
                                            description: createCourseForm.description,
                                            published: !!createCourseForm.published,
                                        });
                                        // Refresh lists and stats
                                        await loadCourses(instructorId);
                                        await loadInstructorStats(instructorId);
                                        setCreateCourseForm({ title: '', description: '', published: true, submitting: false });
                                        setCurrentView('courses');
                                    } catch (err) {
                                        setError(err.message || 'Failed to create course');
                                        setCreateCourseForm((f) => ({ ...f, submitting: false }));
                                    }
                                }}
                            >
                                <div className="form-row">
                                    <label htmlFor="courseTitle">Title</label>
                                    <input
                                        id="courseTitle"
                                        type="text"
                                        value={createCourseForm.title}
                                        onChange={(e) => setCreateCourseForm((f) => ({ ...f, title: e.target.value }))}
                                        required
                                        placeholder="e.g., Intro to Spring Boot"
                                    />
                                </div>

                                <div className="form-row">
                                    <label htmlFor="courseDescription">Description</label>
                                    <textarea
                                        id="courseDescription"
                                        rows={4}
                                        value={createCourseForm.description}
                                        onChange={(e) => setCreateCourseForm((f) => ({ ...f, description: e.target.value }))}
                                        placeholder="Short summary of the course"
                                    />
                                </div>

                                <div className="form-row">
                                    <label className="checkbox">
                                        <input
                                            type="checkbox"
                                            checked={createCourseForm.published}
                                            onChange={(e) => setCreateCourseForm((f) => ({ ...f, published: e.target.checked }))}
                                        />
                                        <span>Publish immediately</span>
                                    </label>
                                </div>

                                <div className="form-actions">
                                    <button type="button" className="btn-secondary" onClick={() => setCurrentView('courses')}>
                                        Cancel
                                    </button>
                                    <button type="submit" className="btn-primary" disabled={createCourseForm.submitting}>
                                        {createCourseForm.submitting ? 'Creating...' : 'Create Course'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}

                    {/* Dashboard Overview */}
                    {currentView === 'dashboard' && (
                        <div className="dashboard-overview">
                            <div className="welcome-section">
                                <h2>Welcome back, {user?.fullName || 'Instructor'}! 👨‍🏫</h2>
                                <p>Here's an overview of your teaching activities.</p>
                            </div>

                            {/* Statistics Cards */}
                            <div className="stats-grid">
                                <div className="stat-card primary">
                                    <div className="stat-icon">👨‍🎓</div>
                                    <div className="stat-content">
                                        <h3>Total Students</h3>
                                        <p className="stat-value">{instructorStats.totalStudents}</p>
                                        <p className="stat-description">Across all courses</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card success">
                                    <div className="stat-icon">📚</div>
                                    <div className="stat-content">
                                        <h3>Active Courses</h3>
                                        <p className="stat-value">{instructorStats.activeCourses}</p>
                                        <p className="stat-description">Currently teaching</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card info">
                                    <div className="stat-icon">📝</div>
                                    <div className="stat-content">
                                        <h3>Assignments</h3>
                                        <p className="stat-value">{instructorStats.completedAssignments}</p>
                                        <p className="stat-description">Submitted this month</p>
                                    </div>
                                </div>
                                
                                <div className="stat-card warning">
                                    <div className="stat-icon">📊</div>
                                    <div className="stat-content">
                                        <h3>Average Grade</h3>
                                        <p className="stat-value">{instructorStats.avgGrade}%</p>
                                        <p className="stat-description">Class performance</p>
                                    </div>
                                </div>
                                <div className="stat-card info">
                                    <div className="stat-icon">💰</div>
                                    <div className="stat-content">
                                        <h3>Total Earnings</h3>
                                        <p className="stat-value">${instructorStats.totalEarnings?.toFixed?.(2) || instructorStats.totalEarnings}</p>
                                        <p className="stat-description">All courses</p>
                                    </div>
                                </div>
                            </div>

                            {/* Recent Activities */}
                            <div className="activity-section">
                                <h3>Recent Activities</h3>
                                <div className="activity-feed">
                                    <div className="activity-item">
                                        <div className="activity-icon">📝</div>
                                        <div className="activity-content">
                                            <p><strong>New assignment submission</strong> from Sarah Johnson</p>
                                            <span className="activity-time">2 hours ago</span>
                                        </div>
                                    </div>
                                    <div className="activity-item">
                                        <div className="activity-icon">👨‍🎓</div>
                                        <div className="activity-content">
                                            <p><strong>Student enrolled</strong> in Mathematics 101</p>
                                            <span className="activity-time">1 day ago</span>
                                        </div>
                                    </div>
                                    <div className="activity-item">
                                        <div className="activity-icon">📊</div>
                                        <div className="activity-content">
                                            <p><strong>Grade updated</strong> for Mike Wilson</p>
                                            <span className="activity-time">2 days ago</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Students View */}
                    {currentView === 'students' && (
                        <div className="students-view">
                            <div className="section-header">
                                <h2>My Students</h2>
                                <p>Manage and track your students' progress</p>
                            </div>

                            <div style={{ marginBottom: 16, display: 'flex', gap: 12, alignItems: 'center' }}>
                                <label htmlFor="courseSelect"><strong>Course:</strong></label>
                                <select
                                    id="courseSelect"
                                    value={selectedCourseId || ''}
                                    onChange={(e) => setSelectedCourseId(Number(e.target.value))}
                                >
                                    {courses.map(c => (
                                        <option key={c.id} value={c.id}>{c.name} (#{c.id})</option>
                                    ))}
                                </select>
                                <button className="btn-secondary" onClick={() => instructorId && selectedCourseId && loadStudents(instructorId, selectedCourseId)}>Reload Students</button>
                            </div>

                            <div className="students-grid">
                                {students.map(student => (
                                    <div key={student.id} className="student-card">
                                        <div className="student-header">
                                            <div className="student-avatar">
                                                {student.fullName.charAt(0)}
                                            </div>
                                            <div className="student-info">
                                                <h4>{student.fullName}</h4>
                                                <p>{student.email}</p>
                                                <span className="course-badge">{student.course}</span>
                                            </div>
                                        </div>
                                        
                                        <div className="student-progress">
                                            <div className="progress-header">
                                                <span>Progress</span>
                                                <span className="grade">{student.grade}</span>
                                            </div>
                                            <div className="progress-bar">
                                                <div 
                                                    className="progress-fill" 
                                                    style={{width: `${student.progress}%`}}
                                                ></div>
                                            </div>
                                            <div className="progress-details">
                                                <span>{student.progress}% Complete</span>
                                                <span className="last-activity">Last active: {student.lastActivity}</span>
                                            </div>
                                        </div>
                                        
                                        <div className="student-actions">
                                            <button className="btn-secondary">View Profile</button>
                                            <button className="btn-primary">Grade Work</button>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* Assign/Update Grade */}
                            <div className="form-card" style={{ marginTop: 16 }}>
                                <h3>Assign / Update Grade</h3>
                                <div className="form-row">
                                    <label>Student ID</label>
                                    <input type="number" id="gradeStudentId" placeholder="e.g., 101" />
                                </div>
                                <div className="form-row">
                                    <label>Grade</label>
                                    <input type="number" step="0.1" id="gradeValue" placeholder="e.g., 92.5" />
                                </div>
                                <div className="form-row">
                                    <label>Remarks</label>
                                    <input type="text" id="gradeRemarks" placeholder="Optional" />
                                </div>
                                <div className="form-actions">
                                    <button className="btn-primary" onClick={async () => {
                                        if (!instructorId || !selectedCourseId) { setError('Select a course first'); return; }
                                        const studentId = Number(document.getElementById('gradeStudentId').value);
                                        const grade = Number(document.getElementById('gradeValue').value);
                                        const remarks = document.getElementById('gradeRemarks').value;
                                        try {
                                            await instructorApi.setGrade(instructorId, selectedCourseId, { studentId, grade, remarks });
                                            alert('Grade saved');
                                        } catch (e) {
                                            setError(e.message || 'Failed to set grade');
                                        }
                                    }}>Save Grade</button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Courses View */}
                    {currentView === 'courses' && (
                        <div className="courses-view">
                            <div className="section-header">
                                <h2>My Courses</h2>
                                <p>Manage your teaching courses</p>
                            </div>

                            <div className="courses-grid">
                                {courses.map(course => (
                                    <div key={course.id} className="course-card">
                                        <div className="course-header">
                                            <h3>{course.name}</h3>
                                            <span className={`status-badge ${course.status.toLowerCase()}`}>
                                                {course.status}
                                            </span>
                                        </div>
                                        
                                        <div className="course-stats">
                                            <div className="stat">
                                                <span className="stat-value">{course.students}</span>
                                                <span className="stat-label">Students</span>
                                            </div>
                                            <div className="stat">
                                                <span className="stat-value">
                                                    {new Date(course.startDate).toLocaleDateString()}
                                                </span>
                                                <span className="stat-label">Start Date</span>
                                            </div>
                                        </div>
                                        
                                        <div className="course-actions">
                                            <button className="btn-secondary" onClick={() => { setSelectedCourseId(course.id); setCurrentView('students'); loadStudents(instructorId, course.id); }}>View Students</button>
                                            {editCourseId === course.id ? (
                                                <>
                                                    <button className="btn-primary" disabled={true}>Editing...</button>
                                                </>
                                            ) : (
                                                <>
                                                    <button className="btn-primary" onClick={() => beginEditCourse(course)}>Edit</button>
                                                    <button className="btn-danger" onClick={() => deleteCourseAction(course.id)}>Delete</button>
                                                </>
                                            )}
                                        </div>

                                        {/* Upload Resource for this course */}
                                        {selectedCourseId === course.id && (
                                            <div className="form-card" style={{ marginTop: 12 }}>
                                                <h4>Upload Resource</h4>
                                                <input type="file" id={`file-${course.id}`} />
                                                <div className="form-actions">
                                                    <button className="btn-primary" onClick={async () => {
                                                        const input = document.getElementById(`file-${course.id}`);
                                                        const file = input?.files?.[0];
                                                        if (!file) { alert('Choose a file'); return; }
                                                        try {
                                                            await instructorApi.uploadResource(instructorId, course.id, file);
                                                            alert('File uploaded');
                                                        } catch (e) {
                                                            setError(e.message || 'Upload failed');
                                                        }
                                                    }}>Upload</button>
                                                </div>
                                            </div>
                                        )}

                                        {editCourseId === course.id && (
                                            <div className="form-card" style={{ marginTop: 12 }}>
                                                <div className="form-row">
                                                    <label>Title</label>
                                                    <input
                                                        type="text"
                                                        value={editCourseForm.title}
                                                        onChange={(e) => setEditCourseForm((f) => ({ ...f, title: e.target.value }))}
                                                    />
                                                </div>
                                                <div className="form-row">
                                                    <label>Description</label>
                                                    <textarea
                                                        rows={3}
                                                        value={editCourseForm.description}
                                                        onChange={(e) => setEditCourseForm((f) => ({ ...f, description: e.target.value }))}
                                                    />
                                                </div>
                                                <div className="form-row">
                                                    <label className="checkbox">
                                                        <input
                                                            type="checkbox"
                                                            checked={!!editCourseForm.published}
                                                            onChange={(e) => setEditCourseForm((f) => ({ ...f, published: e.target.checked }))}
                                                        />
                                                        <span>Published</span>
                                                    </label>
                                                </div>
                                                <div className="form-actions">
                                                    <button className="btn-secondary" onClick={() => setEditCourseId(null)}>Cancel</button>
                                                    <button className="btn-primary" disabled={editCourseForm.submitting} onClick={submitEditCourse}>
                                                        {editCourseForm.submitting ? 'Saving...' : 'Save Changes'}
                                                    </button>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Grades & Analytics View */}
                    {currentView === 'grades' && (
                        <div className="grades-view">
                            <div className="section-header">
                                <h2>Grades & Analytics</h2>
                                <p>Track student performance and analytics</p>
                            </div>

                            <div className="analytics-grid">
                                <div className="analytics-card">
                                    <h3>Grade Distribution</h3>
                                    <div className="chart-placeholder">
                                        <div className="grade-bar">
                                            <span className="grade-label">A</span>
                                            <div className="grade-fill" style={{width: '30%'}}></div>
                                            <span className="grade-count">6</span>
                                        </div>
                                        <div className="grade-bar">
                                            <span className="grade-label">B</span>
                                            <div className="grade-fill" style={{width: '45%'}}></div>
                                            <span className="grade-count">9</span>
                                        </div>
                                        <div className="grade-bar">
                                            <span className="grade-label">C</span>
                                            <div className="grade-fill" style={{width: '20%'}}></div>
                                            <span className="grade-count">4</span>
                                        </div>
                                        <div className="grade-bar">
                                            <span className="grade-label">D</span>
                                            <div className="grade-fill" style={{width: '5%'}}></div>
                                            <span className="grade-count">1</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div className="analytics-card">
                                    <h3>Assignment Completion</h3>
                                    <div className="completion-stats">
                                        <div className="completion-item">
                                            <span>On Time</span>
                                            <span className="completion-percentage success">85%</span>
                                        </div>
                                        <div className="completion-item">
                                            <span>Late</span>
                                            <span className="completion-percentage warning">12%</span>
                                        </div>
                                        <div className="completion-item">
                                            <span>Missing</span>
                                            <span className="completion-percentage danger">3%</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div className="analytics-card">
                                    <h3>Class Performance Trends</h3>
                                    <div className="trend-chart">
                                        <div className="trend-line">
                                            <span>This Week: 87.5%</span>
                                            <span className="trend-up">↗ +2.3%</span>
                                        </div>
                                        <div className="trend-line">
                                            <span>This Month: 85.2%</span>
                                            <span className="trend-up">↗ +1.8%</span>
                                        </div>
                                        <div className="trend-line">
                                            <span>This Semester: 84.1%</span>
                                            <span className="trend-stable">→ +0.5%</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

export default InstructorDashboard;