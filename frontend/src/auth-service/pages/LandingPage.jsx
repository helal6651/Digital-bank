import React from 'react';
import { Link } from 'react-router-dom';
import logo from '../assets/images/logo.png';
import { Navbar, Footer } from '../components/layout';

const LandingPage = () => {
  return (
    <div className="content">
      <Navbar />
      <header className="landing-header" style={{ textAlign: 'center' }}>
        <div>
          <img src={logo} alt="Logo" className="landing-logo" />
          <h1>Welcome to Digital Banking</h1>
          <p>Your trusted partner for financial services.</p>
          <Link to="/register" className="btn btn-dark">Get Started</Link>
        </div>
      </header>
      <Footer />
    </div>
  );
};

export default LandingPage;