const API_BASE = 'http://localhost:8080';

let token = localStorage.getItem('jwtToken') || '';

export const setToken = (newToken) => {
    token = newToken;
    localStorage.setItem('jwtToken', newToken);
};

export const getToken = () => {
    return token;
};

export const request = async (endpoint, options = {}) => {
    const url = `${API_BASE}${endpoint}`;
    
    const headers = { ...options.headers };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const requestOptions = {
        ...options,
        headers
    };
    
    try {
        const response = await fetch(url, requestOptions);
        
        console.log('API Response:', {
            endpoint,
            status: response.status,
            contentType: response.headers.get('content-type')
        });
        
        if (response.headers.get('content-type')?.includes('application/zip') ||
            response.headers.get('content-type')?.includes('application/octet-stream')) {
            return response;
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('API Error:', errorText);
            throw new Error(errorText || `Ошибка ${response.status}`);
        }
        
        if (response.status === 204) {
            return null;
        }
        
        const responseText = await response.text();
        console.log('API Response text:', responseText);
        
        try {
            return JSON.parse(responseText);
        } catch (jsonError) {
            return responseText;
        }
        
    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
};

export const createEventSource = (endpoint) => {
    const url = `${API_BASE}${endpoint}`;
    const eventSourceUrl = token ? `${url}?token=${encodeURIComponent(token)}` : url;
    return new EventSource(eventSourceUrl);
};

export const authenticate = async (email, password) => {
    const result = await request(`/auth/login?email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`);
    console.log('Authenticate result:', result);
    return result;
};

export const register = async (username, password, email) => {
    const result = await request(`/auth/register?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}&email=${encodeURIComponent(email)}`);
    console.log('Register result:', result);
    return result;
};

export const logout = () => {
    setToken('');
};