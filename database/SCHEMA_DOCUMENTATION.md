# FIXIT Mobile Sales & Services - Database Schema Documentation

## Database Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     FIXIT DATABASE SCHEMA                            │
└─────────────────────────────────────────────────────────────────────┘

PUBLIC SECTION (Accessible to all authenticated and anonymous users via RLS):
═══════════════════════════════════════════════════════════════════════

  ┌──────────────┐
  │  PRODUCTS    │         ┌──────────────────────┐
  │──────────────│         │ PRODUCT_ENQUIRIES    │
  │ id (UUID)    │◄────────│──────────────────────│
  │ name         │         │ id (UUID)            │
  │ price        │         │ product_id (FK)      │
  │ description  │         │ customer_name        │
  │ image_path   │         │ customer_phone       │
  │ is_active    │         │ customer_email       │
  │ created_at   │         │ enquiry_status       │
  │ updated_at   │         │ created_at           │
  └──────────────┘         └──────────────────────┘

  ┌──────────────────┐
  │   SERVICES       │
  │──────────────────│
  │ id (UUID)        │
  │ service_name     │
  │ description      │
  │ icon_path        │
  │ is_active        │
  │ display_order    │
  │ created_at       │
  │ updated_at       │
  └──────────────────┘

  ┌────────────────────────┐
  │ STORE_INFORMATION      │
  │────────────────────────│
  │ id (UUID)              │
  │ business_name          │
  │ phone_number           │
  │ whatsapp_number        │
  │ email                  │
  │ address                │
  │ city, state, pincode   │
  │ working_hours_*        │
  │ about_content          │
  │ map_lat, map_lon       │
  │ created_at             │
  │ updated_at             │
  └────────────────────────┘

PRIVATE SECTION (Admin only, NEVER visible to public):
═══════════════════════════════════════════════════════════════════════

  ┌──────────────────────────────┐
  │  SECOND_HAND_LISTINGS        │         ┌────────────────────┐
  │──────────────────────────────│         │ SECOND_HAND_IMAGES │
  │ id (UUID)                    │◄────────│────────────────────│
  │ seller_name                  │         │ id (UUID)          │
  │ seller_phone                 │         │ listing_id (FK)    │
  │ seller_email                 │         │ storage_path       │
  │ product_name                 │         │ image_order (1-4)  │
  │ condition (NEW|GOOD|...)     │         │ is_thumbnail       │
  │ detailed_description         │         │ created_at         │
  │ expected_price               │         └────────────────────┘
  │ listing_status               │
  │ email_verified               │
  │ email_verification_token     │
  │ email_verified_at            │
  │ created_at                   │
  │ updated_at                   │
  └──────────────────────────────┘

STORAGE SECTION (Separate from PostgreSQL):
═══════════════════════════════════════════════════════════════════════

  ┌─────────────────┐
  │ STORAGE BUCKETS │
  └─────────────────┘

  products/ (PUBLIC)
  ├── {product_id}/
  │   └── image.jpg  ──→  Referenced in products.image_path
  │
  service-icons/ (PUBLIC)
  ├── {service_id}/
  │   └── icon.png   ──→  Referenced in services.icon_path
  │
  second-hand/ (PRIVATE - Admin Only)
  ├── {listing_id}/
  │   ├── thumbnail.jpg  ──→  Referenced in second_hand_images (is_thumbnail=true)
  │   ├── 1.jpg
  │   ├── 2.jpg
  │   ├── 3.jpg
  │   └── 4.jpg          ──→  All referenced in second_hand_images (image_order=1-4)
```

---

## Detailed Table Specifications

### 1. PRODUCTS

**Purpose:** Main product catalog for customer purchase/enquiry.

**Visibility:** PUBLIC (anyone can read active products)

```sql
Table: products
├── id UUID PRIMARY KEY
├── name VARCHAR(255) NOT NULL
├── price DECIMAL(10, 2) NOT NULL [CHECK: price >= 0]
├── description TEXT NOT NULL
├── image_path VARCHAR(500) [Reference to storage: products/{product_id}/filename]
├── is_active BOOLEAN DEFAULT true [Filter for public visibility]
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP [Auto-updated by trigger]

Indexes:
├── idx_products_is_active [For filtering active products]
├── idx_products_created_at [For chronological queries]

Foreign Keys:
└── product_id referenced by product_enquiries

Triggers:
└── update_products_updated_at [Auto-update timestamp on modification]

RLS Policies:
├── public_read_active_products [Anonymous: SELECT where is_active=true]
├── admin_insert_products [Admin: INSERT]
├── admin_update_products [Admin: UPDATE]
└── admin_delete_products [Admin: DELETE]
```

**Example Row:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Samsung Galaxy S21 Screen Replacement",
  "price": 8999.00,
  "description": "Original Samsung Galaxy S21 AMOLED display replacement...",
  "image_path": "products/550e8400-e29b-41d4-a716-446655440000/screen.jpg",
  "is_active": true,
  "created_at": "2026-09-01T10:00:00Z",
  "updated_at": "2026-09-01T10:00:00Z"
}
```

---

### 2. PRODUCT_ENQUIRIES

**Purpose:** Capture customer interest in products for WhatsApp follow-up.

**Visibility:** PUBLIC (create), ADMIN ONLY (read/update)

```sql
Table: product_enquiries
├── id UUID PRIMARY KEY
├── product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE
├── customer_name VARCHAR(255) NOT NULL
├── customer_phone VARCHAR(20) NOT NULL
├── customer_email VARCHAR(255) NOT NULL
├── enquiry_status VARCHAR(50) DEFAULT 'PENDING'
│   └── CHECK: enquiry_status IN ('PENDING', 'CONTACTED', 'CONVERTED', 'REJECTED')
└── created_at TIMESTAMP DEFAULT NOW()

Indexes:
├── idx_product_enquiries_product_id [For finding enquiries by product]
├── idx_product_enquiries_created_at [For chronological queries]
└── idx_product_enquiries_status [For filtering by status]

Foreign Keys:
└── product_id → products(id) [Cascading delete]

RLS Policies:
├── public_insert_enquiries [Anonymous: Can insert their own enquiry]
├── admin_read_enquiries [Admin: Can view all enquiries]
└── admin_update_enquiries [Admin: Can update enquiry status]

Security:
└── No public read: Customers cannot see other customers' enquiries
```

**Example Row:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440111",
  "product_id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_name": "John Doe",
  "customer_phone": "+91-9876543210",
  "customer_email": "john@example.com",
  "enquiry_status": "PENDING",
  "created_at": "2026-09-01T12:30:00Z"
}
```

---

### 3. SECOND_HAND_LISTINGS

**Purpose:** PRIVATE user submissions for old mobile/product sales.

**Visibility:** ADMIN ONLY (completely hidden from public)

```sql
Table: second_hand_listings
├── id UUID PRIMARY KEY
├── seller_name VARCHAR(255) NOT NULL
├── seller_phone VARCHAR(20) NOT NULL
├── seller_email VARCHAR(255) NOT NULL [PRIVATE - Not exposed publicly]
├── product_name VARCHAR(255) NOT NULL
├── condition VARCHAR(50) NOT NULL
│   └── CHECK: condition IN ('NEW', 'GOOD', 'FAIR', 'POOR')
├── detailed_description TEXT NOT NULL
├── expected_price DECIMAL(10, 2) NOT NULL [CHECK: price >= 0]
├── listing_status VARCHAR(50) DEFAULT 'NEW'
│   └── CHECK: listing_status IN ('NEW', 'REVIEWING', 'CONTACTED', 'ACCEPTED', 'REJECTED', 'COMPLETED')
├── email_verified BOOLEAN DEFAULT false NOT NULL [Required before acceptance]
├── email_verification_token VARCHAR(500) [Unique token for email link]
├── email_verified_at TIMESTAMP [When email was verified]
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP [Auto-updated by trigger]

Indexes:
├── idx_second_hand_listings_seller_email [For finding seller's listings]
├── idx_second_hand_listings_status [For filtering by status]
├── idx_second_hand_listings_email_verified [For verification workflow]
└── idx_second_hand_listings_created_at [For chronological queries]

Foreign Keys:
└── id referenced by second_hand_images

Triggers:
└── update_second_hand_listings_updated_at [Auto-update timestamp]

RLS Policies:
├── admin_read_listings [Admin: Full access]
├── admin_insert_listings [Admin: Can insert]
├── admin_update_listings [Admin: Can modify]
└── admin_delete_listings [Admin: Can delete]

Security:
└── NO PUBLIC POLICIES - Anonymous users blocked completely
```

**Example Row:**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440222",
  "seller_name": "Rajesh Kumar",
  "seller_phone": "+91-9876543210",
  "seller_email": "rajesh@example.com",
  "product_name": "iPhone 12 (64GB)",
  "condition": "GOOD",
  "detailed_description": "Used iPhone 12 in excellent condition...",
  "expected_price": 28000.00,
  "listing_status": "NEW",
  "email_verified": false,
  "email_verification_token": "eyJhbGciOiJIUzI1NiIs...",
  "email_verified_at": null,
  "created_at": "2026-09-01T14:00:00Z",
  "updated_at": "2026-09-01T14:00:00Z"
}
```

**Status Flow:**
```
NEW → REVIEWING → CONTACTED → ACCEPTED → COMPLETED
        ↓
      REJECTED
```

---

### 4. SECOND_HAND_IMAGES

**Purpose:** Store up to 4 images per second-hand listing with thumbnail identification.

**Visibility:** ADMIN ONLY (completely hidden from public)

```sql
Table: second_hand_images
├── id UUID PRIMARY KEY
├── listing_id UUID NOT NULL REFERENCES second_hand_listings(id) ON DELETE CASCADE
├── storage_path VARCHAR(500) NOT NULL [Path to: second-hand/{listing_id}/{order}.jpg]
├── image_order INTEGER NOT NULL [CHECK: image_order BETWEEN 1 AND 4]
├── is_thumbnail BOOLEAN NOT NULL DEFAULT false [Only ONE per listing]
└── created_at TIMESTAMP DEFAULT NOW()

Unique Constraints:
└── UNIQUE(listing_id, image_order) [Each order position unique per listing]

Check Constraints:
└── image_order BETWEEN 1 AND 4 [Maximum 4 images per listing]

Indexes:
├── idx_second_hand_images_listing_id [For finding images by listing]
└── idx_second_hand_images_is_thumbnail [For finding thumbnail quickly]

Foreign Keys:
└── listing_id → second_hand_listings(id) [Cascading delete]

Triggers:
├── check_second_hand_images_limit [Prevents > 4 images per listing]
└── ensure_single_thumbnail [Ensures only 1 thumbnail per listing]

RLS Policies:
├── admin_read_images [Admin: Full access]
├── admin_insert_images [Admin: Can insert]
├── admin_update_images [Admin: Can modify]
└── admin_delete_images [Admin: Can delete]

Security:
└── NO PUBLIC POLICIES - Completely private
```

**Example Rows:**
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440333",
    "listing_id": "770e8400-e29b-41d4-a716-446655440222",
    "storage_path": "second-hand/770e8400-e29b-41d4-a716-446655440222/thumbnail.jpg",
    "image_order": 1,
    "is_thumbnail": true,
    "created_at": "2026-09-01T14:05:00Z"
  },
  {
    "id": "990e8400-e29b-41d4-a716-446655440444",
    "listing_id": "770e8400-e29b-41d4-a716-446655440222",
    "storage_path": "second-hand/770e8400-e29b-41d4-a716-446655440222/2.jpg",
    "image_order": 2,
    "is_thumbnail": false,
    "created_at": "2026-09-01T14:06:00Z"
  }
]
```

---

### 5. SERVICES

**Purpose:** Display available services on home page.

**Visibility:** PUBLIC (anyone can read active services)

```sql
Table: services
├── id UUID PRIMARY KEY
├── service_name VARCHAR(255) NOT NULL
├── description TEXT NOT NULL
├── icon_path VARCHAR(500) [Reference to storage: service-icons/{service_id}/filename]
├── is_active BOOLEAN DEFAULT true [Filter for public display]
├── display_order INTEGER DEFAULT 0 [Sequence on home page]
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP [Auto-updated by trigger]

Indexes:
├── idx_services_is_active [For filtering active services]
└── idx_services_display_order [For sorting by sequence]

Triggers:
└── update_services_updated_at [Auto-update timestamp]

RLS Policies:
├── public_read_active_services [Anonymous: SELECT where is_active=true]
├── admin_insert_services [Admin: INSERT]
├── admin_update_services [Admin: UPDATE]
└── admin_delete_services [Admin: DELETE]
```

**Example Row:**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440555",
  "service_name": "Screen Replacement",
  "description": "Professional mobile screen repair and replacement...",
  "icon_path": "service-icons/aa0e8400-e29b-41d4-a716-446655440555/screen.png",
  "is_active": true,
  "display_order": 1,
  "created_at": "2026-09-01T09:00:00Z",
  "updated_at": "2026-09-01T09:00:00Z"
}
```

---

### 6. STORE_INFORMATION

**Purpose:** Business contact and operational information.

**Visibility:** PUBLIC (anyone can read)

```sql
Table: store_information
├── id UUID PRIMARY KEY
├── business_name VARCHAR(255) NOT NULL
├── phone_number VARCHAR(20) NOT NULL
├── whatsapp_number VARCHAR(20) NOT NULL
├── email VARCHAR(255) NOT NULL
├── address VARCHAR(500) NOT NULL
├── city VARCHAR(100) NOT NULL
├── state VARCHAR(100) NOT NULL
├── pincode VARCHAR(10) NOT NULL
├── working_hours_monday_to_friday VARCHAR(255) [e.g., "9:00 AM - 9:00 PM"]
├── working_hours_saturday VARCHAR(255) [e.g., "10:00 AM - 8:00 PM"]
├── working_hours_sunday VARCHAR(255) [e.g., "10:00 AM - 6:00 PM"]
├── about_content TEXT [HTML or markdown for about page]
├── map_lat DECIMAL(10, 8) [Google Maps latitude]
├── map_lon DECIMAL(11, 8) [Google Maps longitude]
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP [Auto-updated by trigger]

Triggers:
└── update_store_information_updated_at [Auto-update timestamp]

RLS Policies:
├── public_read_store_info [Anonymous: Can read all]
├── admin_insert_store_info [Admin: Can insert]
├── admin_update_store_info [Admin: Can update]
└── admin_delete_store_info [Admin: Can delete]

Note:
└── Typically only ONE row exists in this table
```

**Example Row:**
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440666",
  "business_name": "FIXIT Mobile Sales & Services",
  "phone_number": "+91-XXXXXXXXXX",
  "whatsapp_number": "+91-XXXXXXXXXX",
  "email": "contact@fixitmobile.com",
  "address": "123 Main Street, Shopping Complex",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "working_hours_monday_to_friday": "9:00 AM - 9:00 PM",
  "working_hours_saturday": "10:00 AM - 8:00 PM",
  "working_hours_sunday": "10:00 AM - 6:00 PM",
  "about_content": "FIXIT is a trusted mobile repair...",
  "map_lat": 28.6139,
  "map_lon": 77.2090,
  "created_at": "2026-09-01T08:00:00Z",
  "updated_at": "2026-09-01T08:00:00Z"
}
```

---

## Row Level Security (RLS) Summary

### Access Matrix

```
                 │ Anonymous │ Authenticated │ Admin
─────────────────┼───────────┼───────────────┼──────────
PRODUCTS         │ Read*     │ Read*         │ CRUD
PRODUCT_ENQUIRY  │ Create    │ Create        │ CRUD
SERVICES         │ Read*     │ Read*         │ CRUD
STORE_INFO       │ Read      │ Read          │ CRUD
SECOND_HAND      │ ❌ NONE   │ ❌ NONE       │ CRUD
SECOND_HAND_IMG  │ ❌ NONE   │ ❌ NONE       │ CRUD

* = Only active records (is_active = true)
```

### RLS Policy Details

**Public Access (RLS Allows):**
- ✅ Read active products and details
- ✅ Submit product enquiries
- ✅ Read active services
- ✅ Read public store information
- ❌ Cannot read second-hand listings
- ❌ Cannot read other customers' enquiries
- ❌ Cannot see seller contact information

**Admin Access (RLS Allows):**
- ✅ Full CRUD on all tables
- ✅ View all private second-hand listings
- ✅ See seller contact information
- ✅ View all product enquiries
- ✅ Manage products, services, store info

---

## Database Functions

### 1. verify_second_hand_listing_email()
**Purpose:** Verify email for second-hand listings
```sql
verify_second_hand_listing_email(p_listing_id UUID, p_token VARCHAR)
RETURNS: JSONB
```

### 2. create_second_hand_listing()
**Purpose:** Create new listing with validation
```sql
create_second_hand_listing(
  seller_name, seller_phone, seller_email,
  product_name, condition, description, price, token
)
RETURNS: JSONB
```

### 3. get_product_with_enquiry_count()
**Purpose:** Get product + enquiry statistics
```sql
get_product_with_enquiry_count(p_product_id UUID)
RETURNS: JSONB
```

### 4. get_listing_with_images()
**Purpose:** Get complete listing with all images
```sql
get_listing_with_images(p_listing_id UUID)
RETURNS: JSONB
```

### 5. update_listing_status()
**Purpose:** Update listing workflow status
```sql
update_listing_status(p_listing_id UUID, p_new_status VARCHAR)
RETURNS: JSONB
```

### 6. get_enquiries_for_product()
**Purpose:** Get all enquiries for a product
```sql
get_enquiries_for_product(p_product_id UUID)
RETURNS: JSONB
```

### 7. get_listing_stats()
**Purpose:** Get second-hand listing statistics
```sql
get_listing_stats()
RETURNS: JSONB
```

---

## Constraints & Validation

### Data Integrity

```
products:
  ├── price >= 0 (CHECK constraint)
  ├── name NOT NULL
  └── description NOT NULL

product_enquiries:
  ├── enquiry_status IN (PENDING, CONTACTED, CONVERTED, REJECTED)
  ├── product_id references products (CASCADE DELETE)
  └── customer_email NOT NULL

second_hand_listings:
  ├── expected_price >= 0 (CHECK constraint)
  ├── condition IN (NEW, GOOD, FAIR, POOR) (CHECK constraint)
  ├── listing_status IN (NEW, REVIEWING, CONTACTED, ACCEPTED, REJECTED, COMPLETED)
  ├── seller_email NOT NULL
  ├── email_verified NOT NULL (DEFAULT false)
  └── product_name NOT NULL

second_hand_images:
  ├── image_order BETWEEN 1 AND 4 (CHECK constraint)
  ├── UNIQUE(listing_id, image_order)
  ├── listing_id references second_hand_listings (CASCADE DELETE)
  ├── Only 1 thumbnail per listing (TRIGGER enforce)
  └── Maximum 4 images per listing (TRIGGER enforce)

services:
  └── service_name NOT NULL

store_information:
  └── business_name NOT NULL
```

---

## Triggers

### Auto-Update Triggers
```sql
update_products_updated_at
update_second_hand_listings_updated_at
update_services_updated_at
update_store_information_updated_at
```

### Constraint Enforcement Triggers
```sql
check_second_hand_images_limit
  → Prevents more than 4 images per listing

ensure_single_thumbnail
  → Ensures only one thumbnail per listing
  → Automatically unsets previous thumbnail when new one is set
```

---

## Performance Considerations

### Indexes
- All foreign keys indexed for JOIN performance
- is_active indexed for filtering public content
- created_at indexed for chronological queries
- Status fields indexed for filtering workflows
- Email indexed for verification lookups

### Query Optimization
- Use indexes for WHERE clauses on: is_active, created_at, status, email
- RLS policies automatically filter results at database level
- Functions use efficient aggregations and JOINs
- Storage references avoid N+1 queries (no binary data in DB)

---

## Backup & Recovery

### Important
- Regular PostgreSQL backups via Supabase
- Storage bucket backups (image files stored separately)
- Database schema exported: `database/migrations/`
- RLS configuration documented: `database/migrations/002_rls_policies.sql`
- Seed data available: `database/seed/sample_data.sql`

---

## Migration History

| File | Purpose | Status |
|------|---------|--------|
| 001_initial_schema.sql | Table creation & triggers | ✅ Required |
| 002_rls_policies.sql | Security policies | ✅ Required |
| functions/backend_operations.sql | Helper functions | ✅ Required |
| seed/sample_data.sql | Test data | ⏳ Optional |

---

## Related Documentation

- [README.md](./README.md) - Overview and architecture
- [SUPABASE_SETUP.md](./SUPABASE_SETUP.md) - Manual Supabase configuration steps
- [storage-config/storage_buckets.sql](./storage-config/storage_buckets.sql) - Storage configuration details
- [migrations/](./migrations/) - All SQL migration files
- [functions/](./functions/) - PostgreSQL functions
- [seed/](./seed/) - Sample data for development

