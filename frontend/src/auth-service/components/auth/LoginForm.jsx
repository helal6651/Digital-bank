import React, { useState } from 'react';
import Button from '../common/Button';

const LoginForm = ({ onSubmit }) => {
  const [formData, setFormData] = useState({
    email: '',
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
      <div className="form-group">
        <input
          type="email"
          id="email"
          name="email"
          className="input"
          placeholder="Enter your email"
          value={formData.email}
          onChange={handleChange}
          required
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
        />
      </div>
      <Button type="submit">Login</Button>
    </form>
  );
};

export default LoginForm;