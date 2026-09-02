import React from "react";
import { Package, ShieldCheck, ShoppingBag } from "lucide-react";

function ProductHero() {
  return (
    <section className="fixit-products-hero">

      <div className="container">

        <div className="fixit-products-hero-content">

          <div className="fixit-products-hero-icon">
            <ShoppingBag size={30} />
          </div>

          <span className="section-label">
            FIXIT MOBILE SALES & SERVICES
          </span>

          <h1>
            Explore Our
            <span> Products</span>
          </h1>

          <p>
            Browse mobile products available from FIXIT.
            Product availability and details are updated by
            our team.
          </p>

          <div className="fixit-products-hero-trust">

            <div>
              <Package size={18} />
              <span>Quality Products</span>
            </div>

            <div>
              <ShieldCheck size={18} />
              <span>Trusted Service</span>
            </div>

          </div>

        </div>

      </div>

    </section>
  );
}

export default ProductHero;