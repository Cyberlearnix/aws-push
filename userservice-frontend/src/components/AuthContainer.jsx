import React, { useState } from 'react';
import EmailForm from './EmailForm';
import OtpVerificationForm from './OtpVerificationForm';
import RegistrationForm from './RegistrationForm';
import UserDashboard from './UserDashboard';
import AdminDashboard from './AdminDashboard';
import InstructorDashboard from './InstructorDashboard';
import apiService from '../services/api';
import { useNavigate } from 'react-router-dom';

const AuthContainer = () => {
    const [step, setStep] = useState('email');
    const [email, setEmail] = useState('');
    const [otpSessionId, setOtpSessionId] = useState('');
    const [tempToken, setTempToken] = useState('');
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    const handleEmailSubmit = (session, userEmail) => {
        setOtpSessionId(session);
        setEmail(userEmail);
        setStep('verify-otp');
    };

    const handleVerificationSuccess = (data) => {
        console.log('handleVerificationSuccess called with data:', data);
        
        if (data.accessToken && data.refreshToken) {
            // Case 1: Existing user. Backend returned auth tokens.
            console.log('Existing user flow - setting tokens and navigating to dashboard');
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            setUser(data.user); // Assuming the backend returns user data
            
            // Route based on user role
            const userRole = data.user?.role?.toLowerCase() || apiService.getUserRole();
            console.log('User role:', userRole);
            
            if (userRole === 'admin') {
                console.log('Routing to admin dashboard');
                setStep('admin-dashboard');
                navigate('/admin');
            } else if (userRole === 'instructor') {
                console.log('Routing to instructor dashboard');
                setStep('instructor-dashboard');
                navigate('/instructor');
            } else if (userRole === 'student') {
                console.log('Routing to student dashboard');
                setStep('dashboard');
                navigate('/dashboard');
            } else {
                console.log('Unknown role, defaulting to student dashboard');
                setStep('dashboard');
                navigate('/dashboard');
            }
        } else if (data.token) {
            // Case 2: New user. Backend returned a temporary token for registration.
            console.log('New user flow - proceeding to registration');
            setTempToken(data.token);
            try { localStorage.setItem('tempToken', data.token); } catch {}
            setStep('register');
        } else {
            // Handle unexpected response
            console.error("Invalid response from verification API:", data);
            console.log('Restarting flow due to unexpected response');
            setStep('email'); // Restart the flow
        }
    };

    const handleRegistrationSuccess = (user) => {
        // After successful registration, the backend should return tokens and user data.
        // We'll assume this is handled within the RegistrationForm component.
        setUser(user);
        
        // Route newly registered user based on role
        const userRole = user?.role?.toLowerCase();
        console.log('Registered user role:', userRole);
        
        if (userRole === 'admin') {
            console.log('Routing new admin user to admin dashboard');
            setStep('admin-dashboard');
            navigate('/admin');
        } else if (userRole === 'instructor') {
            console.log('Routing new instructor to instructor dashboard');
            setStep('instructor-dashboard');
            navigate('/instructor');
        } else {
            console.log('Routing new user to student dashboard');
            setStep('dashboard');
            navigate('/dashboard');
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        setUser(null);
        setStep('email');
        navigate('/');
    };

    // Render logic based on the current step
    return (
        <div className="auth-container">
            <div className="card">
                {/* Header with progress indicator */}
                <div className="auth-header">
                    <div className="logo">
                        <h1>UserService</h1>
                    </div>
                    
                    {step !== 'dashboard' && step !== 'admin-dashboard' && step !== 'instructor-dashboard' && (
                        <div className="progress-indicator">
                            <div className={`step ${step === 'email' ? 'active' : step === 'verify-otp' || step === 'register' ? 'completed' : ''}`}>
                                <span>1</span>
                            </div>
                            <div className="progress-line"></div>
                            <div className={`step ${step === 'verify-otp' ? 'active' : step === 'register' ? 'completed' : ''}`}>
                                <span>2</span>
                            </div>
                            <div className="progress-line"></div>
                            <div className={`step ${step === 'register' ? 'active' : ''}`}>
                                <span>3</span>
                            </div>
                        </div>
                    )}
                </div>

                {/* Content Area */}
                <div className="auth-content">
                    {step === 'email' && <EmailForm onEmailSubmit={handleEmailSubmit} />}
                    {step === 'verify-otp' && (
                        <OtpVerificationForm 
                            email={email} 
                            otpSessionId={otpSessionId} 
                            onVerificationSuccess={handleVerificationSuccess} 
                        />
                    )}
                    {step === 'register' && (
                        <RegistrationForm 
                            email={email} 
                            tempToken={tempToken}
                            onRegistrationSuccess={handleRegistrationSuccess} 
                        />
                    )}
                    {step === 'dashboard' && <UserDashboard user={user} onLogout={handleLogout} />}
                    {step === 'admin-dashboard' && <AdminDashboard user={user} onLogout={handleLogout} />}
                    {step === 'instructor-dashboard' && <InstructorDashboard user={user} onLogout={handleLogout} />}
                </div>
            </div>
        </div>
    );
};

export default AuthContainer;