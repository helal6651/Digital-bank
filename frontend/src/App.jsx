import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState } from 'react';
import LandingPage from './auth-service/pages/LandingPage';
import RegisterPage from './auth-service/pages/RegisterPage';
import LoginPage from './auth-service/pages/LoginPage';
import Dashboard from './auth-service/pages/Dashboard';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(true);

  return (
    <Router>
       <div className="app">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route 
            path="/register" 
            element={<RegisterPage onRegisterSuccess={() => setIsAuthenticated(true)} />} 
          />
          <Route 
            path="/login" 
            element={<LoginPage onLoginSuccess={() => setIsAuthenticated(true)} />} 
          />
          <Route 
            path="/dashboard" 
            element={isAuthenticated ? <Dashboard /> : <Navigate to="/" />} 
          />
        </Routes>
      </div> 
    </Router>
  );
}

export default App;