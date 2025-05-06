import { BrowserRouter as Router } from "react-router-dom";
import { AuthProvider } from "./auth-service/context/AuthContext";
import AppContent from "./routing/AppContent";
import "./App.css";

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

export default App;
