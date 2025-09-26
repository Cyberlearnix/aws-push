import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

const EmailForm = ({ onEmailSubmit }) => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const data = await apiService.sendOtp(email);
            if (data?.success) {
                onEmailSubmit(data.otpSessionId, email);
            } else {
                setError(data?.message || 'Failed to send OTP.');
            }
        } catch (err) {
            setError(err?.message || 'An error occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="email-form">
            <div className="form-header">
                <h2>Welcome back!</h2>
                <p>Enter your email address to get started</p>
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
                            className={`form-input ${error ? 'error' : ''}`}
                            disabled={loading}
                            required 
                        />
                        <div className="input-icon">
                            📧
                        </div>
                    </div>
                </div>
                
                <button 
                    type="submit" 
                    className={`btn-primary ${loading ? 'loading' : ''}`}
                    disabled={loading}
                >
                    {loading ? 'Sending OTP...' : 'Continue'}
                </button>
                
                {error && (
                    <div className="error-message">
                        <span>⚠️</span>
                        {error}
                    </div>
                )}
            </form>
            
            <div className="form-footer">
                <p>We'll send you a secure code to verify your identity</p>
            </div>
        </div>
    );
};

export default EmailForm;