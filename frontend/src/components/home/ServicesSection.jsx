import React, { useRef } from "react";
import { ArrowRight, Wrench } from "lucide-react";
import { Link } from "react-router-dom";

import ServiceCard from "./ServiceCard";

function ServicesSection({ services = [] }) {
  const scrollRef = useRef(null);

  const scrollServices = (direction) => {
    if (!scrollRef.current) {
      return;
    }

    scrollRef.current.scrollBy({
      left: direction === "right" ? 320 : -320,
      behavior: "smooth",
    });
  };

  return (
    <section className="section fixit-services-section">

      <div className="container">

        <div className="fixit-services-header">

          <div>
            <span className="section-label">
              What We Do
            </span>

            <h2 className="section-title">
              Mobile Services
            </h2>

            <p className="section-description">
              Professional mobile service and repair,
              handled with care and technical expertise.
            </p>
          </div>

          <div className="fixit-services-controls">

            <button
              type="button"
              onClick={() => scrollServices("left")}
              aria-label="Scroll services left"
            >
              ←
            </button>

            <button
              type="button"
              onClick={() => scrollServices("right")}
              aria-label="Scroll services right"
            >
              →
            </button>

            <Link to="/contact">
              View All
              <ArrowRight size={16} />
            </Link>

          </div>

        </div>

        {services.length > 0 ? (
          <div
            className="fixit-services-scroller"
            ref={scrollRef}
          >
            {services.map((service) => (
              <ServiceCard
                key={service.id || service.name}
                service={service}
              />
            ))}
          </div>
        ) : (
          <div className="fixit-services-empty">

            <div className="fixit-services-empty-icon">
              <Wrench size={28} />
            </div>

            <div>
              <h3>Our services</h3>

              <p>
                Available mobile services will appear here.
              </p>
            </div>

          </div>
        )}

      </div>

    </section>
  );
}

export default ServicesSection;