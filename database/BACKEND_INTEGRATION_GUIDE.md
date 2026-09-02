# Backend Integration Guide - FIXIT Database

**For Backend Developers:** Quick reference for integrating the FIXIT database with backend APIs.

---

## Quick Start

1. **Database Setup:** All tables, RLS policies, and functions are in `database/migrations/`
2. **Configuration:** Save your Supabase credentials (see step 11 in SUPABASE_SETUP.md)
3. **Client Library:** Use Supabase JavaScript, Python, or Go SDK
4. **Authentication:** Use Supabase Auth for admin login
5. **API Endpoints:** Implement REST/GraphQL endpoints per requirements below

---

## Environment Variables Needed

```env
SUPABASE_URL=https://[project-id].supabase.co
SUPABASE_ANON_KEY=[public-key]
SUPABASE_SERVICE_KEY=[secret-key]  # NEVER expose publicly!
ADMIN_EMAIL=your-admin@example.com
```

---

## Authentication Setup

### Admin Identification

The database checks for admin status using custom JWT claims:

```javascript
// In your auth middleware
const user = await supabase.auth.getSession();
const isAdmin = user?.user?.user_metadata?.role === 'admin';
```

Alternative: Use auth table if configured
```sql
SELECT * FROM admin_users WHERE id = auth.uid();
```

### Login Endpoint

```javascript
const { data, error } = await supabase.auth.signInWithPassword({
  email: email,
  password: password
});
```

### Verify Admin Role

```javascript
function ensureAdmin(req, res, next) {
  const token = req.headers.authorization?.split(' ')[1];
  const decoded = jwt_decode(token);
  
  if (decoded.role !== 'admin') {
    return res.status(403).json({ error: 'Admin access required' });
  }
  next();
}
```

---

## API Endpoints Overview

### Public Endpoints (No Auth Required)

#### GET /api/products
**Purpose:** Get all active products
```javascript
const { data, error } = await supabase
  .from('products')
  .select('*')
  .eq('is_active', true);
```

#### GET /api/products/:id
**Purpose:** Get single product with enquiry count
```javascript
const { data, error } = await supabase
  .rpc('get_product_with_enquiry_count', { p_product_id: productId });
```

#### GET /api/services
**Purpose:** Get all active services
```javascript
const { data, error } = await supabase
  .from('services')
  .select('*')
  .eq('is_active', true)
  .order('display_order', { ascending: true });
```

#### GET /api/store-info
**Purpose:** Get store information
```javascript
const { data, error } = await supabase
  .from('store_information')
  .select('*')
  .single();  // Only one row
```

#### POST /api/enquiries
**Purpose:** Submit product enquiry (public can submit)
```javascript
const { data, error } = await supabase
  .from('product_enquiries')
  .insert([
    {
      product_id: productId,
      customer_name: name,
      customer_phone: phone,
      customer_email: email,
      enquiry_status: 'PENDING'
    }
  ]);
```

#### POST /api/second-hand/listings
**Purpose:** Submit second-hand listing (requires email verification after)
```javascript
// Step 1: Call database function
const { data, error } = await supabase
  .rpc('create_second_hand_listing', {
    p_seller_name: name,
    p_seller_phone: phone,
    p_seller_email: email,
    p_product_name: productName,
    p_condition: condition,
    p_detailed_description: description,
    p_expected_price: price,
    p_verification_token: generateToken()  // Backend generates this
  });

// Step 2: Send verification email with token
await sendVerificationEmail(email, data.listing_id, token);
```

#### POST /api/second-hand/verify-email
**Purpose:** Verify second-hand seller email
```javascript
const { data, error } = await supabase
  .rpc('verify_second_hand_listing_email', {
    p_listing_id: listingId,
    p_token: verificationToken
  });
```

---

### Admin Endpoints (Auth Required)

#### POST /api/products (Admin)
**Purpose:** Create new product
```javascript
const { data, error } = await supabase
  .from('products')
  .insert([
    {
      name: productName,
      price: price,
      description: description,
      image_path: imagePath,  // Set after uploading to storage
      is_active: true
    }
  ])
  .select();
```

#### PUT /api/products/:id (Admin)
**Purpose:** Update product
```javascript
const { data, error } = await supabase
  .from('products')
  .update({
    name: newName,
    price: newPrice,
    description: newDescription,
    is_active: isActive
  })
  .eq('id', productId)
  .select();
```

#### DELETE /api/products/:id (Admin)
**Purpose:** Delete product
```javascript
const { data, error } = await supabase
  .from('products')
  .delete()
  .eq('id', productId);
```

#### GET /api/admin/enquiries (Admin)
**Purpose:** Get all enquiries
```javascript
const { data, error } = await supabase
  .from('product_enquiries')
  .select('*, products(name, price)')
  .order('created_at', { ascending: false });
```

#### GET /api/admin/enquiries/:product-id (Admin)
**Purpose:** Get enquiries for specific product
```javascript
const { data, error } = await supabase
  .rpc('get_enquiries_for_product', { p_product_id: productId });
```

#### PUT /api/admin/enquiries/:id (Admin)
**Purpose:** Update enquiry status
```javascript
const { data, error } = await supabase
  .from('product_enquiries')
  .update({ enquiry_status: newStatus })
  .eq('id', enquiryId);
```

#### GET /api/admin/second-hand (Admin)
**Purpose:** Get all second-hand listings
```javascript
const { data, error } = await supabase
  .from('second_hand_listings')
  .select('*')
  .order('created_at', { ascending: false });
```

#### GET /api/admin/second-hand/:id (Admin)
**Purpose:** Get listing with all images
```javascript
const { data, error } = await supabase
  .rpc('get_listing_with_images', { p_listing_id: listingId });
```

#### PUT /api/admin/second-hand/:id/status (Admin)
**Purpose:** Update listing status
```javascript
const { data, error } = await supabase
  .rpc('update_listing_status', {
    p_listing_id: listingId,
    p_new_status: newStatus
  });
```

#### GET /api/admin/second-hand/stats (Admin)
**Purpose:** Get listing statistics
```javascript
const { data, error } = await supabase
  .rpc('get_listing_stats');
```

---

## Image Upload Workflow

### Important: Image Compression Required

**DO NOT** upload raw images. Always compress first!

#### Step 1: Compress Image (Backend)
```javascript
// Using Sharp (Node.js)
const sharp = require('sharp');

const compressed = await sharp(buffer)
  .resize(1200, 1200, {
    fit: 'inside',
    withoutEnlargement: true
  })
  .jpeg({ quality: 80 })
  .toBuffer();
```

#### Step 2: Upload to Storage
```javascript
const filename = `${Date.now()}-${originalName}`;

// For products
const { data, error } = await supabase.storage
  .from('products')
  .upload(`${productId}/${filename}`, compressed, {
    contentType: 'image/jpeg',
    upsert: false
  });

// For second-hand
const { data, error } = await supabase.storage
  .from('second-hand')
  .upload(`${listingId}/1.jpg`, compressed, {
    contentType: 'image/jpeg'
  });
```

#### Step 3: Save Storage Path in Database
```javascript
// For products
await supabase
  .from('products')
  .update({ image_path: data.path })
  .eq('id', productId);

// For second-hand images
await supabase
  .from('second_hand_images')
  .insert({
    listing_id: listingId,
    storage_path: data.path,
    image_order: 1,
    is_thumbnail: true
  });
```

#### Step 4: Generate Public URL (for public buckets)
```javascript
const { data } = supabase.storage
  .from('products')
  .getPublicUrl(storagePath);

const publicUrl = data.publicUrl;
// https://[project-url]/storage/v1/object/public/products/[path]
```

---

## Error Handling for RLS Violations

When RLS blocks access, you get 403 Forbidden:

```javascript
try {
  const { data, error } = await supabase
    .from('second_hand_listings')
    .select('*');
    
  if (error?.code === 'PGRST116') {
    // RLS policy violation - user not authorized
    console.error('Access denied - admin role required');
  }
} catch (err) {
  console.error('Database error:', err);
}
```

---

## Key Database Functions

### 1. Create Second-Hand Listing
```javascript
const result = await supabase.rpc('create_second_hand_listing', {
  p_seller_name: 'John Doe',
  p_seller_phone: '+91-9876543210',
  p_seller_email: 'john@example.com',
  p_product_name: 'iPhone 12',
  p_condition: 'GOOD',
  p_detailed_description: 'Excellent condition...',
  p_expected_price: 28000,
  p_verification_token: crypto.randomUUID()
});
```

### 2. Verify Email
```javascript
const result = await supabase.rpc('verify_second_hand_listing_email', {
  p_listing_id: listingId,
  p_token: tokenFromEmail
});
```

### 3. Get Product Stats
```javascript
const result = await supabase.rpc('get_product_with_enquiry_count', {
  p_product_id: productId
});
```

### 4. Get Complete Listing
```javascript
const result = await supabase.rpc('get_listing_with_images', {
  p_listing_id: listingId
});
```

---

## Email Verification Workflow (Second-Hand)

### Backend Implementation

**Step 1: Generate Verification Token**
```javascript
const token = crypto.randomBytes(32).toString('hex');
const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 hours
```

**Step 2: Save Token in Database**
```javascript
const listing = await supabase.rpc('create_second_hand_listing', {
  // ... other fields
  p_verification_token: token
});
```

**Step 3: Send Verification Email**
```javascript
const verificationLink = `https://yourfrontend.com/verify-email?token=${token}&listing=${listing.id}`;

await sendEmail({
  to: sellerEmail,
  subject: 'Verify Your FIXIT Listing',
  html: `
    <p>Click here to verify your listing:</p>
    <a href="${verificationLink}">Verify Email</a>
    <p>This link expires in 24 hours.</p>
  `
});
```

**Step 4: Frontend Calls Verification**
```javascript
// Frontend receives token and listing_id from email link
const result = await supabase.rpc('verify_second_hand_listing_email', {
  p_listing_id: listingId,
  p_token: token
});

if (result.data.success) {
  // Show success message
  // Listing now verified and can be reviewed by admin
}
```

---

## Listing Status Workflow

```
┌─────────────────────────────────────────────────────┐
│         SECOND-HAND LISTING STATUS FLOW             │
└─────────────────────────────────────────────────────┘

1. NEW (Seller submits)
   └─→ Awaiting email verification
   
2. REVIEWING (After email verified)
   └─→ Admin reviews submission
   
3. CONTACTED (Admin reaches out)
   └─→ Admin has contacted seller
   
4. ACCEPTED (Admin accepts listing)
   └─→ Ready for potential buyers
   
5. COMPLETED (Transaction done)
   └─→ Listing fulfilled
   
   OR
   
6. REJECTED (Admin rejects)
   └─→ Does not meet criteria
```

**Update Status:**
```javascript
await supabase.rpc('update_listing_status', {
  p_listing_id: listingId,
  p_new_status: 'REVIEWING'  // or any valid status
});
```

---

## Security Checklist for Backend

- [ ] **Never expose Service Role Key** - Only use server-side
- [ ] **Use Anon Key in frontend** - Safe for public use
- [ ] **Check RLS policies** - Automatically enforce access control
- [ ] **Verify admin role** - Check JWT claims before admin operations
- [ ] **Compress images** - Before uploading to storage
- [ ] **Validate email** - Before accepting second-hand listings
- [ ] **Sanitize inputs** - All user inputs should be validated
- [ ] **Hash sensitive data** - If storing additional sensitive info
- [ ] **Log access** - Track admin actions for audit trail
- [ ] **Rate limit** - Prevent abuse of public endpoints

---

## Common Issues & Solutions

### Issue: "Failed to authenticate user" on admin operations
**Solution:** Check that user has admin metadata set in Supabase Auth

### Issue: RLS policy violation on product update
**Solution:** Verify user is authenticated and has admin role

### Issue: Image upload fails with 403 Forbidden
**Solution:** Check storage bucket RLS policies are configured correctly

### Issue: Email verification token not working
**Solution:** Ensure token was saved correctly before sending email; check token hasn't expired

### Issue: Second-hand listings visible to public
**Solution:** Verify RLS policy is preventing SELECT without admin role

---

## Data Validation

### Product Creation
```javascript
{
  name: string (required, max 255 chars),
  price: number (required, >= 0),
  description: string (required),
  image_path: string (optional, max 500 chars),
  is_active: boolean (optional, default true)
}
```

### Product Enquiry
```javascript
{
  product_id: UUID (required, must exist),
  customer_name: string (required, max 255 chars),
  customer_phone: string (required, max 20 chars),
  customer_email: string (required, valid email),
  enquiry_status: string (default 'PENDING')
}
```

### Second-Hand Listing
```javascript
{
  seller_name: string (required, max 255 chars),
  seller_phone: string (required, max 20 chars),
  seller_email: string (required, valid email),
  product_name: string (required, max 255 chars),
  condition: string (required, one of: NEW, GOOD, FAIR, POOR),
  detailed_description: string (required),
  expected_price: number (required, >= 0),
  email_verified: boolean (default false),
  listing_status: string (default 'NEW')
}
```

---

## Performance Tips

1. **Use Indexes:** Queries on `is_active`, `created_at`, `status` are indexed
2. **Eager Loading:** Use `.select('*, products(*)')` for foreign key data
3. **Pagination:** Use `.range(0, 50)` for large result sets
4. **Caching:** Cache active products/services on frontend
5. **Compression:** Compress images before storage upload

---

## Related Files

- [README.md](./README.md) - Database overview
- [SCHEMA_DOCUMENTATION.md](./SCHEMA_DOCUMENTATION.md) - Detailed schema info
- [SUPABASE_SETUP.md](./SUPABASE_SETUP.md) - Supabase dashboard setup
- [migrations/](./migrations/) - SQL migration files
- [functions/](./functions/) - PostgreSQL functions reference

---

## Support & Questions

Refer to:
- Supabase Documentation: https://supabase.com/docs
- PostgreSQL Documentation: https://www.postgresql.org/docs/
- RLS Policy Examples: Check database/migrations/002_rls_policies.sql

