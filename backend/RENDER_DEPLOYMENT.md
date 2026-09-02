# Render Deployment Guide - FIXIT Backend

## Overview
This guide provides step-by-step instructions to deploy the FIXIT Backend Spring Boot application to Render.com with Supabase PostgreSQL database.

## Prerequisites
- Git repository with backend code pushed to GitHub
- Render.com account (free or paid)
- Supabase account with FIXIT database configured
- Docker installed locally (optional, for testing)

---

## Part 1: Prepare Your Application

### 1.1 Create Dockerfile
Create a `Dockerfile` in the `backend/` directory:

```dockerfile
FROM eclipse-temurin:17-jdk-jammy as build

WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/fixit-backend-*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar", "-Dspring.profiles.active=prod"]
```

### 1.2 Create .dockerignore
Create `.dockerignore` in `backend/` directory:

```
.git
.gitignore
.DS_Store
*.log
target/
.idea/
*.iml
node_modules/
.env
.env.local
.env.*.local
```

### 1.3 Create render.yaml (Optional - for Infrastructure as Code)
Create `render.yaml` in repository root:

```yaml
services:
  - type: web
    name: fixit-backend
    env: docker
    dockerfilePath: ./backend/Dockerfile
    branch: main
    plan: starter
    healthCheckPath: /api/health
    envVars:
      - key: PORT
        value: 8080
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      # Add other non-secret environment variables here
```

### 1.4 Ensure Git is Updated
```bash
cd backend
git add Dockerfile .dockerignore
git commit -m "Add Docker configuration for Render deployment"
git push
```

---

## Part 2: Create Render Service

### 2.1 Log In to Render
1. Go to https://render.com
2. Log in to your account (create one if needed)
3. Go to Dashboard → Web Services

### 2.2 Create New Web Service
1. Click **"New +"** → **"Web Service"**
2. Select **"Deploy an existing Git repository"**
3. Connect GitHub (if not already connected):
   - Click "Connect account"
   - Authorize Render to access your GitHub
   - Select your repository

### 2.3 Configure Service
**Basic Information:**
- Name: `fixit-backend`
- Region: Choose closest to your users (e.g., US East, EU West)
- Branch: `main` (or your main branch)

**Build & Deploy:**
- Runtime: `Docker`
- Build Command: (leave empty - Dockerfile will handle it)
- Start Command: (leave empty - Dockerfile will handle it)

**Plan:**
- Select **Starter** for development (free with limitations)
- Or **Standard** for production

### 2.4 Set Environment Variables (CRITICAL)
Click **"Advanced"** and then **"Add Environment Variable"** for each:

**Required Variables:**

| Key | Value | Notes |
|-----|-------|-------|
| `PORT` | `8080` | Render automatically assigns port, but app should listen on 8080 |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production Spring profile |
| `SUPABASE_URL` | `https://YOUR_PROJECT.supabase.co` | From Supabase dashboard |
| `SUPABASE_ANON_KEY` | `YOUR_ANON_KEY` | From Supabase → Settings → API Keys |
| `SUPABASE_SERVICE_KEY` | `YOUR_SERVICE_KEY` | From Supabase → Settings → API Keys (SERVICE_ROLE) |
| `JWT_SECRET` | `YOUR_SECURE_RANDOM_STRING_MIN_32_CHARS` | Generate a secure random string |
| `JWT_EXPIRATION_MS` | `86400000` | 24 hours in milliseconds |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` | Production should validate only |
| `DATABASE_URL` | `postgresql://USER:PASSWORD@HOST:5432/DB` | From Supabase PostgreSQL connection string |

**Optional Email Variables (if using SMTP):**

| Key | Value | Example |
|-----|-------|---------|
| `SPRING_MAIL_HOST` | SMTP server hostname | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | SMTP server port | `587` |
| `SPRING_MAIL_USERNAME` | Email address | `noreply@fixit.com` |
| `SPRING_MAIL_PASSWORD` | Email password/app-key | Use app-specific password for Gmail |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | `true` | Enable SMTP auth |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | `true` | Enable TLS |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED` | `true` | Require TLS |

**App Configuration:**

| Key | Value | Notes |
|-----|-------|-------|
| `APP_NAME` | `FIXIT` | Application name |
| `APP_ADMIN_EMAIL` | `admin@fixit.com` | Admin email for notifications |
| `APP_WHATSAPP_NUMBER` | `+91XXXXXXXXXX` | Business WhatsApp number |
| `APP_FRONTEND_URL` | `https://YOUR_FRONTEND_URL` | Frontend app URL for email links |
| `IMAGE_MAX_FILE_SIZE` | `5242880` | 5MB in bytes |
| `IMAGE_COMPRESSION_QUALITY` | `80` | JPEG compression quality 0-100 |
| `CORS_ORIGINS` | `https://YOUR_FRONTEND_URL,https://OTHER_FRONTEND` | Comma-separated CORS allowed origins |

### 2.5 Complete Deployment
1. Scroll to top
2. Click **"Create Web Service"** or **"Deploy"**
3. Render will:
   - Clone your repository
   - Build Docker image
   - Deploy application
   - Assign a `.onrender.com` domain

---

## Part 3: Verify Deployment

### 3.1 Check Deployment Status
1. In Render dashboard, click on your service
2. Watch the "Deploy" tab for logs
3. Once deployed, you'll see green checkmarks

### 3.2 Test Health Endpoint
Open browser and navigate to:
```
https://YOUR_SERVICE_NAME.onrender.com/api/health
```

Expected response (200 OK):
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "service": "FIXIT-Backend",
  "version": "1.0.0"
}
```

### 3.3 Test Public Endpoints
Test GET /api/store-info:
```bash
curl https://YOUR_SERVICE_NAME.onrender.com/api/store-info
```

### 3.4 Monitor Logs
1. In Render dashboard, go to "Logs" tab
2. Watch for any errors
3. Common issues:
   - Database connection timeout → Check `SUPABASE_URL` and credentials
   - JWT errors → Check `JWT_SECRET` format
   - CORS errors → Check `CORS_ORIGINS`

---

## Part 4: Configure Custom Domain (Optional)

### 4.1 Add Custom Domain
1. In Render dashboard → Your service
2. Go to "Settings" → "Custom Domains"
3. Click "Add Custom Domain"
4. Enter your domain (e.g., `api.fixit.com`)

### 4.2 Update DNS
1. Point your domain's DNS records to Render:
   - CNAME: `YOUR_RENDER_SERVICE.onrender.com`
   - Or follow Render's DNS setup instructions

### 4.3 Enable Auto-renewal for SSL
Render automatically provides free SSL certificates. No additional configuration needed.

---

## Part 5: Post-Deployment Tasks

### 5.1 Configure Supabase RLS Policies
Ensure your database RLS policies are properly configured to work with JWT tokens from the backend.

### 5.2 Test Email Integration
If using email verification:
1. Create a test second-hand listing
2. Check email is sent successfully
3. Verify the verification link works

### 5.3 Test Admin Features
1. Create JWT token with admin role
2. Test `/api/admin/*` endpoints require authentication
3. Verify only admins can access admin endpoints

### 5.4 Set Up Monitoring
1. Configure error alerts in Render dashboard
2. Set up uptime monitoring (optional: use third-party services)
3. Monitor disk usage and resource usage

### 5.5 Enable Auto-Deploy
1. In Render → Settings → GitHub
2. Enable "Auto-deploy on push" if desired
3. Or disable for manual deployments only

---

## Part 6: Troubleshooting

### Issue: Application crashes after deployment

**Symptoms:** Red error status, logs show exceptions

**Solutions:**
1. Check environment variables are all set correctly
2. Verify Supabase credentials
3. Check database connection string format
4. Ensure `JWT_SECRET` is at least 32 characters
5. Review application logs for specific error messages

### Issue: Database connection timeout

**Symptoms:** Connection refused, database error logs

**Solutions:**
1. Verify `SUPABASE_URL` includes `https://`
2. Ensure `DATABASE_URL` is correct format
3. Check Supabase is not rate-limiting connections
4. Verify network isn't blocking connections

### Issue: CORS errors when frontend calls backend

**Symptoms:** Browser console shows CORS blocked errors

**Solutions:**
1. Add frontend URL to `CORS_ORIGINS` environment variable
2. Use comma to separate multiple URLs
3. Restart service after updating environment variable

### Issue: Email verification not sending

**Symptoms:** Listing created but verification email not received

**Solutions:**
1. Verify SMTP credentials are correct
2. Check `APP_FRONTEND_URL` is set correctly
3. Verify email not in spam/junk folder
4. Check Render logs for email sending errors
5. If no SMTP configured, emails silently fail (log warnings only)

### Issue: Image upload fails

**Symptoms:** 400/500 error when uploading product image

**Solutions:**
1. Verify `SUPABASE_ANON_KEY` has storage permissions
2. Ensure Supabase storage bucket "products" exists and is public
3. Check image file size (max 5MB)
4. Verify image format is jpg/jpeg/png/webp
5. Check disk space on server

---

## Part 7: Maintenance & Updates

### 7.1 Redeploy After Code Changes
```bash
git add .
git commit -m "Your changes"
git push origin main
```
Render automatically redeploys if auto-deploy is enabled.

### 7.2 Manual Redeploy
1. Go to Render dashboard
2. Click "Manual Deploy" or "Re-deploy"
3. Select branch to deploy

### 7.3 Update Environment Variables
1. Go to service → Settings
2. Update any environment variable
3. Click "Save"
4. Service automatically redeploys with new variables

### 7.4 Rollback
1. Go to "Deploys" tab
2. Click "Rollback" on a previous successful deployment
3. Confirm rollback

### 7.5 View Resource Usage
1. Go to "Settings" tab
2. Monitor CPU, Memory, Disk usage
3. Upgrade plan if resources insufficient

---

## Part 8: Production Best Practices

### 8.1 Security
- ✅ Never commit `.env` files to Git
- ✅ Use strong, random `JWT_SECRET` (minimum 32 characters)
- ✅ Rotate `JWT_SECRET` periodically (requires user re-login)
- ✅ Keep Supabase keys secure (never expose in frontend)
- ✅ Use service-role key only in backend, never in frontend
- ✅ Enable HTTPS/SSL (automatic with Render)
- ✅ Restrict CORS origins to only trusted domains

### 8.2 Database
- ✅ Set `spring.jpa.hibernate.ddl-auto=validate` (don't auto-create)
- ✅ Use connection pooling
- ✅ Implement database backups
- ✅ Monitor query performance
- ✅ Keep RLS policies enabled

### 8.3 Performance
- ✅ Enable gzip compression in application.properties
- ✅ Implement caching where appropriate
- ✅ Use CDN for static content
- ✅ Monitor response times
- ✅ Implement rate limiting if needed

### 8.4 Monitoring
- ✅ Set up error tracking (e.g., Sentry)
- ✅ Monitor API response times
- ✅ Set up uptime monitoring
- ✅ Review logs regularly
- ✅ Alert on critical errors

---

## Deployment Checklist

- [ ] Dockerfile created and tested locally
- [ ] All environment variables documented
- [ ] Git repository updated with Dockerfile
- [ ] Render account created and configured
- [ ] Service deployed successfully
- [ ] Health endpoint responds (200 OK)
- [ ] Database connection verified
- [ ] JWT authentication working
- [ ] CORS configured for frontend
- [ ] Email notifications configured (if needed)
- [ ] Admin endpoints tested
- [ ] SSL certificate issued
- [ ] Custom domain configured (if applicable)
- [ ] Monitoring/alerts set up
- [ ] Backup strategy documented
- [ ] Runbook created for common issues

---

## Support & Documentation

- Render Docs: https://render.com/docs
- Spring Boot Docs: https://spring.io/projects/spring-boot
- Supabase Docs: https://supabase.com/docs
- JWT Docs: https://jwt.io

---

## Emergency Contacts

Keep these handy for production support:
- Render Support: support@render.com
- Supabase Support: support@supabase.com
- Your development team contact info

---

**Document Version:** 1.0
**Last Updated:** 2024
**Author:** FIXIT Development Team
