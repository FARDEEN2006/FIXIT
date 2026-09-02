-- FIXIT Mobile Sales & Services - Supabase Setup Guide
-- Complete Manual Configuration Instructions
-- Created: 2026-09-01

-- ============================================================
-- IMPORTANT: THIS IS NOT SQL CODE
-- This file explains the manual steps you must perform in the
-- Supabase dashboard AFTER running the SQL migrations
-- ============================================================

-- ============================================================
-- STEP 1: Create Supabase Project
-- ============================================================

MANUAL SETUP REQUIRED:

1. Go to https://supabase.com
2. Sign in or create account
3. Click "New Project"
4. Fill in project details:
   - Name: fixit-mobile-sales-services (or your choice)
   - Database Password: Create strong password (save securely!)
   - Region: Choose closest to your users (e.g., India for Indian users)
5. Click "Create new project"
6. Wait for project initialization (2-3 minutes)

WHAT TO SAVE:
  ✓ Project URL
  ✓ Anon/Public Key
  ✓ Service Role Key (NEVER share publicly!)
  ✓ Database password
  ✓ Project ID

Store these in a secure location (password manager, environment file - not git!)

-- ============================================================
-- STEP 2: Run SQL Migrations
-- ============================================================

MANUAL SETUP REQUIRED:

1. In Supabase Dashboard, go to: SQL Editor
2. Create new query
3. Copy contents of: database/migrations/001_initial_schema.sql
4. Paste into SQL editor
5. Click "Run" button
6. Wait for success message

Repeat for:
  - database/migrations/002_rls_policies.sql
  - database/functions/backend_operations.sql

OPTIONAL (Development/Testing):
  - database/seed/sample_data.sql (for test data)

EXPECTED OUTPUT:
  ✓ 6 tables created (products, product_enquiries, etc.)
  ✓ 4 update triggers created
  ✓ 2 constraint check functions created
  ✓ Multiple RLS policies created
  ✓ 6 backend operation functions created

If you see errors:
  - Read error message carefully
  - Check SQL syntax
  - Verify no duplicate table names exist
  - Try running again with "DROP TABLE IF EXISTS" to clear state

-- ============================================================
-- STEP 3: Create Storage Buckets
-- ============================================================

MANUAL SETUP REQUIRED:

Storage > Buckets > Create New Bucket

BUCKET 1: products
  - Name: products
  - Visibility: PUBLIC
  - Click "Create Bucket"

BUCKET 2: second-hand
  - Name: second-hand
  - Visibility: PRIVATE
  - Click "Create Bucket"

BUCKET 3: service-icons
  - Name: service-icons
  - Visibility: PUBLIC
  - Click "Create Bucket"

IMPORTANT:
  ✓ Note the exact bucket names
  ✓ Products bucket = PUBLIC (anyone can view product images)
  ✓ Second-hand bucket = PRIVATE (only admin can access)
  ✓ Service-icons bucket = PUBLIC

-- ============================================================
-- STEP 4: Configure Storage Bucket Policies (RLS)
-- ============================================================

MANUAL SETUP REQUIRED:

Go to: Storage > Policies

For PRODUCTS bucket:

  Policy 1 - Allow Public Read:
    - Click "New Policy"
    - Operation: SELECT
    - Check the "PUBLIC" role checkbox (for anonymous users)
    - Leave "with_check" blank
    - Click "Save"

  Policy 2 - Allow Admin Write:
    - Click "New Policy"
    - Operation: INSERT
    - Check the "Authenticated" role checkbox
    - Set "with_check" to something like:
      (auth.jwt() ->> 'role' = 'admin')
    - Click "Save"

For SECOND-HAND bucket:

  Policy 1 - Allow Admin Read:
    - Click "New Policy"
    - Operation: SELECT
    - Check the "Authenticated" role checkbox
    - Set "using" to:
      (auth.jwt() ->> 'role' = 'admin')
    - Click "Save"

  Policy 2 - Allow Admin Write:
    - Click "New Policy"
    - Operation: INSERT
    - Check the "Authenticated" role checkbox
    - Set "with_check" to:
      (auth.jwt() ->> 'role' = 'admin')
    - Click "Save"

  Policy 3 - Allow Admin Delete:
    - Click "New Policy"
    - Operation: DELETE
    - Check the "Authenticated" role checkbox
    - Set "using" to:
      (auth.jwt() ->> 'role' = 'admin')
    - Click "Save"

For SERVICE-ICONS bucket:

  Policy 1 - Allow Public Read:
    - Click "New Policy"
    - Operation: SELECT
    - Check the "PUBLIC" role checkbox
    - Click "Save"

  Policy 2 - Allow Admin Write:
    - Click "New Policy"
    - Operation: INSERT
    - Check the "Authenticated" role checkbox
    - Set "with_check" to:
      (auth.jwt() ->> 'role' = 'admin')
    - Click "Save"

-- ============================================================
-- STEP 5: Configure Authentication
-- ============================================================

MANUAL SETUP REQUIRED:

1. Go to: Authentication > Providers
2. Email/Password: Enabled by default
3. Go to: Authentication > Settings
4. Ensure "Disable email signup" is OFF (allow public signup)
5. Set custom SMTP if needed (optional, for production)

-- ============================================================
-- STEP 6: Create Admin Users
-- ============================================================

MANUAL SETUP REQUIRED:

1. Go to: Authentication > Users
2. Click "Add User"
3. Fill in:
   - Email: your-admin-email@example.com
   - Password: Create strong password
4. Click "Create User"

IMPORTANT:
  - You need AT LEAST one admin user for the dashboard
  - This user must have admin access configured (next step)

-- ============================================================
-- STEP 7: Configure Admin Role
-- ============================================================

MANUAL SETUP REQUIRED:

Admin identification can be done via:

Option A: Custom Claims in JWT (Recommended)
  1. Go to: Authentication > Users
  2. Click on admin user
  3. Click "User Metadata" tab
  4. Add JSON:
     {
       "is_admin": true,
       "role": "admin"
     }
  5. Save

Option B: Create Auth Table (Alternative)
  1. Go to: SQL Editor
  2. Run this:

     CREATE TABLE admin_users (
       id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
       email VARCHAR(255) NOT NULL UNIQUE,
       created_at TIMESTAMP DEFAULT NOW()
     );

  3. Add your admin user:

     INSERT INTO admin_users (id, email)
     SELECT id, email FROM auth.users WHERE email = 'admin@example.com';

TESTING:
  - Backend should check auth.jwt() ->> 'role' = 'admin'
  - Or query admin_users table if using Option B

-- ============================================================
-- STEP 8: Enable Row Level Security
-- ============================================================

MANUAL SETUP REQUIRED:

Verify RLS is enabled:
  1. Go to: Database > Roles
  2. Check that "Enable Row Level Security" is ON for:
     - postgres (admin role)
     - anon (public/anonymous role)
     - authenticated (logged-in users)

If policies aren't working:
  1. Go to: Database > Roles
  2. Select each role
  3. Ensure "Use row level security" is toggled ON
  4. Review policies under: Database > Policies

-- ============================================================
-- STEP 9: Configure Email Verification (For Second-Hand Listings)
-- ============================================================

MANUAL SETUP REQUIRED:

For email verification of second-hand sellers:

1. Go to: Authentication > Email Templates
2. Select "Confirm Email" template
3. Customize the email with FIXIT branding
4. Include this verification link pattern:
   https://yourfrontend.com/verify-email?token={{token}}&listing={{listing_id}}

5. Backend will:
   - Generate verification token
   - Send email via Supabase auth (or custom SMTP)
   - User clicks link
   - Frontend calls verify_second_hand_listing_email() function
   - Listing status updates

IMPORTANT:
  - Only authenticated emails can submit second-hand listings
  - Email must be verified before listing is accepted
  - Use Supabase Auth email templates for consistency

-- ============================================================
-- STEP 10: Verify Database Setup
-- ============================================================

MANUAL SETUP REQUIRED:

In Supabase Dashboard:

1. Go to: SQL Editor
2. Run verification queries:

   -- Check tables exist
   SELECT table_name 
   FROM information_schema.tables 
   WHERE table_schema = 'public';

   -- Check RLS policies
   SELECT schemaname, tablename, policyname 
   FROM pg_policies;

   -- Test public read
   SELECT * FROM products WHERE is_active = true;

   -- Test functions exist
   SELECT proname FROM pg_proc 
   WHERE proname LIKE '%second_hand%';

3. Expected output:
   ✓ 6 tables listed
   ✓ Multiple policies shown
   ✓ Products displayed (if seed data run)
   ✓ 6+ functions with 'second_hand' in name

-- ============================================================
-- STEP 11: Note Down Configuration Values
-- ============================================================

IMPORTANT - Save These Values for Backend:

Project Configuration:
  ✓ Project URL: https://[project-id].supabase.co
  ✓ Anon Key: [public-key]
  ✓ Service Role Key: [secret-key] ⚠️ NEVER COMMIT TO GIT

Storage Buckets:
  ✓ products bucket name: products
  ✓ second-hand bucket name: second-hand
  ✓ service-icons bucket name: service-icons

Database:
  ✓ Tables: products, product_enquiries, second_hand_listings, etc.
  ✓ RLS: Enabled on all tables
  ✓ Policies: Configured per table

Authentication:
  ✓ Admin email: your-admin-email@example.com
  ✓ Admin can access: all tables, all buckets
  ✓ Public can access: products, services, store_information
  ✓ Public cannot access: second_hand_listings, product_enquiries

Backend Environment Variables Needed:
  SUPABASE_URL=https://[project-id].supabase.co
  SUPABASE_ANON_KEY=[public-key]
  SUPABASE_SERVICE_KEY=[secret-key]
  ADMIN_EMAIL=[your-admin-email]

-- ============================================================
-- STEP 12: Quick Testing Checklist
-- ============================================================

Before proceeding to backend development:

□ Supabase project created
□ All 4 migrations run successfully
□ 6 tables created (verify in Tables list)
□ 3 storage buckets created
□ Storage RLS policies configured
□ Admin user created with admin metadata
□ Sample data loaded (if desired)
□ Test public read:
  → Login as anonymous
  → Should see active products
  → Should NOT see second_hand_listings
□ Test admin access:
  → Login with admin account
  → Should see all tables
  → Should be able to create/update products
□ Test RLS policies:
  → Query tables as anon user (should respect RLS)
  → Query tables as admin user (should see all)
□ Functions created and callable:
  → verify_second_hand_listing_email()
  → create_second_hand_listing()
  → get_product_with_enquiry_count()
  → get_listing_with_images()
  → Others...

If any step fails:
  - Review error messages in Supabase Dashboard
  - Check SQL syntax in migrations
  - Verify RLS policies are correctly scoped
  - Ensure storage buckets have correct visibility setting

-- ============================================================
-- ADDITIONAL NOTES
-- ============================================================

1. Email Verification for Second-Hand:
   - Requires backend to generate verification tokens
   - Backend sends email with verification link
   - User clicks link → Frontend calls verify function
   - Only then can listing be finalized

2. Image Upload Workflow:
   - Frontend sends image to backend
   - Backend compresses using Sharp/PIL/ImageMagick
   - Backend uploads compressed image to Storage bucket
   - Backend records storage_path in PostgreSQL
   - Database stores REFERENCE, not binary data
   - Frontend displays image by loading from Storage bucket URL

3. Admin Dashboard:
   - Backend should check admin role from JWT claims
   - Use Supabase Auth for login
   - Admin can:
     → Create/edit/delete products
     → View all product enquiries
     → View/manage second-hand listings
     → View seller contact info (private data)
     → Manage store information

4. Public Frontend:
   - Can view active products
   - Can view services
   - Can view store information
   - Can submit product enquiries (no auth needed)
   - Can submit second-hand listings (requires email verification)
   - Cannot view other customers' enquiries
   - Cannot view second-hand listings

5. Security Reminders:
   ✓ Never commit .env files with keys to git
   ✓ Always use Service Role key server-side only
   ✓ Use Anon key in frontend (public)
   ✓ All sensitive data protected by RLS
   ✓ Second-hand bucket is PRIVATE
   ✓ Product images are PUBLIC
   ✓ Email verification is REQUIRED before listing acceptance

-- ============================================================
-- NEXT STEPS
-- ============================================================

After completing manual Supabase setup:

1. STOP - Do not start backend development yet
2. Wait for backend developer confirmation
3. Share this information with backend team:
   - Project URL
   - Anon Key (safe to share)
   - Service Role Key (keep SECRET)
   - Admin email / password (share securely)
   - Table structure overview
   - RLS policy documentation
   - Function documentation
   - Storage bucket setup

4. Backend developer will:
   - Set up Supabase client (SDK)
   - Implement API endpoints for:
     → Product CRUD
     → Product enquiry submission
     → Second-hand listing submission
     → Second-hand listing email verification
     → Image upload with compression
   - Implement authentication logic
   - Set up error handling for RLS violations

-- ============================================================
-- End of Supabase Setup Guide
-- ============================================================

Good luck with your FIXIT project! 🚀
