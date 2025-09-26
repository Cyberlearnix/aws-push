# LMS Student Service - API Testing Guide

## 🚀 Quick Start

### 1. Start the Application
```bash
./gradlew bootRun
```
The application will start on `http://localhost:8081`

### 2. Test User Credentials
The application automatically creates test users with the following credentials:

| Role | Email | Password | Description |
|------|-------|----------|-------------|
| **Student** | `testuser@example.com` | `test123` | Main test user with comprehensive data |
| **Instructor** | `instructor@example.com` | `instructor123` | For testing instructor-specific features |
| **Admin** | `admin@example.com` | `admin123` | For testing admin features |

### 3. Import Postman Collection
1. Open Postman
2. Click "Import" button
3. Select the `LMS_Student_Service_Postman_Collection.json` file
4. The collection will be imported with all API endpoints

## 📋 Test Data Overview

The test user (`testuser@example.com`) comes with pre-populated data:

### Enrollments
- **Course 1 (Java Programming)**: Active, 75% complete
- **Course 2 (Spring Boot)**: Active, 40% complete  
- **Course 3 (Database Design)**: Completed, 100%

### Progress Records
- Course-level progress tracking
- Module-level completion records
- Lesson-level detailed progress

### Quizzes
- **Java Basics Quiz**: 5 questions, 30-minute limit
- **Spring Boot Fundamentals**: 3 questions, 45-minute limit
- Multiple choice and true/false questions

### Assignments
- **Java Calculator Project**: Due in 7 days, 100 points
- **REST API Development**: Due in 14 days, 150 points

### Certificates
- **Database Design Certificate**: Completed course with verification code

### Reviews
- 5-star review for Database Design course
- Verified and published review

### Announcements
- Midterm exam schedule (important, pinned)
- Spring Boot workshop announcement

### Messages
- Question about Assignment 3 (read, replied)
- Spring Boot configuration issue (unread)

## 🔐 Authentication Flow

### Step 1: Login
```bash
POST /auth/login
{
    "email": "testuser@example.com",
    "password": "test123"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "email": "testuser@example.com",
    "role": "STUDENT",
    "firstName": "Test",
    "lastName": "User"
}
```

### Step 2: Use JWT Token
Include the JWT token in the Authorization header for all subsequent requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 📊 API Testing Scenarios

### 1. Enrollment Testing
```bash
# Get enrolled courses
GET /students/1/courses

# Enroll in new course
POST /students/1/courses/4/enroll
{
    "courseId": 4,
    "notes": "Interested in learning React"
}

# Unenroll from course
DELETE /students/1/courses/4
```

### 2. Progress Tracking
```bash
# Get course progress
GET /students/1/courses/1/progress

# Update course progress
PUT /students/1/courses/1/progress
{
    "courseId": 1,
    "type": "COURSE",
    "status": "IN_PROGRESS",
    "completionPercentage": 80.0,
    "timeSpentMinutes": 150,
    "notes": "Completed chapter 4"
}

# Get overall progress
GET /students/1/progress/overall
```

### 3. Quiz Testing
```bash
# Get quizzes for module
GET /students/1/courses/1/modules/1/quiz

# Submit quiz
POST /students/1/courses/1/modules/1/quiz/1/submit
{
    "quizId": 1,
    "answers": {
        "1": "1",
        "2": "true"
    },
    "notes": "Quiz completed successfully"
}

# Get quiz results
GET /students/1/courses/1/quizzes/results
```

### 4. Assignment Testing
```bash
# Get assignments
GET /students/1/assignments

# Submit assignment
POST /students/1/assignments/1/submit
{
    "assignmentId": 1,
    "submissionText": "I have completed the Java Calculator project...",
    "notes": "Please review my implementation"
}

# Get assignment submissions
GET /students/1/assignments/submissions
```

### 5. Certificate Testing
```bash
# Get all certificates
GET /students/1/certificates

# Get specific certificate
GET /students/1/certificates/1

# Verify certificate (public endpoint)
GET /students/1/certificates/verify/VERIFY-1234567890
```

### 6. Review Testing
```bash
# Add review
POST /students/1/courses/1/reviews
{
    "courseId": 1,
    "rating": 5,
    "title": "Excellent Course!",
    "comment": "This course exceeded my expectations..."
}

# Update review
PUT /students/1/courses/1/reviews/1
{
    "courseId": 1,
    "rating": 4,
    "title": "Good Course with Minor Issues",
    "comment": "Updated review: The course is good overall..."
}

# Delete review
DELETE /students/1/courses/1/reviews/1

# Get student reviews
GET /students/1/reviews
```

### 7. Communication Testing
```bash
# Get announcements
GET /students/1/announcements

# Get course announcements
GET /students/1/courses/1/announcements

# Get messages
GET /students/1/messages

# Get course messages
GET /students/1/courses/1/messages

# Mark message as read
PUT /students/1/messages/1/read

# Get unread message count
GET /students/1/messages/unread-count
```

### 8. Dashboard & Analytics
```bash
# Get dashboard
GET /students/1/dashboard

# Get learning statistics
GET /students/1/stats
```

## 🧪 Testing Different User Roles

### Student Role Testing
- Use `testuser@example.com` / `test123`
- Test all student-specific endpoints
- Verify data isolation (can only access own data)

### Instructor Role Testing
- Use `instructor@example.com` / `instructor123`
- Test instructor-specific endpoints (if implemented)
- Verify access to student progress in their courses

### Admin Role Testing
- Use `admin@example.com` / `admin123`
- Test admin-specific endpoints (if implemented)
- Verify access to all student activities

## 🔍 Error Testing

### Test Error Scenarios
```bash
# Invalid credentials
POST /auth/login
{
    "email": "invalid@example.com",
    "password": "wrongpassword"
}

# Unauthorized access (no token)
GET /students/1/dashboard

# Access another student's data
GET /students/2/dashboard  # Should return 403

# Invalid course ID
GET /students/1/courses/999/progress  # Should return 404

# Validation errors
POST /students/1/courses/1/reviews
{
    "courseId": 1,
    "rating": 6,  # Invalid rating (should be 1-5)
    "comment": ""  # Empty comment
}
```

## 📈 Performance Testing

### Load Testing Scenarios
1. **Concurrent Login**: Test multiple users logging in simultaneously
2. **Dashboard Load**: Test dashboard API with large datasets
3. **Progress Updates**: Test frequent progress updates
4. **Quiz Submissions**: Test concurrent quiz submissions

### Recommended Tools
- **Postman**: For manual API testing
- **JMeter**: For load testing
- **Newman**: For automated testing with Postman collections

## 🐛 Common Issues & Solutions

### Issue 1: JWT Token Expired
**Error**: `401 Unauthorized`
**Solution**: Re-login to get a new token

### Issue 2: Database Connection
**Error**: `500 Internal Server Error`
**Solution**: Ensure PostgreSQL is running and database exists

### Issue 3: Validation Errors
**Error**: `422 Unprocessable Entity`
**Solution**: Check request body format and required fields

### Issue 4: Access Denied
**Error**: `403 Forbidden`
**Solution**: Verify user role and permissions

## 📝 Test Checklist

### ✅ Authentication
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] JWT token validation
- [ ] Token expiration handling

### ✅ Enrollment
- [ ] Get enrolled courses
- [ ] Enroll in new course
- [ ] Unenroll from course
- [ ] Duplicate enrollment prevention

### ✅ Progress
- [ ] Get course progress
- [ ] Update course progress
- [ ] Update module progress
- [ ] Update lesson progress
- [ ] Get overall progress

### ✅ Quizzes
- [ ] Get quizzes for module
- [ ] Submit quiz answers
- [ ] Get quiz results
- [ ] Quiz attempt limits

### ✅ Assignments
- [ ] Get assignments
- [ ] Submit assignment
- [ ] Get assignment submissions
- [ ] Late submission handling

### ✅ Certificates
- [ ] Get all certificates
- [ ] Get specific certificate
- [ ] Verify certificate (public)

### ✅ Reviews
- [ ] Add review
- [ ] Update review
- [ ] Delete review
- [ ] Get student reviews

### ✅ Communication
- [ ] Get announcements
- [ ] Get course announcements
- [ ] Get messages
- [ ] Mark message as read
- [ ] Get unread count

### ✅ Dashboard
- [ ] Get dashboard data
- [ ] Get learning statistics

### ✅ Security
- [ ] Role-based access control
- [ ] Data isolation
- [ ] Input validation
- [ ] Error handling

## 🎯 Success Criteria

A successful test run should demonstrate:
1. **All APIs respond correctly** with appropriate status codes
2. **Authentication works** for all user roles
3. **Data isolation** is maintained (students can only access their own data)
4. **Validation works** for all input fields
5. **Error handling** provides meaningful error messages
6. **Performance** meets requirements (< 250ms for 90% of requests)

## 📞 Support

If you encounter any issues during testing:
1. Check the application logs for detailed error messages
2. Verify database connectivity and data integrity
3. Ensure all required environment variables are set
4. Check the health endpoint: `GET /actuator/health`

Happy Testing! 🚀
