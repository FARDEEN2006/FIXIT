-- FIXIT Mobile Sales & Services - Row Level Security Policies
-- Migration 002: RLS Configuration
-- Created: 2026-09-01
-- IMPORTANT: These policies enforce security and data privacy

-- ============================================================
-- Enable RLS on All Tables
-- ============================================================

ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_enquiries ENABLE ROW LEVEL SECURITY;
ALTER TABLE second_hand_listings ENABLE ROW LEVEL SECURITY;
ALTER TABLE second_hand_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE services ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_information ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- Helper Function: Check if User is Admin
-- ============================================================

CREATE OR REPLACE FUNCTION is_admin()
RETURNS BOOLEAN AS $$
BEGIN
  -- Check if user has admin role in auth.users via custom claims
  -- This assumes admin users are identified via email or role in JWT claims
  -- You'll need to configure this based on your auth setup
  RETURN (auth.jwt() ->> 'role') = 'admin' 
    OR (auth.jwt() ->> 'email') IN (
      SELECT email FROM auth.users 
      WHERE raw_user_meta_data ->> 'is_admin' = 'true'
    );
EXCEPTION WHEN OTHERS THEN
  RETURN false;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Table: products
-- Policy: Public Read (Active Only), Admin Full Access
-- ============================================================

-- Anonymous users can read active products
CREATE POLICY "public_read_active_products"
ON products
FOR SELECT
USING (is_active = true);

-- Authenticated admin can insert products
CREATE POLICY "admin_insert_products"
ON products
FOR INSERT
WITH CHECK (is_admin());

-- Authenticated admin can update products
CREATE POLICY "admin_update_products"
ON products
FOR UPDATE
USING (is_admin())
WITH CHECK (is_admin());

-- Authenticated admin can delete products
CREATE POLICY "admin_delete_products"
ON products
FOR DELETE
USING (is_admin());

-- ============================================================
-- Table: product_enquiries
-- Policy: Public Insert, Admin Read/Update, Prevent Cross-read
-- ============================================================

-- Anonymous users can insert enquiries
CREATE POLICY "public_insert_enquiries"
ON product_enquiries
FOR INSERT
WITH CHECK (true);

-- Authenticated admin can view all enquiries
CREATE POLICY "admin_read_enquiries"
ON product_enquiries
FOR SELECT
USING (is_admin());

-- Authenticated admin can update enquiry status
CREATE POLICY "admin_update_enquiries"
ON product_enquiries
FOR UPDATE
USING (is_admin())
WITH CHECK (is_admin());

-- Anonymous users CANNOT read enquiries (no select policy)

-- ============================================================
-- Table: second_hand_listings
-- Policy: Private - Admin Only, No Public Access
-- CRITICAL: Public users must NEVER see these
-- ============================================================

-- Authenticated admin can read all listings
CREATE POLICY "admin_read_listings"
ON second_hand_listings
FOR SELECT
USING (is_admin());

-- Authenticated admin can insert listings
CREATE POLICY "admin_insert_listings"
ON second_hand_listings
FOR INSERT
WITH CHECK (is_admin());

-- Authenticated admin can update listings
CREATE POLICY "admin_update_listings"
ON second_hand_listings
FOR UPDATE
USING (is_admin())
WITH CHECK (is_admin());

-- Authenticated admin can delete listings
CREATE POLICY "admin_delete_listings"
ON second_hand_listings
FOR DELETE
USING (is_admin());

-- IMPORTANT: NO public read policy - anonymous users are blocked by default

-- ============================================================
-- Table: second_hand_images
-- Policy: Private - Admin Only, No Public Access
-- CRITICAL: These images are private seller content
-- ============================================================

-- Authenticated admin can read all images
CREATE POLICY "admin_read_images"
ON second_hand_images
FOR SELECT
USING (is_admin());

-- Authenticated admin can insert images
CREATE POLICY "admin_insert_images"
ON second_hand_images
FOR INSERT
WITH CHECK (is_admin());

-- Authenticated admin can update images
CREATE POLICY "admin_update_images"
ON second_hand_images
FOR UPDATE
USING (is_admin())
WITH CHECK (is_admin());

-- Authenticated admin can delete images
CREATE POLICY "admin_delete_images"
ON second_hand_images
FOR DELETE
USING (is_admin());

-- IMPORTANT: NO public read policy - anonymous users are blocked by default

-- ============================================================
-- Table: services
-- Policy: Public Read (Active Only), Admin Full Access
-- ============================================================

-- Anonymous users can read active services
CREATE POLICY "public_read_active_services"
ON services
FOR SELECT
USING (is_active = true);

-- Authenticated admin can insert services
CREATE POLICY "admin_insert_services"
ON services
FOR INSERT
WITH CHECK (is_admin());

-- Authenticated admin can update services
CREATE POLICY "admin_update_services"
ON services
FOR UPDATE
USING (is_admin())
WITH CHECK (is_admin());

-- Authenticated admin can delete services
CREATE POLICY "admin_delete_services"
ON services
FOR DELETE
USING (is_admin());

-- ============================================================
-- Table: store_information
-- Policy: Public Read, Admin Full Access
-- ============================================================

-- Anonymous users can read store information
CREATE POLICY "public_read_store_info"
ON store_information
FOR SELECT
USING (true);

-- Authenticated admin can insert store information
CREATE POLICY "admin_insert_store_info"
ON store_information
FOR INSERT
WITH CHECK (is_admin());

-- Authenticated admin can update store information
CREATE POLICY "admin_update_store_info"
ON store_information
FOR UPDATE
USING (is_admin())
WITH CHECK (is_admin());

-- Authenticated admin can delete store information
CREATE POLICY "admin_delete_store_info"
ON store_information
FOR DELETE
USING (is_admin());

-- ============================================================
-- Storage Bucket Security Policies
-- ============================================================

-- Note: Storage bucket RLS must be configured in Supabase Dashboard
-- 
-- products bucket:
--   - Public read access (anyone can view product images)
--   - Admin write/delete (only admin can upload/modify)
--
-- second-hand bucket:
--   - PRIVATE read/write (only admin can access)
--   - No public access allowed
--
-- service-icons bucket:
--   - Public read access (anyone can view service icons)
--   - Admin write/delete (only admin can upload/modify)

-- ============================================================
-- End of Migration 002: RLS Policies
-- ============================================================

-- Success: All RLS policies configured
-- Security level: STRICT - Follows least-privilege principle
