import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar, Footer } from '../components/layout';
import RegisterForm from '../components/auth/RegisterForm';
import PageHeader from '../components/layout/PageHeader';

const RegisterPage = ({ onRegisterSuccess }) => {
  const navigate = useNavigate();

  const handleRegisterSuccess = () => {
    onRegisterSuccess();
    navigate('/dashboard');
  };

  return (
    <div className="register-page">
      <Navbar />
      <PageHeader text="Digital Banking" />
      <div className="register-container">
        <div className="register-header">
          <h2>Create a new account</h2>
          <p>It's quick and easy.</p>
          <div className="divider"></div>
        </div>
        <RegisterForm onRegisterSuccess={handleRegisterSuccess} />
      </div>
      <Footer />
    </div>
  );
};

export default RegisterPage;