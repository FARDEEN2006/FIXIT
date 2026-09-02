-- FIXIT Mobile Sales & Services - Seed Data
-- Migration 003: Sample Data for Development
-- Created: 2026-09-01
-- Note: Use this for development/testing only

-- ============================================================
-- Seed: Store Information
-- ============================================================

INSERT INTO store_information (
  business_name,
  phone_number,
  whatsapp_number,
  email,
  address,
  city,
  state,
  pincode,
  working_hours_monday_to_friday,
  working_hours_saturday,
  working_hours_sunday,
  about_content,
  map_lat,
  map_lon
) VALUES (
  'FIXIT Mobile Sales & Services',
  '+91-XXXXXXXXXX',
  '+91-XXXXXXXXXX',
  'contact@fixitmobile.com',
  '123 Main Street, Shopping Complex',
  'Delhi',
  'Delhi',
  '110001',
  '9:00 AM - 9:00 PM',
  '10:00 AM - 8:00 PM',
  '10:00 AM - 6:00 PM',
  'FIXIT is a trusted mobile repair and sales center with over 5 years of experience in providing quality service to our customers.',
  28.6139,
  77.2090
) ON CONFLICT DO NOTHING;

-- ============================================================
-- Seed: Services
-- ============================================================

INSERT INTO services (
  service_name,
  description,
  icon_path,
  is_active,
  display_order
) VALUES
  (
    'Screen Replacement',
    'Professional mobile screen repair and replacement with genuine parts',
    'service-icons/screen-replacement.png',
    true,
    1
  ),
  (
    'Battery Replacement',
    'Battery service for all major mobile brands',
    'service-icons/battery-replacement.png',
    true,
    2
  ),
  (
    'Water Damage Repair',
    'Expert water damage assessment and repair',
    'service-icons/water-damage.png',
    true,
    3
  ),
  (
    'Software Issues',
    'Fix software problems, OS updates, and installations',
    'service-icons/software-issues.png',
    true,
    4
  ),
  (
    'Charging Port Repair',
    'Charging port replacement and repair',
    'service-icons/charging-port.png',
    true,
    5
  )
ON CONFLICT DO NOTHING;

-- ============================================================
-- Seed: Products
-- ============================================================

INSERT INTO products (
  name,
  price,
  description,
  image_path,
  is_active
) VALUES
  (
    'Samsung Galaxy S21 Screen Replacement',
    8999,
    'Original Samsung Galaxy S21 AMOLED display replacement with full installation. Includes 6 months warranty on parts and labor.',
    'products/samsung-s21-screen.jpg',
    true
  ),
  (
    'iPhone 13 Battery Pack',
    4999,
    'Genuine Apple battery replacement for iPhone 13. Professional installation included. Restores battery health to 100%.',
    'products/iphone-13-battery.jpg',
    true
  ),
  (
    'Mobile Screen Protector Pack (5 pieces)',
    299,
    'Tempered glass screen protectors for most smartphones. Easy installation with alignment frame included.',
    'products/screen-protector-pack.jpg',
    true
  ),
  (
    'Mobile Phone Stand',
    199,
    'Adjustable mobile phone stand for desk and office use. Compatible with all smartphone sizes.',
    'products/phone-stand.jpg',
    true
  ),
  (
    'Micro USB Charging Cable',
    149,
    'High-quality micro USB charging cable. 1 meter length. Fast charging support.',
    'products/micro-usb-cable.jpg',
    true
  )
ON CONFLICT DO NOTHING;

-- ============================================================
-- Seed: Product Enquiries (Sample)
-- ============================================================

-- Note: These are sample enquiries for testing
-- In production, these will be created by customers via the frontend

INSERT INTO product_enquiries (
  product_id,
  customer_name,
  customer_phone,
  customer_email,
  enquiry_status
) SELECT 
  id,
  'John Doe',
  '+91-9876543210',
  'john@example.com',
  'PENDING'
FROM products
LIMIT 1
ON CONFLICT DO NOTHING;

-- ============================================================
-- Seed: Second-Hand Listings (Sample - for admin review)
-- ============================================================

-- Note: These are example listings in NEW status
-- Email verification must happen before these are reviewed

INSERT INTO second_hand_listings (
  seller_name,
  seller_phone,
  seller_email,
  product_name,
  condition,
  detailed_description,
  expected_price,
  listing_status,
  email_verified
) VALUES
  (
    'Rajesh Kumar',
    '+91-9876543210',
    'rajesh@example.com',
    'iPhone 12 (64GB)',
    'GOOD',
    'Used iPhone 12 in excellent condition. No scratches on screen. Original charger included. Battery health around 85%. No service history needed.',
    28000,
    'NEW',
    false
  )
ON CONFLICT DO NOTHING;

-- ============================================================
-- Notes on Seed Data
-- ============================================================

-- 1. Store Information:
--    - Only ONE store record should exist
--    - Update phone numbers and email with actual values
--    - Update map coordinates with actual location
--
-- 2. Services:
--    - Can have multiple services
--    - display_order determines home page sequence
--    - icon_path references need actual files uploaded
--
-- 3. Products:
--    - All visible to public when is_active=true
--    - image_path must reference actual uploaded images
--    - Prices are stored in rupees (DECIMAL 10,2)
--
-- 4. Product Enquiries:
--    - Created automatically when customer submits form
--    - No need for seed data in production
--
-- 5. Second-Hand Listings:
--    - Private listings not visible to public
--    - Must be verified by owner before accepting
--    - Not automatically displayed in marketplace
--
-- 6. Second-Hand Images:
--    - Must be inserted with listing_id
--    - Maximum 4 images per listing
--    - Separate thumbnail flag identifies preview image

-- ============================================================
-- End of Seed Data
-- ============================================================
