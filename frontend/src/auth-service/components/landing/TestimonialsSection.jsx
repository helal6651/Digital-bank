import React from "react";
import { FaQuoteLeft } from "react-icons/fa";

const TestimonialsSection = () => {
  const testimonials = [
    {
      quote:
        "Digital Banking has transformed how I manage my finances. The app is intuitive and the customer service is exceptional.",
      author: "Sarah Johnson",
      role: "Small Business Owner",
    },
    {
      quote:
        "I've been using Digital Banking for over a year now and I'm impressed by how easy it is to transfer money and track my spending.",
      author: "Michael Chen",
      role: "Software Engineer",
    },
    {
      quote:
        "The security features give me peace of mind, and the financial insights have helped me save for my first home.",
      author: "Emily Rodriguez",
      role: "Marketing Specialist",
    },
  ];

  return (
    <section className="testimonials-section">
      <div className="container">
        <div className="section-header">
          <h2>What Our Customers Say</h2>
          <p className="section-subtitle">
            Don't just take our word for it. Here's what our satisfied customers
            have to say.
          </p>
        </div>
        <div className="testimonials-grid">
          {testimonials.map((testimonial, index) => (
            <div key={index} className="testimonial-card">
              <div className="quote-icon">
                <FaQuoteLeft size={30} />
              </div>
              <p className="testimonial-quote">{testimonial.quote}</p>
              <div className="testimonial-author">
                <h4>{testimonial.author}</h4>
                <p>{testimonial.role}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default TestimonialsSection;
