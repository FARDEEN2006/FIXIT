# FIXIT Backend - Manual Setup Guide

## Prerequisites

Before starting, ensure you have:

- ✅ Java 17 or higher installed
- ✅ Maven 3.8.0 or higher installed
- ✅ Git installed
- ✅ Supabase account with FIXIT database configured
- ✅ IDE (IntelliJ IDEA recommended, or VS Code with extensions)
- ✅ Postman or similar API testing tool
- ✅ Terminal/Command Prompt access

### System Requirements
- RAM: 4GB minimum (8GB recommended)
- Disk Space: 2GB free
- OS: Windows 10+, macOS 10.15+, or Linux (Ubuntu 20.04+)

---

## Part 1: Environment Setup

### 1.1 Install Java 17

**Windows:**
```powershell
# Download from https://www.oracle.com/java/technologies/downloads/#java17
# Run installer and follow prompts
# Verify installation:
java -version
javac -version
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@17
# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/local/opt/openjdk@17' >> ~/.zprofile
source ~/.zprofile
java -version
```

**Linux (Ubuntu):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

### 1.2 Install Maven

**Windows:**
1. Download Apache Maven from https://maven.apache.org/download.cgi
2. Extract to a folder (e.g., C:\maven)
3. Add to PATH environment variable
4. Verify: `mvn -version`

**macOS:**
```bash
brew install maven
mvn -version
```

**Linux (Ubuntu):**
```bash
sudo apt install maven
mvn -version
```

### 1.3 Install Git

**Windows:**
Download from https://git-scm.com/download/win and run installer

**macOS:**
```bash
brew install git
```

**Linux:**
```bash
sudo apt install git
```

---

## Part 2: Clone Repository

```bash
# Navigate to your workspace
cd ~/projects

# Clone the repository
git clone https://github.com/YOUR_USERNAME/fixit.git
cd fixit/backend

# Verify structure
ls -la
# Should see: pom.xml, src/, Dockerfile, RENDER_DEPLOYMENT.md, etc.
```

---

## Part 3: Configure Environment Variables

### 3.1 Create .env File

In the `backend/` directory, create a `.env` file:

```bash
# Copy from template
cp .env.example .env

# Edit .env with your values
nano .env  # or use your editor
```

### 3.2 Set Supabase Credentials

Open your Supabase dashboard:
1. Go to https://app.supabase.com
2. Select your FIXIT project
3. Go to Settings → API Keys

Copy the following and paste into `.env`:

```env
# Database
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_ANON_KEY=eyJhbGc...  # (anon public key)
SUPABASE_SERVICE_KEY=eyJhbGc... # (service_role secret key)
DATABASE_URL=postgresql://postgres.YOUR_PROJECT:PASSWORD@db.YOUR_PROJECT.supabase.co:5432/postgres
```

### 3.3 Generate JWT Secret

Generate a strong random string (minimum 32 characters):

```bash
# Linux/macOS
openssl rand -base64 32

# Windows (PowerShell)
[System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
```

Add to `.env`:
```env
JWT_SECRET=YOUR_GENERATED_SECRET_HERE
JWT_EXPIRATION_MS=86400000
```

### 3.4 Set Application Configuration

```env
# Application
APP_NAME=FIXIT
APP_ADMIN_EMAIL=admin@fixit.local
APP_WHATSAPP_NUMBER=+919876543210
APP_FRONTEND_URL=http://localhost:3000
PORT=8080

# Image Processing
IMAGE_MAX_FILE_SIZE=5242880
IMAGE_COMPRESSION_QUALITY=80

# CORS
SECURITY_CORS_ORIGINS=http://localhost:3000,http://localhost:3001

# Spring
SPRING_PROFILES_ACTIVE=dev
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

### 3.5 Email Configuration (Optional)

If using Gmail for email verification:

```env
# Email (Gmail SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
```

**Note:** For Gmail, generate an app-specific password (not your regular password):
1. Enable 2-Factor Authentication on your Google account
2. Go to https://myaccount.google.com/apppasswords
3. Create app password for "Mail" and "Windows (or Mac/Linux)"
4. Use that password in the environment variable

### 3.6 Verify .env File

```bash
# Check that .env is in gitignore (don't commit it)
cat .gitignore | grep ".env"

# Should output: .env
```

---

## Part 4: Build Project

### 4.1 Clean Build

```bash
# Navigate to backend directory
cd backend

# Clean and build
mvn clean install

# This will:
# - Download all dependencies
# - Compile Java code
# - Run tests
# - Package as JAR
# Expected time: 2-5 minutes on first build
```

### 4.2 Troubleshooting Build Errors

**Error: "Could not find Java compilation unit"**
```bash
# Ensure JAVA_HOME is set
echo $JAVA_HOME  # Linux/macOS
echo %JAVA_HOME% # Windows

# Set if not set
export JAVA_HOME=/usr/libexec/java_home  # macOS
```

**Error: "Failed to download dependencies"**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Try build again
mvn clean install -X
```

**Error: "Tests failed"**
```bash
# Skip tests for now (if needed)
mvn clean install -DskipTests
```

---

## Part 5: Run Application

### 5.1 Using Maven

```bash
# From backend directory
mvn spring-boot:run

# You should see output like:
# 2024-01-15 10:30:00.123 INFO  FixitBackendApplication : Started FixitBackendApplication
# 2024-01-15 10:30:00.456 INFO  FixitBackendApplication : Tomcat started on port(s): 8080
```

### 5.2 Using Compiled JAR

```bash
# Build first
mvn clean package -DskipTests

# Run JAR
java -jar target/fixit-backend-*.jar

# Or with environment variables
java -jar target/fixit-backend-*.jar --server.port=8080
```

### 5.3 Using IDE

**IntelliJ IDEA:**
1. Open project in IntelliJ
2. Right-click on `FixitBackendApplication.java`
3. Select "Run 'FixitBackendApplication.main()'"
4. Application starts in IDE

**VS Code:**
1. Open project in VS Code
2. Install "Extension Pack for Java"
3. Press Ctrl+Shift+D (Debug)
4. Create launch configuration for Spring Boot
5. Press F5 to run

### 5.4 Verify Running Application

```bash
# In another terminal, test health endpoint
curl http://localhost:8080/api/health

# Expected response:
# {"status":"UP","timestamp":"2024-01-15T10:30:00","service":"FIXIT-Backend","version":"1.0.0"}
```

---

## Part 6: Testing API Endpoints

### 6.1 Using curl

**Test Public Endpoint:**
```bash
# Health check
curl http://localhost:8080/api/health

# Get store info
curl http://localhost:8080/api/store-info

# List products
curl http://localhost:8080/api/products?page=0&pageSize=10
```

**Test Admin Endpoint (requires JWT token):**
```bash
# First, generate a JWT token (placeholder)
TOKEN="your-jwt-token-here"

# Create product
curl -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "iPhone 13 Pro",
    "price": 75000,
    "description": "Apple iPhone 13 Pro in excellent condition",
    "isActive": true
  }'
```

### 6.2 Using Postman

1. **Download Postman** from https://www.postman.com/downloads/
2. **Create Collection** named "FIXIT Backend"
3. **Add Requests:**

**Request 1: Health Check**
- Method: GET
- URL: `http://localhost:8080/api/health`
- Send → Response: 200 OK

**Request 2: Get Products**
- Method: GET
- URL: `http://localhost:8080/api/products`
- Params: page=0, pageSize=10
- Send → Response: 200 OK (empty array if no products)

**Request 3: Create Product (requires JWT)**
- Method: POST
- URL: `http://localhost:8080/api/admin/products`
- Headers: 
  - Content-Type: application/json
  - Authorization: Bearer {your-jwt-token}
- Body (raw JSON):
```json
{
  "name": "iPhone 13 Pro",
  "price": 75000,
  "description": "Apple iPhone 13 Pro in excellent condition with all accessories",
  "isActive": true
}
```

### 6.3 Generate Test JWT Token

For testing admin endpoints, you need a JWT token with admin role:

```java
// This code would generate a token
// Using JwtTokenProvider from your backend
// userId: "test-user-123"
// email: "admin@fixit.local"
// isAdmin: true
```

**For now, you can:**
1. Test backend endpoints without authentication first
2. Once frontend is ready, use it to generate real tokens
3. Or modify SecurityConfig temporarily for testing

---

## Part 7: Database Verification

### 7.1 Connect to Supabase Database

```bash
# Using psql (PostgreSQL command line)
psql postgresql://postgres.YOUR_PROJECT:PASSWORD@db.YOUR_PROJECT.supabase.co:5432/postgres

# Or use Supabase web interface:
# Go to supabase.com → Your Project → SQL Editor
```

### 7.2 Verify Tables Exist

```sql
-- In Supabase SQL Editor, run:
SELECT tablename FROM pg_tables WHERE schemaname = 'public';

-- Should show:
-- products
-- product_enquiries
-- services
-- store_information
-- second_hand_listings
-- second_hand_images
```

### 7.3 Insert Test Data

```sql
-- Insert a test product
INSERT INTO products (name, price, description, is_active)
VALUES ('Test iPhone', 50000, 'Test product description', true);

-- Verify insert
SELECT * FROM products LIMIT 5;
```

Now you should see it via API:
```bash
curl http://localhost:8080/api/products
```

---

## Part 8: Debug Mode

### 8.1 Enable Debug Logging

Edit `application.properties`:

```properties
# Add or modify:
logging.level.root=INFO
logging.level.com.fixit=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG

# Log SQL queries
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### 8.2 View Application Logs

```bash
# Logs display in console when running with:
mvn spring-boot:run

# Or in IDE console

# Look for:
# - ERROR: Application failed to start
# - WARN: Missing required properties
# - DEBUG: Detailed request/response flow
```

### 8.3 Use IDE Debugger

**IntelliJ IDEA:**
1. Set breakpoints by clicking left margin of code
2. Run in Debug mode (Shift+F9)
3. Step through code with F8 (over) or F7 (into)
4. View variables in "Variables" panel

**VS Code:**
1. Install "Debugger for Java"
2. Set breakpoints
3. Press F5 to start debugging
4. Use Debug Console

---

## Part 9: Common Issues & Solutions

### Issue: "SUPABASE_URL environment variable not set"

**Solution:**
```bash
# Verify .env file exists and has SUPABASE_URL
cat .env | grep SUPABASE_URL

# Load environment variables
export $(cat .env | xargs)

# Verify loaded
echo $SUPABASE_URL

# Try running again
mvn spring-boot:run
```

### Issue: "Cannot connect to Supabase database"

**Solution:**
```bash
# Verify DATABASE_URL is correct format
# Should be: postgresql://user:password@host:port/database

# Test connection with psql
psql "postgresql://postgres.YOUR_PROJECT:PASSWORD@db.YOUR_PROJECT.supabase.co:5432/postgres"

# If connection fails, check:
# 1. Supabase project is running
# 2. Firewall isn't blocking connection
# 3. Password is correct
```

### Issue: "JWT token is invalid"

**Solution:**
```bash
# Verify JWT_SECRET is set and has minimum 32 characters
echo $JWT_SECRET | wc -c

# Should be > 32

# Regenerate if needed
openssl rand -base64 32
```

### Issue: "Image upload fails"

**Solution:**
1. Verify storage bucket exists in Supabase
2. Check bucket permissions (should be public for "products")
3. Verify image file is not corrupted
4. Check image is within 5MB size limit

### Issue: "CORS error - blocked by browser"

**Solution:**
```bash
# In application.properties, verify CORS is configured:
# SECURITY_CORS_ORIGINS=http://localhost:3000

# If frontend is on different port, add it:
# SECURITY_CORS_ORIGINS=http://localhost:3000,http://localhost:3001
```

---

## Part 10: Development Workflow

### 10.1 Typical Development Session

```bash
# 1. Start your day
cd ~/projects/fixit/backend

# 2. Pull latest changes
git pull origin main

# 3. Update .env if needed
# (check with team for any new variables)

# 4. Start backend
mvn spring-boot:run

# 5. In another terminal, run frontend (if working on integration)
cd ../frontend
npm start

# 6. Test your changes
# Use Postman or curl to test endpoints

# 7. When done, commit changes
git add .
git commit -m "Fix: [issue description]"
git push origin your-branch-name

# 8. Create Pull Request on GitHub
```

### 10.2 Code Organization Best Practices

```
Add feature:
1. Create DTOs (src/main/java/com/fixit/dto/)
2. Create Service (src/main/java/com/fixit/service/)
3. Add Controller (src/main/java/com/fixit/controller/)
4. Write Tests (src/test/java/com/fixit/)
5. Update Documentation

Modify existing feature:
1. Update DTO if needed
2. Update Service logic
3. Update Controller if endpoints change
4. Run full test suite
5. Update API documentation
```

### 10.3 Before Pushing to Git

```bash
# 1. Run full build
mvn clean install

# 2. Check for errors
# (fix any compilation errors)

# 3. Run tests
mvn test

# 4. Verify no secrets in code
grep -r "REAL_PASSWORD" src/
grep -r "SECRET_KEY" src/

# 5. Commit and push
git add .
git commit -m "Your message"
git push origin your-branch
```

---

## Part 11: Performance Optimization

### 11.1 Maven Build Optimization

```bash
# Use parallel compilation (faster builds)
mvn -T 1C clean install

# Skip tests for faster development builds
mvn clean install -DskipTests

# Skip test compilation
mvn clean compile -DskipTests
```

### 11.2 IDE Configuration

**IntelliJ IDEA:**
- Preferences → Build, Execution, Deployment → Compiler
- Enable "Build project automatically"
- Increase heap size: VM options: -Xmx2g

**VS Code:**
- Install "Maven for Java" extension
- Configure memory settings in workspace

---

## Deployment Checklist (Before Render)

- [ ] All environment variables set in `.env`
- [ ] Application runs without errors locally
- [ ] Health endpoint returns 200
- [ ] Database connectivity verified
- [ ] Sample API calls work (GET /api/products)
- [ ] No console errors or warnings
- [ ] All tests pass (mvn test)
- [ ] Dockerfile created and tested
- [ ] No secrets in Git repository
- [ ] Dependencies updated (mvn clean install)
- [ ] Application builds successfully (mvn clean package)
- [ ] README.md created with setup instructions
- [ ] All endpoints documented
- [ ] Code reviewed by team member

---

## Useful Commands Reference

```bash
# Build and run
mvn clean install                    # Full build
mvn clean package -DskipTests        # Build JAR
mvn spring-boot:run                  # Run locally
mvn clean spring-boot:run -DskipTests # Run without tests

# Testing
mvn test                             # Run all tests
mvn test -Dtest=ProductServiceTest   # Run specific test
mvn test -DskipTests                 # Skip tests

# Debugging
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# IDE support
mvn eclipse:eclipse                  # For Eclipse IDE
mvn idea:idea                        # For IntelliJ

# Git operations
git status                           # Check status
git add .                            # Stage changes
git commit -m "message"              # Commit
git push origin branch-name          # Push to GitHub
git pull origin main                 # Pull updates
```

---

## Support & Resources

- **Java Documentation:** https://docs.oracle.com/en/java/javase/17/
- **Spring Boot Documentation:** https://spring.io/projects/spring-boot/
- **Supabase Documentation:** https://supabase.com/docs
- **Maven Documentation:** https://maven.apache.org/guides/
- **JWT Documentation:** https://jwt.io/

---

**Document Version:** 1.0
**Last Updated:** 2024
**Author:** FIXIT Development Team
