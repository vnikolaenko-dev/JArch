export const decodeJWT = (token) => {
    try {
        const payload = token.split('.')[1];
        const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decodedPayload);
    } catch (error) {
        console.error('Ошибка декодирования JWT:', error);
        return null;
    }
};

export const getUsernameFromToken = () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) return null;
    
    const decoded = decodeJWT(token);
    console.log('Decoded JWT:', decoded); 
    return decoded?.sub || null; 
};