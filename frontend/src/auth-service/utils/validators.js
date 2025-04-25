export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(String(email).toLowerCase());
};

export const validatePassword = (password) => {
  return password.length >= 6; 
};

export const validateUsername = (username) => {
  const re = /^[a-zA-Z0-9_]{3,15}$/; 
  return re.test(String(username));
};