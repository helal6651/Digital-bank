import React, { useState } from "react";
import { Navbar, Footer } from "../components/layout";
import "./css/Dashboard.css";
import axios from "axios";

const userList = [

  {
    id: 2,
    name: "Jane Smith",
    email: "jane.smith@example.com",
    phone: "+9876543210",
    hasAccount: false,
  },

];

const Dashboard = () => {
  const [showForm, setShowForm] = useState(false); // Toggle for showing/hiding the form
  const [formData, setFormData] = useState({
    accountName: "",
    accountType: "",
    balance: "",
    currency: "",
    status: "active", // Default status
  });
  const [selectedUser, setSelectedUser] = useState(null); // Keep track of the selected user
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState(userList); // Manage user data with state

  // Open the Create Account form and select a user
  const openCreateAccountForm = (user) => {
    setSelectedUser(user);
    setShowForm(true);
  };

  // Handle input change in the form
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevState) => ({ ...prevState, [name]: value }));
  };

  // Handle form submission to create an account
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const payload = {
        userId: selectedUser.id,
        ...formData,
      };

      const response = await axios.post(
        "http://localhost:9492/api/accounts/create",
        payload
      );

      if (response.status === 201) {
        setMessage(`Account created successfully for ${selectedUser.name}!`);

        // Update the user's state to toggle "hasAccount" to true
        setUsers((prevUsers) =>
          prevUsers.map((user) =>
            user.id === selectedUser.id ? { ...user, hasAccount: true } : user
          )
        );
      } else {
        setMessage(`Failed to create account for ${selectedUser.name}.`);
      }
    } catch (error) {
      console.error("Error creating account:", error);
      setMessage("An error occurred while trying to create the account.");
    }

    setLoading(false);
    setShowForm(false);
    setSelectedUser(null);
  };

  return (
    <div className="content">
      <Navbar />
      <div className="container">
        <div className="dashboard">
          <h1>Dashboard</h1>

          {/* Feedback Message */}
          {message && <p className="feedback-message">{message}</p>}

          {/* User Info Table */}
          <div className="user-table">
            <h3>User Information</h3>
            <table className="styled-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone Number</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td>{user.phone}</td>
                    <td>
                      {user.hasAccount ? (
                        <button className="account-created-button" disabled>
                          Account Created
                        </button>
                      ) : (
                        <button
                          onClick={() => openCreateAccountForm(user)}
                          className="create-account-button"
                        >
                          Create Account
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Loading Window */}
          {loading && (
            <div className="loading-window">
              <p>Creating account...</p>
              <img
                src="https://i.gifer.com/ZZ5H.gif"
                alt="Loading spinner"
                style={{ width: "50px", height: "50px" }}
              />
            </div>
          )}

{/* Account Creation Form Modal */}
{showForm && selectedUser && (
  <div
    className="modal"
    style={{
      display: 'flex',
      visibility: 'visible',
      opacity: 1,
      position: 'fixed',
      top: '0',
      left: '0',
      width: '100%',
      height: '100%',
      backgroundColor: 'rgba(0, 0, 0, 0.6)',
      zIndex: 1000,
      justifyContent: 'center',
      alignItems: 'center',
    }}
  >
    <div
      className="modal-content"
      style={{
        padding: '20px 30px',
        backgroundColor: '#ffffff',
        borderRadius: '10px',
        maxWidth: '500px',
        width: '90%',
        boxShadow: '0 8px 16px rgba(0, 0, 0, 0.2)',
        fontFamily: `'Roboto', sans-serif`,
      }}
    >
      <h3
        style={{
          color: '#333333',
          fontSize: '24px',
          fontWeight: 'bold',
          marginBottom: '20px',
          textAlign: 'center',
        }}
      >
        Create Account for {selectedUser.name}
      </h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="accountName" className="form-label">
            Account Name <span style={{ color: 'red' }}>*</span>
          </label>
          <input
            type="text"
            id="accountName"
            name="accountName"
            value={formData.accountName}
            onChange={handleInputChange}
            placeholder="Enter account name"
            required
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="accountType" className="form-label">
            Account Type <span style={{ color: 'red' }}>*</span>
          </label>
          <select
            id="accountType"
            name="accountType"
            value={formData.accountType}
            onChange={handleInputChange}
            required
            className="form-select"
          >
            <option value="" disabled>
              Select Account Type
            </option>
            <option value="savings">Savings</option>
            <option value="checking">Checking</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="balance" className="form-label">
            Initial Balance <span style={{ color: 'red' }}>*</span>
          </label>
          <input
            type="number"
            id="balance"
            name="balance"
            value={formData.balance}
            onChange={handleInputChange}
            min="0"
            step="0.01"
            placeholder="Enter initial balance"
            required
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="currency" className="form-label">
            Currency <span style={{ color: 'red' }}>*</span>
          </label>
          <select
            id="currency"
            name="currency"
            value={formData.currency}
            onChange={handleInputChange}
            required
            className="form-select"
          >
            <option value="" disabled>
              Select Currency
            </option>
            <option value="USD">USD</option>
            <option value="BDT">BDT</option>
            <option value="EUR">EUR</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="status" className="form-label">
            Account Status
          </label>
          <select
            id="status"
            name="status"
            value={formData.status}
            onChange={handleInputChange}
            className="form-select"
          >
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
            <option value="closed">Closed</option>
          </select>
        </div>

        <div className="form-buttons" style={{ textAlign: 'center', marginTop: '20px' }}>
          <button type="submit" className="form-submit-button">
            Create Account
          </button>
          <button
            type="button"
            onClick={() => {
              setShowForm(false);
              setSelectedUser(null);
            }}
            className="form-cancel-button"
          >
            Cancel
          </button>
        </div>
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