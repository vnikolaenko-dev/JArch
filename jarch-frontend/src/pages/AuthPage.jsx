import React, { useState } from 'react';
import { authService } from '../services/authService';
import { useNavigate } from 'react-router-dom';

const AuthPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            let token;
            if (isLogin) {
                token = await authService.login(formData.email, formData.password);
            } else {
                token = await authService.register(formData.username, formData.password, formData.email);
            }
            
            if (token) {
                authService.setToken(token);
                window.dispatchEvent(new Event('authChange'));
                navigate('/');
            } else {
                throw new Error('Ошибка авторизации');
            }
        } catch (error) {
            setError(error.message || "Ошибка авторизации");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <h2 className="auth-title">
                    {isLogin ? '[вход]' : '[регистрация]'}
                </h2>
                
                <form onSubmit={handleSubmit} className="auth-form">
                    {!isLogin && (
                        <div className="form-group">
                            <label>Имя пользователя</label>
                            <input
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                placeholder="Введите имя пользователя"
                                required
                                disabled={loading}
                            />
                        </div>
                    )}
                    
                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="Введите email"
                            required
                            disabled={loading}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Пароль</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="Введите пароль"
                            required
                            disabled={loading}
                        />
                    </div>
                    
                    {error && (
                        <div style={{
                            color: 'var(--color-error)',
                            fontSize: '0.7rem',
                            textAlign: 'left',
                        }}>
                            {error}
                        </div>
                    )}
                    
                    <button 
                        type="submit" 
                        disabled={loading}
                        className="auth-submit-button"
                    >
                        {loading ? 'Загрузка...' : (isLogin ? '[Войти]' : '[Зарегистрироваться]')}
                    </button>
                </form>
                
                <div className="auth-switch">
                    <button
                        type="button"
                        onClick={() => setIsLogin(!isLogin)}
                        disabled={loading}
                        className="auth-switch-button"
                    >
                        {isLogin ? '[нет аккаунта? зарегистрироваться]' : '[уже есть аккаунт? войти]'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AuthPage;