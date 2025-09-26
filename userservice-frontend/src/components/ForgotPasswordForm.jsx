import React, { useState } from 'react';
import apiService from '../services/api';

const ForgotPasswordForm = ({ onSuccess, onCancel }) => {
    const [step, setStep] = useState('email'); // 'email', 'otp', 'reset'
    const [email, setEmail] = useState('');
    const [otp, setOtp] = useState(['', '', '', '', '', '']);
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});
    const [otpSessionId, setOtpSessionId] = useState('');
    const [resending, setResending] = useState(false);

    const handleEmailSubmit = async (e) => {
        e.preventDefault();
        
        if (!email.trim()) {
            setErrors({ email: 'Email is required' });
            return;
        }
        
        if (!/\S+@\S+\.\S+/.test(email)) {
            setErrors({ email: 'Please enter a valid email address' });
            return;
        }
        
        setLoading(true);
        setErrors({});

        try {
            const data = await apiService.forgotPassword(email);
            if (data.success) {
                setOtpSessionId(data.otpSessionId);
                setStep('otp');
            } else {
                setErrors({ general: data.message || 'Failed to send reset code' });
            }
        } catch (error) {
            console.error('Forgot password error:', error);
            setErrors({ general: error.message || 'Failed to send reset code. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    const handleResend = async () => {
        if (!email) return;
        setResending(true);
        setErrors({});
        try {
            const data = await apiService.forgotPassword(email);
            if (data?.success) {
                setOtpSessionId(data.otpSessionId);
            } else {
                setErrors({ general: data?.message || 'Failed to resend code' });
            }
        } catch (error) {
            setErrors({ general: error?.message || 'Failed to resend code. Please try again.' });
        } finally {
            setResending(false);
        }
    };

    const handleOtpChange = (index, value) => {
        if (!/^\d*$/.test(value)) return;
        
        const newOtp = [...otp];
        newOtp[index] = value.slice(-1);
        setOtp(newOtp);
        setErrors({});
        
        // Auto focus next input
        if (value && index < 5) {
            document.getElementById(`otp-${index + 1}`)?.focus();
        }
        
        // Auto verify when complete
        if (newOtp.every(digit => digit !== '')) {
            handleOtpVerification(newOtp.join(''));
        }
    };

    const handleOtpVerification = async (otpValue = otp.join('')) => {
        if (otpValue.length !== 6) {
            setErrors({ otp: 'Please enter all 6 digits' });
            return;
        }

        setLoading(true);
        setErrors({});

        try {
            const data = await apiService.verifyOtp(email, otpValue, otpSessionId);
            if (data.success) {
                setStep('reset');
            } else {
                setErrors({ otp: data.message || 'Invalid verification code' });
            }
        } catch (error) {
            console.error('OTP verification error:', error);
            setErrors({ otp: error.message || 'Verification failed. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    const handlePasswordReset = async (e) => {
        e.preventDefault();
        
        const newErrors = {};
        
        if (!newPassword) {
            newErrors.newPassword = 'New password is required';
        } else if (newPassword.length < 6) {
            newErrors.newPassword = 'Password must be at least 6 characters';
        }
        
        if (!confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password';
        } else if (newPassword !== confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }
        
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }
        
        setLoading(true);
        setErrors({});

        try {
            // Implementation would depend on your backend API
            // This is a placeholder - you might need to implement reset-password endpoint
            const data = await apiService.resetPassword(email, newPassword, otpSessionId);
            if (data.success) {
                onSuccess('Password reset successfully! Please sign in with your new password.');
            } else {
                setErrors({ general: data.message || 'Password reset failed' });
            }
        } catch (error) {
            console.error('Password reset error:', error);
            setErrors({ general: error.message || 'Password reset failed. Please try again.' });
        } finally {
            setLoading(false);
        }
    };

    if (step === 'email') {
        return (
            <div className="forgot-password-form">
                <div className="form-header">
                    <h2>Reset your password</h2>
                    <p>Enter your email address and we'll send you a verification code</p>
                </div>
                
                <form onSubmit={handleEmailSubmit} className="auth-form">
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
                                autoFocus
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
                    
                    <button 
                        type="submit" 
                        className={`btn-primary ${loading ? 'loading' : ''}`}
                        disabled={loading}
                    >
                        {loading ? 'Sending...' : 'Send Verification Code'}
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
                        Remember your password? {' '}
                        <button type="button" className="link-button" onClick={onCancel}>
                            Back to Sign In
                        </button>
                    </p>
                </div>
            </div>
        );
    }

    if (step === 'otp') {
        return (
            <div className="forgot-password-form">
                <div className="form-header">
                    <h2>Enter verification code</h2>
                    <p>We sent a 6-digit code to <strong>{email}</strong></p>
                </div>
                
                <form onSubmit={(e) => { e.preventDefault(); handleOtpVerification(); }} className="auth-form">
                    <div className="otp-container">
                        {otp.map((digit, index) => (
                            <input
                                key={index}
                                id={`otp-${index}`}
                                type="text"
                                inputMode="numeric"
                                pattern="\d{1}"
                                maxLength="1"
                                value={digit}
                                onChange={(e) => handleOtpChange(index, e.target.value)}
                                className={`otp-input ${digit ? 'filled' : ''} ${errors.otp ? 'error' : ''}`}
                                disabled={loading}
                            />
                        ))}
                    </div>
                    
                    <button 
                        type="submit" 
                        className={`btn-primary ${loading ? 'loading' : ''}`}
                        disabled={loading || otp.join('').length !== 6}
                    >
                        {loading ? 'Verifying...' : 'Verify Code'}
                    </button>
                    
                    {errors.otp && (
                        <div className="error-message">
                            <span>⚠️</span>
                            {errors.otp}
                        </div>
                    )}
                </form>
                
                <div className="form-footer">
                    <p>
                        Didn't receive a code? {' '}
                        <button type="button" className="link-button" onClick={handleResend} disabled={resending}>
                            {resending ? 'Resending...' : 'Resend'}
                        </button>
                    </p>
                </div>
            </div>
        );
    }

    if (step === 'reset') {
        return (
            <div className="forgot-password-form">
                <div className="form-header">
                    <h2>Create new password</h2>
                    <p>Enter your new password below</p>
                </div>
                
                <form onSubmit={handlePasswordReset} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="newPassword" className="form-label">
                            New Password
                        </label>
                        <div className="input-container">
                            <input 
                                id="newPassword"
                                type={showPassword ? 'text' : 'password'} 
                                placeholder="Enter new password" 
                                value={newPassword} 
                                onChange={(e) => setNewPassword(e.target.value)} 
                                className={`form-input ${errors.newPassword ? 'error' : ''}`}
                                disabled={loading}
                                autoFocus
                            />
                            <button 
                                type="button" 
                                className="password-toggle"
                                onClick={() => setShowPassword(!showPassword)}
                            >
                                {showPassword ? '🙈' : '👁️'}
                            </button>
                        </div>
                        {errors.newPassword && (
                            <div className="error-message">
                                <span>⚠️</span>
                                {errors.newPassword}
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
                                placeholder="Confirm new password" 
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
                        {loading ? 'Resetting...' : 'Reset Password'}
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
    }

    return null;
};

export default ForgotPasswordForm;