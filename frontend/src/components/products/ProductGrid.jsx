import React from "react";
import ProductCard from "./ProductCard";

function ProductGrid({ products = [] }) {
  // No products available
  if (!products || products.length === 0) {
    return (
      <div className="fixit-products-empty-state">
        <div className="fixit-products-empty-box">
          <span className="fixit-products-empty-symbol">
            +
          </span>
        </div>

        <h2>
          Products will appear here
        </h2>

        <p>
          FIXIT products are currently being updated.
          Please check back soon or contact us directly
          for product availability.
        </p>
      </div>
    );
  }

  // Products available
  return (
    <div className="fixit-products-grid">
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
        />
      ))}
    </div>
  );
}

export default ProductGrid;