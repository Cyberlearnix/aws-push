# LMS Student Service

A comprehensive Learning Management System (LMS) Student Service built with Spring Boot that manages all student-facing operations including course enrollment, progress tracking, quizzes, assignments, certificates, reviews, and analytics.

## Features

### Core Functionality
- **Course Enrollment**: Enroll, view, and unenroll from courses
- **Progress Tracking**: Track course, module, and lesson completion
- **Quizzes & Assignments**: Take quizzes, submit assignments, view grades
- **Certificates**: Access and download completion certificates
- **Reviews & Ratings**: Post and manage course reviews
- **Dashboard & Analytics**: Personalized learning statistics and progress

### Security & Access Control
- JWT-based authentication
- Role-based access control (RBAC)
- Student, Instructor, and Admin roles
- Data isolation (students can only access their own data)

### Technical Features
- RESTful API design
- PostgreSQL database with JPA/Hibernate
- Input validation and error handling
- Comprehensive logging
- CORS support
- Health check endpoints

## API Endpoints

### Authentication
- `POST /auth/login` - Login and get JWT token

### Enrollment
- `POST /students/{id}/courses/{courseId}/enroll` - Enroll in course
- `GET /students/{id}/courses` - Get enrolled courses
- `DELETE /students/{id}/courses/{courseId}` - Unenroll from course

### Progress Tracking
- `GET /students/{id}/courses/{courseId}/progress` - Get course progress
- `PUT /students/{id}/courses/{courseId}/progress` - Update progress
- `GET /students/{id}/progress/overall` - Get overall progress

### Quizzes & Assignments
- `GET /students/{id}/courses/{courseId}/modules/{moduleId}/quiz` - Get quizzes
- `POST /students/{id}/courses/{courseId}/modules/{moduleId}/quiz/{quizId}/submit` - Submit quiz
- `GET /students/{id}/courses/{courseId}/quizzes/results` - Get quiz results
- `GET /students/{id}/assignments` - Get assignments
- `POST /students/{id}/assignments/{assignmentId}/submit` - Submit assignment

### Certificates
- `GET /students/{id}/certificates` - Get all certificates
- `GET /students/{id}/certificates/{certificateId}` - Get specific certificate
- `GET /students/{id}/certificates/verify/{verificationCode}` - Verify certificate

### Reviews & Interaction
- `POST /students/{id}/courses/{courseId}/reviews` - Add review
- `PUT /students/{id}/courses/{courseId}/reviews/{reviewId}` - Update review
- `DELETE /students/{id}/courses/{courseId}/reviews/{reviewId}` - Delete review
- `GET /students/{id}/announcements` - Get announcements
- `GET /students/{id}/messages` - Get messages

### Dashboard & Analytics
- `GET /students/{id}/dashboard` - Get dashboard summary
- `GET /students/{id}/stats` - Get learning statistics

## Setup Instructions

### Prerequisites
- Java 21+
- PostgreSQL 12+
- Maven 3.6+

### Database Setup
1. Create a PostgreSQL database named `lms_student_db`
2. Update database credentials in `application.properties` if needed

### Application Setup
1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

The application will start on port 8081 and automatically create sample data including:
- Sample students (student1@example.com, student2@example.com)
- Sample instructor (instructor@example.com)
- Sample admin (admin@example.com)

All sample accounts use password: `password123`

## Testing the API

### 1. Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student1@example.com",
    "password": "password123"
  }'
```

### 2. Use JWT Token
Include the JWT token in subsequent requests:
```bash
curl -X GET http://localhost:8081/students/1/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Database Schema

The service uses the following main entities:
- **Student**: User information and authentication
- **Enrollment**: Course enrollment tracking
- **Progress**: Learning progress tracking
- **Quiz/QuizSubmission**: Quiz management and submissions
- **Assignment/AssignmentSubmission**: Assignment management
- **Certificate**: Course completion certificates
- **Review**: Course reviews and ratings
- **Announcement**: Course announcements
- **Message**: Instructor-student communication

## Security

- All endpoints (except auth and public certificate verification) require JWT authentication
- Students can only access their own data
- Role-based access control ensures proper permissions
- Input validation prevents malicious data
- CORS configured for cross-origin requests

## Error Handling

The service provides comprehensive error handling with appropriate HTTP status codes:
- 401: Unauthorized (invalid/missing JWT)
- 403: Forbidden (insufficient permissions)
- 404: Not Found (resource doesn't exist)
- 409: Conflict (duplicate enrollment, etc.)
- 422: Validation Error (invalid input)
- 500: Internal Server Error

## Monitoring

Health check endpoints are available at:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information

## Development

The project follows standard Spring Boot conventions:
- Controllers handle HTTP requests
- Services contain business logic
- Repositories manage data access
- DTOs handle request/response mapping
- Global exception handling for consistent error responses

## Future Enhancements

- File upload for assignments
- Real-time notifications
- Advanced analytics and reporting
- Integration with external content services
- Caching for improved performance
- API rate limiting
- Audit logging for compliance




