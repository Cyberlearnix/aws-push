import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

const RegistrationForm = ({ email, onRegistrationSuccess }) => {
    const [fullName, setFullName] = useState('');
    const [countryCode, setCountryCode] = useState('+91');
    const [phone, setPhone] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const validateForm = () => {
        const newErrors = {};
        
        if (!fullName.trim()) {
            newErrors.fullName = 'Full name is required';
        } else if (fullName.trim().length < 2) {
            newErrors.fullName = 'Name must be at least 2 characters';
        }
        
        if (!countryCode.trim()) {
            newErrors.countryCode = 'Country code is required';
        } else if (!/^\+\d{1,4}$/.test(countryCode)) {
            newErrors.countryCode = 'Invalid country code format';
        }
        
        if (!phone.trim()) {
            newErrors.phone = 'Phone number is required';
        } else if (!/^\d{10}$/.test(phone.replace(/\D/g, ''))) {
            newErrors.phone = 'Phone number must be 10 digits';
        }
        
        if (!password) {
            newErrors.password = 'Password is required';
        } else if (password.length < 6) {
            newErrors.password = 'Password must be at least 6 characters';
        }
        
        if (!confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password';
        } else if (password !== confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
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

        const tempToken = localStorage.getItem('tempToken');
        if (!tempToken) {
            setErrors({ general: 'Missing authentication token. Please restart the process.' });
            setLoading(false);
            return;
        }

        try {
            const data = await apiService.register(fullName, countryCode, phone, password, tempToken);
            if (data?.success) {
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);
                localStorage.removeItem('tempToken');
                // Pass user object (if provided) so AuthContainer can route by role
                onRegistrationSuccess(data?.user);
            } else {
                setErrors({ general: data?.message || 'Registration failed.' });
            }
        } catch (err) {
            setErrors({ general: err?.message || 'An error occurred during registration.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="registration-form">
            <div className="form-header">
                <h2>Complete your profile</h2>
                <p>Just a few more details to get you started</p>
                <div className="email-display">
                    <span className="email-label">Email:</span>
                    <strong>{email}</strong>
                </div>
            </div>
            
            <form onSubmit={handleSubmit} className="auth-form">
                <div className="form-group">
                    <label htmlFor="fullName" className="form-label">
                        Full Name
                    </label>
                    <div className="input-container">
                        <input 
                            id="fullName"
                            type="text" 
                            placeholder="Enter your full name" 
                            value={fullName} 
                            onChange={(e) => setFullName(e.target.value)} 
                            className={`form-input ${errors.fullName ? 'error' : ''}`}
                            disabled={loading}
                        />
                        <div className="input-icon">
                            👤
                        </div>
                    </div>
                    {errors.fullName && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.fullName}
                        </div>
                    )}
                </div>
                
                <div className="form-row">
                    <div className="form-group">
                        <label htmlFor="countryCode" className="form-label">
                            Country Code
                        </label>
                        <select 
                            id="countryCode"
                            value={countryCode} 
                            onChange={(e) => setCountryCode(e.target.value)}
                            className={`form-input ${errors.countryCode ? 'error' : ''}`}
                            disabled={loading}
                        >
                            <option value="+91">+91 (India)</option>
                            <option value="+1">+1 (US/Canada)</option>
                            <option value="+44">+44 (UK)</option>
                            <option value="+61">+61 (Australia)</option>
                            <option value="+49">+49 (Germany)</option>
                            <option value="+33">+33 (France)</option>
                        </select>
                        {errors.countryCode && (
                            <div className="error-message">
                                <span>⚠️</span>
                                {errors.countryCode}
                            </div>
                        )}
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="phone" className="form-label">
                            Phone Number
                        </label>
                        <div className="input-container">
                            <input 
                                id="phone"
                                type="tel" 
                                placeholder="1234567890" 
                                value={phone} 
                                onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))} 
                                className={`form-input ${errors.phone ? 'error' : ''}`}
                                disabled={loading}
                                maxLength="10"
                            />
                            <div className="input-icon">
                                📱
                            </div>
                        </div>
                        {errors.phone && (
                            <div className="error-message">
                                <span>⚠️</span>
                                {errors.phone}
                            </div>
                        )}
                    </div>
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
                
                <div className="form-group">
                    <label htmlFor="confirmPassword" className="form-label">
                        Confirm Password
                    </label>
                    <div className="input-container">
                        <input 
                            id="confirmPassword"
                            type={showPassword ? 'text' : 'password'} 
                            placeholder="Confirm your password" 
                            value={confirmPassword} 
                            onChange={(e) => setConfirmPassword(e.target.value)} 
                            className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
                            disabled={loading}
                        />
                        <div className="input-icon">
                            🔒
                        </div>
                    </div>
                    {errors.confirmPassword && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.confirmPassword}
                        </div>
                    )}
                </div>
                
                <button 
                    type="submit" 
                    className={`btn-primary ${loading ? 'loading' : ''}`}
                    disabled={loading}
                >
                    {loading ? 'Creating account...' : 'Create Account'}
                </button>
                
                {errors.general && (
                    <div className="error-message">
                        <span>⚠️</span>
                        {errors.general}
                    </div>
                )}
            </form>
        </div>
    );
};

export default RegistrationForm;