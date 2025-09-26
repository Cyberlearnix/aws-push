const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || '/api';

class ApiService {
    constructor() {
        this.baseURL = API_BASE_URL;
    }

    // Helper method to get headers with auth token
    getHeaders(contentType = 'application/json', includeAuth = true) {
        const headers = {
            'Content-Type': contentType,
        };

        if (includeAuth) {
            const token = localStorage.getItem('accessToken');
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
        }

        return headers;
    }

    // Helper method for API requests with error handling
    async makeRequest(endpoint, options = {}) {
        try {
            const url = `${this.baseURL}${endpoint}`;
            const response = await fetch(url, {
                ...options,
                headers: {
                    ...this.getHeaders(),
                    ...options.headers,
                },
            });

            const data = await response.json();

            if (!response.ok) {
                // Handle token expiration
                if (response.status === 401 && localStorage.getItem('refreshToken')) {
                    const refreshed = await this.refreshToken();
                    if (refreshed) {
                        // Retry the original request with new token
                        return this.makeRequest(endpoint, {
                            ...options,
                            headers: {
                                ...this.getHeaders(),
                                ...options.headers,
                            },
                        });
                    }
                }
                throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            return data;
        } catch (error) {
            console.error('API Request Error:', error);
            throw error;
        }
    }

    // Auth APIs
    async sendOtp(email) {
        return this.makeRequest('/email-auth/send-otp', {
            method: 'POST',
            headers: this.getHeaders('application/json', false),
            body: JSON.stringify({ email }),
        });
    }

    async verifyOtp(email, otp, otpSessionId) {
        return this.makeRequest('/email-auth/verify-otp', {
            method: 'POST',
            headers: this.getHeaders('application/json', false),
            body: JSON.stringify({ email, otp, otpSessionId }),
        });
    }

    async register(fullName, countryCode, phone, password, tempToken) {
        return this.makeRequest('/email-auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${tempToken}`,
            },
            body: JSON.stringify({ fullName, countryCode, phone, password }),
        });
    }

    async loginWithPassword(email, password) {
        return this.makeRequest('/users/login-with-password', {
            method: 'POST',
            headers: this.getHeaders('application/json', false),
            body: JSON.stringify({ email, password }),
        });
    }

    async logout() {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
            await this.makeRequest('/users/logout', {
                method: 'POST',
                body: JSON.stringify({ refreshToken }),
            });
        }
        
        // Clear tokens regardless of API response
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('tempToken');
    }

    async refreshToken() {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (!refreshToken) {
                throw new Error('No refresh token available');
            }

            const response = await fetch(`${this.baseURL}/users/refresh-token`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ refreshToken }),
            });

            const data = await response.json();

            if (response.ok && data.accessToken) {
                localStorage.setItem('accessToken', data.accessToken);
                if (data.refreshToken) {
                    localStorage.setItem('refreshToken', data.refreshToken);
                }
                return true;
            } else {
                // Refresh failed, clear tokens
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                return false;
            }
        } catch (error) {
            console.error('Token refresh failed:', error);
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            return false;
        }
    }

    async forgotPassword(email) {
        return this.makeRequest('/users/forgot-password', {
            method: 'POST',
            headers: this.getHeaders('application/json', false),
            body: JSON.stringify({ email }),
        });
    }

    async resetPassword(email, newPassword, otpSessionId) {
        return this.makeRequest('/users/reset-password', {
            method: 'POST',
            headers: this.getHeaders('application/json', false),
            body: JSON.stringify({ email, newPassword, otpSessionId }),
        });
    }

    // User Profile APIs
    async getUserProfile(userId = 'me') {
        return this.makeRequest(`/users/${userId}`, {
            method: 'GET',
        });
    }

    async updateProfile(profileData) {
        return this.makeRequest('/users/update-profile', {
            method: 'PUT',
            body: JSON.stringify(profileData),
        });
    }

    async uploadPhoto(file) {
        const formData = new FormData();
        formData.append('photo', file);

        return this.makeRequest('/users/upload-photo', {
            method: 'POST',
            headers: this.getHeaders(null), // Don't set content-type for FormData
            body: formData,
        });
    }

    // Admin APIs
    async getAllUsers(page = 1, limit = 10) {
        return this.makeRequest(`/admin/all-users-details?page=${page}&limit=${limit}`, {
            method: 'GET',
        });
    }

    async getUser(userId) {
        return this.makeRequest(`/admin/users/${userId}`, {
            method: 'GET',
        });
    }

    async createUser(userData) {
        return this.makeRequest('/admin/add-users', {
            method: 'POST',
            body: JSON.stringify(userData),
        });
    }

    async updateUser(userId, userData) {
        return this.makeRequest(`/admin/users/${userId}`, {
            method: 'PUT',
            body: JSON.stringify(userData),
        });
    }

    async deleteUser(userId) {
        return this.makeRequest(`/admin/users/${userId}`, {
            method: 'DELETE',
        });
    }

    async deactivateUser(userId) {
        return this.makeRequest(`/admin/users/${userId}/deactivate`, {
            method: 'POST',
        });
    }

    async activateUser(userId) {
        return this.makeRequest(`/admin/users/${userId}/activate`, {
            method: 'POST',
        });
    }

    // Helper method to check if user is authenticated
    isAuthenticated() {
        return !!localStorage.getItem('accessToken');
    }

    // Helper method to get user role from token
    getUserRole() {
        const token = localStorage.getItem('accessToken');
        if (!token) return null;
        
        try {
            // Decode JWT token (simple base64 decode for payload)
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.role?.toLowerCase() || null;
        } catch (error) {
            return null;
        }
    }

    // Helper method to check if user is admin
    isAdmin() {
        const role = this.getUserRole();
        return role === 'admin';
    }

    // Helper method to check if user is instructor
    isInstructor() {
        const role = this.getUserRole();
        return role === 'instructor';
    }

    // Helper method to check if user is student
    isStudent() {
        const role = this.getUserRole();
        return role === 'student';
    }
}

// Export singleton instance
const apiService = new ApiService();
export default apiService;