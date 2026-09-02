# FIXIT Database - Deployment & Testing Checklist

Complete this checklist before considering the database ready for backend development.

---

## Phase 1: Supabase Project Setup ✅

- [ ] Supabase project created at https://supabase.com
- [ ] Project URL saved: `https://[project-id].supabase.co`
- [ ] Anon Key saved (for frontend use)
- [ ] Service Role Key saved securely (never commit to git)
- [ ] Database password secured
- [ ] Region selected appropriately

**Checkpoint:** All credentials stored securely in password manager, not in git/environment

---

## Phase 2: Database Migrations ✅

### Run SQL Migrations in Order

In Supabase SQL Editor:

- [ ] Ran: `migrations/001_initial_schema.sql`
  - [ ] 6 tables created (verify in Tables list)
  - [ ] 4 update triggers created
  - [ ] 2 constraint check functions created
  - [ ] All indexes created

- [ ] Ran: `migrations/002_rls_policies.sql`
  - [ ] `is_admin()` function created
  - [ ] RLS enabled on all 6 tables
  - [ ] All policies configured (multiple per table)

- [ ] Ran: `functions/backend_operations.sql`
  - [ ] 7 backend functions created:
    - [ ] verify_second_hand_listing_email
    - [ ] create_second_hand_listing
    - [ ] get_product_with_enquiry_count
    - [ ] get_listing_with_images
    - [ ] update_listing_status
    - [ ] get_enquiries_for_product
    - [ ] get_listing_stats

**Verification Query:**
```sql
-- Run in SQL Editor to verify all tables exist
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Expected output: 6 tables
-- - products
-- - product_enquiries
-- - second_hand_listings
-- - second_hand_images
-- - services
-- - store_information
```

**Checkpoint:** All tables, triggers, and functions present

---

## Phase 3: Storage Buckets ✅

### Create Storage Buckets

In Supabase Dashboard > Storage:

- [ ] Bucket "products" created
  - [ ] Visibility: PUBLIC
  - [ ] Max file size: 5 MB (or your preference)

- [ ] Bucket "second-hand" created
  - [ ] Visibility: PRIVATE
  - [ ] Max file size: 5 MB (or your preference)

- [ ] Bucket "service-icons" created
  - [ ] Visibility: PUBLIC
  - [ ] Max file size: 2 MB (or your preference)

**Checkpoint:** All 3 buckets created with correct visibility settings

---

## Phase 4: Storage RLS Policies ✅

### Configure Storage Bucket Policies

In Supabase Dashboard > Storage > Policies:

**For "products" bucket:**
- [ ] Policy "Allow public read" created
  - Role: PUBLIC
  - Operation: SELECT
  
- [ ] Policy "Allow admin write" created
  - Role: AUTHENTICATED
  - Operation: INSERT
  - Check: User is admin

**For "second-hand" bucket:**
- [ ] Policy "Allow admin read" created
  - Role: AUTHENTICATED
  - Operation: SELECT
  - Check: User is admin
  
- [ ] Policy "Allow admin write" created
  - Role: AUTHENTICATED
  - Operation: INSERT
  - Check: User is admin
  
- [ ] Policy "Allow admin delete" created
  - Role: AUTHENTICATED
  - Operation: DELETE
  - Check: User is admin

**For "service-icons" bucket:**
- [ ] Policy "Allow public read" created
  - Role: PUBLIC
  - Operation: SELECT
  
- [ ] Policy "Allow admin write" created
  - Role: AUTHENTICATED
  - Operation: INSERT
  - Check: User is admin

**Checkpoint:** All storage policies configured

---

## Phase 5: Authentication Setup ✅

In Supabase Dashboard > Authentication:

- [ ] Email/Password authentication enabled (default)
- [ ] Email signup allowed (Disable email signup = OFF)
- [ ] Custom SMTP configured (optional for production)
- [ ] Email templates customized (optional)

### Create Admin User

In Supabase Dashboard > Authentication > Users:

- [ ] Admin user created
  - Email: [your admin email]
  - Password: [strong password saved securely]
  - Confirmed: YES

### Set Admin Metadata

In Supabase Dashboard > Authentication > Users > [Select your user]:

- [ ] Click "User Metadata" tab
- [ ] Add JSON:
  ```json
  {
    "is_admin": true,
    "role": "admin"
  }
  ```
- [ ] Save changes

**Checkpoint:** Admin user created with proper metadata

---

## Phase 6: Row Level Security Verification ✅

### Test Public Access (Anonymous User)

Run in SQL Editor (no authentication):

```sql
-- Should return results (active products only)
SELECT * FROM products WHERE is_active = true;

-- Should return results (active services only)
SELECT * FROM services WHERE is_active = true;

-- Should return results
SELECT * FROM store_information;

-- Should return ERROR or 0 rows
SELECT * FROM second_hand_listings;

-- Should return ERROR or 0 rows
SELECT * FROM second_hand_images;

-- Should FAIL - RLS blocks insert
INSERT INTO second_hand_listings (...) VALUES (...);
```

- [ ] Active products visible
- [ ] Active services visible
- [ ] Store info visible
- [ ] Second-hand blocked
- [ ] Second-hand images blocked
- [ ] Insert blocked for non-admin

### Test Admin Access (Authenticated User)

1. Log in as admin user in SQL Editor
2. Run queries:

```sql
-- Should return ALL products (active and inactive)
SELECT * FROM products;

-- Should return ALL second-hand listings
SELECT * FROM second_hand_listings;

-- Should return ALL second-hand images
SELECT * FROM second_hand_images;

-- Should allow insert
INSERT INTO products (name, price, description) 
VALUES ('Test Product', 999.99, 'Test');
```

- [ ] Can see all products
- [ ] Can see second-hand listings
- [ ] Can see second-hand images
- [ ] Can insert new products
- [ ] Can update products
- [ ] Can delete products

**Checkpoint:** RLS policies working correctly for public and admin

---

## Phase 7: Data Integrity Testing ✅

### Test Constraints

```sql
-- Should FAIL - Price cannot be negative
INSERT INTO products (name, price, description) 
VALUES ('Bad Price', -100, 'Test');

-- Should FAIL - Invalid condition
INSERT INTO second_hand_listings (..., condition) 
VALUES (..., 'INVALID');

-- Should FAIL - Invalid enquiry status
INSERT INTO product_enquiries (..., enquiry_status) 
VALUES (..., 'INVALID');

-- Should FAIL - Image order out of range
INSERT INTO second_hand_images (..., image_order) 
VALUES (..., 5);

-- Should FAIL - Duplicate image order for same listing
INSERT INTO second_hand_images (listing_id, image_order, storage_path, is_thumbnail)
VALUES ('listing-1', 1, 'path-1', false);
INSERT INTO second_hand_images (listing_id, image_order, storage_path, is_thumbnail)
VALUES ('listing-1', 1, 'path-2', false);  -- Should fail
```

- [ ] Price validation works
- [ ] Condition enum validated
- [ ] Enquiry status enum validated
- [ ] Image order range validated (1-4)
- [ ] Image order uniqueness enforced

### Test Foreign Keys

```sql
-- Should FAIL - Product doesn't exist
INSERT INTO product_enquiries (product_id, customer_name, ...)
VALUES ('invalid-uuid', 'John', ...);

-- Should FAIL - Listing doesn't exist
INSERT INTO second_hand_images (listing_id, storage_path, image_order, is_thumbnail)
VALUES ('invalid-uuid', 'path', 1, true);

-- Should CASCADE DELETE images when listing deleted
DELETE FROM second_hand_listings WHERE id = 'valid-listing-id';
-- Verify: All images for this listing should also be deleted
SELECT COUNT(*) FROM second_hand_images WHERE listing_id = 'valid-listing-id';
-- Should return 0
```

- [ ] Foreign key constraints enforced
- [ ] Cascade delete works for second_hand_images
- [ ] Invalid IDs rejected

### Test Triggers

```sql
-- Should auto-update updated_at timestamp
UPDATE products SET name = 'Updated Name' WHERE id = 'product-id';
SELECT updated_at FROM products WHERE id = 'product-id';
-- Verify: updated_at is now CURRENT_TIMESTAMP

-- Should enforce single thumbnail per listing
INSERT INTO second_hand_images (..., is_thumbnail) VALUES (..., true);
INSERT INTO second_hand_images (..., is_thumbnail) VALUES (..., true);
-- Verify: First is_thumbnail set to false, second remains true

-- Should prevent > 4 images per listing
INSERT INTO second_hand_images (listing_id, image_order, ...) 
VALUES ('listing-1', 1, ...);
INSERT INTO second_hand_images (listing_id, image_order, ...) 
VALUES ('listing-1', 2, ...);
INSERT INTO second_hand_images (listing_id, image_order, ...) 
VALUES ('listing-1', 3, ...);
INSERT INTO second_hand_images (listing_id, image_order, ...) 
VALUES ('listing-1', 4, ...);
INSERT INTO second_hand_images (listing_id, image_order, ...) 
VALUES ('listing-1', 5, ...);  -- Should fail
```

- [ ] Updated timestamps auto-updated on INSERT
- [ ] Updated timestamps auto-updated on UPDATE
- [ ] Only one thumbnail per listing enforced
- [ ] Maximum 4 images per listing enforced

**Checkpoint:** All constraints and triggers working correctly

---

## Phase 8: Function Testing ✅

### Test Backend Functions

```sql
-- Test: get_product_with_enquiry_count
SELECT * FROM products WHERE is_active = true LIMIT 1;
-- Note the product_id, then run:
SELECT supabase.rpc('get_product_with_enquiry_count', 
  json_build_object('p_product_id', 'noted-product-id')
);
-- Should return product with enquiry_count field

-- Test: create_second_hand_listing
SELECT supabase.rpc('create_second_hand_listing',
  json_build_object(
    'p_seller_name', 'Test Seller',
    'p_seller_phone', '+91-9876543210',
    'p_seller_email', 'test@example.com',
    'p_product_name', 'Test Phone',
    'p_condition', 'GOOD',
    'p_detailed_description', 'Good condition',
    'p_expected_price', 15000,
    'p_verification_token', 'test-token-123'
  )
);
-- Should return success and listing_id

-- Test: verify_second_hand_listing_email
-- Use the listing_id from above with matching token
SELECT supabase.rpc('verify_second_hand_listing_email',
  json_build_object(
    'p_listing_id', 'listing-id-from-above',
    'p_token', 'test-token-123'
  )
);
-- Should return success, email_verified should be true

-- Test: get_listing_with_images
SELECT supabase.rpc('get_listing_with_images',
  json_build_object('p_listing_id', 'listing-id')
);
-- Should return listing with images array

-- Test: update_listing_status
SELECT supabase.rpc('update_listing_status',
  json_build_object(
    'p_listing_id', 'listing-id',
    'p_new_status', 'REVIEWING'
  )
);
-- Should return success, status should be REVIEWING

-- Test: get_enquiries_for_product
SELECT supabase.rpc('get_enquiries_for_product',
  json_build_object('p_product_id', 'product-id')
);
-- Should return array of enquiries

-- Test: get_listing_stats
SELECT supabase.rpc('get_listing_stats');
-- Should return statistics
```

- [ ] get_product_with_enquiry_count works
- [ ] create_second_hand_listing works
- [ ] verify_second_hand_listing_email works
- [ ] get_listing_with_images works
- [ ] update_listing_status works
- [ ] get_enquiries_for_product works
- [ ] get_listing_stats works

**Checkpoint:** All functions callable and return expected data

---

## Phase 9: Sample Data (Optional) ✅

To help with testing, load sample data:

- [ ] Ran: `seed/sample_data.sql` (optional)
- [ ] Verify sample data loaded:
  ```sql
  SELECT COUNT(*) FROM products;
  SELECT COUNT(*) FROM services;
  SELECT COUNT(*) FROM store_information;
  ```

**Checkpoint:** Sample data loaded if desired (optional)

---

## Phase 10: Security Verification ✅

### Verify Security Setup

- [ ] Anon Key is NOT in .env or git repository
- [ ] Service Role Key is NOT in .env or git repository
- [ ] Service Role Key is saved securely (password manager)
- [ ] Admin password saved securely (password manager)
- [ ] Admin user metadata includes `"role": "admin"`
- [ ] RLS policies prevent public access to second-hand data
- [ ] Storage policies prevent public upload to second-hand bucket
- [ ] Email verification required for second-hand listings
- [ ] No plain-text passwords in database

**Verification Checklist:**
```bash
# Check git history for leaked keys
git log -p | grep -i "supabase_key\|service_role"

# Check .env file (should not exist in repo)
git ls-files | grep -E "\.env|secrets|keys"

# Result should be: nothing found (good!)
```

- [ ] No credentials in git history
- [ ] No .env file committed
- [ ] No secrets in source code

**Checkpoint:** Security verified

---

## Phase 11: Configuration Documentation ✅

### Save for Backend Team

Create a secure document with:

- [ ] Project URL: `https://[project-id].supabase.co`
- [ ] Anon Key: `[public-key]` (safe to share with backend)
- [ ] Service Role Key: `[secret-key]` (keep secret, only backend .env)
- [ ] Admin Email: `[admin-email@example.com]`
- [ ] Admin Password: `[saved securely]`
- [ ] Database tables documented
- [ ] RLS policies documented
- [ ] Functions documented
- [ ] Storage buckets documented
- [ ] Example API calls provided

**Share with Backend Developer:**
- Project URL
- Anon Key (public)
- Admin email & temp password (via secure channel)
- This entire Backend Integration Guide
- Schema Documentation

**DO NOT SHARE:**
- Service Role Key (unless to trusted backend team member)
- Admin password plaintext (share via secure method only)

**Checkpoint:** Documentation prepared for handoff

---

## Phase 12: Final Validation ✅

### Run Complete Test Suite

```sql
-- Public user can read public data
SELECT COUNT(*) FROM products WHERE is_active = true;
SELECT COUNT(*) FROM services WHERE is_active = true;
SELECT * FROM store_information;

-- Public user cannot read private data
BEGIN
  SET ROLE anon;
  SELECT * FROM second_hand_listings;
  ROLLBACK;
END;
-- Should error: new row violates row-level security policy

-- Admin can read all data
BEGIN
  SET ROLE authenticated;
  SET app.current_user_id = 'admin-user-id';
  SELECT COUNT(*) FROM second_hand_listings;
  SELECT COUNT(*) FROM second_hand_images;
  ROLLBACK;
END;

-- Image upload workflow works
-- 1. Can insert product
INSERT INTO products (name, price, description) VALUES ('Test', 100, 'Test');
-- 2. Can insert second-hand listing
SELECT supabase.rpc('create_second_hand_listing', ...);
-- 3. Can insert images
INSERT INTO second_hand_images (listing_id, storage_path, image_order, is_thumbnail) VALUES (...);
-- 4. Can verify email
SELECT supabase.rpc('verify_second_hand_listing_email', ...);
-- 5. Can update status
SELECT supabase.rpc('update_listing_status', ...);

-- Email verification workflow works
-- 1. Seller submits listing (creates with token)
-- 2. Email sent with verification link
-- 3. Click link calls verify function
-- 4. Listing status updates to REVIEWING
```

- [ ] Public read works
- [ ] Private data blocked
- [ ] Admin full access
- [ ] Constraints enforced
- [ ] Triggers working
- [ ] Functions callable
- [ ] Email verification workflow complete

**Checkpoint:** Complete validation passed

---

## Phase 13: Backup & Documentation ✅

### Final Steps

- [ ] Database schema exported:
  ```bash
  # Schema file already in: database/migrations/
  # Backup available at: database/README.md
  ```

- [ ] Documentation complete:
  - [ ] [README.md](./README.md)
  - [ ] [SCHEMA_DOCUMENTATION.md](./SCHEMA_DOCUMENTATION.md)
  - [ ] [SUPABASE_SETUP.md](./SUPABASE_SETUP.md)
  - [ ] [BACKEND_INTEGRATION_GUIDE.md](./BACKEND_INTEGRATION_GUIDE.md)
  - [ ] [DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md)

- [ ] Backup files stored:
  - [ ] migrations/
  - [ ] functions/
  - [ ] seed/
  - [ ] All documentation

**Checkpoint:** Database ready for production

---

## ✅ DEPLOYMENT COMPLETE

The database is ready for backend development!

### Next Steps:

1. **Backend Team:** Review BACKEND_INTEGRATION_GUIDE.md
2. **Backend Team:** Implement API endpoints per specification
3. **Backend Team:** Test with actual Supabase credentials
4. **Backend Team:** Implement image compression and upload
5. **Backend Team:** Implement email verification workflow
6. **Testing Team:** Test all RLS policies
7. **Testing Team:** Test all constraints and validations
8. **QA Team:** Full integration testing with frontend
9. **DevOps:** Configure CI/CD pipeline
10. **Deployment:** Ready for staging environment

---

## Quick Reference

### File Locations

```
database/
├── README.md                         ← Overview
├── SCHEMA_DOCUMENTATION.md           ← Detailed schema
├── SUPABASE_SETUP.md                ← Setup instructions
├── BACKEND_INTEGRATION_GUIDE.md      ← For backend dev
├── DEPLOYMENT_CHECKLIST.md           ← This file
├── migrations/
│   ├── 001_initial_schema.sql       ← Tables & triggers
│   └── 002_rls_policies.sql         ← Security
├── functions/
│   └── backend_operations.sql       ← PostgreSQL functions
├── seed/
│   └── sample_data.sql              ← Test data
└── storage-config/
    └── storage_buckets.sql          ← Storage guide
```

### Emergency Contact

If issues arise during development:
1. Check the relevant documentation file
2. Review SUPABASE_SETUP.md for manual configuration
3. Review SCHEMA_DOCUMENTATION.md for table details
4. Check Supabase documentation: https://supabase.com/docs
5. Verify RLS policies are correctly configured

---

**Database Deployment Status: ✅ READY FOR BACKEND DEVELOPMENT**

*Last Updated: 2026-09-01*

