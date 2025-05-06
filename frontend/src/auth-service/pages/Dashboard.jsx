import React, { useState } from 'react';
import { Navbar, Footer } from '../components/layout';
import './css/Dashboard.css'; // Optional: Add CSS styles for customization.
import axios from 'axios';

// Sample User Info (This can come from props or context)
const userInfo = {
  id: 2, // Example User ID (this will be hidden in the form)
  name: 'John Doe', // Replace with actual user data
  email: 'john.doe@example.com',
  phone: '+1234567890',
};

const Dashboard = () => {
  const [showForm, setShowForm] = useState(false); // Toggle for showing/hiding the form
  const [formData, setFormData] = useState({
    accountName: '',
    accountType: '',
    balance: '',
    currency: '',
    status: 'active', // Default status
  });
  const [message, setMessage] = useState('');

  // Handle input change in the form
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        userId: userInfo.id,
        ...formData,
      };
      const response = await axios.post('http://localhost:9492/api/accounts/create', payload);
      if (response.status === 201) {
        setMessage('Account created successfully!');
      } else {
        setMessage('Failed to create account.');
      }
    } catch (error) {
      console.error('Error creating account:', error);
      setMessage('An error occurred while trying to create the account.');
    }
    setShowForm(false); // Close the form modal after submission
  };

  return (
    <div className="content">
      <Navbar />
      <div className="container">
        <div className="dashboard">
          <h1>Welcome to your Dashboard</h1>
          <p>You are logged in successfully!</p>

          {/* User Info Section */}
          <div className="user-info">
            <h3>User Information</h3>
            <p><strong>Name:</strong> {userInfo.name}</p>
            <p><strong>Email:</strong> {userInfo.email}</p>
            <p><strong>Phone Number:</strong> {userInfo.phone}</p>
          </div>

          {/* Create Account Button */}
          <button onClick={() => setShowForm(true)} className="create-account-button">
            Create Account
          </button>

          {/* Feedback Message */}
          {message && <p className="feedback-message">{message}</p>}

          {/* Account Creation Form Modal */}
          {showForm && (
            <div className="modal">
              <div className="modal-content">
                <h3>Create Account</h3>
                <form onSubmit={handleSubmit}>
                  <div className="form-group">
                    <label htmlFor="accountName">Account Name</label>
                    <input
                      type="text"
                      id="accountName"
                      name="accountName"
                      value={formData.accountName}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="accountType">Account Type</label>
                    <select
                      id="accountType"
                      name="accountType"
                      value={formData.accountType}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">Select Type</option>
                      <option value="savings">Savings</option>
                      <option value="checking">Checking</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label htmlFor="balance">Initial Balance</label>
                    <input
                      type="number"
                      id="balance"
                      name="balance"
                      value={formData.balance}
                      onChange={handleInputChange}
                      min="0"
                      step="0.01"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="currency">Currency</label>
                    <select
                      id="currency"
                      name="currency"
                      value={formData.currency}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">Select Currency</option>
                      <option value="USD">USD</option>
                      <option value="BDT">BDT</option>
                      <option value="EUR">EUR</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label htmlFor="status">Account Status</label>
                    <select
                      id="status"
                      name="status"
                      value={formData.status}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="active">Active</option>
                      <option value="inactive">Inactive</option>
                      <option value="closed">Closed</option>
                    </select>
                  </div>
                  <button type="submit" className="submit-button">Submit</button>
                  <button onClick={() => setShowForm(false)} className="cancel-button">
                    Cancel
                  </button>
                </form>
              </div>
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Dashboard;