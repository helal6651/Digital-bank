import React from "react";
import { Link } from "react-router-dom";
import bankingAppImage from "../../assets/images/banking-app.png"; // You'll need to add this image

const HeroSection = () => {
  return (
    <section className="hero-section">
      <div className="container">
        <div className="hero-content">
          <div className="hero-text">
            <h1>Banking Made Simple for Everyone</h1>
            <p className="hero-subtitle">
              Secure, fast, and user-friendly digital banking solutions tailored
              to your financial needs. Experience the future of banking today.
            </p>
            <div className="hero-buttons">
              <Link to="/register" className="btn btn-dark btn-lg hero-btn">
                Get Started
              </Link>
              <Link
                to="/login"
                className="btn btn-outline-dark btn-lg hero-btn"
              >
                Login
              </Link>
            </div>
          </div>
          <div className="hero-image">
            <img src={bankingAppImage} alt="Digital Banking App" />
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
