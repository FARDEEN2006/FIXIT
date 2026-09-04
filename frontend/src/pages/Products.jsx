import React, { useEffect, useState } from "react";

import ProductHero from "../components/products/ProductHero";
import ProductFilters from "../components/products/ProductFilters";
import ProductGrid from "../components/products/ProductGrid";
import LoadingSpinner from "../components/common/LoadingSpinner";

import { getProducts } from "../services/api";

import "../styles/products.css";

function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function fetchProducts() {
      try {
        setLoading(true);
        setError("");

        const data = await getProducts();

        if (!cancelled) {
          const activeProducts = Array.isArray(data)
            ? data.filter(
                (product) => product?.isActive === true
              )
            : [];

          setProducts(activeProducts);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            "Unable to load products right now. Please try again later."
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    fetchProducts();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="fixit-products-page">
      <ProductHero />

      <section className="section fixit-products-list-section">
        <div className="container">
          <div className="fixit-products-section-heading">
            <div>
              <span className="section-label">
                SHOP
              </span>

              <h2 className="section-title">
                Available Products
              </h2>

              <p className="section-description">
                Explore products currently listed by FIXIT
                Mobile Sales & Services.
              </p>
            </div>
          </div>

          {loading ? (
            <div className="fixit-products-loading">
              <LoadingSpinner size={32} />
            </div>
          ) : error ? (
            <p
              className="fixit-form-error"
              role="alert"
            >
              {error}
            </p>
          ) : (
            <>
              <ProductFilters count={products.length} />
              <ProductGrid products={products} />
            </>
          )}
        </div>
      </section>
    </main>
  );
}

export default Products;