import React from "react";
import { Link } from "react-router-dom";
import {
  FaFacebook,
  FaTwitter,
  FaInstagram,
  FaLinkedin,
  FaPhoneAlt,
  FaEnvelope,
  FaMapMarkerAlt,
} from "react-icons/fa";
import logo from "../../assets/images/logo.png";

const EnhancedFooter = () => {
  return (
    <footer className="enhanced-footer">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand">
            <img
              src={logo}
              alt="Digital Banking Logo"
              className="footer-logo"
            />
            <p>
              Digital Banking provides secure, convenient, and innovative
              financial services to individuals and businesses across the globe.
            </p>
            <div className="social-icons">
              <a href="#" className="social-icon">
                <FaFacebook />
              </a>
              <a href="#" className="social-icon">
                <FaTwitter />
              </a>
              <a href="#" className="social-icon">
                <FaInstagram />
              </a>
              <a href="#" className="social-icon">
                <FaLinkedin />
              </a>
            </div>
          </div>

          <div className="footer-links">
            <h4>Quick Links</h4>
            <ul>
              <li>
                <Link to="/">Home</Link>
              </li>
              <li>
                <Link to="/about">About Us</Link>
              </li>
              <li>
                <Link to="/services">Services</Link>
              </li>
              <li>
                <Link to="/contact">Contact</Link>
              </li>
              <li>
                <Link to="/register">Register</Link>
              </li>
              <li>
                <Link to="/login">Login</Link>
              </li>
            </ul>
          </div>

          <div className="footer-links">
            <h4>Services</h4>
            <ul>
              <li>
                <Link to="/personal-banking">Personal Banking</Link>
              </li>
              <li>
                <Link to="/business-banking">Business Banking</Link>
              </li>
              <li>
                <Link to="/investments">Investments</Link>
              </li>
              <li>
                <Link to="/loans">Loans</Link>
              </li>
              <li>
                <Link to="/credit-cards">Credit Cards</Link>
              </li>
              <li>
                <Link to="/insurance">Insurance</Link>
              </li>
            </ul>
          </div>

          <div className="footer-contact">
            <h4>Contact Us</h4>
            <ul className="contact-info">
              <li>
                <FaPhoneAlt className="contact-icon" />
                <span>+1 (555) 123-4567</span>
              </li>
              <li>
                <FaEnvelope className="contact-icon" />
                <span>support@digitalbanking.com</span>
              </li>
              <li>
                <FaMapMarkerAlt className="contact-icon" />
                <span>123 Financial District, New York, NY 10004</span>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <div className="copyright">
            <p>
              &copy; {new Date().getFullYear()} Digital Banking. All rights
              reserved.
            </p>
          </div>
          <div className="legal-links">
            <Link to="/terms">Terms of Service</Link>
            <Link to="/privacy">Privacy Policy</Link>
            <Link to="/security">Security</Link>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default EnhancedFooter;
