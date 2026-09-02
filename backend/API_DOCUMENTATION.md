# FIXIT Backend API Documentation

## Base URL
```
Production: https://fixit-backend.onrender.com/api
Development: http://localhost:8080/api
```

## Authentication
All endpoints marked with 🔒 require JWT token in Authorization header:
```
Authorization: Bearer {jwt_token}
```

## Response Format

### Success Response (HTTP 2xx)
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ }
}
```

### Error Response (HTTP 4xx/5xx)
```json
{
  "success": false,
  "message": "Error message",
  "error": { /* error details */ },
  "statusCode": 400,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Health Check

### GET /api/health
Check if backend is running.

**Authentication:** None  
**Response Code:** 200 OK

**Example Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "service": "FIXIT-Backend",
  "version": "1.0.0"
}
```

---

## Products Endpoints

### GET /api/products
List all active products with pagination.

**Authentication:** None  
**Query Parameters:**
- `page` (int, default: 0) - Page number (0-indexed)
- `pageSize` (int, default: 10) - Items per page

**Response Code:** 200 OK

**Example Request:**
```
GET /api/products?page=0&pageSize=10
```

**Example Response:**
```json
{
  "success": true,
  "message": "Products fetched successfully",
  "data": {
    "products": [
      {
        "id": "uuid-1",
        "name": "iPhone 13 Pro",
        "price": 75000.00,
        "description": "Like new condition...",
        "imagePath": "products/uuid-1/file.jpg",
        "isActive": true,
        "createdAt": "2024-01-10T10:00:00",
        "updatedAt": "2024-01-10T10:00:00"
      }
    ],
    "page": 0,
    "pageSize": 10,
    "totalCount": 25
  }
}
```

---

### GET /api/products/{id}
Get product details with enquiry count.

**Authentication:** None  
**Path Parameters:**
- `id` (uuid) - Product ID

**Response Code:** 200 OK | 404 Not Found

**Example Request:**
```
GET /api/products/550e8400-e29b-41d4-a716-446655440000
```

**Example Response:**
```json
{
  "success": true,
  "message": "Product fetched successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "iPhone 13 Pro",
    "price": 75000.00,
    "description": "Apple iPhone 13 Pro in excellent condition...",
    "imagePath": "products/550e8400/image.jpg",
    "imageUrl": "https://supabase.../storage/v1/object/public/products/550e8400/image.jpg",
    "isActive": true,
    "enquiryCount": 5,
    "createdAt": "2024-01-10T10:00:00",
    "updatedAt": "2024-01-15T14:30:00"
  }
}
```

---

### POST /api/admin/products
Create a new product. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Content-Type:** application/json

**Request Body:**
```json
{
  "name": "iPhone 13 Pro",
  "price": 75000.00,
  "description": "Apple iPhone 13 Pro in excellent condition with all accessories.",
  "isActive": true
}
```

**Validation Rules:**
- `name`: 3-255 characters, required
- `price`: >0, required
- `description`: 10-2000 characters, required
- `isActive`: boolean, default true

**Response Code:** 201 Created | 400 Bad Request | 401 Unauthorized | 403 Forbidden

**Example Response:**
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": "new-uuid",
    "name": "iPhone 13 Pro",
    "price": 75000.00,
    "description": "Apple iPhone 13 Pro...",
    "imagePath": null,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

### PUT /api/admin/products/{id}
Update product details. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Path Parameters:**
- `id` (uuid) - Product ID

**Request Body:** (Same as POST)

**Response Code:** 200 OK | 400 Bad Request | 404 Not Found | 401 Unauthorized | 403 Forbidden

**Example Response:**
```json
{
  "success": true,
  "message": "Product updated successfully",
  "data": { /* Updated product */ }
}
```

---

### DELETE /api/admin/products/{id}
Soft delete product (deactivate). 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Path Parameters:**
- `id` (uuid) - Product ID

**Response Code:** 200 OK | 404 Not Found | 401 Unauthorized | 403 Forbidden

**Example Response:**
```json
{
  "success": true,
  "message": "Product deleted successfully",
  "data": null
}
```

---

### POST /api/admin/products/{id}/image
Upload product image. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Content-Type:** multipart/form-data  
**Path Parameters:**
- `id` (uuid) - Product ID

**Form Parameters:**
- `image` (file, required) - Image file (jpg, jpeg, png, webp)
  - Max size: 5MB
  - Will be compressed to 1200x1200 at 80% quality

**Response Code:** 201 Created | 400 Bad Request | 404 Not Found | 401 Unauthorized | 403 Forbidden

**Example Response:**
```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "imagePath": "products/550e8400/compressed_image.jpg",
    "imageUrl": "https://supabase.../storage/v1/object/public/products/550e8400/compressed_image.jpg",
    "message": "Image uploaded successfully"
  }
}
```

---

## Product Enquiry Endpoints

### POST /api/product-enquiries
Create a new product enquiry.

**Authentication:** None  
**Content-Type:** application/json

**Request Body:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "John Doe",
  "customerPhone": "+919876543210",
  "customerEmail": "john@example.com"
}
```

**Validation Rules:**
- `productId`: Required, must exist
- `customerName`: 2-255 characters, required
- `customerPhone`: Valid phone format (10-15 digits), required
- `customerEmail`: Valid email, required

**Response Code:** 201 Created | 400 Bad Request

**Example Response:**
```json
{
  "success": true,
  "message": "Enquiry created successfully. We'll contact you soon via WhatsApp.",
  "data": {
    "id": "enquiry-uuid",
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "customerName": "John Doe",
    "customerPhone": "+919876543210",
    "customerEmail": "john@example.com",
    "whatsappMessage": "Hi, I am interested in the product. My name is John Doe and my phone number is +919876543210...",
    "enquiryStatus": "NEW",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### GET /api/admin/product-enquiries
List all enquiries. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Query Parameters:**
- `status` (string, optional) - Filter by status (NEW, CONTACTED, COMPLETED)
- `page` (int, default: 0) - Page number
- `pageSize` (int, default: 10) - Items per page

**Response Code:** 200 OK | 401 Unauthorized | 403 Forbidden

**Example Response:**
```json
{
  "success": true,
  "message": "Enquiries fetched successfully",
  "data": {
    "enquiries": [
      {
        "id": "enquiry-uuid",
        "productId": "product-uuid",
        "customerName": "John Doe",
        "customerPhone": "+919876543210",
        "customerEmail": "john@example.com",
        "enquiryStatus": "NEW",
        "whatsappMessage": "...",
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "page": 0,
    "pageSize": 10,
    "totalCount": 5
  }
}
```

---

### GET /api/admin/product-enquiries/{id}
Get enquiry details. 🔒

**Authentication:** Required (ROLE_ADMIN)

**Response Code:** 200 OK | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

### PUT /api/admin/product-enquiries/{id}/status
Update enquiry status. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Content-Type:** application/json

**Request Body:**
```json
{
  "status": "CONTACTED"
}
```

**Response Code:** 200 OK | 400 Bad Request | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

### DELETE /api/admin/product-enquiries/{id}
Delete enquiry. 🔒

**Authentication:** Required (ROLE_ADMIN)

**Response Code:** 200 OK | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

### GET /api/admin/products/{productId}/enquiries
List enquiries for a specific product. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Query Parameters:**
- `page` (int, default: 0)
- `pageSize` (int, default: 10)

**Response Code:** 200 OK | 401 Unauthorized | 403 Forbidden

---

## Second-Hand Marketplace Endpoints

### POST /api/sell/verify/request
Create second-hand listing request (step 1 of workflow).

**Authentication:** None  
**Content-Type:** application/json

**Request Body:**
```json
{
  "sellerName": "Raj Kumar",
  "sellerPhone": "+919876543210",
  "sellerEmail": "raj@example.com",
  "productName": "Samsung Galaxy S21",
  "condition": "GOOD",
  "detailedDescription": "Samsung Galaxy S21 in good condition with minimal scratches on the back...",
  "expectedPrice": 35000.00
}
```

**Validation Rules:**
- `sellerName`: 2-255 characters, required
- `sellerPhone`: Valid phone format, required
- `sellerEmail`: Valid email, required
- `productName`: 3-255 characters, required
- `condition`: One of (NEW, GOOD, FAIR, POOR), required
- `detailedDescription`: 20-2000 characters, required
- `expectedPrice`: >0, required

**Response Code:** 201 Created | 400 Bad Request

**Example Response:**
```json
{
  "success": true,
  "message": "Verification email sent. Please check your email to proceed.",
  "data": {
    "success": true,
    "listing_id": "listing-uuid",
    "message": "Verification email sent. Please check your email to proceed.",
    "next_step": "verify_email"
  }
}
```

---

### POST /api/sell/verify/confirm
Verify seller email (step 2 of workflow).

**Authentication:** None  
**Content-Type:** application/json

**Request Body:**
```json
{
  "listingId": "listing-uuid",
  "token": "token-from-email-link"
}
```

**Response Code:** 200 OK | 400 Bad Request

**Example Response:**
```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "success": true,
    "message": "Email verified successfully",
    "listingId": "listing-uuid",
    "nextStep": "upload_images"
  }
}
```

---

### POST /api/sell/listings/{id}/images
Add images to listing (step 3 of workflow, up to 4 images). 🔒 (open access during verification)

**Authentication:** None (Open to all during verification window)  
**Content-Type:** multipart/form-data  
**Path Parameters:**
- `id` (uuid) - Listing ID

**Form Parameters:**
- `image` (file, required) - Image file
- `isThumbnail` (boolean, default: false) - Mark as thumbnail

**Response Code:** 201 Created | 400 Bad Request | 404 Not Found

**Example Response:**
```json
{
  "success": true,
  "message": "Image added successfully",
  "data": {
    "id": "image-uuid",
    "storagePath": "second-hand/listing-uuid/1.jpg",
    "imageUrl": "https://signed-url-for-private-access...",
    "imageOrder": 1,
    "isThumbnail": false,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### GET /api/admin/second-hand
List all second-hand listings. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Query Parameters:**
- `status` (string, optional) - Filter (NEW, REVIEWING, CONTACTED, ACCEPTED, REJECTED, COMPLETED)
- `page` (int, default: 0)
- `pageSize` (int, default: 10)

**Response Code:** 200 OK | 401 Unauthorized | 403 Forbidden

**Example Response:**
```json
{
  "success": true,
  "message": "Listings fetched successfully",
  "data": {
    "page": 0,
    "pageSize": 10,
    "totalCount": 12,
    "listings": [
      {
        "id": "listing-uuid",
        "sellerName": "Raj Kumar",
        "sellerPhone": "+919876543210",
        "sellerEmail": "raj@example.com",
        "productName": "Samsung Galaxy S21",
        "condition": "GOOD",
        "detailedDescription": "Samsung Galaxy S21...",
        "expectedPrice": 35000.00,
        "listingStatus": "REVIEWING",
        "emailVerified": true,
        "emailVerifiedAt": "2024-01-15T10:30:00",
        "images": [],
        "createdAt": "2024-01-15T10:25:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ]
  }
}
```

---

### GET /api/admin/second-hand/{id}
Get listing details with all images. 🔒

**Authentication:** Required (ROLE_ADMIN)

**Response Code:** 200 OK | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

### PUT /api/admin/second-hand/{id}/status
Update listing status. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Content-Type:** application/json

**Request Body:**
```json
{
  "status": "ACCEPTED"
}
```

**Valid Statuses:**
- NEW → REVIEWING (admin reviews)
- REVIEWING → CONTACTED (admin contacted seller)
- CONTACTED → ACCEPTED or REJECTED
- ACCEPTED → COMPLETED (deal closed)

**Response Code:** 200 OK | 400 Bad Request | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

### DELETE /api/admin/second-hand/{id}
Delete listing and all images. 🔒

**Authentication:** Required (ROLE_ADMIN)

**Response Code:** 200 OK | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

## Store Information Endpoints

### GET /api/store-info
Get store information.

**Authentication:** None

**Response Code:** 200 OK | 404 Not Found

**Example Response:**
```json
{
  "success": true,
  "message": "Store information fetched successfully",
  "data": {
    "id": "store-uuid",
    "businessName": "FIXIT Mobile Solutions",
    "phone": "+919876543210",
    "whatsapp": "+919876543210",
    "email": "info@fixit.com",
    "address": "123 Main Street",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560034",
    "workingHoursOpen": "09:00 AM",
    "workingHoursClose": "06:00 PM",
    "aboutContent": "We provide mobile phone repair, sales, and second-hand device marketplace...",
    "mapLat": 12.9352,
    "mapLon": 77.6245,
    "createdAt": "2024-01-10T10:00:00",
    "updatedAt": "2024-01-15T14:30:00"
  }
}
```

---

### PUT /api/admin/store-info
Update store information. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Content-Type:** application/json

**Request Body:**
```json
{
  "businessName": "FIXIT Mobile Solutions",
  "phone": "+919876543210",
  "whatsapp": "+919876543210",
  "email": "info@fixit.com",
  "address": "123 Main Street",
  "city": "Bangalore",
  "state": "Karnataka",
  "pincode": "560034",
  "workingHoursOpen": "09:00 AM",
  "workingHoursClose": "06:00 PM",
  "aboutContent": "We provide mobile phone repair, sales, and second-hand device marketplace...",
  "mapLat": 12.9352,
  "mapLon": 77.6245
}
```

**Response Code:** 200 OK | 400 Bad Request | 401 Unauthorized | 403 Forbidden

---

## Services (Repair/Maintenance) Endpoints

### GET /api/services
List all active services.

**Authentication:** None

**Response Code:** 200 OK

**Example Response:**
```json
{
  "success": true,
  "message": "Services fetched successfully",
  "data": {
    "services": [
      {
        "id": "service-uuid",
        "serviceName": "Screen Replacement",
        "description": "Replace broken or damaged phone screens...",
        "isActive": true,
        "displayOrder": 1,
        "iconPath": "service-icons/screen-replacement.png",
        "createdAt": "2024-01-10T10:00:00",
        "updatedAt": "2024-01-10T10:00:00"
      }
    ]
  }
}
```

---

### GET /api/services/{id}
Get service details.

**Authentication:** None

**Response Code:** 200 OK | 404 Not Found

---

### POST /api/admin/services
Create service. 🔒

**Authentication:** Required (ROLE_ADMIN)  
**Content-Type:** application/json

**Request Body:**
```json
{
  "serviceName": "Battery Replacement",
  "description": "Replace degraded or non-functional phone batteries...",
  "isActive": true,
  "displayOrder": 2
}
```

**Validation Rules:**
- `serviceName`: 3-255 characters, required
- `description`: 10-1000 characters, required
- `isActive`: boolean, default true
- `displayOrder`: integer, required

**Response Code:** 201 Created | 400 Bad Request | 401 Unauthorized | 403 Forbidden

---

### PUT /api/admin/services/{id}
Update service. 🔒

**Authentication:** Required (ROLE_ADMIN)

**Request Body:** (Same as POST)

**Response Code:** 200 OK | 400 Bad Request | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

### DELETE /api/admin/services/{id}
Delete service (soft delete - deactivate). 🔒

**Authentication:** Required (ROLE_ADMIN)

**Response Code:** 200 OK | 404 Not Found | 401 Unauthorized | 403 Forbidden

---

## Error Codes Reference

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | Request successful |
| 201 | Created | Resource created |
| 400 | Bad Request | Validation failed, missing required fields |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | User lacks required role (not ADMIN) |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Unexpected server error |

---

## Common Error Examples

### Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "customerName": "Name must be between 2 and 255 characters",
    "customerEmail": "Invalid email format"
  },
  "statusCode": 400,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Unauthorized Error
```json
{
  "success": false,
  "message": "Unauthorized",
  "error": {
    "error": "Missing or invalid JWT token"
  },
  "statusCode": 401,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Forbidden Error
```json
{
  "success": false,
  "message": "Access denied",
  "error": {
    "error": "User does not have required admin role"
  },
  "statusCode": 403,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

**Document Version:** 1.0
**Last Updated:** 2024
**Total Endpoints:** 34
