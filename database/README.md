# FIXIT Mobile Sales & Services - Database Architecture

## Overview

This database is built using **Supabase PostgreSQL** and provides the backend data layer for the FIXIT Mobile Sales & Services website.

## Database Structure

```
database/
├── migrations/           # SQL migration files for schema setup
├── seed/                # Seed data for testing/initial setup
├── functions/           # PostgreSQL functions and triggers
├── storage-config/      # Storage bucket configuration
└── README.md           # This file
```

## Core Tables

### 1. `products`
Main product catalog for FIXIT services and products.

**Purpose:** Store products that customers can view and enquire about.

**Fields:**
- `id`: Primary key (UUID)
- `name`: Product name (required)
- `price`: Product price in rupees (required, must be >= 0)
- `description`: Detailed product description (required)
- `image_path`: Storage reference to single product image (optional)
- `is_active`: Availability status (default: true)
- `created_at`: Timestamp (auto)
- `updated_at`: Timestamp (auto)

**Security:** Public read access for active products only. Admin-only for modifications.

---

### 2. `product_enquiries`
Customer enquiries for products.

**Purpose:** Capture customer interest in products for follow-up via WhatsApp.

**Fields:**
- `id`: Primary key (UUID)
- `product_id`: Foreign key to products (required)
- `customer_name`: Customer name (required)
- `customer_phone`: Phone number (required)
- `customer_email`: Email address (required)
- `enquiry_status`: Status tracking (default: 'PENDING')
- `created_at`: Timestamp (auto)

**Security:** Public create access, admin can view all enquiries.

---

### 3. `second_hand_listings`
User-submitted listings for old mobile phones and products.

**Purpose:** Private marketplace for sellers to submit used products. NEVER publicly displayed as marketplace.

**Fields:**
- `id`: Primary key (UUID)
- `seller_name`: Seller name (required)
- `seller_phone`: Seller phone (required)
- `seller_email`: Seller email (required)
- `product_name`: Mobile/product name (required)
- `condition`: Product condition (NEW, GOOD, FAIR, POOR) (required)
- `detailed_description`: Full description (required)
- `expected_price`: Expected selling price (required)
- `listing_status`: Status (NEW, REVIEWING, CONTACTED, ACCEPTED, REJECTED, COMPLETED) (default: 'NEW')
- `email_verified`: Email verification flag (required, default: false)
- `email_verification_token`: Token for email verification (optional)
- `email_verified_at`: Timestamp of verification (optional)
- `created_at`: Timestamp (auto)
- `updated_at`: Timestamp (auto)

**Security:** Private. Public users MUST NOT access. Admin-only. RLS enforced.

---

### 4. `second_hand_images`
Images associated with second-hand listings.

**Purpose:** Store up to 4 images per listing with thumbnail identification.

**Fields:**
- `id`: Primary key (UUID)
- `listing_id`: Foreign key to second_hand_listings (required)
- `storage_path`: Path to image in Supabase Storage (required)
- `image_order`: Order within listing (1-4) (required)
- `is_thumbnail`: Flag for thumbnail image (required)
- `created_at`: Timestamp (auto)

**Constraints:**
- Foreign key enforces listing existence
- CHECK: image_order BETWEEN 1 AND 4
- Unique: (listing_id, image_order)
- Only one thumbnail per listing

**Security:** Private. Admin-only access. RLS enforced.

---

### 5. `services`
FIXIT services offered.

**Purpose:** Display available services on home page.

**Fields:**
- `id`: Primary key (UUID)
- `service_name`: Service name (required)
- `description`: Service description (required)
- `icon_path`: Storage reference to service icon (optional)
- `is_active`: Availability status (default: true)
- `display_order`: Display sequence (default: 0)
- `created_at`: Timestamp (auto)
- `updated_at`: Timestamp (auto)

**Security:** Public read access for active services. Admin-only for modifications.

---

### 6. `store_information`
Business information and contact details.

**Purpose:** Store business details, contact info, hours, address, etc.

**Fields:**
- `id`: Primary key (UUID)
- `business_name`: Store name (required)
- `phone_number`: Main phone (required)
- `whatsapp_number`: WhatsApp contact (required)
- `email`: Business email (required)
- `address`: Physical address (required)
- `city`: City (required)
- `state`: State (required)
- `pincode`: Postal code (required)
- `working_hours_monday_to_friday`: Hours text (e.g., "9:00 AM - 6:00 PM") (optional)
- `working_hours_saturday`: Hours text (optional)
- `working_hours_sunday`: Hours text (optional)
- `about_content`: About section HTML/text (optional)
- `map_lat`: Map latitude (optional)
- `map_lon`: Map longitude (optional)
- `created_at`: Timestamp (auto)
- `updated_at`: Timestamp (auto)

**Security:** Public read access. Admin-only for modifications.

---

## Storage Buckets

### `products`
- Stores product images (1 per product)
- **Access:** Public read, authenticated admin write
- **Naming:** `products/{product_id}/{filename}`

### `second-hand`
- Stores second-hand listing images (up to 4 per listing)
- **Access:** Private, authenticated admin only
- **Naming:** `second-hand/{listing_id}/{image_order}.jpg` or similar

### `service-icons`
- Stores service icons/images
- **Access:** Public read, authenticated admin write
- **Naming:** `service-icons/{service_id}/{filename}`

---

## Row Level Security (RLS) Policies

### `products` Table
- **Public (anon):** SELECT only active records
- **Authenticated (admin):** Full CRUD access

### `product_enquiries` Table
- **Public (anon):** INSERT allowed
- **Authenticated (admin):** SELECT, UPDATE all records
- **Public (anon):** Cannot read other user's enquiries

### `second_hand_listings` Table
- **Public (anon):** SELECT BLOCKED
- **Authenticated (admin):** Full CRUD access
- **Sensitive fields:** Not exposed to public at all

### `second_hand_images` Table
- **Public (anon):** SELECT BLOCKED
- **Authenticated (admin):** Full CRUD access
- **Private storage:** Only admin can view image URLs

### `services` Table
- **Public (anon):** SELECT only active records
- **Authenticated (admin):** Full CRUD access

### `store_information` Table
- **Public (anon):** SELECT allowed
- **Authenticated (admin):** Full CRUD access

---

## Authentication

- Uses **Supabase Auth** for admin authentication
- Admin users must be set up in Supabase Auth
- Admin role determined via custom claims or auth table
- Public access via Row Level Security policies

---

## Email Verification

For second-hand listings:
- Email verification required before final submission
- Backend will generate verification token
- Seller clicks link to verify
- Only verified listings can be accepted

---

## Setup Checklist

1. ✅ SQL Migrations created
2. ✅ RLS Policies defined
3. ✅ Storage bucket structure planned
4. ⏳ Manual: Create Supabase project
5. ⏳ Manual: Run migrations
6. ⏳ Manual: Create storage buckets
7. ⏳ Manual: Configure RLS in Supabase
8. ⏳ Manual: Set up authentication
9. ⏳ Manual: Configure email verification

---

## Seed Data

Optional seed data is available in `seed/` directory for development testing.

---

## File Reference

- `migrations/001_initial_schema.sql` - All table definitions
- `migrations/002_rls_policies.sql` - Row Level Security policies
- `seed/sample_data.sql` - Sample data for development

