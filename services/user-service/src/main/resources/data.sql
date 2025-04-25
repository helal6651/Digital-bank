BEGIN;

-- Insert permissions if they don't exist
INSERT INTO permissions (name)
SELECT 'USER_PROFILE_SHOW'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_PROFILE_SHOW');

INSERT INTO permissions (name)
SELECT 'USER_PROFILE_UPDATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_PROFILE_UPDATE');

INSERT INTO permissions (name)
SELECT 'USER_LIST'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_LIST');

-- Insert roles if they don't exist
INSERT INTO roles (name)
SELECT 'USER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

INSERT INTO roles (name)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

-- Grant permissions to roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER'
AND p.name IN ('USER_PROFILE_SHOW', 'USER_PROFILE_UPDATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
AND p.name = 'USER_LIST'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Insert admin user with hashed password

COMMIT;