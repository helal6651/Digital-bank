import React from "react";
import { Navbar } from "../components/layout";
import HeroSection from "../components/landing/HeroSection";
import FeaturesSection from "../components/landing/FeaturesSection";
import ServicesSection from "../components/landing/ServicesSection";
import TestimonialsSection from "../components/landing/TestimonialsSection";
import CTASection from "../components/landing/CTASection";
import EnhancedFooter from "../components/landing/EnhancedFooter";

const LandingPage = () => {
  return (
    <div className="content">
      <Navbar />
      <HeroSection />
      <FeaturesSection />
      <ServicesSection />
      <TestimonialsSection />
      <CTASection />
      <EnhancedFooter />
    </div>
  );
};

export default LandingPage;
