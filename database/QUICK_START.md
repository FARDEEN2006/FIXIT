# FIXIT Database - Quick Start Guide

**TL;DR:** Everything is ready. Now you need to manually configure Supabase.

---

## What's Done ✅

- ✅ Database schema designed (6 tables)
- ✅ SQL migrations written (ready to run)
- ✅ RLS policies defined (security configured)
- ✅ Storage buckets planned (3 buckets)
- ✅ PostgreSQL functions created (7 functions)
- ✅ Complete documentation written

**Status:** Ready for manual Supabase setup

---

## What You Need To Do

### Phase 1: Create Supabase Project (15 min)

1. Go to https://supabase.com
2. Create new project
3. Save these 3 values:
   - **Project URL:** `https://[project-id].supabase.co`
   - **Anon Key:** `[public-key]`
   - **Service Role Key:** `[secret-key]` ⚠️ Keep SECRET

### Phase 2: Run SQL Migrations (10 min)

1. Open Supabase Dashboard → SQL Editor
2. Copy & paste: `database/migrations/001_initial_schema.sql`
3. Click Run, wait for success
4. Copy & paste: `database/migrations/002_rls_policies.sql`
5. Click Run, wait for success
6. Copy & paste: `database/functions/backend_operations.sql`
7. Click Run, wait for success

✅ Result: All 6 tables + functions created

### Phase 3: Create Storage Buckets (5 min)

1. Supabase Dashboard → Storage → Buckets
2. Create bucket named: `products` (PUBLIC)
3. Create bucket named: `second-hand` (PRIVATE)
4. Create bucket named: `service-icons` (PUBLIC)

✅ Result: 3 storage buckets ready

### Phase 4: Configure Admin User (5 min)

1. Supabase Dashboard → Authentication → Users
2. Click "Add User"
   - Email: your-admin@example.com
   - Password: [strong password]
3. Click "Create User"
4. Click on user in list
5. Click "User Metadata" tab
6. Paste this JSON:
   ```json
   {
     "is_admin": true,
     "role": "admin"
   }
   ```
7. Save

✅ Result: Admin user created with admin role

### Phase 5: Verify Everything Works (10 min)

Use the test queries in `DEPLOYMENT_CHECKLIST.md` to verify:
- [ ] All 6 tables exist
- [ ] All policies configured
- [ ] Public can read products/services
- [ ] Public cannot read second-hand
- [ ] Admin can access everything
- [ ] All functions work

---

## File Structure

```
database/
├── README.md                    ← Start here for overview
├── DATABASE_SETUP_COMPLETE.txt  ← Full summary
├── SUPABASE_SETUP.md           ← Step-by-step setup (FOLLOW THIS)
├── SCHEMA_DOCUMENTATION.md      ← Complete schema reference
├── BACKEND_INTEGRATION_GUIDE.md ← For backend developer
├── DEPLOYMENT_CHECKLIST.md     ← Testing procedures
│
├── migrations/
│   ├── 001_initial_schema.sql  ← Tables & triggers (RUN THIS)
│   └── 002_rls_policies.sql    ← Security policies (RUN THIS)
│
├── functions/
│   └── backend_operations.sql  ← PostgreSQL functions (RUN THIS)
│
├── seed/
│   └── sample_data.sql         ← Optional test data (OPTIONAL)
│
└── storage-config/
    └── storage_buckets.sql     ← Storage guide (REFERENCE)
```

---

## What Gets Created

### Tables (6)
- `products` - Product catalog
- `product_enquiries` - Customer enquiries
- `services` - Service offerings
- `store_information` - Business info
- `second_hand_listings` - Private seller submissions
- `second_hand_images` - Images for listings

### Functions (7)
- verify_second_hand_listing_email()
- create_second_hand_listing()
- get_product_with_enquiry_count()
- get_listing_with_images()
- update_listing_status()
- get_enquiries_for_product()
- get_listing_stats()

### Storage Buckets (3)
- `products` (PUBLIC) - Product images
- `second-hand` (PRIVATE) - Listing images
- `service-icons` (PUBLIC) - Service icons

### Security
- RLS on all tables
- Admin-only access to private data
- Public read for products/services/store info
- Email verification for sellers

---

## Critical: Save These Values

After Supabase setup, save for backend team:

```env
# SAFE to share with frontend
SUPABASE_URL=https://[project-id].supabase.co
SUPABASE_ANON_KEY=[public-key]

# KEEP SECRET - Only backend .env
SUPABASE_SERVICE_KEY=[secret-key]

# Admin access
ADMIN_EMAIL=your-admin@example.com
ADMIN_PASSWORD=[temp password - change after first login]
```

---

## Validation Queries

Run these in Supabase SQL Editor to verify:

```sql
-- Check all tables exist
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' ORDER BY table_name;

-- Check RLS policies
SELECT schemaname, tablename, policyname FROM pg_policies;

-- Test public read (products)
SELECT * FROM products WHERE is_active = true;

-- Test public cannot read private data (should fail)
SELECT * FROM second_hand_listings;

-- Check functions exist
SELECT proname FROM pg_proc WHERE proname LIKE '%second_hand%';
```

---

## Common Issues

**Q: "Error running migration"**
A: Read the error message carefully. Usually:
   - SQL syntax error (check file content)
   - Table already exists (try DROP TABLE IF EXISTS first)
   - Missing permissions (use postgres role)

**Q: "RLS policy not working"**
A: Make sure you:
   1. Ran migrations/002_rls_policies.sql
   2. Set RLS enabled on all tables
   3. Set admin user metadata correctly

**Q: "Cannot access second-hand data"**
A: This is correct! Second-hand is PRIVATE.
   - Public cannot access at all
   - Only admin with auth token can access
   - Backend must verify admin role before querying

---

## What Backend Developer Needs

Once Supabase is set up, give backend developer:

1. **BACKEND_INTEGRATION_GUIDE.md** (all API specs)
2. **Supabase credentials:**
   - Project URL
   - Anon Key
   - Service Role Key (in .env only)
3. **Admin access:**
   - Admin email
   - Temporary password

Backend will then implement:
- API endpoints
- Image compression & upload
- Email verification
- RLS policy enforcement

---

## Timeline

| Task | Time | Who |
|------|------|-----|
| Create Supabase project | 15 min | You |
| Run migrations | 10 min | You |
| Create storage buckets | 5 min | You |
| Create admin user | 5 min | You |
| Verify setup | 10 min | You |
| **Total** | **45 min** | You |
| Backend implementation | Days | Backend dev |

---

## Key Reminders

⚠️ **NEVER:**
- Commit .env files with secrets
- Share Service Role Key publicly
- Store passwords in code
- Make second-hand bucket public
- Skip email verification for sellers

✅ **ALWAYS:**
- Use Supabase Auth for admin login
- Compress images before upload
- Check RLS policies are working
- Validate user input on backend
- Use prepared statements (prevent SQL injection)

---

## Questions?

1. Check: `SUPABASE_SETUP.md` (step-by-step guide)
2. Check: `SCHEMA_DOCUMENTATION.md` (detailed reference)
3. Check: `DEPLOYMENT_CHECKLIST.md` (testing procedures)
4. Check: Supabase docs https://supabase.com/docs
5. Check: PostgreSQL docs https://www.postgresql.org/docs/

---

## Next Phase

After manual Supabase setup is complete:

→ **Backend development begins**

Backend developer will:
- Read BACKEND_INTEGRATION_GUIDE.md
- Implement API endpoints
- Add image handling
- Set up authentication
- Deploy to staging

---

**Ready?** Start with `SUPABASE_SETUP.md` for detailed step-by-step instructions.

⏱️ **Total time needed:** 45 minutes
