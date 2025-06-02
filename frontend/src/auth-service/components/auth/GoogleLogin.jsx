// GoogleLogin.jsx
import { useEffect, useRef } from "react";

export default function GoogleLogin({ onGoogleSignIn }) {
  const googleSignInButton = useRef(null);

  useEffect(() => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

    if (!clientId) {
      console.error("Google Client ID is not defined.");
      return;
    }

    const loadGoogleScript = () => {
      const script = document.createElement("script");
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      script.onload = () => {
        if (window.google && window.google.accounts.id) {
          window.google.accounts.id.initialize({
            client_id: clientId,
            callback: onGoogleSignIn,
          });

          window.google.accounts.id.renderButton(googleSignInButton.current, {
            theme: "filled_blue",
            size: "large",
            text: "signin_with",
            width: "328",
          });

          console.log("Google Sign-In initialized");
        }
      };
      document.body.appendChild(script);
    };

    if (!window.google || !window.google.accounts) {
      loadGoogleScript();
    } else {
      // Already loaded
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: onGoogleSignIn,
      });

      window.google.accounts.id.renderButton(googleSignInButton.current, {
        theme: "filled_blue",
        size: "large",
        text: "signin_with",
        width: "328",
      });
    }
  }, [onGoogleSignIn]);

  return <div ref={googleSignInButton}></div>;
}
