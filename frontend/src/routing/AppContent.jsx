import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import LandingPage from "../auth-service/pages/LandingPage";
import RegisterPage from "../auth-service/pages/RegisterPage";
import LoginPage from "../auth-service/pages/LoginPage";
import Dashboard from "../auth-service/pages/Dashboard";
import { useAuth } from "../auth-service/context/AuthContext";

const ProtectedRoute = ({ children }) => {
  const { isLoggedIn } = useAuth();
  return isLoggedIn ? children : <Navigate to="/login" />;
};

const AppContent = () => {
  const { login } = useAuth();

  return (
    <div className="app">
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route
          path="/register"
          element={<RegisterPage onRegisterSuccess={login} />}
        />
        <Route path="/login" element={<LoginPage onLoginSuccess={login} />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
      </Routes>
    </div>
  );
};

export default AppContent;
