-- Check Admin User in Database
-- Run these queries in your PostgreSQL client (psql, pgAdmin, etc.)

-- 1. Check if cyberlearnix@gmail.com exists and their role
SELECT 
    id,
    full_name,
    email,
    role,
    is_active,
    email_verified,
    created_at,
    failed_login_attempts,
    lockout_until
FROM user_entity 
WHERE email = 'cyberlearnix@gmail.com';

-- 2. Show all users and their roles
SELECT 
    email,
    full_name,
    role,
    is_active,
    created_at
FROM user_entity 
ORDER BY created_at DESC;

-- 3. Count users by role
SELECT 
    role,
    COUNT(*) as count,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_count
FROM user_entity 
GROUP BY role;

-- 4. If admin user doesn't exist, create one:
-- (Uncomment and modify as needed)
/*
INSERT INTO user_entity (
    id,
    full_name,
    email,
    phone,
    role,
    password,
    email_verified,
    is_active,
    created_at,
    updated_at,
    failed_login_attempts
) VALUES (
    gen_random_uuid(),
    'CyberLearnix Admin',
    'cyberlearnix@gmail.com',
    '1234567890',
    'ADMIN',
    '$2a$12$example.hash.here', -- You need to generate proper BCrypt hash
    true,
    true,
    NOW(),
    NOW(),
    0
);
*/

-- 5. If user exists but role is not ADMIN, update it:
-- (Uncomment to fix)
/*
UPDATE user_entity 
SET 
    role = 'ADMIN',
    updated_at = NOW()
WHERE email = 'cyberlearnix@gmail.com';
*/

-- 6. Check database connection and table structure
SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'user_entity' 
ORDER BY ordinal_position;