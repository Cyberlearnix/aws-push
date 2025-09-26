-- PostgreSQL Database Setup Script for Cyber LMS Microservices
-- Run this script as a PostgreSQL superuser (usually 'postgres')

-- Create the database user if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cyberlearnix') THEN
        CREATE USER cyberlearnix WITH PASSWORD 'cyberlearnix123';
    END IF;
END
$$;

-- Grant necessary privileges to the user
ALTER USER cyberlearnix CREATEDB;
ALTER USER cyberlearnix WITH SUPERUSER;

-- Create the database if it doesn't exist
SELECT 'CREATE DATABASE cyberlearnixdb OWNER cyberlearnix'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cyberlearnixdb')\gexec

-- Connect to the new database and set up permissions
\c cyberlearnixdb

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON DATABASE cyberlearnixdb TO cyberlearnix;

-- Grant usage and create privileges on public schema
GRANT USAGE ON SCHEMA public TO cyberlearnix;
GRANT CREATE ON SCHEMA public TO cyberlearnix;

-- Grant all privileges on all tables in public schema (current and future)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cyberlearnix;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cyberlearnix;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO cyberlearnix;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO cyberlearnix;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO cyberlearnix;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO cyberlearnix;

-- Ensure the user can create tables and indexes
ALTER USER cyberlearnix SET search_path = public;

-- Display confirmation
SELECT 'Database setup completed successfully!' as status;
SELECT 'Database: cyberlearnixdb' as database_info;
SELECT 'User: cyberlearnix' as user_info;
SELECT 'You can now start your microservices!' as next_step;
