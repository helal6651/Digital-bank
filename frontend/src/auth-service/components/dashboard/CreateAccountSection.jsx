import React from "react";
import CreateAccountButton from "./CrateAccountButton";
import "./styles/CreateAccountSection.css";

const CreateAccountSection = ({ onCreateAccount }) => {
  return (
    <div className="create-account-section">
      <div className="text-content">
        <h2>Create a New Bank Account</h2>
        <p>
          Manage your finances better by creating multiple bank accounts. You
          can create personal or business accounts as needed.
        </p>
      </div>
      <div className="button-container">
        <CreateAccountButton text="Create Account" onClick={onCreateAccount} />
      </div>
    </div>
  );
};

export default CreateAccountSection;
