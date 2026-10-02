-- Demo logins (development only, remove with V2). Showroom code DEMO.
--   ali@example.com  / Admin@123   -> Admin role
--   sara@example.com / Viewer@123  -> Viewer role (view-only)
UPDATE users SET password_hash = '$2b$10$E759X9pE8pGWg7M2ZcS2Qe88LFdkMvK.SkDSHXsNgKO3tVtg/ZNjS' WHERE id = 1;
UPDATE users SET password_hash = '$2b$10$KWNNdIlrLawI3IcU6VFeuuKuMvF2QtN0kjx6hkDybNnDkyJQzdHkC' WHERE id = 2;

INSERT INTO user_roles (user_id, role_id)
SELECT 1, id FROM roles WHERE showroom_id = 1 AND is_system = 1;

INSERT INTO roles (showroom_id, name, description, is_system)
VALUES (1, 'Viewer', 'Can look at vehicles and users, cannot change anything', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('VEHICLE_VIEW', 'USER_VIEW')
WHERE r.showroom_id = 1 AND r.name = 'Viewer';

INSERT INTO user_roles (user_id, role_id)
SELECT 2, id FROM roles WHERE showroom_id = 1 AND name = 'Viewer';
