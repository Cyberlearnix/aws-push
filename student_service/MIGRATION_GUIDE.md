# Database Migration Guide: Long to UUID

## Problem
Your database tables have `student_id` columns as `BIGINT` (Long), but your entities now expect `UUID`. Hibernate cannot automatically convert between these types.

## Solution Options

### Option 1: Drop and Recreate Tables (RECOMMENDED FOR DEVELOPMENT)

**This will delete all existing data!**

1. **Stop your Spring Boot application**

2. **Connect to PostgreSQL and run the migration script:**
   ```bash
   psql -U postgres -d student_service_db -f src/main/resources/db-migration-uuid.sql
   ```
   
   Or manually execute:
   ```sql
   DROP TABLE IF EXISTS quiz_answers CASCADE;
   DROP TABLE IF EXISTS quiz_submissions CASCADE;
   DROP TABLE IF EXISTS quiz_questions CASCADE;
   DROP TABLE IF EXISTS question_options CASCADE;
   DROP TABLE IF EXISTS quizzes CASCADE;
   DROP TABLE IF EXISTS assignment_submissions CASCADE;
   DROP TABLE IF EXISTS assignments CASCADE;
   DROP TABLE IF EXISTS reviews CASCADE;
   DROP TABLE IF EXISTS certificates CASCADE;
   DROP TABLE IF EXISTS messages CASCADE;
   DROP TABLE IF EXISTS announcements CASCADE;
   DROP TABLE IF EXISTS progress CASCADE;
   DROP TABLE IF EXISTS enrollments CASCADE;
   DROP TABLE IF EXISTS students CASCADE;
   ```

3. **Restart your Spring Boot application**
   - Hibernate will recreate all tables with correct UUID types

### Option 2: Use Hibernate's Create-Drop Mode (TEMPORARY)

1. **Edit `application.properties` or `application.yml`:**
   ```properties
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

2. **Restart the application** (this will drop and recreate tables automatically)

3. **After successful startup, change it back to:**
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

### Option 3: Manual Data Migration (FOR PRODUCTION)

If you have important data to preserve:

1. Create backup
2. Export data
3. Drop tables
4. Recreate with UUID types
5. Convert and re-import data with generated UUIDs

## Fixed Issues

✅ All services now use `UUID` for `studentId` parameters  
✅ All repositories accept `UUID` for student lookups  
✅ DTOs updated to use `UUID` for `studentId` fields  
✅ `Certificate.courseId` corrected to `Long` (courses use Long IDs)  
✅ `Quiz.getPassingScore()` method name fixed  
✅ Message mapping fixed to use correct entity fields  

## Security Improvements Needed

After migration, implement these security measures:

1. **JWT Role Validation**: Ensure only users with role `STUDENT` can access student APIs
2. **Student ID Verification**: Match JWT student UUID with path `/{id}` parameter
3. **Message Privacy**: Filter messages by authenticated student's UUID only
4. **No Horizontal Privilege Escalation**: Students cannot access other students' data

## Next Steps

1. Choose migration option above
2. Drop existing tables
3. Restart application
4. Verify all tables created with UUID types
5. Test API endpoints
6. Implement security hardening
