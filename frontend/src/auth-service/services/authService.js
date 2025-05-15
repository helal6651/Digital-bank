import axios from 'axios';

const API_URL = 'http://localhost:9491/v1/api/user/';
//const API_URL = 'http://127.0.0.1:51220/v1/api/user/';
//const API_URL = 'http://user-service:9491/v1/api/user/';
const API_URL_LOGIN = "http://localhost:9491/v1/api/";

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
  try {
    const response = await axios.post(`${API_URL_LOGIN}authenticate`, userData);
    
    // If login is successful, store tokens
    if (response.data.code === "200") {
      storeTokens(response.data.result);
    }
    
    return response.data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

// Store JWT tokens securely
export const storeTokens = (tokens) => {
  localStorage.setItem("accessToken", tokens.accessToken);
  localStorage.setItem("refreshToken", tokens.refreshToken);

  // Set default Authorization header for all future requests
  axios.defaults.headers.common["Authorization"] = `Bearer ${tokens.accessToken}`;
};

// Get the current access token
export const getAccessToken = () => {
  return localStorage.getItem('accessToken');
};

// Check if user is authenticated
export const isAuthenticated = () => {
  return !!getAccessToken();
};

// Logout user
export const logout = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  delete axios.defaults.headers.common['Authorization'];
};

export const postLoginToken = async (token) => {
  const API_URL = import.meta.env.VITE_API_URL;
  console.log("idToken: ", token);

  try {
    const response = await fetch(`${API_URL_LOGIN}authenticate`, {
      method: "POST",
      credentials: "include", 
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ snsAccessToken: token, type: 2 }),
    });

    if (!response.ok) {
      throw new Error("Bad server condition");
    }
    const res = await response.json();

    if (res && res.code === "200" && res.result) {
      storeTokens(res.result); 
    }
    console.log("postLoginToken response: ", res.result);
    return res;
  } catch (e) {
    console.error("postLoginToken Error:", e.message);
    return null;
  }
};