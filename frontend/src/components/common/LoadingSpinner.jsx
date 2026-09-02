import React from "react";
import { LoaderCircle } from "lucide-react";

function LoadingSpinner({ size = 24 }) {
  return (
    <LoaderCircle
      className="fixit-spinner"
      size={size}
      strokeWidth={2.5}
      aria-label="Loading"
    />
  );
}

export default LoadingSpinner;