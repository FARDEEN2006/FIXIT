import React from "react";
import {
  ArrowRight,
  MapPin,
  Phone,
} from "lucide-react";
import { Link } from "react-router-dom";

function StoreInfoSection() {
  const gallery = [
    "/assets/design/images (1).png",
    "/assets/design/images (2).png",
    "/assets/design/images (3).png",
    "/assets/design/images (4).png",
  ];

  return (
    <section className="section fixit-store-section">

      <div className="container">

        <div className="fixit-store-grid">

          <div className="fixit-store-gallery">

            <div className="fixit-store-gallery-main">
              <img
                src={gallery[0]}
                alt="FIXIT Mobile Sales & Services"
              />
            </div>

            <div className="fixit-store-gallery-small">
              <img
                src={gallery[1]}
                alt="FIXIT store"
              />

              <img
                src={gallery[2]}
                alt="FIXIT mobile service"
              />

              <img
                src={gallery[3]}
                alt="FIXIT service environment"
              />
            </div>

          </div>

          <div className="fixit-store-content">

            <span className="section-label">
              Visit FIXIT
            </span>

            <h2 className="section-title">
              Your Local Mobile
              <br />
              Service Partner
            </h2>

            <p>
              From mobile sales to repairs and technical
              service, FIXIT is focused on providing a
              dependable experience for every customer.
            </p>

            <div className="fixit-store-details">

              <div>
                <MapPin size={21} />

                <div>
                  <strong>Store Location</strong>
                  <span>
                    Location details coming from FIXIT.
                  </span>
                </div>
              </div>

              <div>
                <Phone size={21} />

                <div>
                  <strong>Contact Us</strong>
                  <span>
                    Contact details will be connected
                    from the store information.
                  </span>
                </div>
              </div>

            </div>

            <Link
              to="/contact"
              className="fixit-button fixit-button-secondary"
            >
              <span>Contact & Location</span>
              <ArrowRight size={18} />
            </Link>

          </div>

        </div>

      </div>

    </section>
  );
}

export default StoreInfoSection;