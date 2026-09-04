import React, { useState } from "react";
import AdminHeader from "./AdminHeader";
import AdminSidebar from "./AdminSidebar";
import ProductManager from "./ProductManager";
import ListingManager from "./ListingManager";

const AdminLayout = () => {
  const [activeSection, setActiveSection] = useState("dashboard");
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const renderContent = () => {
    switch (activeSection) {
      case "products":
        return <ProductManager />;

      case "listings":
        return <ListingManager />;

      case "dashboard":
      default:
        return <DashboardHome setActiveSection={setActiveSection} />;
    }
  };

  return (
    <div className="admin-layout">
      <AdminSidebar
        activeSection={activeSection}
        setActiveSection={setActiveSection}
        sidebarOpen={sidebarOpen}
        setSidebarOpen={setSidebarOpen}
      />

      <div className="admin-main">
        <AdminHeader
          setSidebarOpen={setSidebarOpen}
          activeSection={activeSection}
        />

        <main className="admin-content">{renderContent()}</main>
      </div>
    </div>
  );
};

const DashboardHome = ({ setActiveSection }) => {
  return (
    <section className="admin-dashboard">
      <div className="admin-page-heading">
        <div>
          <span className="admin-eyebrow">FIXIT ADMIN</span>
          <h1>Dashboard</h1>
          <p>Manage your products and second-hand mobile listings.</p>
        </div>
      </div>

      <div className="admin-stat-grid">
        <button
          className="admin-stat-card"
          onClick={() => setActiveSection("products")}
        >
          <div className="admin-stat-icon">📱</div>

          <div>
            <span>Products</span>
            <strong>Manage</strong>
          </div>
        </button>

        <button
          className="admin-stat-card"
          onClick={() => setActiveSection("listings")}
        >
          <div className="admin-stat-icon">📦</div>

          <div>
            <span>Mobile Listings</span>
            <strong>Review</strong>
          </div>
        </button>
      </div>

      <div className="admin-welcome-card">
        <div>
          <span className="admin-eyebrow">STORE CONTROL</span>

          <h2>FIXIT Mobile Sales & Services</h2>

          <p>
            Use the sidebar to manage products and second-hand mobile
            listings.
          </p>
        </div>

        <button
          onClick={() => setActiveSection("products")}
          className="admin-primary-button"
        >
          Manage Products →
        </button>
      </div>
    </section>
  );
};

export default AdminLayout;