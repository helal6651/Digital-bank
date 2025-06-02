import React, { useState } from "react";
import { register } from "../../services/authService";
import Input from "../common/Input";
import Button from "../common/Button";
import googleLogo from "../../assets/images/google-logo.png";

const RegisterForm = ({ onRegisterSuccess }) => {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    try {
      setIsLoading(true);
      const { username, email, password } = formData;
      const response = await register({ username, email, password });

      if (response.code !== "201") {
        throw new Error(
          response.message?.[0] ||
            "Registration failed with code: " + response.code
        );
      }
      console.log(response.message?.[0] ?? "No message");
      onRegisterSuccess();
    } catch (err) {
      setError(
        err.response?.data?.message || "Registration failed. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  };

  // const handleGoogleRegister = () => {
  //   console.log("Google register clicked");
  //   // Add Google OAuth logic here
  // };

  return (
    <form onSubmit={handleSubmit} className="register-form">
      {error && <div className="error-message">{error}</div>}
      <Input
        type="text"
        name="username"
        placeholder="Username"
        value={formData.username}
        onChange={handleChange}
        required
      />
      <Input
        type="email"
        name="email"
        placeholder="Email"
        value={formData.email}
        onChange={handleChange}
        required
      />
      <Input
        type="password"
        name="password"
        placeholder="Password"
        value={formData.password}
        onChange={handleChange}
        required
      />
      <Input
        type="password"
        name="confirmPassword"
        placeholder="Confirm Password"
        value={formData.confirmPassword}
        onChange={handleChange}
        required
      />
      <Button type="submit" disabled={isLoading}>
        {isLoading ? "Registering..." : "Sign Up"}
      </Button>
      {/* <div className="styled-divider">
        <span>or</span>
      </div>
      <div className="google-login-container">
        <button
          type="button"
          className="google-login-button"
          onClick={handleGoogleRegister}
        >
          <img src={googleLogo} alt="Google logo" className="google-logo" />
          Continue with Google
        </button>
      </div> */}
    </form>
  );
};

export default RegisterForm;
