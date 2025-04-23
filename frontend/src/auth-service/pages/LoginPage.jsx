import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Footer } from '../components/layout';
import LoginForm from '../components/auth/LoginForm';
import PageHeader from '../components/layout/PageHeader';

const LoginPage = ({ onLoginSuccess }) => {
  const navigate = useNavigate();
  const [error, setError] = useState('');

  const handleLogin = async (formData) => {
    try {
      // call API here
      console.log('Logging in with:', formData);

      // Simulate successful login
      onLoginSuccess();
      navigate('/dashboard');
    } catch (err) {
      setError('Login failed. Please check your credentials.');
    }
  };

  return (
    <div className="content">
      <Navbar />
      <PageHeader text="Digital Banking" />
      <div className="container">
        <div className="login-container">
          <h5>Log in to Digital Banking</h5>
          {error && <div className="error-message">{error}</div>}
          <LoginForm onSubmit={handleLogin} />
          <p className="auth-redirect">
            Don't have an account? <Link to="/register">Register now</Link>
          </p>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default LoginPage;