import React from "react";
import { ArrowRight, MessageCircle, Wrench } from "lucide-react";
import { Link } from "react-router-dom";

function Hero() {
  return (
    <section className="fixit-hero">

      <div className="fixit-hero-background">
        <img
          src="/assets/background/background.png"
          alt=""
          aria-hidden="true"
        />
      </div>

      <div className="fixit-hero-overlay" />

      <div className="container fixit-hero-container">

        <div className="fixit-hero-content">

          <div className="fixit-hero-brand">
            <img
              src="/assets/logo/logo.png"
              alt="FIXIT Mobile Sales & Services"
            />
          </div>

          <div className="fixit-hero-label">
            <Wrench size={17} />
            <span>THE REPAIR COMPANY</span>
          </div>

          <h1>
            Your Mobile.
            <br />
            <span>Our Expertise.</span>
          </h1>

          <p>
            Professional mobile sales and repair services
            you can trust — from everyday phone repairs
            to advanced technical service.
          </p>

          <div className="fixit-hero-actions">

            <Link
              to="/products"
              className="fixit-button fixit-button-primary"
            >
              <span>Explore Products</span>
              <ArrowRight size={18} />
            </Link>

            <Link
              to="/contact"
              className="fixit-hero-secondary-button"
            >
              <MessageCircle size={18} />
              <span>Contact FIXIT</span>
            </Link>

          </div>

          <div className="fixit-hero-trust">

            <div>
              <strong>Professional</strong>
              <span>Service</span>
            </div>

            <div className="fixit-hero-divider" />

            <div>
              <strong>Trusted</strong>
              <span>Support</span>
            </div>

            <div className="fixit-hero-divider" />

            <div>
              <strong>Quality</strong>
              <span>Repairs</span>
            </div>

          </div>

        </div>

      </div>

      <div className="fixit-hero-bottom-shape" />

    </section>
  );
}

export default Hero;