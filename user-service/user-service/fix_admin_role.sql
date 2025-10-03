-- Fix Admin Role in Database
-- Run these commands in your PostgreSQL client

-- 1. First, check current user status
SELECT 
    email, 
    full_name, 
    role, 
    is_active, 
    email_verified,
    created_at
FROM user_entity 
WHERE email IN ('cyberlearnix@gmail.com', 'cyberlearnixprivatelimited@gmail.com')
ORDER BY created_at;

-- 2. Show all users and their roles (to see what's in the system)
SELECT 
    email, 
    role, 
    is_active,
    created_at
FROM user_entity 
ORDER BY created_at DESC;

-- 3. Fix: Update existing user to ADMIN role
-- (Change the email to match the one you're using for login)
UPDATE user_entity 
SET 
    role = 'ADMIN',
    updated_at = NOW(),
    is_active = true,
    email_verified = true
WHERE email = 'cyberlearnixprivatelimited@gmail.com';

-- 4. Alternative: If you want to use cyberlearnix@gmail.com instead
/*
UPDATE user_entity 
SET 
    role = 'ADMIN',
    updated_at = NOW(),
    is_active = true,
    email_verified = true
WHERE email = 'cyberlearnix@gmail.com';
*/

-- 5. Verify the change was applied
SELECT 
    email, 
    role, 
    is_active, 
    email_verified,
    updated_at
FROM user_entity 
WHERE email IN ('cyberlearnix@gmail.com', 'cyberlearnixprivatelimited@gmail.com');

-- 6. If no admin user exists at all, create one:
-- (Uncomment and modify if needed)
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
    'System Administrator',
    'admin@cyberlearnix.com',
    '9999999999',
    'ADMIN',
    '$2a$12$placeholder.hash.here', -- Replace with actual BCrypt hash
    true,
    true,
    NOW(),
    NOW(),
    0
);
*/

-- 7. Check role distribution in your system
SELECT 
    role,
    COUNT(*) as total_users,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_users
FROM user_entity 
GROUP BY role
ORDER BY role;