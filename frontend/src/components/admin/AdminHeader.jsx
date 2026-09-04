import React from "react";

const AdminHeader = ({ setSidebarOpen, activeSection }) => {
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
    </header>
  );
};

export default AdminHeader;