import { useEffect } from "react";

export default function useScript(src, onLoad) {
  useEffect(() => {
    const script = document.createElement("script");
    script.src = src;
    script.async = true;
    script.defer = true;

    script.onload = () => {
      console.log(`Script loaded: ${src}`);
      if (onLoad) onLoad();
    };

    script.onerror = () => {
      console.error(`Failed to load script: ${src}`);
    };

    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, [src, onLoad]);
}
