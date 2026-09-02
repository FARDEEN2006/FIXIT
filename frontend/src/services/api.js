import API_BASE_URL from "../config/environment";

async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;

  const response = await fetch(url, {
    ...options,
    headers: {
      ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
      ...(options.headers || {}),
    },
  });

  let data = null;

  const contentType = response.headers.get("content-type");

  if (contentType && contentType.includes("application/json")) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const message =
      typeof data === "object" && data?.message
        ? data.message
        : `Request failed with status ${response.status}`;

    throw new Error(message);
  }

  return data;
}

export default apiRequest;

// ---------------------------------------------------------------------------
// Normalise a product row from the backend response into the shape that
// ProductCard and ProductDetails already expect.
//
// Backend (Java/Jackson) serialises:
//   isActive  → "active"   (Jackson strips the "is" prefix on boolean getters)
//   imageUrl  → "imageUrl"  (only present on detail response, fully-resolved URL)
//   imagePath → "imagePath" (list response – raw storage path, not a URL)
//
// Components expect:
//   product.active  / product.available   → availability badge
//   product.image_url                     → <img src={product.image_url} />
// ---------------------------------------------------------------------------
function normalizeProduct(p) {
  return {
    ...p,
    // Use the fully-resolved imageUrl if available (detail endpoint),
    // otherwise leave undefined so ProductCard shows "Image unavailable".
    image_url: p.imageUrl || null,
  };
}

/**
 * Fetch all active products (paginated).
 * GET /api/products?page=0&pageSize=10
 *
 * Backend response shape:
 *   { success, message, data: { page, pageSize, totalCount, products: [...] } }
 */
export async function getProducts(page = 0, pageSize = 20) {
  const data = await apiRequest(
    `/products?page=${page}&pageSize=${pageSize}`
  );
  const products = data?.data?.products || [];
  return products.map(normalizeProduct);
}

/**
 * Fetch a single product by UUID.
 * GET /api/products/{id}
 *
 * Backend response shape:
 *   { success, message, data: { id, name, price, description, imageUrl, active, ... } }
 */
export async function getProductById(id) {
  const data = await apiRequest(`/products/${id}`);
  const product = data?.data || null;
  return product ? normalizeProduct(product) : null;
}

export async function createProductEnquiry(values) {
  return apiRequest("/product-enquiries", { method: "POST", body: JSON.stringify(values) });
}

export async function getServices() {
  const response = await apiRequest("/services");
  return response?.data?.services || [];
}

export async function getStoreInfo() {
  const response = await apiRequest("/store-info");
  return response?.data || null;
}

export async function requestListingVerification(values) {
  return apiRequest("/sell/verify/request", { method: "POST", body: JSON.stringify(values) });
}

export async function confirmListingVerification(listingId, token) {
  return apiRequest("/sell/verify/confirm", { method: "POST", body: JSON.stringify({ listingId, token }) });
}

export async function uploadListingImage(listingId, token, image, isThumbnail) {
  const body = new FormData();
  body.append("image", image);
  body.append("isThumbnail", String(isThumbnail));
  body.append("verificationToken", token);
  return apiRequest(`/sell/listings/${listingId}/images`, { method: "POST", body });
}

export async function adminLogin(email, password) {
  return apiRequest("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) });
}
