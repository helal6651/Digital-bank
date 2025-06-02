import React from "react";
import Card from "./Card";
import {
  FaShieldAlt,
  FaRegCreditCard,
  FaChartLine,
  FaMobileAlt,
} from "react-icons/fa";

const FeaturesSection = () => {
  const features = [
    {
      icon: <FaShieldAlt size={40} />,
      title: "Bank-Level Security",
      description:
        "Your money and data are protected with industry-leading security measures and encryption.",
    },
    {
      icon: <FaRegCreditCard size={40} />,
      title: "Instant Transactions",
      description:
        "Transfer money instantly to any account, regardless of the bank or location.",
    },
    {
      icon: <FaChartLine size={40} />,
      title: "Financial Insights",
      description:
        "Get personalized insights and analytics to better manage your finances.",
    },
    {
      icon: <FaMobileAlt size={40} />,
      title: "Mobile Banking",
      description:
        "Access your accounts, make transactions, and manage your finances on the go.",
    },
  ];

  return (
    <section className="features-section">
      <div className="container">
        <div className="section-header">
          <h2>Why Choose Digital Banking</h2>
          <p className="section-subtitle">
            Experience banking that's designed for the digital age with features
            that make your financial life easier.
          </p>
        </div>
        <div className="features-grid">
          {features.map((feature, index) => (
            <Card
              key={index}
              icon={feature.icon}
              title={feature.title}
              description={feature.description}
            />
          ))}
        </div>
      </div>
    </section>
  );
};

export default FeaturesSection;
