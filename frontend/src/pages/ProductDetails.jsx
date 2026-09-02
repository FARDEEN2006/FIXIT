import React, { useEffect, useState } from "react";
import { ArrowLeft, CheckCircle, PackageX } from "lucide-react";
import { Link, useParams } from "react-router-dom";

import ProductEnquiryForm from "../components/products/ProductEnquiryForm";
import LoadingSpinner from "../components/common/LoadingSpinner";

import { getProductById } from "../services/api";

import "../styles/products.css";

function ProductDetails() {
  const { id } = useParams();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function fetchProduct() {
      try {
        setLoading(true);
        setNotFound(false);
        const data = await getProductById(id);
        if (!cancelled) {
          if (data) {
            setProduct(data);
          } else {
            setNotFound(true);
          }
        }
      } catch (err) {
        if (!cancelled) setNotFound(true);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    fetchProduct();

    return () => {
      cancelled = true;
    };
  }, [id]);

  // Loading state
  if (loading) {
    return (
      <main
        style={{
          minHeight: "60vh",
          display: "grid",
          placeItems: "center",
        }}
      >
        <LoadingSpinner size={36} />
      </main>
    );
  }

  // Not found state (reuses existing UI)
  if (notFound || !product) {
    return (
      <main className="fixit-product-not-found">

        <div className="container">

          <div className="fixit-product-not-found-box">

            <div className="fixit-product-not-found-icon">
              <PackageX size={35} />
            </div>

            <span className="section-label">
              PRODUCT
            </span>

            <h1>
              Product Not Found
            </h1>

            <p>
              This product is unavailable or has been
              removed from the current listing.
            </p>

            <Link
              to="/products"
              className="fixit-button fixit-button-primary"
            >
              <ArrowLeft size={17} />
              <span>Back to Products</span>
            </Link>

          </div>

        </div>

      </main>
    );
  }

  const isAvailable =
    product.available !== false &&
    product.active !== false;

  return (
    <main className="fixit-product-details-page">

      <section className="section">

        <div className="container">

          <Link
            to="/products"
            className="fixit-back-link"
          >
            <ArrowLeft size={17} />
            Back to Products
          </Link>

          <div className="fixit-product-details-grid">

            <div className="fixit-product-details-image">

              {product.image_url ? (
                <img
                  src={product.image_url}
                  alt={product.name}
                />
              ) : (
                <div className="fixit-product-no-image large">
                  <PackageX size={50} />
                  <span>Image unavailable</span>
                </div>
              )}

            </div>

            <div className="fixit-product-details-content">

              <div
                className={
                  isAvailable
                    ? "fixit-product-detail-status available"
                    : "fixit-product-detail-status unavailable"
                }
              >

                {isAvailable ? (
                  <>
                    <CheckCircle size={15} />
                    Available
                  </>
                ) : (
                  <>
                    <PackageX size={15} />
                    Currently unavailable
                  </>
                )}

              </div>

              <span className="section-label">
                FIXIT PRODUCT
              </span>

              <h1>
                {product.name}
              </h1>

              <div className="fixit-product-detail-price">
                ₹{Number(product.price || 0).toLocaleString("en-IN")}
              </div>

              <div className="fixit-product-description">

                <h2>
                  Product Details
                </h2>

                <p>
                  {product.description ||
                    "Product details will be provided by FIXIT."}
                </p>

              </div>

              {isAvailable && (
                <ProductEnquiryForm
                  product={product}
                />
              )}

            </div>

          </div>

        </div>

      </section>

    </main>
  );
}

export default ProductDetails;