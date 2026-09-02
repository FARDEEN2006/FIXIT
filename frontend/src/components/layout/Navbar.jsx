import React, { useEffect, useState } from "react";
import {
  Menu,
  X,
  Phone,
  MessageCircle,
} from "lucide-react";
import { NavLink, Link } from "react-router-dom";

import "../../styles/navbar.css";

function Navbar() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };

    handleScroll();

    window.addEventListener("scroll", handleScroll);

    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);

  const closeMobileMenu = () => {
    setMobileMenuOpen(false);
  };

  const navItems = [
    {
      label: "Home",
      path: "/",
    },
    {
      label: "Products",
      path: "/products",
    },
    {
      label: "Sell Your Mobile",
      path: "/sell-your-mobile",
    },
    {
      label: "About",
      path: "/about",
    },
    {
      label: "Contact",
      path: "/contact",
    },
  ];

  return (
    <header
      className={`fixit-navbar ${
        scrolled ? "fixit-navbar-scrolled" : ""
      }`}
    >
      <div className="fixit-navbar-container">

        {/* Logo */}
        <Link
          to="/"
          className="fixit-logo"
          onClick={closeMobileMenu}
          aria-label="FIXIT Mobile Sales & Services Home"
        >
          <div className="fixit-logo-image-wrapper">
            <img
              src="/assets/logo/logo.png"
              alt="FIXIT Mobile Sales & Services"
              className="fixit-logo-image"
              onError={(event) => {
                event.currentTarget.style.display = "none";
                event.currentTarget.nextElementSibling.style.display =
                  "flex";
              }}
            />

            <span className="fixit-logo-fallback">
              FIXIT
            </span>
          </div>

          <div className="fixit-logo-text">
            <strong>FIXIT</strong>
            <span>THE REPAIR COMPANY</span>
          </div>
        </Link>

        {/* Desktop Navigation */}
        <nav
          className="fixit-desktop-nav"
          aria-label="Main navigation"
        >
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `fixit-nav-link ${
                  isActive ? "active" : ""
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* Desktop Actions */}
        <div className="fixit-navbar-actions">

          <a
            href="tel:"
            className="fixit-nav-icon-button"
            aria-label="Call FIXIT"
          >
            <Phone size={18} />
          </a>

          <a
            href="https://wa.me/"
            target="_blank"
            rel="noopener noreferrer"
            className="fixit-nav-whatsapp"
            aria-label="Contact FIXIT on WhatsApp"
          >
            <MessageCircle size={18} />
            <span>WhatsApp</span>
          </a>

        </div>

        {/* Mobile menu button */}
        <button
          type="button"
          className="fixit-mobile-menu-button"
          onClick={() =>
            setMobileMenuOpen((previous) => !previous)
          }
          aria-label={
            mobileMenuOpen
              ? "Close navigation menu"
              : "Open navigation menu"
          }
          aria-expanded={mobileMenuOpen}
        >
          {mobileMenuOpen ? (
            <X size={26} />
          ) : (
            <Menu size={26} />
          )}
        </button>
      </div>

      {/* Mobile Navigation */}
      <div
        className={`fixit-mobile-menu ${
          mobileMenuOpen ? "open" : ""
        }`}
      >
        <nav aria-label="Mobile navigation">
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              onClick={closeMobileMenu}
              className={({ isActive }) =>
                `fixit-mobile-nav-link ${
                  isActive ? "active" : ""
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="fixit-mobile-actions">
          <a href="tel:" className="fixit-mobile-call">
            <Phone size={18} />
            Call FIXIT
          </a>

          <a
            href="https://wa.me/"
            target="_blank"
            rel="noopener noreferrer"
            className="fixit-mobile-whatsapp"
          >
            <MessageCircle size={18} />
            WhatsApp
          </a>
        </div>
      </div>
    </header>
  );
}

export default Navbar;