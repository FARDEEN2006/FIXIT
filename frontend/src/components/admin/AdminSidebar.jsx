import React from "react";
import { useNavigate } from "react-router-dom";

const AdminSidebar = ({
  activeSection,
  setActiveSection,
  sidebarOpen,
  setSidebarOpen,
}) => {
  const navigate = useNavigate();

  const menuItems = [
    {
      id: "dashboard",
      label: "Dashboard",
      icon: "▦",
    },
    {
      id: "products",
      label: "Products",
      icon: "📱",
    },
    {
      id: "listings",
      label: "Sell Mobile",
      icon: "📦",
    },
  ];

  const handleNavigation = (id) => {
    setActiveSection(id);
    setSidebarOpen(false);
  };

  const handleLogout = () => {
    sessionStorage.removeItem("fixit_admin_token");
    sessionStorage.removeItem("fixit_admin_user");
    localStorage.removeItem("fixit_admin_token");
    localStorage.removeItem("fixit_admin_user");

    navigate("/admin/login");
  };

  return (
    <>
      {sidebarOpen && (
        <div
          className="admin-sidebar-overlay"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <aside className={`admin-sidebar ${sidebarOpen ? "open" : ""}`}>
        <div className="admin-sidebar-top">
          <div className="admin-logo">
            <div className="admin-logo-mark">F</div>

            <div>
              <strong>FIXIT</strong>
              <span>THE REPAIR COMPANY</span>
            </div>
          </div>

          <button
            className="admin-sidebar-close"
            onClick={() => setSidebarOpen(false)}
          >
            ×
          </button>
        </div>

        <div className="admin-sidebar-divider" />

        <nav className="admin-navigation">
          <span className="admin-navigation-title">MANAGEMENT</span>

          {menuItems.map((item) => (
            <button
              key={item.id}
              className={`admin-nav-item ${
                activeSection === item.id ? "active" : ""
              }`}
              onClick={() => handleNavigation(item.id)}
            >
              <span className="admin-nav-icon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        <div className="admin-sidebar-bottom">
          <button
            className="admin-nav-item admin-store-button"
            onClick={() => navigate("/")}
          >
            <span className="admin-nav-icon">↗</span>
            <span>View Website</span>
          </button>

          <button
            className="admin-nav-item admin-logout-button"
            onClick={handleLogout}
          >
            <span className="admin-nav-icon">⇥</span>
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
};

export default AdminSidebar;