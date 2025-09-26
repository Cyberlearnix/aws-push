import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

const OtpVerificationForm = ({ email, otpSessionId, onVerificationSuccess }) => {
    const [otp, setOtp] = useState(['', '', '', '', '', '']);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const inputRefs = useRef([]);

    // Auto focus first input on mount
    useEffect(() => {
        if (inputRefs.current[0]) {
            inputRefs.current[0].focus();
        }
    }, []);

    const handleInputChange = (index, value) => {
        if (!/^\d*$/.test(value)) return; // Only allow digits
        
        const newOtp = [...otp];
        newOtp[index] = value.slice(-1); // Only take the last digit
        setOtp(newOtp);
        setError(null);
        
        // Auto focus next input
        if (value && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }
        
        // Auto submit when all digits are filled
        if (newOtp.every(digit => digit !== '') && newOtp.join('').length === 6) {
            handleSubmit(newOtp.join(''));
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !otp[index] && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
        const newOtp = pastedData.split('').concat(Array(6 - pastedData.length).fill(''));
        setOtp(newOtp);
        setError(null);
        
        // Focus next empty input or last input
        const nextEmptyIndex = newOtp.findIndex(digit => digit === '');
        if (nextEmptyIndex !== -1) {
            inputRefs.current[nextEmptyIndex]?.focus();
        } else {
            inputRefs.current[5]?.focus();
        }
        
        // Auto submit if complete
        if (pastedData.length === 6) {
            handleSubmit(pastedData);
        }
    };

    const handleSubmit = async (otpValue = otp.join('')) => {
        // Validate OTP input
        if (!otpValue || otpValue.length !== 6) {
            setError('Please enter all 6 digits of the OTP.');
            return;
        }
        
        setLoading(true);
        setError(null);

        try {
            console.log('Attempting OTP verification with:', { email, otp: otpValue.slice(0, 2) + '****', otpSessionId });
            const data = await apiService.verifyOtp(email, otpValue, otpSessionId);
            console.log('Response data:', data);
            // Accept either existing-user tokens or new-user temp token
            if ((data?.accessToken && data?.refreshToken) || data?.token || data?.success) {
                console.log('OTP verification accepted');
                onVerificationSuccess(data);
            } else {
                console.log('OTP verification failed:', data?.message || 'Unknown error');
                setError(data?.message || 'Invalid OTP.');
            }
        } catch (err) {
            console.error('OTP verification error:', err);
            setError(`An error occurred: ${err.message}. Please try again.`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="otp-form">
            <div className="form-header">
                <h2>Verify your code</h2>
                <p>Enter the 6-digit code sent to <strong>{email}</strong></p>
            </div>
            
            <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }} className="auth-form">
                <div className="otp-container" onPaste={handlePaste}>
                    {otp.map((digit, index) => (
                        <input
                            key={index}
                            ref={el => inputRefs.current[index] = el}
                            type="text"
                            inputMode="numeric"
                            pattern="\d{1}"
                            maxLength="1"
                            value={digit}
                            onChange={(e) => handleInputChange(index, e.target.value)}
                            onKeyDown={(e) => handleKeyDown(index, e)}
                            className={`otp-input ${digit ? 'filled' : ''} ${error ? 'error' : ''}`}
                            disabled={loading}
                            autoComplete="one-time-code"
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
                
                {error && (
                    <div className="error-message">
                        <span>⚠️</span>
                        {error}
                    </div>
                )}
            </form>
            
            <div className="form-footer">
                <p>Didn't receive a code? <button type="button" className="link-button">Resend</button></p>
            </div>
        </div>
    );
};

export default OtpVerificationForm;