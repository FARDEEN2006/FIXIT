import API_BASE_URL from "../config/environment";

async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;

  const response = await fetch(url, {
    ...options,
    headers: {
      ...(options.body instanceof FormData
        ? {}
        : { "Content-Type": "application/json" }),
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
// ---------------------------------------------------------------------------
function normalizeProduct(p) {
  return {
    ...p,

    // Backend detail response may provide imageUrl.
    // Product list response may only provide imagePath.
    image_url: p.imageUrl || null,
  };
}

// ---------------------------------------------------------------------------
// PRODUCTS
// ---------------------------------------------------------------------------

/**
 * Fetch all active products.
 *
 * Backend:
 * GET /api/products?page=0&pageSize=20
 */
export async function getProducts(page = 0, pageSize = 20) {
  const data = await apiRequest(
    `/api/products?page=${page}&pageSize=${pageSize}`
  );

  const products = data?.data?.products || [];

  return products.map(normalizeProduct);
}

/**
 * Fetch a single product.
 *
 * Backend:
 * GET /api/products/{id}
 */
export async function getProductById(id) {
  const data = await apiRequest(`/api/products/${id}`);

  const product = data?.data || null;

  return product ? normalizeProduct(product) : null;
}

// ---------------------------------------------------------------------------
// PRODUCT ENQUIRIES
// ---------------------------------------------------------------------------

/**
 * Create a product enquiry.
 *
 * Backend:
 * POST /api/product-enquiries
 */
export async function createProductEnquiry(values) {
  return apiRequest("/api/product-enquiries", {
    method: "POST",
    body: JSON.stringify(values),
  });
}

// ---------------------------------------------------------------------------
// SERVICES
// ---------------------------------------------------------------------------

/**
 * Fetch active services.
 *
 * Backend:
 * GET /api/services
 */
export async function getServices() {
  const response = await apiRequest("/api/services");

  return response?.data?.services || [];
}

// ---------------------------------------------------------------------------
// STORE INFORMATION
// ---------------------------------------------------------------------------

/**
 * Fetch store information.
 *
 * Backend:
 * GET /api/store-info
 */
export async function getStoreInfo() {
  const response = await apiRequest("/api/store-info");

  return response?.data || null;
}

// ---------------------------------------------------------------------------
// SELL YOUR MOBILE
// ---------------------------------------------------------------------------

/**
 * Create second-hand mobile listing.
 *
 * Backend:
 * POST /api/sell/listings
 */
export async function createSecondHandListing(values, images, onProgress) {
  const body = new FormData();

  Object.entries(values).forEach(([key, value]) => {
    body.append(key, value);
  });

  images.forEach((image) => {
    body.append("images", image);
  });

  if (!onProgress) {
    return apiRequest("/api/sell/listings", {
      method: "POST",
      body,
      headers: { "X-Client-Compressed": "true" },
    });
  }

  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open("POST", `${API_BASE_URL}/api/sell/listings`);
    request.setRequestHeader("X-Client-Compressed", "true");
    request.upload.addEventListener("progress", (event) => {
      if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100));
    });
    request.addEventListener("load", () => {
      let data = null;
      try {
        data = request.getResponseHeader("content-type")?.includes("application/json")
          ? JSON.parse(request.responseText)
          : request.responseText;
      } catch {
        reject(new Error("The server returned an invalid response."));
        return;
      }
      if (request.status < 200 || request.status >= 300) {
        reject(new Error(typeof data === "object" && data?.message ? data.message : `Request failed with status ${request.status}`));
        return;
      }
      resolve(data);
    });
    request.addEventListener("error", () => reject(new Error("Network error while uploading photos.")));
    request.addEventListener("abort", () => reject(new Error("Photo upload was cancelled.")));
    request.send(body);
  });
}

// ---------------------------------------------------------------------------
// ADMIN LOGIN
// ---------------------------------------------------------------------------

/**
 * Admin login.
 *
 * Backend:
 * POST /api/auth/login
 */
export async function adminLogin(email, password) {
  return apiRequest("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({
      email,
      password,
    }),
  });
}