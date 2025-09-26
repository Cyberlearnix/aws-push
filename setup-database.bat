@echo off
echo ========================================
echo Cyber LMS Database Setup
echo ========================================
echo.
echo This script will create the PostgreSQL database and user for Cyber LMS.
echo Make sure PostgreSQL is running before continuing.
echo.
pause

echo.
echo Setting up database...
echo.

REM Run the SQL script using psql
psql -U postgres -f database-setup.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS: Database setup completed!
    echo ========================================
    echo.
    echo Database: cyberlearnixdb
    echo User: cyberlearnix
    echo Password: cyberlearnix123
    echo.
    echo You can now start your microservices!
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR: Database setup failed!
    echo ========================================
    echo.
    echo Please check:
    echo 1. PostgreSQL is running
    echo 2. You have admin privileges
    echo 3. psql command is available in PATH
    echo.
)

pause
