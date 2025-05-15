import React from "react";
import { Navbar, Footer } from "../components/layout";
import CreateAccountSection from "../components/dashboard/CreateAccountSection";
import { useAuth } from "../context/AuthContext";

const Dashboard = () => {
  const { isLoggedIn } = useAuth();

  const handleCreateAccount = () => {
    console.log("Create Account button clicked");
    console.log("isLoggedIn button: ", isLoggedIn);
  };

  return (
    <div className="content">
      <Navbar />
      <div className="container">
        <div className="dashboard">
          <CreateAccountSection onCreateAccount={handleCreateAccount} />
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Dashboard;
