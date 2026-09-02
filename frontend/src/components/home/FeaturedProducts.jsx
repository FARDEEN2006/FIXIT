import React from "react";
import { ArrowRight, PackageOpen } from "lucide-react";
import { Link } from "react-router-dom";

function FeaturedProducts({ products = [] }) {
  return (
    <section className="section fixit-featured-products">

      <div className="container">

        <div className="fixit-products-header">

          <div>
            <span className="section-label">
              Shop With FIXIT
            </span>

            <h2 className="section-title">
              Featured Products
            </h2>

            <p className="section-description">
              Browse products available from FIXIT Mobile
              Sales & Services.
            </p>
          </div>

          <Link
            to="/products"
            className="fixit-text-link"
          >
            View Products
            <ArrowRight size={17} />
          </Link>

        </div>

        {products.length > 0 ? (
          <div className="fixit-featured-products-grid">

            {products.slice(0, 4).map((product) => (
              <Link
                to={`/products/${product.id}`}
                className="fixit-home-product-card"
                key={product.id}
              >

                <div className="fixit-home-product-image">
                  <img
                    src={product.image_url}
                    alt={product.name}
                  />
                </div>

                <div className="fixit-home-product-info">
                  <h3>{product.name}</h3>

                  <strong>
                    ₹{Number(product.price).toLocaleString("en-IN")}
                  </strong>
                </div>

              </Link>
            ))}

          </div>
        ) : (
          <div className="fixit-products-empty">

            <div className="fixit-products-empty-icon">
              <PackageOpen size={30} />
            </div>

            <h3>Products coming soon</h3>

            <p>
              Available products will appear here once
              they are added by FIXIT.
            </p>

            <Link
              to="/products"
              className="fixit-button fixit-button-outline"
            >
              <span>Browse Products</span>
              <ArrowRight size={17} />
            </Link>

          </div>
        )}

      </div>

    </section>
  );
}

export default FeaturedProducts;