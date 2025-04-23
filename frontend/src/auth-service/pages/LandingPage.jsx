import React from 'react';
import { Link } from 'react-router-dom';
import logo from '../assets/images/logo.svg';
import { Navbar, Footer } from '../components/layout';

const LandingPage = () => {
  return (
    <div className="content">
      <Navbar />
      <header className="landing-header" style={{ textAlign: 'center' }}>
        <img src={logo} alt="Logo" className="landing-logo" />
        <h1>Welcome to Micro Banking</h1>
        <p>Your trusted partner for financial services.</p>
        <Link to="/register" className="btn btn-primary">Get Started</Link>
      </header>
      <Footer />
    </div>
  );
};

export default LandingPage;