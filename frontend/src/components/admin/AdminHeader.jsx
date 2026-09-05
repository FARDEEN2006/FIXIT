import React from "react";
import { useNavigate } from "react-router-dom";

const AdminHeader = ({ setSidebarOpen, activeSection }) => {
  const navigate = useNavigate();
  const titles = {
    dashboard: "Dashboard",
    products: "Products",
    listings: "Sell Your Mobile",
    enquiries: "Product Enquiries",
    services: "Services",
    store: "Store Information",
  };

  return (
    <header className="admin-header">
      <div className="admin-header-left">
        <button
          className="admin-menu-button"
          onClick={() => setSidebarOpen(true)}
          aria-label="Open menu"
        >
          ☰
        </button>

        <div>
          <span className="admin-header-label">ADMIN PANEL</span>
          <h2>{titles[activeSection] || "Dashboard"}</h2>
        </div>
      </div>

      <div className="admin-header-brand">
        <span>FIXIT</span>
        <small>THE REPAIR COMPANY</small>
      </div>

      <button
        className="admin-header-logout"
        onClick={() => {
          sessionStorage.removeItem("fixit_admin_token");
          sessionStorage.removeItem("fixit_admin_user");
          localStorage.removeItem("fixit_admin_token");
          localStorage.removeItem("fixit_admin_user");
          navigate("/admin/login");
        }}
      >
        Logout
      </button>
    </header>
  );
};

export default AdminHeader;