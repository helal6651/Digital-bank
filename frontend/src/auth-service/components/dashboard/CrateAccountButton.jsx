import React from "react";
import "./styles/CreateAccountButton.css";

const CreateAccountButton = ({ text, onClick }) => {
  return (
    <button className="create-account-button" onClick={onClick}>
      {text}
    </button>
  );
};

export default CreateAccountButton;
