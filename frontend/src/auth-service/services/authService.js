import axios from 'axios';

const API_URL = 'http://localhost:9491/v1/api/user/';
//const API_URL = 'http://127.0.0.1:51220/v1/api/user/';
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
//    const response = await axios.post(`${API_URL_LOGIN}authenticate`, userData);
//
//    // If login is successful, store tokens
//    if (response.data.code === "200") {
//      storeTokens(response.data.result);
//    }

       // Mocked response data
        const mockedResponse = {
          code: "200",
          message: "Login successful",
          result: {
            accessToken: "mockAccessToken12345",
            refreshToken: "mockRefreshToken67890",
            user: {
              id: "1",
              username: userData.username,
              email: "dummyemail@example.com",
            },
          },
        };

        // Simulate network delay
        await new Promise((resolve) => setTimeout(resolve, 1000));

        // If login is successful, store tokens
        if (mockedResponse.code === "200") {
          storeTokens(mockedResponse.result);
        }

        return mockedResponse;

    return response.data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

// Store JWT tokens securely
export const storeTokens = (tokens) => {
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
  
  // Set default Authorization header for all future requests
  axios.defaults.headers.common['Authorization'] = `Bearer ${tokens.accessToken}`;
};

// Get the current access token
export const getAccessToken = () => {
  return localStorage.getItem('accessToken');
};

// Check if user is authenticated
export const isAuthenticated = () => {
 // return !!getAccessToken();
 return true;
};

// Logout user
export const logout = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  delete axios.defaults.headers.common['Authorization'];
};