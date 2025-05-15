import { useEffect, useRef } from "react";
import useScript from "../../hooks/useScript";

export default function GoogleLogin({ onGoogleSignIn }) {
  const googleSignInButton = useRef(null);

  // Dynamically load the Google Identity Services library
  useScript("https://accounts.google.com/gsi/client", () => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

    if (!clientId) {
      console.error("Google Client ID is not defined. Check your .env file.");
      return;
    }

    if (window.google && window.google.accounts && window.google.accounts.id) {
      // Initialize the Google Identity Services library
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: onGoogleSignIn,
      });

      // Render the Google Sign-In button
      window.google.accounts.id.renderButton(googleSignInButton.current, {
        theme: "filled_blue",
        size: "large",
        text: "signin_with",
        width: "328",
      });

      console.log("Google Identity Services initialized");
    } else {
      console.error("Google Identity Services library is not loaded.");
    }
  });

  return <div ref={googleSignInButton}></div>;
}
