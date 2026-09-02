import React from "react";
import {
  ArrowRight,
  Camera,
  MailCheck,
  Smartphone,
} from "lucide-react";
import { Link } from "react-router-dom";

function SellMobileCTA() {
  return (
    <section className="fixit-sell-cta-section">

      <div className="container">

        <div className="fixit-sell-cta">

          <div className="fixit-sell-cta-content">

            <span className="fixit-cta-label">
              Have an old phone?
            </span>

            <h2>
              Sell Your Mobile
              <br />
              <span>with FIXIT.</span>
            </h2>

            <p>
              Submit your mobile details and images.
              Verify your email and let our team review
              your device.
            </p>

            <Link
              to="/sell-your-mobile"
              className="fixit-button fixit-button-primary"
            >
              <span>Sell Your Mobile</span>
              <ArrowRight size={18} />
            </Link>

          </div>

          <div className="fixit-sell-steps">

            <div className="fixit-sell-step">
              <div>
                <Smartphone size={23} />
              </div>

              <span>
                Add mobile details
              </span>
            </div>

            <div className="fixit-sell-step">
              <div>
                <Camera size={23} />
              </div>

              <span>
                Upload up to 4 images
              </span>
            </div>

            <div className="fixit-sell-step">
              <div>
                <MailCheck size={23} />
              </div>

              <span>
                Verify your email
              </span>
            </div>

          </div>

        </div>

      </div>

    </section>
  );
}

export default SellMobileCTA;