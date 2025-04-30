import React, { useState } from 'react';
import Button from '../common/Button';

const LoginForm = ({ onSubmit, isLoading, errorMessage }) => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="login-form">
      {errorMessage && <div className="error-message">{errorMessage}</div>}
      <div className="form-group">
        <input
          type="text"
          id="username"
          name="username"
          className="input"
          placeholder="Enter your username"
          value={formData.username}
          onChange={handleChange}
          required
          disabled={isLoading}
        />
      </div>
      <div className="form-group">
        <input
          type="password"
          id="password"
          name="password"
          className="input"
          placeholder="Enter your password"
          value={formData.password}
          onChange={handleChange}
          required
          disabled={isLoading}
        />
      </div>
      <Button type="submit" disabled={isLoading}>
        {isLoading ? 'Logging in...' : 'Login'}
      </Button>
    </form>
  );
};

export default LoginForm;