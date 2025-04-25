import React from 'react';

const Button = ({ type, children, disabled }) => {
  return (
    <button type={type} disabled={disabled} className="form-button">
      {children}
    </button>
  );
};

export default Button;