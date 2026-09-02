import React from "react";
import {
  Phone,
  Mail,
  MapPin,
  MessageCircle,
  ArrowUpRight,
} from "lucide-react";
import { Link } from "react-router-dom";

function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="fixit-footer">

      <div className="container">

        <div className="fixit-footer-main">

          {/* Brand */}
          <div className="fixit-footer-brand">

            <Link
              to="/"
              className="fixit-footer-logo"
            >
              <div className="fixit-footer-logo-mark">
                FIXIT
              </div>

              <div>
                <strong>
                  FIXIT Mobile Sales & Services
                </strong>

                <span>
                  THE REPAIR COMPANY
                </span>
              </div>
            </Link>

            <p>
              Professional mobile sales, repair and
              service — from everyday repairs to
              advanced motherboard-level service.
            </p>

            <div className="fixit-footer-socials">

              <a
                href="https://wa.me/"
                target="_blank"
                rel="noopener noreferrer"
                aria-label="WhatsApp"
              >
                <MessageCircle size={19} />
              </a>

              <a
                href="tel:"
                aria-label="Call FIXIT"
              >
                <Phone size={19} />
              </a>

              <a
                href="mailto:"
                aria-label="Email FIXIT"
              >
                <Mail size={19} />
              </a>

            </div>
          </div>

          {/* Quick links */}
          <div className="fixit-footer-column">

            <h3>Quick Links</h3>

            <Link to="/">
              Home
              <ArrowUpRight size={15} />
            </Link>

            <Link to="/products">
              Products
              <ArrowUpRight size={15} />
            </Link>

            <Link to="/sell-your-mobile">
              Sell Your Mobile
              <ArrowUpRight size={15} />
            </Link>

            <Link to="/about">
              About Us
              <ArrowUpRight size={15} />
            </Link>

            <Link to="/contact">
              Contact
              <ArrowUpRight size={15} />
            </Link>

          </div>

          {/* Services */}
          <div className="fixit-footer-column">

            <h3>Our Services</h3>

            <span>Mobile Repair</span>
            <span>Display Service</span>
            <span>Battery Service</span>
            <span>Charging Service</span>
            <span>Software Service</span>
            <span>Advanced Repair</span>

          </div>

          {/* Contact */}
          <div className="fixit-footer-column">

            <h3>Contact</h3>

            <div className="fixit-footer-contact-item">
              <Phone size={18} />
              <span>Phone</span>
            </div>

            <div className="fixit-footer-contact-item">
              <MessageCircle size={18} />
              <span>WhatsApp</span>
            </div>

            <div className="fixit-footer-contact-item">
              <Mail size={18} />
              <span>Email</span>
            </div>

            <div className="fixit-footer-contact-item">
              <MapPin size={18} />
              <span>Store Location</span>
            </div>

          </div>

        </div>

        <div className="fixit-footer-bottom">

          <p>
            © {currentYear} FIXIT Mobile Sales &
            Services. All rights reserved.
          </p>

          <p>
            THE REPAIR COMPANY
          </p>

        </div>

      </div>
    </footer>
  );
}

export default Footer;