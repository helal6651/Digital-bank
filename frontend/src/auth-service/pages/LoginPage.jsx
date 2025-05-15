import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Navbar, Footer } from "../components/layout";
import LoginForm from "../components/auth/LoginForm";
import PageHeader from "../components/layout/PageHeader";
import { login as loginService, postLoginToken } from "../services/authService";
import { useAuth } from "../context/AuthContext";

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoggedIn } = useAuth();
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (location.state?.registrationSuccess) {
      setSuccessMessage(location.state.message);
    }
  }, [location]);

  const handleLogin = async (formData) => {
    try {
      setError("");
      setSuccessMessage("");
      setIsLoading(true);

      const response = await loginService(formData);

      if (response.code === "200") {
        console.log("Login successful, tokens stored");
        login();
        navigate("/dashboard");
      } else {
        setError(
          response.message?.[0] ||
            "Login failed. Please check your credentials."
        );
      }
    } catch (err) {
      console.error("Login error:", err);
      setError("Login failed. Please check your credentials.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleSignIn = async (response) => {
    try {
      setError("");
      setSuccessMessage("");
      setIsLoading(true);

      console.log("Google ID Token:", response.credential);
      const res = await postLoginToken(response.credential);

      if (res && res.code === "200") {
        console.log("Google login successful");
        login();
        navigate("/dashboard");
      } else {
        setError(res?.message?.[0] || "Google login failed. Please try again.");
      }
    } catch (error) {
      console.error("Error during Google login:", error);
      setError("Google login failed. Please try again.");
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
          {successMessage && (
            <div className="success-message">{successMessage}</div>
          )}
          <LoginForm
            onSubmit={handleLogin}
            onGoogleSignIn={handleGoogleSignIn}
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
