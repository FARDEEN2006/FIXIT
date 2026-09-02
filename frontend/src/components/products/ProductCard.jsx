import React from "react";
import { ArrowRight, CheckCircle, PackageX } from "lucide-react";
import { Link } from "react-router-dom";

function ProductCard({ product }) {
  if (!product) {
    return null;
  }

  const isAvailable =
    product.available !== false &&
    product.active !== false;

  return (
    <article className="fixit-product-card">

      <Link
        to={`/products/${product.id}`}
        className="fixit-product-card-image"
      >

        {product.image_url ? (
          <img
            src={product.image_url}
            alt={product.name}
            loading="lazy"
          />
        ) : (
          <div className="fixit-product-no-image">
            <PackageX size={42} />
            <span>Image unavailable</span>
          </div>
        )}

        <div
          className={
            isAvailable
              ? "fixit-product-status available"
              : "fixit-product-status unavailable"
          }
        >

          {isAvailable ? (
            <>
              <CheckCircle size={14} />
              Available
            </>
          ) : (
            <>
              <PackageX size={14} />
              Unavailable
            </>
          )}

        </div>

      </Link>

      <div className="fixit-product-card-content">

        <h2>
          {product.name}
        </h2>

        <div className="fixit-product-card-bottom">

          <strong>
            ₹{Number(product.price || 0).toLocaleString("en-IN")}
          </strong>

          <Link
            to={`/products/${product.id}`}
            className="fixit-product-view-button"
            aria-label={`View ${product.name}`}
          >
            <ArrowRight size={18} />
          </Link>

        </div>

      </div>

    </article>
  );
}

export default ProductCard;