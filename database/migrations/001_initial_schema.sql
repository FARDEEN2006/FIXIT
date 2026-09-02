-- FIXIT Mobile Sales & Services Database Schema
-- Migration 001: Initial Schema Setup
-- Created: 2026-09-01

-- ============================================================
-- Enable UUID Extension
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- Table: products
-- Purpose: Main product catalog for FIXIT services
-- ============================================================

CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(255) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  description TEXT NOT NULL,
  image_path VARCHAR(500),  -- Storage path reference only
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  -- Constraints
  CONSTRAINT price_non_negative CHECK (price >= 0)
);

-- Indexes for products
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_created_at ON products(created_at);

-- ============================================================
-- Table: product_enquiries
-- Purpose: Customer enquiries for products (lead capture)
-- ============================================================

CREATE TABLE IF NOT EXISTS product_enquiries (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  customer_name VARCHAR(255) NOT NULL,
  customer_phone VARCHAR(20) NOT NULL,
  customer_email VARCHAR(255) NOT NULL,
  enquiry_status VARCHAR(50) DEFAULT 'PENDING',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  -- Constraints
  CONSTRAINT valid_status CHECK (
    enquiry_status IN ('PENDING', 'CONTACTED', 'CONVERTED', 'REJECTED')
  )
);

-- Indexes for product_enquiries
CREATE INDEX idx_product_enquiries_product_id ON product_enquiries(product_id);
CREATE INDEX idx_product_enquiries_created_at ON product_enquiries(created_at);
CREATE INDEX idx_product_enquiries_status ON product_enquiries(enquiry_status);

-- ============================================================
-- Table: second_hand_listings
-- Purpose: Private user-submitted listings for used products
-- IMPORTANT: These must NEVER be publicly displayed
-- ============================================================

CREATE TABLE IF NOT EXISTS second_hand_listings (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  seller_name VARCHAR(255) NOT NULL,
  seller_phone VARCHAR(20) NOT NULL,
  seller_email VARCHAR(255) NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  condition VARCHAR(50) NOT NULL,
  detailed_description TEXT NOT NULL,
  expected_price DECIMAL(10, 2) NOT NULL,
  listing_status VARCHAR(50) DEFAULT 'NEW',
  email_verified BOOLEAN DEFAULT false NOT NULL,
  email_verification_token VARCHAR(500),
  email_verified_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  -- Constraints
  CONSTRAINT valid_condition CHECK (
    condition IN ('NEW', 'GOOD', 'FAIR', 'POOR')
  ),
  CONSTRAINT valid_listing_status CHECK (
    listing_status IN ('NEW', 'REVIEWING', 'CONTACTED', 'ACCEPTED', 'REJECTED', 'COMPLETED')
  ),
  CONSTRAINT price_non_negative CHECK (expected_price >= 0)
);

-- Indexes for second_hand_listings
CREATE INDEX idx_second_hand_listings_seller_email ON second_hand_listings(seller_email);
CREATE INDEX idx_second_hand_listings_status ON second_hand_listings(listing_status);
CREATE INDEX idx_second_hand_listings_email_verified ON second_hand_listings(email_verified);
CREATE INDEX idx_second_hand_listings_created_at ON second_hand_listings(created_at);

-- ============================================================
-- Table: second_hand_images
-- Purpose: Images for second-hand listings (up to 4 per listing)
-- IMPORTANT: Private access only
-- ============================================================

CREATE TABLE IF NOT EXISTS second_hand_images (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  listing_id UUID NOT NULL REFERENCES second_hand_listings(id) ON DELETE CASCADE,
  storage_path VARCHAR(500) NOT NULL,
  image_order INTEGER NOT NULL,
  is_thumbnail BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  -- Constraints
  CONSTRAINT valid_image_order CHECK (image_order BETWEEN 1 AND 4),
  UNIQUE (listing_id, image_order)
);

-- Indexes for second_hand_images
CREATE INDEX idx_second_hand_images_listing_id ON second_hand_images(listing_id);
CREATE INDEX idx_second_hand_images_is_thumbnail ON second_hand_images(is_thumbnail);

-- ============================================================
-- Table: services
-- Purpose: FIXIT services offered
-- ============================================================

CREATE TABLE IF NOT EXISTS services (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  service_name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  icon_path VARCHAR(500),  -- Storage path reference only
  is_active BOOLEAN DEFAULT true,
  display_order INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for services
CREATE INDEX idx_services_is_active ON services(is_active);
CREATE INDEX idx_services_display_order ON services(display_order);

-- ============================================================
-- Table: store_information
-- Purpose: Business contact and information
-- ============================================================

CREATE TABLE IF NOT EXISTS store_information (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  business_name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  whatsapp_number VARCHAR(20) NOT NULL,
  email VARCHAR(255) NOT NULL,
  address VARCHAR(500) NOT NULL,
  city VARCHAR(100) NOT NULL,
  state VARCHAR(100) NOT NULL,
  pincode VARCHAR(10) NOT NULL,
  working_hours_monday_to_friday VARCHAR(255),
  working_hours_saturday VARCHAR(255),
  working_hours_sunday VARCHAR(255),
  about_content TEXT,
  map_lat DECIMAL(10, 8),
  map_lon DECIMAL(11, 8),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Update Trigger for updated_at Columns
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to products
CREATE TRIGGER update_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to second_hand_listings
CREATE TRIGGER update_second_hand_listings_updated_at
BEFORE UPDATE ON second_hand_listings
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to services
CREATE TRIGGER update_services_updated_at
BEFORE UPDATE ON services
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to store_information
CREATE TRIGGER update_store_information_updated_at
BEFORE UPDATE ON store_information
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- Constraint to Limit Images per Listing
-- ============================================================

CREATE OR REPLACE FUNCTION check_image_count()
RETURNS TRIGGER AS $$
BEGIN
  IF (
    SELECT COUNT(*) FROM second_hand_images 
    WHERE listing_id = NEW.listing_id
  ) >= 4 THEN
    RAISE EXCEPTION 'Cannot add more than 4 images per listing';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_second_hand_images_limit
BEFORE INSERT ON second_hand_images
FOR EACH ROW
EXECUTE FUNCTION check_image_count();

-- ============================================================
-- Constraint to Ensure Only One Thumbnail per Listing
-- ============================================================

CREATE OR REPLACE FUNCTION check_single_thumbnail()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.is_thumbnail THEN
    UPDATE second_hand_images 
    SET is_thumbnail = false 
    WHERE listing_id = NEW.listing_id AND id != NEW.id;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ensure_single_thumbnail
AFTER INSERT OR UPDATE ON second_hand_images
FOR EACH ROW
EXECUTE FUNCTION check_single_thumbnail();

-- ============================================================
-- End of Migration 001
-- ============================================================

-- Success: All tables created with proper constraints and triggers
