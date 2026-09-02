# FIXIT Backend Architecture

## 1. Overview

The FIXIT Backend is a Spring Boot 3.1.5 REST API that serves the FIXIT Mobile Sales & Services application. It provides product management, customer enquiries, second-hand marketplace, and admin operations with JWT-based authentication and Supabase integration.

**Technology Stack:**
- Java 17
- Spring Boot 3.1.5
- Maven build system
- Supabase PostgreSQL Database
- JWT (JJWT) for authentication
- Image processing with imgscalr

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React/Flutter)                 │
└─────────────────────────────────────────────────────────────┘
                              ↕ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway (CORS Configuration)            │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌──────────────────────────────────────────────────────────────┐
│                   REST Controllers                            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ Health │ Product │ Enquiry │ SecondHand │ Admin │ Auth│ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                              ↕
┌──────────────────────────────────────────────────────────────┐
│                   Service Layer (Business Logic)             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ ProductService  │ EnquiryService │ ListingService │    │ │
│  │ AuthService     │ StorageService │ StoreService   │    │ │
│  │ ServiceService  │ EmailService                    │    │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                              ↕
┌──────────────────────────────────────────────────────────────┐
│                    Integration Layer                         │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ SupabaseService (REST API) │ ImageCompressionUtil    │  │
│  │ StorageService             │ EmailService (SMTP)     │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                              ↕
┌──────────────────────────────────────────────────────────────┐
│         External Services                                    │
│  ┌───────────────────────┐  ┌──────────────────────────────┐ │
│  │  Supabase PostgreSQL  │  │  Supabase Cloud Storage      │ │
│  │  - Database           │  │  - Public buckets (products) │ │
│  │  - RLS Policies       │  │  - Private buckets (2nd hand)│ │
│  │  - JWT Token Support  │  │  - Signed URLs               │ │
│  └───────────────────────┘  └──────────────────────────────┘ │
│         ↕                              ↕                      │
│  ┌────────────────────────────────────────────────────────┐   │
│  │            SMTP Email Service (Optional)               │   │
│  │            - Verification emails                       │   │
│  │            - Admin notifications                       │   │
│  │            - Listing acceptance/rejection             │   │
│  └────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Folder Structure

```
backend/
├── pom.xml                              # Maven configuration
├── application.properties                # Spring configuration
├── .env.example                         # Environment template
├── Dockerfile                           # Docker configuration
├── RENDER_DEPLOYMENT.md                 # Deployment guide
│
├── src/main/java/com/fixit/
│   ├── FixitBackendApplication.java     # Main entry point
│   │
│   ├── config/                          # Configuration classes
│   │   ├── CorsConfig.java              # CORS setup
│   │   ├── RestClientConfig.java        # REST template
│   │   └── SupabaseConfig.java          # Supabase credentials
│   │
│   ├── security/                        # Security & JWT
│   │   ├── JwtTokenProvider.java        # Token operations
│   │   └── JwtAuthenticationFilter.java # Request filter
│   │
│   ├── controller/                      # REST endpoints
│   │   ├── HealthController.java        # /api/health
│   │   ├── ProductController.java       # /api/products/*
│   │   ├── ProductEnquiryController.java # /api/product-enquiries/*
│   │   ├── SecondHandController.java    # /api/sell/* and /api/admin/second-hand/*
│   │   └── AdminController.java         # /api/admin/store-info, services
│   │
│   ├── service/                         # Business logic
│   │   ├── SupabaseService.java         # Database & storage API
│   │   ├── ProductService.java          # Product operations
│   │   ├── ProductEnquiryService.java   # Enquiry operations
│   │   ├── SecondHandListingService.java # Listing operations
│   │   ├── AuthService.java             # Authentication
│   │   ├── StorageService.java          # File storage wrapper
│   │   ├── StoreService.java            # Store info
│   │   ├── ServiceService.java          # Services/repairs
│   │   └── EmailService.java            # Email notifications
│   │
│   ├── dto/                             # Data transfer objects
│   │   ├── ProductDTO.java              # Product DTOs
│   │   ├── ProductEnquiryDTO.java       # Enquiry DTOs
│   │   ├── SecondHandDTO.java           # Listing DTOs
│   │   └── CommonDTO.java               # Auth, Store, Service DTOs
│   │
│   ├── util/                            # Utilities
│   │   └── ImageCompressionUtil.java    # Image processing
│   │
│   └── exception/                       # Exception handling
│       ├── GlobalExceptionHandler.java  # Exception mapper
│       ├── ResourceNotFoundException.java
│       ├── UnauthorizedException.java
│       └── ForbiddenException.java
│
└── src/main/resources/
    └── application.properties            # Configuration
```

---

## 4. Key Components

### 4.1 REST Controllers (5 controllers, 34 endpoints)

**HealthController**
- `GET /api/health` - Health check for monitoring

**ProductController**
- `GET /api/products` - List all active products
- `GET /api/products/{id}` - Get product details
- `POST /api/admin/products` - Create product (admin)
- `PUT /api/admin/products/{id}` - Update product (admin)
- `DELETE /api/admin/products/{id}` - Delete product (admin)
- `POST /api/admin/products/{id}/image` - Upload product image (admin)

**ProductEnquiryController**
- `POST /api/product-enquiries` - Create enquiry
- `GET /api/admin/product-enquiries` - List enquiries (admin)
- `GET /api/admin/product-enquiries/{id}` - Get enquiry (admin)
- `PUT /api/admin/product-enquiries/{id}/status` - Update status (admin)
- `DELETE /api/admin/product-enquiries/{id}` - Delete enquiry (admin)
- `GET /api/admin/products/{id}/enquiries` - List product enquiries (admin)

**SecondHandController**
- `POST /api/sell/verify/request` - Create listing request
- `POST /api/sell/verify/confirm` - Verify seller email
- `POST /api/sell/listings/{id}/images` - Add listing images
- `GET /api/admin/second-hand` - List all listings (admin)
- `GET /api/admin/second-hand/{id}` - Get listing details (admin)
- `PUT /api/admin/second-hand/{id}/status` - Update status (admin)
- `DELETE /api/admin/second-hand/{id}` - Delete listing (admin)

**AdminController**
- `GET /api/store-info` - Get store information
- `PUT /api/admin/store-info` - Update store info (admin)
- `GET /api/services` - List all services
- `GET /api/services/{id}` - Get service
- `POST /api/admin/services` - Create service (admin)
- `PUT /api/admin/services/{id}` - Update service (admin)
- `DELETE /api/admin/services/{id}` - Delete service (admin)

### 4.2 Service Layer (8 services)

**SupabaseService**
- Bridges backend with Supabase REST APIs
- Handles database operations: queryTable, insertRecord, updateRecord, deleteRecord
- Handles storage operations: uploadFile, deleteFile, generatePublicUrl, generateSignedUrl
- Handles RPC function calls

**ProductService**
- Product CRUD operations
- Image upload and management
- Active product filtering
- Enquiry count tracking

**ProductEnquiryService**
- Create, read, update enquiries
- WhatsApp message generation
- Admin enquiry listing
- Email notifications to admin

**SecondHandListingService**
- Listing creation with email verification flow
- Image management (max 4 images)
- Listing status tracking
- Admin operations on listings

**AuthService**
- JWT token validation
- User extraction from tokens
- Admin role verification
- Token generation

**StorageService**
- File upload wrapper for public/private buckets
- URL generation (public and signed)
- File deletion
- Storage management

**StoreService**
- Store information retrieval and updates
- Business details management
- Single store assumption

**ServiceService**
- Service/repair listing CRUD
- Active service filtering
- Display order management
- Soft delete support

**EmailService**
- HTML email template generation
- Email verification for sellers
- Listing approval/rejection notifications
- Admin notifications for enquiries
- Optional SMTP integration

### 4.3 Security & Authentication

**JWT Authentication Flow:**
```
1. Frontend obtains JWT token from Supabase Auth
2. Frontend sends token in Authorization header: "Bearer {token}"
3. JwtAuthenticationFilter intercepts request
4. JwtTokenProvider validates token signature and expiration
5. User ID, email, and admin role extracted from token claims
6. UsernamePasswordAuthenticationToken created with extracted info
7. Spring Security context populated for request
8. Controller method checks @PreAuthorize("hasRole('ADMIN')")
```

**Key Security Features:**
- Stateless authentication (no sessions)
- HMAC SHA-512 token signing
- 24-hour token expiration (configurable)
- Per-endpoint role-based access control
- CORS restricted to allowed origins
- JWT secret minimum 32 characters
- Service role key never exposed to frontend

### 4.4 Image Processing

**Image Compression Workflow:**
```
1. User uploads image file (jpg, jpeg, png, webp)
2. ImageCompressionUtil.validateImage():
   - File size ≤ 5MB (configurable)
   - MIME type matches allowed list
   - Extension matches allowed list
3. ImageCompressionUtil.compressImage():
   - Read image with ImageIO
   - Resize to max 1200x1200 (maintains aspect ratio)
   - Compress to 80% JPEG quality
   - Convert to byte array
4. SupabaseService.uploadFile():
   - Send bytes to Supabase Storage
   - Generate public URL
5. ProductService updates product record with image_path
```

**Compression Settings:**
- Max file size: 5MB
- Max dimensions: 1200x1200 pixels
- Quality: 80% JPEG
- Thumbnail size: 600x600 pixels
- Allowed formats: jpg, jpeg, png, webp

### 4.5 Database Integration

**Supabase API Usage:**

All database operations go through SupabaseService:
```java
// Query table
queryTable("products", filters)

// Insert record
insertRecord("products", productData)

// Update record
updateRecord("products", productId, updateData)

// Delete record
deleteRecord("products", productId)

// Call RPC function
callRpcFunction("complex_operation", params)
```

**Storage Operations:**
```java
// Upload file
uploadFile("bucket_name", "path/to/file", fileBytes)

// Generate public URL
generatePublicUrl("bucket_name", "path/to/file")

// Generate signed URL (temporary access)
generateSignedUrl("bucket_name", "path/to/file", 3600)

// Delete file
deleteFile("bucket_name", "path/to/file")
```

### 4.6 Email Notification System

**Email Templates Provided:**
1. Email verification (seller second-hand listing)
2. Listing approval notification
3. Listing rejection notification
4. Admin notification for new enquiry

**Email Configuration:**
- SMTP server, port, username, password
- TLS/STARTTLS required
- HTML formatted emails with styling
- Optional - if not configured, emails silently fail with warnings in logs

---

## 5. API Response Format

### Successful Response (2xx)
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data
  }
}
```

### Error Response (4xx/5xx)
```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "fieldName": "Error details",
    // Can be object or string
  },
  "statusCode": 400,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 6. Data Models

### Products Table
```sql
products {
  id: UUID (PK)
  name: String
  price: Decimal
  description: String
  image_path: String
  is_active: Boolean
  created_at: Timestamp
  updated_at: Timestamp
}
```

### Product Enquiries Table
```sql
product_enquiries {
  id: UUID (PK)
  product_id: UUID (FK)
  customer_name: String
  customer_phone: String
  customer_email: String
  whatsapp_message: String
  enquiry_status: String (NEW, CONTACTED, COMPLETED)
  created_at: Timestamp
}
```

### Second-Hand Listings Table
```sql
second_hand_listings {
  id: UUID (PK)
  seller_name: String
  seller_phone: String
  seller_email: String
  product_name: String
  condition: String (NEW, GOOD, FAIR, POOR)
  detailed_description: String
  expected_price: Decimal
  listing_status: String (NEW, REVIEWING, CONTACTED, ACCEPTED, REJECTED, COMPLETED)
  email_verified: Boolean
  email_verification_token: String
  email_verified_at: Timestamp
  created_at: Timestamp
  updated_at: Timestamp
}
```

### Second-Hand Images Table
```sql
second_hand_images {
  id: UUID (PK)
  listing_id: UUID (FK)
  storage_path: String
  image_order: Integer (1-4)
  is_thumbnail: Boolean
  created_at: Timestamp
}
```

### Store Information Table
```sql
store_information {
  id: UUID (PK)
  business_name: String
  phone: String
  whatsapp: String
  email: String
  address: String
  city: String
  state: String
  pincode: String
  working_hours_open: String
  working_hours_close: String
  about_content: String
  map_lat: Double
  map_lon: Double
  created_at: Timestamp
  updated_at: Timestamp
}
```

### Services Table
```sql
services {
  id: UUID (PK)
  service_name: String
  description: String
  icon_path: String
  is_active: Boolean
  display_order: Integer
  created_at: Timestamp
  updated_at: Timestamp
}
```

---

## 7. Configuration

### Environment Variables

**Database & Supabase:**
- `SUPABASE_URL` - Supabase project URL
- `SUPABASE_ANON_KEY` - Anon key for Supabase
- `SUPABASE_SERVICE_KEY` - Service role key (backend only)
- `DATABASE_URL` - PostgreSQL connection string

**Security:**
- `JWT_SECRET` - Secret for signing tokens (min 32 characters)
- `JWT_EXPIRATION_MS` - Token expiration in milliseconds

**Application:**
- `PORT` - Server port (default 8080)
- `APP_NAME` - Application name
- `APP_ADMIN_EMAIL` - Admin email for notifications
- `APP_WHATSAPP_NUMBER` - Business WhatsApp number
- `APP_FRONTEND_URL` - Frontend URL for email links

**Image Processing:**
- `IMAGE_MAX_FILE_SIZE` - Max file size in bytes (5MB default)
- `IMAGE_COMPRESSION_QUALITY` - JPEG quality 0-100
- `IMAGE_MAX_WIDTH` - Max image width (1200)
- `IMAGE_MAX_HEIGHT` - Max image height (1200)

**Email (Optional):**
- `SPRING_MAIL_HOST` - SMTP server
- `SPRING_MAIL_PORT` - SMTP port
- `SPRING_MAIL_USERNAME` - Email address
- `SPRING_MAIL_PASSWORD` - Email password
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` - Enable auth
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` - TLS enabled
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED` - TLS required

**CORS:**
- `SECURITY_CORS_ORIGINS` - Allowed origins (comma-separated)

---

## 8. Deployment Targets

### Local Development
```bash
mvn clean spring-boot:run
# Application runs on http://localhost:8080
```

### Docker (Local Testing)
```bash
docker build -t fixit-backend .
docker run -p 8080:8080 --env-file .env fixit-backend
```

### Render.com (Production)
See [RENDER_DEPLOYMENT.md](RENDER_DEPLOYMENT.md)

---

## 9. Testing Strategy

### Unit Tests
- Service layer business logic
- DTOs validation
- Image compression utility

### Integration Tests
- Controller endpoints with mock Supabase
- JWT authentication flow
- Database operations

### E2E Tests
- Full request/response flow
- Database persistence
- Error handling

### Manual Testing Checklist
- [x] Health endpoint returns 200
- [x] Public endpoints accessible without auth
- [x] Admin endpoints require authentication
- [x] Admin endpoints require ROLE_ADMIN
- [x] Image upload compresses correctly
- [x] Email notifications send successfully
- [x] Second-hand email verification works
- [x] CORS allows only configured origins
- [x] JWT token expiration enforced

---

## 10. Monitoring & Logging

**Logging Levels:**
- Root: INFO
- com.fixit: DEBUG
- org.springframework.security: DEBUG

**Key Metrics to Monitor:**
- Request response times
- Database query times
- Image upload sizes and times
- Email delivery success rate
- API error rates
- Database connection pool usage
- Memory and CPU usage

**Alerts to Configure:**
- Service down (health endpoint fails)
- Database connection errors
- SMTP connection failures
- Spike in 5xx errors
- Disk space running low
- Memory usage > 80%

---

## 11. Security Best Practices

✅ Implemented:
- JWT authentication with HS512
- CORS configuration
- Input validation (DTOs with @Valid)
- Service role key isolation
- HTTPS/SSL ready
- Exception handling (no stack traces in responses)

⚠️ Production Considerations:
- Implement rate limiting
- Add request logging for audit trail
- Monitor for suspicious patterns
- Regular security audits
- Keep dependencies updated
- Use secrets management (not env vars in production)
- Implement API versioning
- Add request signing for critical operations

---

## 12. Performance Optimization

**Already Implemented:**
- Image compression (reduces storage & bandwidth)
- Lazy loading (pagination support)
- Connection pooling
- Stateless design (horizontally scalable)

**Recommendations:**
- Add caching layer (Redis) for frequently accessed data
- Implement API request caching
- Use database indexes on frequently queried columns
- Monitor slow queries and optimize
- Consider CDN for static assets
- Implement request tracing (logging IDs)
- Add query result pagination limits

---

**Document Version:** 1.0
**Last Updated:** 2024
**Architecture:** Event-driven REST API
**Scalability:** Horizontal (stateless)
