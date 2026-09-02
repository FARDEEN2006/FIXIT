-- FIXIT Mobile Sales & Services - Storage Configuration
-- Guide for Supabase Storage Buckets
-- Created: 2026-09-01

-- ============================================================
-- Overview
-- ============================================================

-- Supabase Storage holds actual image files separately from PostgreSQL
-- This configuration file documents the bucket structure and RLS policies
-- Note: Storage bucket creation and RLS must be done via Supabase Dashboard

-- ============================================================
-- Bucket 1: products
-- Purpose: Public product images
-- ============================================================

Bucket Name: products
Visibility: Public
Max File Size: 5 MB per file

Naming Convention:
  products/{product_id}/{filename}

Examples:
  products/550e8400-e29b-41d4-a716-446655440000/screen_replacement.jpg
  products/6ba7b810-9dad-11d1-80b4-00c04fd430c8/battery_pack.jpg

RLS Policy (Public Read):
  - Anonymous users: CAN READ ALL FILES
  - Authenticated admin: CAN READ, INSERT, UPDATE, DELETE
  - Other authenticated users: Cannot access

Storage RLS Rules:

  CREATE POLICY "Allow public read products" ON storage.objects
    FOR SELECT
    USING (bucket_id = 'products' AND auth.role() = 'anon');

  CREATE POLICY "Allow admin write products" ON storage.objects
    FOR INSERT
    WITH CHECK (bucket_id = 'products' AND is_admin());

  CREATE POLICY "Allow admin delete products" ON storage.objects
    FOR DELETE
    USING (bucket_id = 'products' AND is_admin());

-- ============================================================
-- Bucket 2: second-hand
-- Purpose: Private second-hand listing images
-- CRITICAL: ABSOLUTELY NO PUBLIC ACCESS
-- ============================================================

Bucket Name: second-hand
Visibility: Private
Max File Size: 5 MB per file

Naming Convention:
  second-hand/{listing_id}/{image_order}.jpg
  second-hand/{listing_id}/thumbnail.jpg

Examples:
  second-hand/770e8400-e29b-41d4-a716-446655440111/1.jpg
  second-hand/770e8400-e29b-41d4-a716-446655440111/thumbnail.jpg
  second-hand/770e8400-e29b-41d4-a716-446655440111/2.jpg
  second-hand/770e8400-e29b-41d4-a716-446655440111/3.jpg
  second-hand/770e8400-e29b-41d4-a716-446655440111/4.jpg

RLS Policy (Admin Only):
  - Anonymous users: CANNOT READ, CANNOT WRITE
  - Authenticated admin: CAN READ, INSERT, UPDATE, DELETE
  - Other authenticated users: Cannot access

Storage RLS Rules:

  CREATE POLICY "Allow admin read second-hand" ON storage.objects
    FOR SELECT
    USING (bucket_id = 'second-hand' AND is_admin());

  CREATE POLICY "Allow admin write second-hand" ON storage.objects
    FOR INSERT
    WITH CHECK (bucket_id = 'second-hand' AND is_admin());

  CREATE POLICY "Allow admin delete second-hand" ON storage.objects
    FOR DELETE
    USING (bucket_id = 'second-hand' AND is_admin());

  -- DENY PUBLIC ACCESS (no anon select policy)

-- ============================================================
-- Bucket 3: service-icons
-- Purpose: Service icons/images (public)
-- ============================================================

Bucket Name: service-icons
Visibility: Public
Max File Size: 2 MB per file

Naming Convention:
  service-icons/{service_id}/{filename}

Examples:
  service-icons/880e8400-e29b-41d4-a716-446655440222/screen-repair.png
  service-icons/880e8400-e29b-41d4-a716-446655440333/battery-service.png

RLS Policy (Public Read):
  - Anonymous users: CAN READ ALL FILES
  - Authenticated admin: CAN READ, INSERT, UPDATE, DELETE
  - Other authenticated users: Cannot access

Storage RLS Rules:

  CREATE POLICY "Allow public read service-icons" ON storage.objects
    FOR SELECT
    USING (bucket_id = 'service-icons' AND auth.role() = 'anon');

  CREATE POLICY "Allow admin write service-icons" ON storage.objects
    FOR INSERT
    WITH CHECK (bucket_id = 'service-icons' AND is_admin());

  CREATE POLICY "Allow admin delete service-icons" ON storage.objects
    FOR DELETE
    USING (bucket_id = 'service-icons' AND is_admin());

-- ============================================================
-- Image Upload Workflow
-- ============================================================

FRONTEND UPLOAD PROCESS (handled by backend in production):

1. User selects image(s)
2. Backend compresses image to appropriate size
3. Backend generates unique storage path
4. Backend uploads to Supabase Storage
5. Backend records storage_path in database
6. Database stores reference only, not binary data

COMPRESSION REQUIREMENTS:
  - Product images: Max 1200px width, 80% JPEG quality
  - Service icons: Max 500px width, PNG format
  - Second-hand images: Max 1200px width, 80% JPEG quality
  - Thumbnails: Max 600px width

-- ============================================================
-- Database & Storage Relationship
-- ============================================================

TABLE: products
  └─> image_path (VARCHAR) -> Storage file at: products/{product_id}/{filename}

TABLE: services
  └─> icon_path (VARCHAR) -> Storage file at: service-icons/{service_id}/{filename}

TABLE: second_hand_listings
  └─> Many second_hand_images (foreign key)
      └─> second_hand_images.storage_path -> Storage at: second-hand/{listing_id}/{order}.jpg

IMPORTANT:
  - Storage paths are REFERENCES, not copies
  - If you delete an image from Storage, the path in DB becomes invalid
  - If you delete a record from DB, the file in Storage remains (must delete separately)
  - Frontend/Backend should handle cascading cleanup

-- ============================================================
-- Access Examples
-- ============================================================

PUBLIC ACCESS (Anonymous User - Browser):
  Product Image:
    https://{project-url}/storage/v1/object/public/products/{product_id}/image.jpg

  Service Icon:
    https://{project-url}/storage/v1/object/public/service-icons/{service_id}/icon.png

  Second-hand Image:
    ❌ BLOCKED - Private bucket, no public URLs

ADMIN ACCESS (Authenticated):
  Requires Supabase client with auth token:
    - Can download private files
    - Can list bucket contents
    - Can upload/delete files

-- ============================================================
-- Recommended Backend Implementation
-- ============================================================

For actual image uploads (handled by backend, not database):

1. Accept image file from client
2. Generate unique ID (UUIDv4)
3. Compress using Sharp/ImageMagick/Pillow:
   - Product: width 1200px, quality 80%
   - Service: width 500px, PNG
   - Second-hand: width 1200px, quality 80%
4. Upload to Supabase Storage
5. Store path in PostgreSQL table
6. Return JSON with:
   {
     "id": "image-record-id",
     "storage_path": "products/product-id/compressed.jpg",
     "url": "https://project-url/storage/v1/object/public/products/..."
   }

-- ============================================================
-- Error Scenarios
-- ============================================================

Scenario 1: User uploads image > 5MB
  → Backend rejects with error before upload
  → Compression should reduce size

Scenario 2: Second-hand listing receives > 4 images
  → Database trigger CHECK constraint prevents insertion
  → Backend should validate before upload

Scenario 3: Storage file deleted manually but DB record exists
  → Frontend/Backend detects 404 when loading image
  → Should handle gracefully with placeholder/retry

Scenario 4: User unauthorized to access second-hand image
  → Storage RLS blocks access with 403 Forbidden
  → Database RLS prevents SELECT query

-- ============================================================
-- End of Storage Configuration
-- ============================================================
