-- Insert default admin role if not exists
INSERT INTO roles (id, authority)
SELECT '11111111-1111-1111-1111-111111111111', 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE authority = 'ROLE_ADMIN');

-- Insert default admin user if not exists
INSERT INTO users (id, email, first_name, last_name, password, is_active, password_update_date)
SELECT '22222222-2222-2222-2222-222222222222', 'admin@example.com', 'TTN', 'Veteran',
       '{bcrypt}$2a$10$XkQm6cY1h6JXJvXfPnMnaOQ5ByNlM2tf3qZ1FyP9B1Zan7Bd5RQ6G',
       true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@example.com');

-- Assign admin role to admin user if not exists
INSERT INTO user_role (user_id, role_id)
SELECT '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111'
WHERE NOT EXISTS (SELECT 1 FROM user_role WHERE user_id = '22222222-2222-2222-2222-222222222222'
                  AND role_id = '11111111-1111-1111-1111-111111111111');
