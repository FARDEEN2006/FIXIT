-- FIXIT Mobile Sales & Services - Database Functions
-- Migration 004: PostgreSQL Functions for Backend Operations
-- Created: 2026-09-01

-- ============================================================
-- Function: verify_second_hand_listing_email
-- Purpose: Mark a second-hand listing as email verified
-- ============================================================

CREATE OR REPLACE FUNCTION verify_second_hand_listing_email(
  p_listing_id UUID,
  p_token VARCHAR
)
RETURNS JSONB AS $$
DECLARE
  v_result JSONB;
  v_listing_count INTEGER;
BEGIN
  -- Check if listing exists and token matches
  SELECT COUNT(*) INTO v_listing_count
  FROM second_hand_listings
  WHERE id = p_listing_id 
    AND email_verification_token = p_token;

  IF v_listing_count = 0 THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Invalid listing ID or verification token'
    );
  END IF;

  -- Update listing to mark email as verified
  UPDATE second_hand_listings
  SET 
    email_verified = true,
    email_verified_at = CURRENT_TIMESTAMP,
    email_verification_token = NULL
  WHERE id = p_listing_id;

  RETURN jsonb_build_object(
    'success', true,
    'message', 'Email verified successfully',
    'listing_id', p_listing_id
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Function: create_second_hand_listing
-- Purpose: Create a new second-hand listing (for admin/backend)
-- ============================================================

CREATE OR REPLACE FUNCTION create_second_hand_listing(
  p_seller_name VARCHAR,
  p_seller_phone VARCHAR,
  p_seller_email VARCHAR,
  p_product_name VARCHAR,
  p_condition VARCHAR,
  p_detailed_description TEXT,
  p_expected_price DECIMAL,
  p_verification_token VARCHAR
)
RETURNS JSONB AS $$
DECLARE
  v_listing_id UUID;
BEGIN
  -- Validate inputs
  IF p_seller_email IS NULL OR p_seller_email = '' THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Seller email is required'
    );
  END IF;

  IF p_condition NOT IN ('NEW', 'GOOD', 'FAIR', 'POOR') THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Invalid condition value'
    );
  END IF;

  IF p_expected_price < 0 THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Price cannot be negative'
    );
  END IF;

  -- Create listing
  INSERT INTO second_hand_listings (
    seller_name,
    seller_phone,
    seller_email,
    product_name,
    condition,
    detailed_description,
    expected_price,
    email_verification_token,
    listing_status
  ) VALUES (
    p_seller_name,
    p_seller_phone,
    p_seller_email,
    p_product_name,
    p_condition,
    p_detailed_description,
    p_expected_price,
    p_verification_token,
    'NEW'
  )
  RETURNING id INTO v_listing_id;

  RETURN jsonb_build_object(
    'success', true,
    'message', 'Listing created successfully',
    'listing_id', v_listing_id,
    'email_verification_required', true
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Function: get_product_with_enquiry_count
-- Purpose: Get product details with enquiry statistics
-- ============================================================

CREATE OR REPLACE FUNCTION get_product_with_enquiry_count(
  p_product_id UUID
)
RETURNS JSONB AS $$
DECLARE
  v_product RECORD;
  v_enquiry_count INTEGER;
BEGIN
  SELECT p.*, COUNT(pe.id) as enquiry_count
  INTO v_product
  FROM products p
  LEFT JOIN product_enquiries pe ON p.id = pe.product_id
  WHERE p.id = p_product_id
  GROUP BY p.id;

  IF NOT FOUND THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Product not found'
    );
  END IF;

  RETURN jsonb_build_object(
    'success', true,
    'data', jsonb_build_object(
      'id', v_product.id,
      'name', v_product.name,
      'price', v_product.price,
      'description', v_product.description,
      'image_path', v_product.image_path,
      'is_active', v_product.is_active,
      'enquiry_count', v_product.enquiry_count,
      'created_at', v_product.created_at,
      'updated_at', v_product.updated_at
    )
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Function: get_listing_with_images
-- Purpose: Get second-hand listing with all images
-- ============================================================

CREATE OR REPLACE FUNCTION get_listing_with_images(
  p_listing_id UUID
)
RETURNS JSONB AS $$
DECLARE
  v_listing RECORD;
  v_images JSONB;
BEGIN
  SELECT * INTO v_listing
  FROM second_hand_listings
  WHERE id = p_listing_id;

  IF NOT FOUND THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Listing not found'
    );
  END IF;

  SELECT jsonb_agg(
    jsonb_build_object(
      'id', id,
      'storage_path', storage_path,
      'image_order', image_order,
      'is_thumbnail', is_thumbnail,
      'created_at', created_at
    ) ORDER BY image_order
  ) INTO v_images
  FROM second_hand_images
  WHERE listing_id = p_listing_id;

  RETURN jsonb_build_object(
    'success', true,
    'data', jsonb_build_object(
      'id', v_listing.id,
      'seller_name', v_listing.seller_name,
      'seller_phone', v_listing.seller_phone,
      'seller_email', v_listing.seller_email,
      'product_name', v_listing.product_name,
      'condition', v_listing.condition,
      'detailed_description', v_listing.detailed_description,
      'expected_price', v_listing.expected_price,
      'listing_status', v_listing.listing_status,
      'email_verified', v_listing.email_verified,
      'email_verified_at', v_listing.email_verified_at,
      'created_at', v_listing.created_at,
      'updated_at', v_listing.updated_at,
      'images', COALESCE(v_images, '[]'::jsonb)
    )
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Function: update_listing_status
-- Purpose: Update second-hand listing status
-- ============================================================

CREATE OR REPLACE FUNCTION update_listing_status(
  p_listing_id UUID,
  p_new_status VARCHAR
)
RETURNS JSONB AS $$
DECLARE
  v_valid_status BOOLEAN;
BEGIN
  -- Validate status
  IF p_new_status NOT IN ('NEW', 'REVIEWING', 'CONTACTED', 'ACCEPTED', 'REJECTED', 'COMPLETED') THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Invalid status value'
    );
  END IF;

  -- Update status
  UPDATE second_hand_listings
  SET listing_status = p_new_status
  WHERE id = p_listing_id;

  IF NOT FOUND THEN
    RETURN jsonb_build_object(
      'success', false,
      'message', 'Listing not found'
    );
  END IF;

  RETURN jsonb_build_object(
    'success', true,
    'message', 'Status updated successfully',
    'listing_id', p_listing_id,
    'new_status', p_new_status
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Function: get_enquiries_for_product
-- Purpose: Get all enquiries for a specific product
-- ============================================================

CREATE OR REPLACE FUNCTION get_enquiries_for_product(
  p_product_id UUID
)
RETURNS JSONB AS $$
DECLARE
  v_enquiries JSONB;
BEGIN
  SELECT jsonb_agg(
    jsonb_build_object(
      'id', id,
      'customer_name', customer_name,
      'customer_phone', customer_phone,
      'customer_email', customer_email,
      'enquiry_status', enquiry_status,
      'created_at', created_at
    ) ORDER BY created_at DESC
  ) INTO v_enquiries
  FROM product_enquiries
  WHERE product_id = p_product_id;

  RETURN jsonb_build_object(
    'success', true,
    'product_id', p_product_id,
    'enquiry_count', COALESCE(jsonb_array_length(v_enquiries), 0),
    'enquiries', COALESCE(v_enquiries, '[]'::jsonb)
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- Function: get_listing_stats
-- Purpose: Get statistics about second-hand listings
-- ============================================================

CREATE OR REPLACE FUNCTION get_listing_stats()
RETURNS JSONB AS $$
DECLARE
  v_total_listings INTEGER;
  v_verified_listings INTEGER;
  v_pending_verification INTEGER;
  v_new_listings INTEGER;
  v_completed_listings INTEGER;
BEGIN
  SELECT COUNT(*) INTO v_total_listings FROM second_hand_listings;
  SELECT COUNT(*) INTO v_verified_listings FROM second_hand_listings WHERE email_verified = true;
  SELECT COUNT(*) INTO v_pending_verification FROM second_hand_listings WHERE email_verified = false;
  SELECT COUNT(*) INTO v_new_listings FROM second_hand_listings WHERE listing_status = 'NEW';
  SELECT COUNT(*) INTO v_completed_listings FROM second_hand_listings WHERE listing_status = 'COMPLETED';

  RETURN jsonb_build_object(
    'total_listings', v_total_listings,
    'verified_listings', v_verified_listings,
    'pending_verification', v_pending_verification,
    'new_listings', v_new_listings,
    'completed_listings', v_completed_listings
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- End of Migration 004: Functions
-- ============================================================

-- Success: All functions created and ready for backend use
