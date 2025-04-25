import axios from 'axios';

const API_URL = 'http://user-service/v1/user/';

export const register = async (userData) => {
  console.log("register() function from authService.js : ");
  const response = await axios.post(`${API_URL}register`, userData);
  
  const { result, code, message } = response.data;
  console.log(`Email: ${result?.email}`);
  console.log(`Response Code: ${code}`);
  console.log(`Message: ${message[0]}`); 
  
  return response.data;
};

export const login = async (userData) => {
  const response = await axios.post(`${API_URL}login`, userData);
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('user');
};