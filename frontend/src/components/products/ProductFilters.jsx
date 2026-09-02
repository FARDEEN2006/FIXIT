import React from "react";
import { SlidersHorizontal } from "lucide-react";

function ProductFilters({ count = 0 }) {
  return (
    <div className="fixit-product-toolbar">

      <div>
        <span className="fixit-product-count">
          {count} {count === 1 ? "product" : "products"}
        </span>
      </div>

      <button
        type="button"
        className="fixit-filter-button"
        aria-label="Product filters"
      >
        <SlidersHorizontal size={17} />
        <span>Products</span>
      </button>

    </div>
  );
}

export default ProductFilters;