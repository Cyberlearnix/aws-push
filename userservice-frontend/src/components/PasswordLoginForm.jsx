import React, { useState } from 'react';
import apiService from '../services/api';

const PasswordLoginForm = ({ onLoginSuccess, onSwitchToOtp }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});

    const validateForm = () => {
        const newErrors = {};
        
        if (!email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            newErrors.email = 'Please enter a valid email address';
        }
        
        if (!password) {
            newErrors.password = 'Password is required';
        }
        
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }
        
        setLoading(true);
        setErrors({});

        try {
            const data = await apiService.loginWithPassword(email, password);
            
            if (data.accessToken && data.refreshToken) {
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);
                onLoginSuccess(data.user);
            } else {
                setErrors({ general: 'Invalid response from server' });
            }
        } catch (error) {
            console.error('Login error:', error);
            setErrors({ general: error.message || 'Login failed. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-form">
            <div className="form-header">
                <h2>Sign in to your account</h2>
                <p>Enter your email and password to continue</p>
            </div>
            
            <form onSubmit={handleSubmit} className="auth-form">
                <div className="form-group">
                    <label htmlFor="email" className="form-label">
                        Email Address
                    </label>
                    <div className="input-container">
                        <input 
                            id="email"
                            type="email" 
                            placeholder="Enter your email address" 
                            value={email} 
                            onChange={(e) => setEmail(e.target.value)} 
                            className={`form-input ${errors.email ? 'error' : ''}`}
                            disabled={loading}
                        />
                        <div className="input-icon">
                            📧
                        </div>
                    </div>
                    {errors.email && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.email}
                        </div>
                    )}
                </div>

                <div className="form-group">
                    <label htmlFor="password" className="form-label">
                        Password
                    </label>
                    <div className="input-container">
                        <input 
                            id="password"
                            type={showPassword ? 'text' : 'password'} 
                            placeholder="Enter your password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            className={`form-input ${errors.password ? 'error' : ''}`}
                            disabled={loading}
                        />
                        <button 
                            type="button" 
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                        >
                            {showPassword ? '🙈' : '👁️'}
                        </button>
                    </div>
                    {errors.password && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.password}
                        </div>
                    )}
                </div>
                
                <button 
                    type="submit" 
                    className={`btn-primary ${loading ? 'loading' : ''}`}
                    disabled={loading}
                >
                    {loading ? 'Signing in...' : 'Sign In'}
                </button>
                
                {errors.general && (
                    <div className="error-message">
                        <span>⚠️</span>
                        {errors.general}
                    </div>
                )}
            </form>
            
            <div className="form-footer">
                <p>
                    Forgot your password? {' '}
                    <button type="button" className="link-button">
                        Reset Password
                    </button>
                </p>
                <p>
                    Don't have an account or prefer OTP login? {' '}
                    <button type="button" className="link-button" onClick={onSwitchToOtp}>
                        Sign in with OTP
                    </button>
                </p>
            </div>
        </div>
    );
};

export default PasswordLoginForm;