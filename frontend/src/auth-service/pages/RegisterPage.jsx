import React from "react";
import { useNavigate } from "react-router-dom";
import { Navbar, Footer } from "../components/layout";
import RegisterForm from "../components/auth/RegisterForm";
import PageHeader from "../components/layout/PageHeader";
import { useAuth } from "../context/AuthContext";

const RegisterPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleRegisterSuccess = () => {
    navigate("/login", {
      state: {
        registrationSuccess: true,
        message: "Registration successful! Please login to use the system.",
      },
    });
  };

  return (
    <div className="register-page">
      <Navbar />
      <PageHeader text="Digital Banking" />
      <div className="register-container">
        <div className="register-header">
          <h5>Create a new account</h5>
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
