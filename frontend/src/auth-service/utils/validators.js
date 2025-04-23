export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(String(email).toLowerCase());
};

export const validatePassword = (password) => {
  return password.length >= 6; // Password must be at least 6 characters long
};

export const validateUsername = (username) => {
  const re = /^[a-zA-Z0-9_]{3,15}$/; // Username must be 3-15 characters long and can include letters, numbers, and underscores
  return re.test(String(username));
};