import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Navbar, Footer } from '../components/layout';
import LoginForm from '../components/auth/LoginForm';
import PageHeader from '../components/layout/PageHeader';
import { login as loginService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Check for registration success message in location state
  useEffect(() => {
    if (location.state?.registrationSuccess) {
      setSuccessMessage(location.state.message);
    }
  }, [location]);

  const handleLogin = async (formData) => {
    try {
      // Clear any previous messages
      setError('');
      setSuccessMessage('');
      setIsLoading(true);
      
      // Call login API
      const response = await loginService(formData);
      
      if (response.code === "200") {
        // Login successful
        console.log('Login successful, tokens stored');
        
        // Update authentication state in context
        login();
        
        // Navigate to dashboard
        navigate('/dashboard');
      } else {
        // Login failed with a non-200 code
        setError(response.message?.[0] || 'Login failed. Please check your credentials.');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="content">
      <Navbar />
      <PageHeader text="Digital Banking" />
      <div className="container">
        <div className="login-container">
          <h5>Log in to Digital Banking</h5>
          {successMessage && <div className="success-message">{successMessage}</div>}
          <LoginForm 
            onSubmit={handleLogin} 
            isLoading={isLoading} 
            errorMessage={error}
          />
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