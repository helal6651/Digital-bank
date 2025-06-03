import React from "react";
import ServiceCard from "./ServiceCard";

const ServicesSection = () => {
  const services = [
    {
      title: "Personal Banking",
      description:
        "Manage your everyday finances with checking and savings accounts that work for you.",
      features: [
        "No monthly fees",
        "High-interest savings",
        "Overdraft protection",
        "Bill pay",
      ],
      color: "#343a40",
    },
    {
      title: "Business Banking",
      description:
        "Comprehensive solutions for businesses of all sizes, from startups to enterprises.",
      features: [
        "Business checking",
        "Merchant services",
        "Business loans",
        "Payroll services",
      ],
      color: "#007bff",
    },
    {
      title: "Investment Services",
      description:
        "Grow your wealth with our range of investment options and expert guidance.",
      features: [
        "Retirement planning",
        "Portfolio management",
        "Stock trading",
        "Investment advice",
      ],
      color: "#28a745",
    },
  ];

  return (
    <section className="services-section">
      <div className="container">
        <div className="section-header">
          <h2>Our Services</h2>
          <p className="section-subtitle">
            Comprehensive financial solutions tailored to meet your personal and
            business needs.
          </p>
        </div>
        <div className="services-grid">
          {services.map((service, index) => (
            <ServiceCard
              key={index}
              title={service.title}
              description={service.description}
              features={service.features}
              color={service.color}
            />
          ))}
        </div>
      </div>
    </section>
  );
};

export default ServicesSection;
