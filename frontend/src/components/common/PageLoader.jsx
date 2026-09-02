import React from "react";
import LoadingSpinner from "./LoadingSpinner";

function PageLoader() {
  return (
    <div className="fixit-page-loader">
      <div className="fixit-page-loader-content">
        <div className="fixit-loader-logo">
          FIXIT
        </div>

        <LoadingSpinner size={38} />

        <p>Loading...</p>
      </div>
    </div>
  );
}

export default PageLoader;