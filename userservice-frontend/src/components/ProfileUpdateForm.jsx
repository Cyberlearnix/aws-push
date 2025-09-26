import React, { useState } from 'react';
import apiService from '../services/api';

const ProfileUpdateForm = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        phone: '',
        alternatePhone: '',
        address: '',
        biography: '',
        language: '',
        linkedinUrl: '',
        instagramUrl: '',
        facebookUrl: '',
        internshalaUrl: '',
        countryCode: '',
        photo: '',
        becomeInstructor: false,
    });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState(null);
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        setError(null);

        try {
            const data = await apiService.updateProfile(formData);
            setMessage(data.message || 'Profile updated successfully!');
        } catch (err) {
            setError(err?.message || 'An error occurred while updating the profile.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Update Your Profile</h2>
            <input name="fullName" placeholder="Full Name" value={formData.fullName} onChange={handleChange} />
            <input name="phone" placeholder="Phone" value={formData.phone} onChange={handleChange} />
            <input name="address" placeholder="Address" value={formData.address} onChange={handleChange} />
            <input name="biography" placeholder="Biography" value={formData.biography} onChange={handleChange} />
            <input name="language" placeholder="Language" value={formData.language} onChange={handleChange} />
            <input name="linkedinUrl" placeholder="LinkedIn URL" value={formData.linkedinUrl} onChange={handleChange} />
            <button type="submit" disabled={loading}>
                {loading ? 'Updating...' : 'Update Profile'}
            </button>
            {message && <p style={{ color: 'green' }}>{message}</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
        </form>
    );
};

export default ProfileUpdateForm;