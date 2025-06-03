import React from "react";
import { Link } from "react-router-dom";

const ServiceCard = ({ title, description, features, color }) => {
  return (
    <div className="service-card" style={{ borderTop: `4px solid ${color}` }}>
      <h3 className="service-title">{title}</h3>
      <p className="service-description">{description}</p>
      <ul className="service-features">
        {features.map((feature, index) => (
          <li key={index}>{feature}</li>
        ))}
      </ul>
      <Link to="/register" className="btn btn-dark btn-sm service-btn">
        Learn More
      </Link>
    </div>
  );
};

export default ServiceCard;
