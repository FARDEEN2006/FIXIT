import React, { useEffect, useState } from "react";
import API_BASE_URL from "../../config/environment";

const ProductManager = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(null);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);

  const [form, setForm] = useState({
    name: "",
    price: "",
    description: "",
    image: null,
  });

  const token = localStorage.getItem("fixit_admin_token");

  const authHeaders = {
    Authorization: `Bearer ${token}`,
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await fetch(
        `${API_BASE_URL}/api/products/admin`,
        {
          headers: authHeaders,
        }
      );

      if (!response.ok) {
        throw new Error("Failed to load products");
      }

      const result = await response.json();

      const data = result?.data;

      const productList = Array.isArray(data)
        ? data
        : Array.isArray(data?.products)
        ? data.products
        : [];

      setProducts(productList);
    } catch (err) {
      setError(err.message || "Unable to load products");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleChange = (event) => {
    const { name, value, files } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: files ? files[0] : value,
    }));
  };

  const resetForm = () => {
    setForm({
      name: "",
      price: "",
      description: "",
      image: null,
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!token) {
      setError("Admin session expired. Please login again.");
      return;
    }

    if (!form.name.trim()) {
      setError("Product name is required.");
      return;
    }

    if (!form.price || Number(form.price) <= 0) {
      setError("Please enter a valid price.");
      return;
    }

    if (!form.description.trim()) {
      setError("Product description is required.");
      return;
    }

    if (form.description.trim().length < 10) {
      setError("Description must be at least 10 characters.");
      return;
    }

    try {
      setSaving(true);
      setError("");

      // =========================================================
      // STEP 1: CREATE PRODUCT USING JSON
      // =========================================================

      const productData = {
        name: form.name.trim(),
        price: Number(form.price),
        description: form.description.trim(),
        isActive: true,
      };

      const createResponse = await fetch(
        `${API_BASE_URL}/api/products/admin`,
        {
          method: "POST",
          headers: {
            ...authHeaders,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(productData),
        }
      );

      const createResult = await createResponse.json();

      if (!createResponse.ok || !createResult?.success) {
        throw new Error(
          createResult?.message || "Failed to create product"
        );
      }

      const createdProduct = createResult?.data;

      if (!createdProduct?.id) {
        throw new Error(
          "Product was created but no product ID was returned."
        );
      }

      const productId = createdProduct.id;

      // =========================================================
      // STEP 2: UPLOAD IMAGE IF SELECTED
      // =========================================================

      if (form.image) {
        const imageFormData = new FormData();

        imageFormData.append("image", form.image);

        const imageResponse = await fetch(
          `${API_BASE_URL}/api/products/admin/${productId}/image`,
          {
            method: "POST",
            headers: {
              ...authHeaders,
            },
            body: imageFormData,
          }
        );

        const imageResult = await imageResponse.json();

        if (!imageResponse.ok || !imageResult?.success) {
          throw new Error(
            imageResult?.message ||
              "Product created, but image upload failed."
          );
        }
      }

      // =========================================================
      // STEP 3: SUCCESS
      // =========================================================

      resetForm();
      setShowForm(false);

      await fetchProducts();
    } catch (err) {
      setError(err.message || "Unable to create product");
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async (product) => {
    if (!token) {
      setError("Admin session expired. Please login again.");
      return;
    }

    const newStatus = !product.isActive;

    const confirmed = window.confirm(
      newStatus
        ? `Make "${product.name}" active? It will become visible on the public Products page.`
        : `Make "${product.name}" inactive? It will be hidden from the public Products page.`
    );

    if (!confirmed) {
      return;
    }

    try {
      setStatusUpdating(product.id);
      setError("");

      const response = await fetch(
        `${API_BASE_URL}/api/products/admin/${product.id}`,
        {
          method: "PUT",
          headers: {
            ...authHeaders,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: product.name,
            price: Number(product.price),
            description: product.description || "",
            isActive: newStatus,
          }),
        }
      );

      const result = await response.json();

      if (!response.ok || !result?.success) {
        throw new Error(
          result?.message || "Failed to update product status"
        );
      }

      await fetchProducts();
    } catch (err) {
      setError(
        err.message || "Unable to update product status"
      );
    } finally {
      setStatusUpdating(null);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this product?"
    );

    if (!confirmed) return;

    if (!token) {
      setError("Admin session expired. Please login again.");
      return;
    }

    try {
      setError("");

      const response = await fetch(
        `${API_BASE_URL}/api/products/admin/${id}`,
        {
          method: "DELETE",
          headers: {
            ...authHeaders,
          },
        }
      );

      const result = await response.json();

      if (!response.ok || !result?.success) {
        throw new Error(
          result?.message || "Failed to delete product"
        );
      }

      await fetchProducts();
    } catch (err) {
      setError(err.message || "Unable to delete product");
    }
  };

  return (
    <section className="admin-manager">
      <div className="admin-manager-heading">
        <div>
          <span className="admin-eyebrow">CATALOG</span>

          <h1>Products</h1>

          <p>
            Manage the products displayed on the FIXIT website.
          </p>
        </div>

        <button
          type="button"
          className="admin-primary-button"
          onClick={() => {
            setError("");
            setShowForm((value) => !value);
          }}
          disabled={saving || statusUpdating !== null}
        >
          {showForm ? "Close Form" : "+ Add Product"}
        </button>
      </div>

      {error && <div className="admin-error">{error}</div>}

      {showForm && (
        <form className="admin-form-card" onSubmit={handleSubmit}>
          <h2>Add Product</h2>

          <div className="admin-form-grid">
            <label>
              Product Name

              <input
                type="text"
                name="name"
                value={form.name}
                onChange={handleChange}
                placeholder="Example: iPhone 15 Case"
                required
                disabled={saving}
              />
            </label>

            <label>
              Price

              <input
                type="number"
                name="price"
                value={form.price}
                onChange={handleChange}
                min="0.01"
                step="0.01"
                placeholder="999"
                required
                disabled={saving}
              />
            </label>

            <label className="admin-full-field">
              Description

              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                rows="5"
                minLength="10"
                maxLength="2000"
                placeholder="Enter product details..."
                required
                disabled={saving}
              />
            </label>

            <label className="admin-full-field">
              Product Image

              <input
                type="file"
                name="image"
                accept="image/*"
                onChange={handleChange}
                disabled={saving}
              />

              <small>
                Maximum 1 image.                 Any supported image format, including JFIF.
              </small>
            </label>
          </div>

          <button
            type="submit"
            className="admin-primary-button"
            disabled={saving}
          >
            {saving ? "Saving Product..." : "Save Product"}
          </button>
        </form>
      )}

      <div className="admin-list-card">
        {loading ? (
          <div className="admin-loading">
            Loading products...
          </div>
        ) : products.length === 0 ? (
          <div className="admin-empty">
            <div>📱</div>

            <h3>No products yet</h3>

            <p>
              Add your first product using the button above.
            </p>
          </div>
        ) : (
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Price</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>
                      <strong>{product.name}</strong>
                    </td>

                    <td>
                      ₹
                      {Number(
                        product.price || 0
                      ).toLocaleString("en-IN")}
                    </td>

                    <td>
                      <span
                        className={`admin-status ${
                          product.isActive
                            ? "active"
                            : "inactive"
                        }`}
                      >
                        {product.isActive
                          ? "ACTIVE"
                          : "INACTIVE"}
                      </span>
                    </td>

                    <td
                      style={{
                        display: "flex",
                        gap: "8px",
                        alignItems: "center",
                      }}
                    >
                      <button
                        type="button"
                        className={
                          product.isActive
                            ? "admin-secondary-button"
                            : "admin-primary-button"
                        }
                        onClick={() =>
                          handleStatusChange(product)
                        }
                        disabled={
                          saving ||
                          statusUpdating === product.id
                        }
                      >
                        {statusUpdating === product.id
                          ? "Updating..."
                          : product.isActive
                          ? "Make Inactive"
                          : "Make Active"}
                      </button>

                      <button
                        type="button"
                        className="admin-danger-button"
                        onClick={() =>
                          handleDelete(product.id)
                        }
                        disabled={
                          saving ||
                          statusUpdating === product.id
                        }
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default ProductManager;