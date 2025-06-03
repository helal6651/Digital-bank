import React from "react";
import { Link } from "react-router-dom";

const CTASection = () => {
  return (
    <section className="cta-section">
      <div className="container">
        <div className="cta-content">
          <h2>Ready to Transform Your Banking Experience?</h2>
          <p>
            Join thousands of satisfied customers who have made the switch to
            Digital Banking. It only takes a few minutes to get started.
          </p>
          <Link to="/register" className="btn btn-dark btn-lg">
            Open an Account Today
          </Link>
        </div>
      </div>
    </section>
  );
};

export default CTASection;
