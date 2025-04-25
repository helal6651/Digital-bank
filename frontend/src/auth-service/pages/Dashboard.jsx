import React from 'react';
import { Navbar, Footer } from '../components/layout';

const Dashboard = () => {
  return (
    <div className="content">
      <Navbar />
      <div className="container">
        <div className="dashboard">
          <h1>Welcome to your Dashboard</h1>
          <p>You are logged in successfully!</p>
          <div className="dashboard-content">
            <div className="card">
              <h3>Account Overview</h3>
              <p>Your personal banking information and activities will appear here.</p>
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Dashboard;