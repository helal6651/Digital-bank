import React from "react";

const Card = ({ icon, title, description }) => {
  return (
    <div className="feature-card">
      <div className="card-icon">{icon}</div>
      <h3 className="card-title">{title}</h3>
      <p className="card-description">{description}</p>
    </div>
  );
};

export default Card;
