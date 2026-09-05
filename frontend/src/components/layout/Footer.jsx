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

            <a className="fixit-footer-contact-item" href="tel:+918870783647">
              <Phone size={18} />
              <span>Phone: 8870783647</span>
            </a>

            <a
              className="fixit-footer-contact-item"
              href="https://wa.me/918870783647"
              target="_blank"
              rel="noopener noreferrer"
            >
              <MessageCircle size={18} />
              <span>WhatsApp: 8870783647</span>
            </a>

            <a
              className="fixit-footer-contact-item"
              href="mailto:fixitmobileservicess@gmail.com"
            >
              <Mail size={18} />
              <span>Email: fixitmobileservicess@gmail.com</span>
            </a>

            <a
              className="fixit-footer-contact-item"
              href="https://www.google.com/maps/search/?api=1&query=Ground+Floor%2C+Shop+No.+7+%26+34%2C+Perris+Plaza%2C+Municipality+Building%2C+Anna+Salai+%28Backside+of+the+Bus+Stand%29%2C+Karur%2C+Tamil+Nadu+-+639001"
              target="_blank"
              rel="noopener noreferrer"
            >
              <MapPin size={18} />
              <span>Store Location: Ground Floor, Shop No. 7 &amp; 34, Perris Plaza, Municipality Building, Anna Salai (Backside of the Bus Stand), Karur, Tamil Nadu - 639001</span>
            </a>

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